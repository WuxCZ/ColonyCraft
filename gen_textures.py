"""
ColonyCraft texture generator.
Creates 16x16 PNG textures for all blocks, items and entities entirely in Python
without any third-party dependencies — uses only the built-in `struct` and `zlib`
modules to write valid PNG files.
"""

import os
import struct
import zlib

# ─── Minimal PNG writer ────────────────────────────────────────────────────────

def _png_chunk(name: bytes, data: bytes) -> bytes:
    c = struct.pack(">I", len(data)) + name + data
    crc = zlib.crc32(name + data) & 0xFFFFFFFF
    return c + struct.pack(">I", crc)

def write_png(path: str, pixels: list[list[tuple[int,int,int,int]]]):
    """Write a 16x16 RGBA PNG to *path*."""
    w, h = len(pixels[0]), len(pixels)
    raw = b""
    for row in pixels:
        raw += b"\x00"  # filter type None
        for r, g, b, a in row:
            raw += bytes([r, g, b, a])
    compressed = zlib.compress(raw, 9)
    sig    = b"\x89PNG\r\n\x1a\n"
    ihdr   = _png_chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
    idat   = _png_chunk(b"IDAT", compressed)
    iend   = _png_chunk(b"IEND", b"")
    with open(path, "wb") as f:
        f.write(sig + ihdr + idat + iend)

# ─── Pixel art helpers ─────────────────────────────────────────────────────────

def solid(color, size=16):
    r, g, b = color
    return [[(r, g, b, 255)] * size for _ in range(size)]

