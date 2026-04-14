#!/usr/bin/env python3
"""Generate the app-owned low-poly shoulder imp GLB asset."""

from __future__ import annotations

import argparse
import json
import math
import struct
from dataclasses import dataclass
from pathlib import Path


ASSET_PATH = Path("app/src/main/assets/models/grotesque_imp.glb")


@dataclass(frozen=True)
class Material:
    name: str
    color: tuple[float, float, float, float]


MATERIALS = [
    Material("warm imp red", (0.82, 0.05, 0.08, 1.0)),
    Material("soft muzzle pink", (1.0, 0.47, 0.42, 1.0)),
    Material("golden eyes", (1.0, 0.86, 0.14, 1.0)),
    Material("ink pupils", (0.03, 0.02, 0.02, 1.0)),
    Material("bone horn and teeth", (0.96, 0.86, 0.66, 1.0)),
    Material("burgundy wings", (0.30, 0.01, 0.08, 1.0)),
    Material("charcoal claws", (0.05, 0.04, 0.04, 1.0)),
    Material("bent halo", (0.98, 0.73, 0.25, 1.0)),
]


class MeshBuilder:
    def __init__(self) -> None:
        self.primitives: list[dict[str, object]] = []

    def add(self, name: str, material: int, vertices: list[tuple[float, float, float]], indices: list[int]) -> None:
        self.primitives.append(
            {
                "name": name,
                "material": material,
                "vertices": vertices,
                "indices": indices,
            }
        )


def ellipsoid(
    center: tuple[float, float, float],
    radii: tuple[float, float, float],
    lat_segments: int = 5,
    lon_segments: int = 10,
) -> tuple[list[tuple[float, float, float]], list[int]]:
    cx, cy, cz = center
    rx, ry, rz = radii
    vertices: list[tuple[float, float, float]] = []
    for lat in range(lat_segments + 1):
        theta = math.pi * lat / lat_segments
        y = math.cos(theta)
        ring = math.sin(theta)
        for lon in range(lon_segments):
            phi = 2.0 * math.pi * lon / lon_segments
            vertices.append((cx + rx * ring * math.cos(phi), cy + ry * y, cz + rz * ring * math.sin(phi)))

    indices: list[int] = []
    for lat in range(lat_segments):
        for lon in range(lon_segments):
            a = lat * lon_segments + lon
            b = lat * lon_segments + (lon + 1) % lon_segments
            c = (lat + 1) * lon_segments + lon
            d = (lat + 1) * lon_segments + (lon + 1) % lon_segments
            indices.extend([a, c, b, b, c, d])
    return vertices, indices


def cone(
    base_center: tuple[float, float, float],
    radius: float,
    height: float,
    segments: int = 6,
    axis: str = "y",
) -> tuple[list[tuple[float, float, float]], list[int]]:
    bx, by, bz = base_center
    vertices: list[tuple[float, float, float]] = []
    for i in range(segments):
        angle = 2.0 * math.pi * i / segments
        if axis == "y":
            vertices.append((bx + radius * math.cos(angle), by, bz + radius * math.sin(angle)))
        elif axis == "z":
            vertices.append((bx + radius * math.cos(angle), by + radius * math.sin(angle), bz))
        else:
            vertices.append((bx, by + radius * math.cos(angle), bz + radius * math.sin(angle)))

    if axis == "y":
        tip = (bx, by + height, bz)
        base = (bx, by, bz)
    elif axis == "z":
        tip = (bx, by, bz + height)
        base = (bx, by, bz)
    else:
        tip = (bx + height, by, bz)
        base = (bx, by, bz)
    vertices.extend([tip, base])
    tip_i = segments
    base_i = segments + 1

    indices: list[int] = []
    for i in range(segments):
        j = (i + 1) % segments
        indices.extend([i, j, tip_i, j, i, base_i])
    return vertices, indices


