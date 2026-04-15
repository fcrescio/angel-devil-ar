#!/usr/bin/env python3
"""Render diagnostic views for skinned GLB joints.

The script exaggerates one joint at a time, software-skins the mesh, then
overlays the neutral vertex cloud with the deformed one. This is intended for
asset forensics when joint names are generic and there are no authored clips.
"""

from __future__ import annotations

import argparse
import json
import math
import struct
from dataclasses import dataclass
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw


COMPONENT_DTYPES = {
    5120: np.int8,
    5121: np.uint8,
    5122: np.int16,
    5123: np.uint16,
    5125: np.uint32,
    5126: np.float32,
}

COMPONENT_COUNTS = {
    "SCALAR": 1,
    "VEC2": 2,
    "VEC3": 3,
    "VEC4": 4,
    "MAT4": 16,
}

NORMALIZED_MAX = {
    5120: 127.0,
    5121: 255.0,
    5122: 32767.0,
    5123: 65535.0,
}


@dataclass(frozen=True)
class SkinnedMesh:
    positions: np.ndarray
    joints: np.ndarray
    weights: np.ndarray
    skin_index: int
    node_index: int


def parse_glb(path: Path) -> tuple[dict, bytes]:
    data = path.read_bytes()
    magic, version, length = struct.unpack_from("<4sII", data, 0)
    if magic != b"glTF" or version != 2 or length != len(data):
        raise ValueError(f"{path} is not a valid GLB v2")

    offset = 12
    document = None
    binary = None
    while offset < len(data):
        chunk_length, chunk_type = struct.unpack_from("<I4s", data, offset)
        offset += 8
        chunk = data[offset : offset + chunk_length]
        offset += chunk_length
        if chunk_type == b"JSON":
            document = json.loads(chunk.decode("utf-8"))
        elif chunk_type == b"BIN\x00":
            binary = bytes(chunk)

    if document is None or binary is None:
        raise ValueError("GLB must contain JSON and BIN chunks")
    return document, binary


def accessor_array(document: dict, binary: bytes, accessor_index: int) -> np.ndarray:
    accessor = document["accessors"][accessor_index]
    view = document["bufferViews"][accessor["bufferView"]]
    component_type = accessor["componentType"]
    component_count = COMPONENT_COUNTS[accessor["type"]]
    dtype = np.dtype(COMPONENT_DTYPES[component_type]).newbyteorder("<")
    count = accessor["count"]
    component_size = dtype.itemsize
    element_size = component_size * component_count
    stride = view.get("byteStride", element_size)
    offset = view.get("byteOffset", 0) + accessor.get("byteOffset", 0)

    if stride == element_size:
        array = np.frombuffer(binary, dtype=dtype, count=count * component_count, offset=offset)
        array = array.reshape((count, component_count))
    else:
        array = np.empty((count, component_count), dtype=dtype)
        for index in range(count):
            element_offset = offset + index * stride
            array[index] = np.frombuffer(binary, dtype=dtype, count=component_count, offset=element_offset)

    if accessor.get("normalized", False):
        max_value = NORMALIZED_MAX.get(component_type)
        if max_value is None:
            raise ValueError(f"Unsupported normalized component type {component_type}")
        array = np.clip(array.astype(np.float32) / max_value, -1.0, 1.0)
    elif component_type == 5126:
        array = array.astype(np.float32, copy=False)

    if accessor["type"] == "SCALAR":
        return array[:, 0]
    if accessor["type"] == "MAT4":
        return array.reshape((count, 4, 4), order="F")
    return array


def translation_matrix(value: list[float]) -> np.ndarray:
    matrix = np.identity(4, dtype=np.float32)
    matrix[:3, 3] = np.array(value, dtype=np.float32)
    return matrix


def scale_matrix(value: list[float]) -> np.ndarray:
    return np.diag([value[0], value[1], value[2], 1.0]).astype(np.float32)


def quaternion_matrix(value: list[float]) -> np.ndarray:
    x, y, z, w = value
    xx, yy, zz = x * x, y * y, z * z
    xy, xz, yz = x * y, x * z, y * z
    wx, wy, wz = w * x, w * y, w * z
    return np.array(
        [
            [1.0 - 2.0 * (yy + zz), 2.0 * (xy - wz), 2.0 * (xz + wy), 0.0],
            [2.0 * (xy + wz), 1.0 - 2.0 * (xx + zz), 2.0 * (yz - wx), 0.0],
            [2.0 * (xz - wy), 2.0 * (yz + wx), 1.0 - 2.0 * (xx + yy), 0.0],
            [0.0, 0.0, 0.0, 1.0],
        ],
        dtype=np.float32,
    )


