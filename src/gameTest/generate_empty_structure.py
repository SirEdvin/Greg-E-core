"""Generate the small, deterministic native GameTest structure resource."""
import gzip
import struct
from pathlib import Path


def name(value):
    encoded = value.encode("utf-8")
    return struct.pack(">H", len(encoded)) + encoded


def integer(value):
    return struct.pack(">i", value)


root = b"\x0a\x00\x00"
root += b"\x03" + name("DataVersion") + integer(3465)
root += b"\x09" + name("size") + b"\x03" + integer(3) + integer(8) * 3
root += b"\x09" + name("palette") + b"\x0a" + integer(1)
root += b"\x08" + name("Name") + name("minecraft:air") + b"\x00"
root += b"\x09" + name("blocks") + b"\x0a" + integer(0)
root += b"\x09" + name("entities") + b"\x0a" + integer(0)
root += b"\x00"
path = Path(__file__).parent / "resources/data/gregecore/structures/empty.nbt"
path.parent.mkdir(parents=True, exist_ok=True)
path.write_bytes(gzip.compress(root, mtime=0))
print(path)