def box(
    center: tuple[float, float, float],
    size: tuple[float, float, float],
) -> tuple[list[tuple[float, float, float]], list[int]]:
    cx, cy, cz = center
    sx, sy, sz = (size[0] / 2.0, size[1] / 2.0, size[2] / 2.0)
    vertices = [
        (cx - sx, cy - sy, cz - sz),
        (cx + sx, cy - sy, cz - sz),
        (cx + sx, cy + sy, cz - sz),
        (cx - sx, cy + sy, cz - sz),
        (cx - sx, cy - sy, cz + sz),
        (cx + sx, cy - sy, cz + sz),
        (cx + sx, cy + sy, cz + sz),
        (cx - sx, cy + sy, cz + sz),
    ]
    indices = [
        0, 1, 2, 0, 2, 3,
        4, 6, 5, 4, 7, 6,
        0, 4, 5, 0, 5, 1,
        1, 5, 6, 1, 6, 2,
        2, 6, 7, 2, 7, 3,
        3, 7, 4, 3, 4, 0,
    ]
    return vertices, indices


def triangle(points: list[tuple[float, float, float]]) -> tuple[list[tuple[float, float, float]], list[int]]:
    return points, [0, 1, 2, 2, 1, 0]


def tail_segment(start: tuple[float, float, float], end: tuple[float, float, float], width: float) -> tuple[list[tuple[float, float, float]], list[int]]:
    sx, sy, sz = start
    ex, ey, ez = end
    dx, dy = ex - sx, ey - sy
    length = math.hypot(dx, dy) or 1.0
    nx, ny = -dy / length * width, dx / length * width
    vertices = [
        (sx - nx, sy - ny, sz),
        (sx + nx, sy + ny, sz),
        (ex + nx, ey + ny, ez),
        (ex - nx, ey - ny, ez),
    ]
    return vertices, [0, 1, 2, 0, 2, 3, 2, 1, 0, 3, 2, 0]


def cross(
    a: tuple[float, float, float],
    b: tuple[float, float, float],
) -> tuple[float, float, float]:
    return (
        (a[1] * b[2]) - (a[2] * b[1]),
        (a[2] * b[0]) - (a[0] * b[2]),
        (a[0] * b[1]) - (a[1] * b[0]),
    )


def dot(
    a: tuple[float, float, float],
    b: tuple[float, float, float],
) -> float:
    return (a[0] * b[0]) + (a[1] * b[1]) + (a[2] * b[2])


def subtract(
    a: tuple[float, float, float],
    b: tuple[float, float, float],
) -> tuple[float, float, float]:
    return (a[0] - b[0], a[1] - b[1], a[2] - b[2])


def average(points: list[tuple[float, float, float]]) -> tuple[float, float, float]:
    count = float(len(points))
    return (
        sum(point[0] for point in points) / count,
        sum(point[1] for point in points) / count,
        sum(point[2] for point in points) / count,
    )


def normalize(vector: tuple[float, float, float]) -> tuple[float, float, float]:
    length = math.sqrt(sum(component * component for component in vector))
    if length <= 0.000001:
        return (0.0, 1.0, 0.0)
    return tuple(component / length for component in vector)


def compute_normals(
    vertices: list[tuple[float, float, float]],
    indices: list[int],
) -> list[tuple[float, float, float]]:
    primitive_center = average(vertices)
    accumulators = [[0.0, 0.0, 0.0] for _ in vertices]
    for i in range(0, len(indices), 3):
        i0, i1, i2 = indices[i], indices[i + 1], indices[i + 2]
        v0, v1, v2 = vertices[i0], vertices[i1], vertices[i2]
        normal = cross(subtract(v1, v0), subtract(v2, v0))
        triangle_center = average([v0, v1, v2])
        outward = subtract(triangle_center, primitive_center)
        if abs(dot(normal, outward)) <= 0.000001:
            outward = triangle_center
        if dot(normal, outward) < 0.0:
            normal = (-normal[0], -normal[1], -normal[2])
        for vertex_index in (i0, i1, i2):
            accumulators[vertex_index][0] += normal[0]
            accumulators[vertex_index][1] += normal[1]
            accumulators[vertex_index][2] += normal[2]
    return [normalize((normal[0], normal[1], normal[2])) for normal in accumulators]