def axis_rotation_matrix(axis: str, angle_degrees: float) -> np.ndarray:
    angle = math.radians(angle_degrees)
    c = math.cos(angle)
    s = math.sin(angle)
    if axis == "x":
        values = [[1, 0, 0, 0], [0, c, -s, 0], [0, s, c, 0], [0, 0, 0, 1]]
    elif axis == "y":
        values = [[c, 0, s, 0], [0, 1, 0, 0], [-s, 0, c, 0], [0, 0, 0, 1]]
    elif axis == "z":
        values = [[c, -s, 0, 0], [s, c, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]]
    else:
        raise ValueError(f"Unsupported axis {axis}")
    return np.array(values, dtype=np.float32)


def node_local_matrix(node: dict) -> np.ndarray:
    if "matrix" in node:
        return np.array(node["matrix"], dtype=np.float32).reshape((4, 4), order="F")
    translation = translation_matrix(node.get("translation", [0.0, 0.0, 0.0]))
    rotation = quaternion_matrix(node.get("rotation", [0.0, 0.0, 0.0, 1.0]))
    scale = scale_matrix(node.get("scale", [1.0, 1.0, 1.0]))
    return translation @ rotation @ scale


def scene_roots(document: dict) -> list[int]:
    scene_index = document.get("scene", 0)
    scenes = document.get("scenes", [])
    roots = list(scenes[scene_index].get("nodes", [])) if scenes else []
    if roots:
        return roots

    children = {child for node in document.get("nodes", []) for child in node.get("children", [])}
    return [index for index in range(len(document.get("nodes", []))) if index not in children]


def world_matrices(
    document: dict,
    target_node: int | None = None,
    perturbation: np.ndarray | None = None,
) -> list[np.ndarray]:
    nodes = document["nodes"]
    worlds = [np.identity(4, dtype=np.float32) for _ in nodes]
    visited: set[int] = set()

    def visit(node_index: int, parent_world: np.ndarray) -> None:
        local = node_local_matrix(nodes[node_index])
        if target_node == node_index and perturbation is not None:
            local = local @ perturbation
        world = parent_world @ local
        worlds[node_index] = world
        visited.add(node_index)
        for child_index in nodes[node_index].get("children", []):
            visit(child_index, world)

    for root_index in scene_roots(document):
        visit(root_index, np.identity(4, dtype=np.float32))

    for node_index in range(len(nodes)):
        if node_index not in visited:
            visit(node_index, np.identity(4, dtype=np.float32))

    return worlds


def find_first_skinned_mesh(document: dict, binary: bytes) -> SkinnedMesh:
    for node_index, node in enumerate(document.get("nodes", [])):
        if "mesh" not in node or "skin" not in node:
            continue
        mesh = document["meshes"][node["mesh"]]
        for primitive in mesh["primitives"]:
            attributes = primitive["attributes"]
            if {"POSITION", "JOINTS_0", "WEIGHTS_0"}.issubset(attributes):
                positions = accessor_array(document, binary, attributes["POSITION"]).astype(np.float32)
                joints = accessor_array(document, binary, attributes["JOINTS_0"]).astype(np.int32)
                weights = accessor_array(document, binary, attributes["WEIGHTS_0"]).astype(np.float32)
                weight_sums = weights.sum(axis=1, keepdims=True)
                weights = np.divide(weights, weight_sums, out=np.zeros_like(weights), where=weight_sums > 0.0)
                return SkinnedMesh(
                    positions=positions,
                    joints=joints,
                    weights=weights,
                    skin_index=node["skin"],
                    node_index=node_index,
                )
    raise ValueError("No skinned mesh primitive with POSITION, JOINTS_0, and WEIGHTS_0 found")


def inverse_bind_matrices(document: dict, binary: bytes, skin: dict) -> np.ndarray:
    accessor_index = skin.get("inverseBindMatrices")
    if accessor_index is None:
        return np.repeat(np.identity(4, dtype=np.float32)[None, :, :], len(skin["joints"]), axis=0)
    return accessor_array(document, binary, accessor_index).astype(np.float32)


def skin_positions(
    mesh: SkinnedMesh,
    skin: dict,
    inverse_binds: np.ndarray,
    worlds: list[np.ndarray],
) -> np.ndarray:
    joint_matrices = np.stack(
        [worlds[node_index] @ inverse_binds[joint_index] for joint_index, node_index in enumerate(skin["joints"])],
        axis=0,
    )
    homogeneous = np.concatenate(
        [mesh.positions, np.ones((mesh.positions.shape[0], 1), dtype=np.float32)],
        axis=1,
    )
    transformed = joint_matrices[mesh.joints] @ homogeneous[:, None, :, None]
    transformed = transformed[:, :, :3, 0]
    return (transformed * mesh.weights[:, :, None]).sum(axis=1)