def checkerboard(c1, c2, size=16, cell=2):
    r1,g1,b1 = c1; r2,g2,b2 = c2
    px = []
    for y in range(size):
        row = []
        for x in range(size):
            if ((x // cell) + (y // cell)) % 2 == 0:
                row.append((r1,g1,b1,255))
            else:
                row.append((r2,g2,b2,255))
        px.append(row)
    return px

def bordered(fill, border, size=16, bw=2):
    rf,gf,bf = fill; rb,gb,bb = border
    px = []
    for y in range(size):
        row = []
        for x in range(size):
            if x < bw or x >= size-bw or y < bw or y >= size-bw:
                row.append((rb,gb,bb,255))
            else:
                row.append((rf,gf,bf,255))
        px.append(row)
    return px

def striped(c1, c2, stripe=2, size=16):
    r1,g1,b1 = c1; r2,g2,b2 = c2
    px = []
    for y in range(size):
        row = []
        for x in range(size):
            if (x // stripe) % 2 == 0:
                row.append((r1,g1,b1,255))
            else:
                row.append((r2,g2,b2,255))
        px.append(row)
    return px

def cross(bg, fg, size=16, thickness=2):
    r1,g1,b1 = bg; r2,g2,b2 = fg
    cx = size // 2
    px = []
    for y in range(size):
        row = []
        for x in range(size):
            if abs(x - cx) < thickness or abs(y - cx) < thickness:
                row.append((r2,g2,b2,255))
            else:
                row.append((r1,g1,b1,255))
        px.append(row)
    return px

def humanoid_skin(base, dark, size=64):
    """64x64 Minecraft-compatible skin PNG, all areas same tone."""
    rb,gb,bb = base; rd,gd,bd = dark
    p = [[(rb,gb,bb,255)] * size for _ in range(size)]
    # Draw face (8x8 at 8,8)
    for y in range(8,16):
        for x in range(8,16):
            p[y][x] = (rd,gd,bd,255)
    # Eyes
    p[10][10] = (30,30,80,255)
    p[10][13] = (30,30,80,255)
    return p

def png64(pixels):
    """Return 64x64 pixel grid (list of 64 rows of 64 RGBA tuples)."""
    return pixels

# ─── Block textures ────────────────────────────────────────────────────────────

BLOCK_ROOT = r"C:\ColonyCraft\src\main\resources\assets\colonycraft\textures\block"
ITEM_ROOT  = r"C:\ColonyCraft\src\main\resources\assets\colonycraft\textures\item"
ENTITY_ROOT= r"C:\ColonyCraft\src\main\resources\assets\colonycraft\textures\entity"

os.makedirs(BLOCK_ROOT, exist_ok=True)
os.makedirs(ITEM_ROOT,  exist_ok=True)
os.makedirs(ENTITY_ROOT,exist_ok=True)

BLOCK_TEXTURES = {
    # name                  : (style_fn, args)
    "colony_banner":        (cross,    ((180,140, 20), (220,180, 50))),
    "stockpile":            (bordered, ((139, 90, 43), ( 80, 50, 20))),
    "research_table":       (checkerboard, ((90,60,30),(120,80,40))),
    "woodcutter_bench":     (bordered, ((160,100, 40), (100, 60, 20))),
    "forester_hut":         (bordered, (( 50,100, 40), ( 30, 70, 20))),
    "miner_hut":            (bordered, ((100,100,100), ( 60, 60, 60))),
    "farmer_hut":           (bordered, ((160,130, 60), ( 80,100, 30))),
    "berry_farm":           (cross,    ((100,180, 60), (200, 50, 80))),
    "fishing_hut":          (bordered, (( 50,100,180), ( 30, 60,120))),
    "water_well":           (bordered, (( 80,160,220), ( 40,  80,140))),
    "stove":                (bordered, (( 80, 40, 20), (180, 80,  20))),
    "bloomery":             (bordered, ((120, 60, 20), (200,100,  30))),
    "blast_furnace":        (bordered, (( 60, 60, 80), (200,140,  40))),
    "tailor_shop":          (checkerboard, ((160,100,160),(100,60,100))),
    "fletcher_bench":       (bordered, ((180,140, 80), (100, 80, 30))),
    "stonemason_bench":     (bordered, ((130,130,130), ( 80, 80, 80))),
    "compost_bin":          (bordered, (( 80,120, 40), ( 50, 80, 20))),
    "grindstone_station":   (bordered, ((160,160,160), ( 80, 80,100))),
    "alchemist_table":      (checkerboard, ((60,20,100),(100,40,160))),
    "research_desk":        (bordered, (( 60, 60,160), ( 30, 30, 90))),
    "guard_tower":          (bordered, ((100,100,120), ( 50, 50, 70))),
    "chicken_coop":         (striped,  ((220,200,160),(180,140,100))),
    "beehive_station":      (striped,  ((220,180, 40),(100, 80, 20))),
    "tanners_bench":        (bordered, ((140, 80, 40), ( 80, 40, 20))),
    "pottery_station":      (bordered, ((180,120, 60), ( 60,  40, 20))),
    "glass_furnace":        (bordered, ((180,220,240), (100,160,200))),
}

for name, (fn, args) in BLOCK_TEXTURES.items():
    path = os.path.join(BLOCK_ROOT, f"{name}.png")
    write_png(path, fn(*args))

print(f"[+] {len(BLOCK_TEXTURES)} block textures written.")

# ─── Item textures ─────────────────────────────────────────────────────────────

write_png(os.path.join(ITEM_ROOT, "job_assignment_book.png"),
          bordered((200,160,80),(100,60,20)))
print("[+] 1 item texture written.")

# ─── Entity textures (64×64 Minecraft skin format) ────────────────────────────

def write_entity(filename, base, dark):
    skin = humanoid_skin(base, dark, size=64)
    path = os.path.join(ENTITY_ROOT, filename)
    # Use the write_png but for 64x64
    w, h = 64, 64
    raw = b""
    for row in skin:
        raw += b"\x00"
        for r, g, b, a in row:
            raw += bytes([r, g, b, a])
    compressed = zlib.compress(raw, 9)
    sig  = b"\x89PNG\r\n\x1a\n"
    ihdr = _png_chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
    idat = _png_chunk(b"IDAT", compressed)
    iend = _png_chunk(b"IEND", b"")
    with open(path, "wb") as f:
        f.write(sig + ihdr + idat + iend)

write_entity("colonist.png",      (230,200,160), (180,150,110))
write_entity("guard.png",         (100,120,160), ( 60, 80,120))
write_entity("colony_monster.png",(200,200,200), (120,120,120))
print("[+] 3 entity textures written.")

# ─── Mod icon (64x64) ─────────────────────────────────────────────────────────

# Golden banner icon
icon = [[(0,0,0,0)]*64 for _ in range(64)]
for y in range(8, 56):
    for x in range(8, 56):
        if x < 10 or x >= 54 or y < 10:
            icon[y][x] = (200,160,20,255)
        elif y < 48:
            icon[y][x] = (240,200,60,255)
        elif x < 28 or x >= 36:
            icon[y][x] = (160,100,30,255)

icon_path = r"C:\ColonyCraft\src\main\resources\assets\colonycraft\icon.png"
with open(icon_path, "wb") as f:
    w, h = 64, 64
    raw = b""
    for row in icon:
        raw += b"\x00"
        for r, g, b, a in row:
            raw += bytes([r, g, b, a])
    compressed = zlib.compress(raw, 9)
    sig  = b"\x89PNG\r\n\x1a\n"
    ihdr = _png_chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
    idat = _png_chunk(b"IDAT", compressed)
    iend = _png_chunk(b"IEND", b"")
    f.write(sig + ihdr + idat + iend)

print("[+] Mod icon written.")
print("\nAll textures generated successfully!")