def build_imp() -> MeshBuilder:
    mesh = MeshBuilder()

    for name, material, vertices, indices in [
        ("round belly", 0, *ellipsoid((0.0, -0.20, 0.0), (0.30, 0.42, 0.22), 5, 9)),
        ("large head", 0, *ellipsoid((0.0, 0.37, 0.02), (0.36, 0.34, 0.30), 5, 10)),
        ("front muzzle", 1, *ellipsoid((0.0, 0.28, 0.285), (0.18, 0.12, 0.055), 3, 8)),
        ("left eye", 2, *ellipsoid((-0.13, 0.43, 0.295), (0.075, 0.090, 0.035), 3, 8)),
        ("right eye", 2, *ellipsoid((0.13, 0.43, 0.295), (0.075, 0.090, 0.035), 3, 8)),
        ("left pupil", 3, *ellipsoid((-0.13, 0.42, 0.326), (0.027, 0.040, 0.014), 3, 6)),
        ("right pupil", 3, *ellipsoid((0.13, 0.42, 0.326), (0.027, 0.040, 0.014), 3, 6)),
        ("left horn", 4, *cone((-0.17, 0.63, 0.02), 0.075, 0.24, 6, "y")),
        ("right horn", 4, *cone((0.17, 0.63, 0.02), 0.075, 0.24, 6, "y")),
        ("left foot", 6, *box((-0.12, -0.67, 0.08), (0.15, 0.10, 0.18))),
        ("right foot", 6, *box((0.12, -0.67, 0.08), (0.15, 0.10, 0.18))),
        ("left arm", 0, *box((-0.34, -0.10, 0.08), (0.10, 0.34, 0.10))),
        ("right arm", 0, *box((0.34, -0.10, 0.08), (0.10, 0.34, 0.10))),
    ]:
        mesh.add(name, material, vertices, indices)

    mesh.add("left grin tooth", 4, *triangle([(-0.08, 0.22, 0.34), (-0.03, 0.22, 0.34), (-0.055, 0.13, 0.35)]))
    mesh.add("right grin tooth", 4, *triangle([(0.04, 0.22, 0.34), (0.10, 0.22, 0.34), (0.07, 0.13, 0.35)]))
    mesh.add("nose chip", 6, *triangle([(-0.03, 0.31, 0.345), (0.04, 0.31, 0.345), (0.00, 0.25, 0.36)]))

    mesh.add("left rag wing", 5, *triangle([(-0.20, 0.05, -0.18), (-0.78, 0.15, -0.28), (-0.36, -0.22, -0.25)]))
    mesh.add("left lower wing", 5, *triangle([(-0.25, -0.04, -0.20), (-0.72, -0.30, -0.28), (-0.33, -0.26, -0.23)]))
    mesh.add("right rag wing", 5, *triangle([(0.20, 0.05, -0.18), (0.78, 0.15, -0.28), (0.36, -0.22, -0.25)]))
    mesh.add("right lower wing", 5, *triangle([(0.25, -0.04, -0.20), (0.72, -0.30, -0.28), (0.33, -0.26, -0.23)]))

    tail_points = [(0.02, -0.40, -0.24), (0.35, -0.52, -0.38), (0.56, -0.38, -0.42), (0.46, -0.26, -0.44)]
    for idx, (start, end) in enumerate(zip(tail_points, tail_points[1:])):
        mesh.add(f"tail segment {idx}", 0, *tail_segment(start, end, 0.035))
    mesh.add("tail arrow", 0, *triangle([(0.46, -0.18, -0.45), (0.58, -0.30, -0.45), (0.39, -0.31, -0.45)]))

    mesh.add("bent halo left", 7, *box((-0.08, 0.91, 0.02), (0.22, 0.025, 0.045)))
    mesh.add("bent halo right", 7, *box((0.13, 0.91, 0.02), (0.19, 0.025, 0.045)))
    mesh.add("halo crack", 6, *box((0.03, 0.88, 0.04), (0.025, 0.10, 0.025)))

    return mesh


