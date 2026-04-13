#!/usr/bin/env bash
set -euo pipefail

asset_dir="app/src/main/assets/models"
glb_file="$asset_dir/grotesque_imp.glb"
max_size=5242880  # 5MB

# Check file exists
if [[ ! -f "$glb_file" ]]; then
    echo "Missing: $glb_file" >&2
    exit 1
fi

# Check file size
file_size=$(stat -c%s "$glb_file" 2>/dev/null || stat -f%z "$glb_file" 2>/dev/null)
if [[ $file_size -gt $max_size ]]; then
    echo "Asset too large: $file_size bytes (max: $max_size)" >&2
    exit 1
fi

# Validate GLB structure using Python for binary parsing
python3 - "$glb_file" << 'PYTHON_SCRIPT'
import sys
import struct
import json

def validate_glb(path):
    with open(path, 'rb') as f:
        # Read header (12 bytes)
        header = f.read(12)
        if len(header) < 12:
            print("Error: File too small for GLB header", file=sys.stderr)
            return False

        magic, version, length = struct.unpack('<4sII', header)

        # Check magic
        if magic != b'glTF':
            print(f"Error: Invalid magic bytes: {magic}", file=sys.stderr)
            return False

        # Check version
        if version != 2:
            print(f"Error: Invalid GLB version: {version} (expected 2)", file=sys.stderr)
            return False

        # Check length matches file size
        f.seek(0, 2)  # Seek to end
        actual_size = f.tell()
        if length != actual_size:
            print(f"Error: Declared length {length} != actual file size {actual_size}", file=sys.stderr)
            return False

        # Read and validate chunks
        f.seek(12)  # After header
        has_json = False
        has_bin = False

        while f.tell() < actual_size:
            # Read chunk header (8 bytes)
            chunk_header = f.read(8)
            if len(chunk_header) < 8:
                print("Error: Incomplete chunk header", file=sys.stderr)
                return False

            chunk_length, chunk_type = struct.unpack('<II', chunk_header)

            # Validate chunk length is padded to 4 bytes
            if chunk_length % 4 != 0:
                print(f"Error: Chunk length {chunk_length} not padded to 4 bytes", file=sys.stderr)
                return False

            # Check chunk type (stored as little-endian int)
            chunk_type_bytes = struct.pack('<I', chunk_type)
            chunk_type_str = chunk_type_bytes.decode('ascii', errors='replace').rstrip('\x00')
            if chunk_type_str == 'JSON':
                has_json = True
                chunk_data = f.read(chunk_length)
                try:
                    json.loads(chunk_data.decode('utf-8'))
                except Exception as exc:
                    print(f"Error: Invalid JSON chunk: {exc}", file=sys.stderr)
                    return False
                continue
            elif chunk_type_str == 'BIN':
                has_bin = True

            # Skip chunk data
            f.seek(chunk_length, 1)

        if f.tell() != actual_size:
            print(f"Error: Chunk scan ended at {f.tell()} but file size is {actual_size}", file=sys.stderr)
            return False

        # Verify required chunks
        if not has_json:
            print("Error: Missing JSON chunk", file=sys.stderr)
            return False

        if not has_bin:
            print("Error: Missing BIN chunk", file=sys.stderr)
            return False

        print(f"GLB validation passed: version={version}, length={length}, has JSON and BIN chunks")
        return True

if __name__ == '__main__':
    if len(sys.argv) != 2:
        print("Usage: validate_glb.py <path>", file=sys.stderr)
        sys.exit(1)

    if not validate_glb(sys.argv[1]):
        sys.exit(1)
PYTHON_SCRIPT

echo "Asset validation passed."