def project(points: np.ndarray, view: str) -> np.ndarray:
    if view == "front":
        return points[:, [0, 1]]
    if view == "back":
        return np.column_stack((-points[:, 0], points[:, 1]))
    if view == "top":
        return points[:, [0, 2]]
    if view == "bottom":
        return np.column_stack((points[:, 0], -points[:, 2]))
    raise ValueError(f"Unsupported view {view}")


def point_list(points: np.ndarray) -> list[tuple[int, int]]:
    rounded = np.rint(points).astype(np.int32)
    return [tuple(row) for row in rounded]


def render_joint_sheet(
    baseline: np.ndarray,
    deformed: np.ndarray,
    moved: np.ndarray,
    title: str,
    output: Path,
    sample_step: int,
) -> None:
    cell_width = 420
    cell_height = 340
    margin = 42
    views = ["front", "back", "top", "bottom"]
    sheet = Image.new("RGB", (cell_width * 2, cell_height * 2), (238, 240, 243))
    draw_sheet = ImageDraw.Draw(sheet)
    draw_sheet.text((10, 8), title, fill=(24, 27, 32))

    baseline_sample = baseline[::sample_step]
    deformed_sample = deformed[::sample_step]
    moved_sample = moved[::sample_step]

    for index, view in enumerate(views):
        origin_x = (index % 2) * cell_width
        origin_y = (index // 2) * cell_height
        cell = Image.new("RGB", (cell_width, cell_height), (238, 240, 243))
        draw = ImageDraw.Draw(cell)
        draw.text((10, 10), view, fill=(24, 27, 32))

        base_2d = project(baseline_sample, view)
        deform_2d = project(deformed_sample, view)
        combined = np.vstack((base_2d, deform_2d))
        minimum = combined.min(axis=0)
        maximum = combined.max(axis=0)
        center = (minimum + maximum) / 2.0
        span = np.maximum(maximum - minimum, 1.0e-5)
        scale = min((cell_width - margin * 2) / span[0], (cell_height - margin * 2) / span[1])

        def to_screen(points: np.ndarray) -> np.ndarray:
            shifted = (points - center) * scale
            return np.column_stack((cell_width / 2.0 + shifted[:, 0], cell_height / 2.0 - shifted[:, 1]))

        draw.point(point_list(to_screen(base_2d)), fill=(145, 150, 158))
        changed = moved_sample > 1.0e-4
        if np.any(changed):
            draw.point(point_list(to_screen(deform_2d[changed])), fill=(204, 38, 38))
        draw.rectangle((0, 0, cell_width - 1, cell_height - 1), outline=(210, 214, 220))
        sheet.paste(cell, (origin_x, origin_y))

    output.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(output)


def render_neutral_sheet(vertices: np.ndarray, output: Path, sample_step: int) -> None:
    cell_width = 420
    cell_height = 340
    margin = 42
    views = ["front", "back", "top", "bottom"]
    sheet = Image.new("RGB", (cell_width * 2, cell_height * 2), (238, 240, 243))
    draw_sheet = ImageDraw.Draw(sheet)
    draw_sheet.text((10, 8), "neutral front/back/top/bottom", fill=(24, 27, 32))
    sample = vertices[::sample_step]

    for index, view in enumerate(views):
        origin_x = (index % 2) * cell_width
        origin_y = (index // 2) * cell_height
        cell = Image.new("RGB", (cell_width, cell_height), (238, 240, 243))
        draw = ImageDraw.Draw(cell)
        draw.text((10, 10), view, fill=(24, 27, 32))

        points_2d = project(sample, view)
        minimum = points_2d.min(axis=0)
        maximum = points_2d.max(axis=0)
        center = (minimum + maximum) / 2.0
        span = np.maximum(maximum - minimum, 1.0e-5)
        scale = min((cell_width - margin * 2) / span[0], (cell_height - margin * 2) / span[1])
        shifted = (points_2d - center) * scale
        screen = np.column_stack((cell_width / 2.0 + shifted[:, 0], cell_height / 2.0 - shifted[:, 1]))

        draw.point(point_list(screen), fill=(70, 76, 84))
        draw.rectangle((0, 0, cell_width - 1, cell_height - 1), outline=(210, 214, 220))
        sheet.paste(cell, (origin_x, origin_y))

    output.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(output)


def joint_label(document: dict, node_index: int, joint_index: int) -> str:
    name = document["nodes"][node_index].get("name")
    return name if name else f"joint_{joint_index}"


def render_joint(
    document: dict,
    mesh: SkinnedMesh,
    skin: dict,
    inverse_binds: np.ndarray,
    baseline: np.ndarray,
    joint_index: int,
    axis: str,
    angle_degrees: float,
    output: Path,
    sample_step: int,
) -> tuple[float, float, int, np.ndarray, np.ndarray]:
    joint_node = skin["joints"][joint_index]
    perturbation = axis_rotation_matrix(axis, angle_degrees)
    worlds = world_matrices(document, target_node=joint_node, perturbation=perturbation)
    deformed = skin_positions(mesh, skin, inverse_binds, worlds)
    moved = np.linalg.norm(deformed - baseline, axis=1)
    max_moved = float(moved.max())
    mean_moved = float(moved.mean())
    changed = moved > 1.0e-4
    changed_count = int(changed.sum())
    if changed_count > 0:
        changed_vertices = baseline[changed]
        moved_center = changed_vertices.mean(axis=0)
        moved_extent = changed_vertices.max(axis=0) - changed_vertices.min(axis=0)
    else:
        moved_center = np.zeros(3, dtype=np.float32)
        moved_extent = np.zeros(3, dtype=np.float32)
    label = joint_label(document, joint_node, joint_index)
    title = (
        f"{joint_index:02d} {label} node={joint_node} "
        f"axis={axis} angle={angle_degrees:g} moved={changed_count} max={max_moved:.4f}"
    )
    render_joint_sheet(baseline, deformed, moved, title, output, sample_step)
    return max_moved, mean_moved, changed_count, moved_center, moved_extent


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "asset",
        type=Path,
        help="Skinned GLB asset to inspect.",
    )
    parser.add_argument(
        "--joint",
        type=int,
        help="Render only one skin joint index. Defaults to rendering every joint.",
    )
    parser.add_argument("--axis", choices=["x", "y", "z"], default="z")
    parser.add_argument("--angle-degrees", type=float, default=45.0)
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=Path("/tmp/angel-asset-renders/joint-sweep"),
    )
    parser.add_argument(
        "--sample-step",
        type=int,
        default=1,
        help="Render every Nth vertex to keep large assets fast.",
    )
    args = parser.parse_args()

    if args.sample_step < 1:
        raise ValueError("--sample-step must be >= 1")

    document, binary = parse_glb(args.asset)
    mesh = find_first_skinned_mesh(document, binary)
    skin = document["skins"][mesh.skin_index]
    inverse_binds = inverse_bind_matrices(document, binary, skin)
    baseline = skin_positions(mesh, skin, inverse_binds, world_matrices(document))
    args.output_dir.mkdir(parents=True, exist_ok=True)
    neutral_output = args.output_dir / "neutral.png"
    render_neutral_sheet(baseline, neutral_output, args.sample_step)
    print(neutral_output)
    joint_count = len(skin["joints"])
    joint_indices = [args.joint] if args.joint is not None else list(range(joint_count))
    for joint_index in joint_indices:
        if joint_index < 0 or joint_index >= joint_count:
            raise ValueError(f"Joint index {joint_index} outside 0..{joint_count - 1}")

    rows = [
        "\t".join(
            [
                "joint_index",
                "node_index",
                "name",
                "moved_vertices",
                "max_delta",
                "mean_delta",
                "center_x",
                "center_y",
                "center_z",
                "extent_x",
                "extent_y",
                "extent_z",
                "image",
            ]
        )
    ]
    for joint_index in joint_indices:
        node_index = skin["joints"][joint_index]
        name = joint_label(document, node_index, joint_index)
        safe_name = "".join(char if char.isalnum() or char in "-_" else "_" for char in name)
        output = args.output_dir / f"joint_{joint_index:02d}_{safe_name}.png"
        max_moved, mean_moved, changed_count, moved_center, moved_extent = render_joint(
            document=document,
            mesh=mesh,
            skin=skin,
            inverse_binds=inverse_binds,
            baseline=baseline,
            joint_index=joint_index,
            axis=args.axis,
            angle_degrees=args.angle_degrees,
            output=output,
            sample_step=args.sample_step,
        )
        rows.append(
            "\t".join(
                [
                    str(joint_index),
                    str(node_index),
                    name,
                    str(changed_count),
                    f"{max_moved:.6f}",
                    f"{mean_moved:.6f}",
                    f"{moved_center[0]:.6f}",
                    f"{moved_center[1]:.6f}",
                    f"{moved_center[2]:.6f}",
                    f"{moved_extent[0]:.6f}",
                    f"{moved_extent[1]:.6f}",
                    f"{moved_extent[2]:.6f}",
                    output.name,
                ]
            )
        )
        print(f"{joint_index:02d}\t{name}\tmoved={changed_count}\tmax={max_moved:.6f}\t{output}")

    summary_path = args.output_dir / "summary.tsv"
    summary_path.write_text("\n".join(rows) + "\n", encoding="utf-8")
    print(summary_path)


if __name__ == "__main__":
    main()