def write_glb(path: Path, mesh: MeshBuilder) -> None:
    bin_blob = bytearray()
    buffer_views: list[dict[str, object]] = []
    accessors: list[dict[str, object]] = []
    meshes: list[dict[str, object]] = []
    nodes: list[dict[str, int]] = []

    def append_blob(blob: bytes, target: int | None) -> int:
        while len(bin_blob) % 4:
            bin_blob.append(0)
        offset = len(bin_blob)
        bin_blob.extend(blob)
        view_index = len(buffer_views)
        view: dict[str, object] = {"buffer": 0, "byteOffset": offset, "byteLength": len(blob)}
        if target is not None:
            view["target"] = target
        buffer_views.append(view)
        return view_index

    for primitive in mesh.primitives:
        vertices = primitive["vertices"]
        indices = primitive["indices"]
        assert isinstance(vertices, list)
        assert isinstance(indices, list)

        vertex_blob = b"".join(struct.pack("<fff", *vertex) for vertex in vertices)
        vertex_view = append_blob(vertex_blob, 34962)
        xs, ys, zs = zip(*vertices)
        vertex_accessor = len(accessors)
        accessors.append(
            {
                "bufferView": vertex_view,
                "componentType": 5126,
                "count": len(vertices),
                "type": "VEC3",
                "min": [min(xs), min(ys), min(zs)],
                "max": [max(xs), max(ys), max(zs)],
            }
        )

        normals = compute_normals(vertices, indices)
        normal_blob = b"".join(struct.pack("<fff", *normal) for normal in normals)
        normal_view = append_blob(normal_blob, 34962)
        normal_accessor = len(accessors)
        accessors.append(
            {
                "bufferView": normal_view,
                "componentType": 5126,
                "count": len(normals),
                "type": "VEC3",
                "min": [
                    min(normal[0] for normal in normals),
                    min(normal[1] for normal in normals),
                    min(normal[2] for normal in normals),
                ],
                "max": [
                    max(normal[0] for normal in normals),
                    max(normal[1] for normal in normals),
                    max(normal[2] for normal in normals),
                ],
            }
        )

        index_blob = b"".join(struct.pack("<H", index) for index in indices)
        index_view = append_blob(index_blob, 34963)
        index_accessor = len(accessors)
        accessors.append(
            {
                "bufferView": index_view,
                "componentType": 5123,
                "count": len(indices),
                "type": "SCALAR",
                "min": [min(indices)],
                "max": [max(indices)],
            }
        )

        mesh_index = len(meshes)
        meshes.append(
            {
                "name": primitive["name"],
                "primitives": [
                    {
                        "attributes": {
                            "POSITION": vertex_accessor,
                            "NORMAL": normal_accessor,
                        },
                        "indices": index_accessor,
                        "material": primitive["material"],
                        "mode": 4,
                    }
                ],
            }
        )
        nodes.append({"mesh": mesh_index})

    while len(bin_blob) % 4:
        bin_blob.append(0)

    document = {
        "asset": {"version": "2.0", "generator": "scripts/generate-grotesque-imp.py"},
        "scene": 0,
        "scenes": [{"nodes": list(range(len(nodes)))}],
        "nodes": nodes,
        "meshes": meshes,
        "materials": [
            {
                "name": material.name,
                "pbrMetallicRoughness": {
                    "baseColorFactor": list(material.color),
                    "metallicFactor": 0.0,
                    "roughnessFactor": 0.95,
                },
                "doubleSided": True,
            }
            for material in MATERIALS
        ],
        "buffers": [{"byteLength": len(bin_blob)}],
        "bufferViews": buffer_views,
        "accessors": accessors,
    }

    json_blob = json.dumps(document, separators=(",", ":")).encode("utf-8")
    while len(json_blob) % 4:
        json_blob += b" "

    total_length = 12 + 8 + len(json_blob) + 8 + len(bin_blob)
    glb = bytearray()
    glb.extend(struct.pack("<4sII", b"glTF", 2, total_length))
    glb.extend(struct.pack("<I4s", len(json_blob), b"JSON"))
    glb.extend(json_blob)
    glb.extend(struct.pack("<I4s", len(bin_blob), b"BIN\x00"))
    glb.extend(bin_blob)

    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(glb)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", type=Path, default=ASSET_PATH)
    args = parser.parse_args()
    write_glb(args.output, build_imp())
    print(f"Wrote {args.output}")


if __name__ == "__main__":
    main()
