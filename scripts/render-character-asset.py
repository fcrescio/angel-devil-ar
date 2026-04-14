#!/usr/bin/env python3
"""Render a GLB asset contact sheet from fixed yaw angles for quick review."""

from __future__ import annotations

import argparse
import json
import math
import struct
from pathlib import Path

from PIL import Image, ImageDraw


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
        chunk = data[offset:offset + chunk_length]
        offset += chunk_length
        if chunk_type == b"JSON":
            document = json.loads(chunk.decode("utf-8"))
        elif chunk_type == b"BIN\x00":
            binary = bytes(chunk)
    if document is None or binary is None:
        raise ValueError("GLB must contain JSON and BIN chunks")
    return document, binary


def accessor_values(document: dict, binary: bytes, accessor_index: int):
    accessor = document["accessors"][accessor_index]
    view = document["bufferViews"][accessor["bufferView"]]
    offset = view.get("byteOffset", 0) + accessor.get("byteOffset", 0)
    count = accessor["count"]
    if accessor["componentType"] == 5126 and accessor["type"] == "VEC3":
        return [
            struct.unpack_from("<fff", binary, offset + i * 12)
            for i in range(count)
        ]
    if accessor["componentType"] == 5123 and accessor["type"] == "SCALAR":
        return [
            struct.unpack_from("<H", binary, offset + i * 2)[0]
            for i in range(count)
        ]
    raise ValueError(f"Unsupported accessor {accessor}")


def collect_triangles(document: dict, binary: bytes):
    materials = []
    for material in document.get("materials", []):
        color = material.get("pbrMetallicRoughness", {}).get("baseColorFactor", [0.8, 0.8, 0.8, 1.0])
        materials.append(tuple(int(max(0, min(1, channel)) * 255) for channel in color[:3]))

    triangles = []
    for mesh in document["meshes"]:
        for primitive in mesh["primitives"]:
            vertices = accessor_values(document, binary, primitive["attributes"]["POSITION"])
            indices = accessor_values(document, binary, primitive["indices"])
            color = materials[primitive.get("material", 0)] if materials else (200, 200, 200)
            for i in range(0, len(indices), 3):
                triangles.append(([vertices[indices[i]], vertices[indices[i + 1]], vertices[indices[i + 2]]], color))
    return triangles


def render_view(triangles, yaw_degrees: float, title: str, size: tuple[int, int] = (320, 260)) -> Image.Image:
    width, height = size
    yaw = math.radians(yaw_degrees)
    cos_yaw = math.cos(yaw)
    sin_yaw = math.sin(yaw)

    projected = []
    xs = []
    ys = []
    for triangle, color in triangles:
        points = []
        depths = []
        for x, y, z in triangle:
            rx = x * cos_yaw + z * sin_yaw
            rz = -x * sin_yaw + z * cos_yaw
            points.append((rx, y))
            depths.append(rz)
            xs.append(rx)
            ys.append(y)
        projected.append((sum(depths) / 3.0, points, color))

    margin = 34
    min_x, max_x = min(xs), max(xs)
    min_y, max_y = min(ys), max(ys)
    scale = min((width - margin * 2) / (max_x - min_x), (height - margin * 2) / (max_y - min_y))

    image = Image.new("RGB", size, (233, 236, 240))
    draw = ImageDraw.Draw(image)
    draw.text((8, 8), title, fill=(25, 28, 34))

    for _, points, color in sorted(projected, key=lambda item: item[0]):
        screen = [
            (
                width / 2 + point[0] * scale,
                height / 2 - (point[1] - (min_y + max_y) / 2.0) * scale,
            )
            for point in points
        ]
        draw.polygon(screen, fill=color, outline=(25, 25, 25))
    return image


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("asset", type=Path)
    parser.add_argument("--output", type=Path, default=Path("/tmp/angel-asset-renders/contact.png"))
    args = parser.parse_args()

    document, binary = parse_glb(args.asset)
    triangles = collect_triangles(document, binary)
    views = [
        ("front +Z", 0),
        ("right", 90),
        ("back -Z", 180),
        ("left", 270),
        ("iso front", -30),
        ("iso back", 150),
    ]
    cells = [render_view(triangles, yaw, label) for label, yaw in views]
    sheet = Image.new("RGB", (960, 520), (233, 236, 240))
    for index, cell in enumerate(cells):
        sheet.paste(cell, ((index % 3) * 320, (index // 3) * 260))
    args.output.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(args.output)
    print(args.output)


if __name__ == "__main__":
    main()
