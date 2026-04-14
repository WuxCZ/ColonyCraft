from PIL import Image, ImageDraw
import os, random

OUT_BLOCK = r"C:\ColonyCraft\src\main\resources\assets\colonycraft\textures\block"
OUT_ITEM  = r"C:\ColonyCraft\src\main\resources\assets\colonycraft\textures\item"

def noise_fill(img, base_color, noise=15):
    draw = ImageDraw.Draw(img)
    r0, g0, b0 = base_color
    for y in range(16):
        for x in range(16):
            nr = max(0, min(255, r0 + random.randint(-noise, noise)))
            ng = max(0, min(255, g0 + random.randint(-noise, noise)))
            nb = max(0, min(255, b0 + random.randint(-noise, noise)))
            draw.point((x, y), (nr, ng, nb, 255))

def draw_planks(img, base_color):
    draw = ImageDraw.Draw(img)
    noise_fill(img, base_color, 12)
    r0, g0, b0 = base_color
    dark = (max(0, r0-40), max(0, g0-40), max(0, b0-40), 255)
    for y in [3, 7, 11, 15]:
        for x in range(16):
            draw.point((x, y), dark)

def draw_stone(img, base_color):
    draw = ImageDraw.Draw(img)
    noise_fill(img, base_color, 18)
    r0, g0, b0 = base_color
    dark = (max(0, r0-35), max(0, g0-35), max(0, b0-35), 255)
    for y in [4, 8, 12]:
        for x in range(16):
            draw.point((x, y), dark)
    for y_start, x_off in [(0, 3), (5, 9), (9, 3), (13, 9)]:
        for dy in range(4):
            if y_start + dy < 16:
                draw.point((x_off, y_start + dy), dark)

def draw_crate(img, base_color):
    noise_fill(img, base_color, 10)
    draw = ImageDraw.Draw(img)
    r0, g0, b0 = base_color
    dark = (max(0, r0-50), max(0, g0-50), max(0, b0-50), 255)
    light = (min(255, r0+30), min(255, g0+30), min(255, b0+30), 255)
    for i in range(16):
        draw.point((i, 0), dark)
        draw.point((i, 15), dark)
        draw.point((0, i), dark)
        draw.point((15, i), dark)
    for i in range(16):
        draw.point((i, i), light)
        draw.point((15-i, i), light)

def draw_workbench(img, base_color, accent_color):
    draw_planks(img, base_color)
    draw = ImageDraw.Draw(img)
    for x in range(2, 14):
        for y in range(6, 10):
            r, g, b = accent_color
            nr = max(0, min(255, r + random.randint(-8, 8)))
            ng = max(0, min(255, g + random.randint(-8, 8)))
            nb = max(0, min(255, b + random.randint(-8, 8)))
            draw.point((x, y), (nr, ng, nb, 255))

def draw_furnace(img, base_color, fire_color):
    draw_stone(img, base_color)
    draw = ImageDraw.Draw(img)
    dark = (30, 30, 30, 255)
    for x in range(5, 11):
        for y in range(6, 13):
            draw.point((x, y), dark)
    for x in range(6, 10):
        for y in range(9, 12):
            r, g, b = fire_color
            nr = max(0, min(255, r + random.randint(-20, 20)))
            ng = max(0, min(255, g + random.randint(-10, 10)))
            draw.point((x, y), (nr, ng, b, 255))

def draw_banner(img):
    draw = ImageDraw.Draw(img)
    noise_fill(img, (60, 80, 60), 8)
    for y in range(0, 16):
        draw.point((7, y), (139, 119, 42, 255))
        draw.point((8, y), (160, 140, 60, 255))
    for x in range(3, 13):
        for y in range(2, 10):
            r = 180 + random.randint(-15, 15)
            g = 30 + random.randint(-5, 5)
            b = 30 + random.randint(-5, 5)
            draw.point((x, y), (r, g, b, 255))
    for x in [6, 7, 8, 9]:
        draw.point((x, 4), (255, 215, 0, 255))

def draw_tower(img, base_color):
    draw_stone(img, base_color)
    draw = ImageDraw.Draw(img)
    for x in range(16):
        if x % 4 < 2:
            for y in range(3):
                c = 100 + random.randint(-10, 10)
                draw.point((x, y), (c, c, c, 255))
    for y in range(6, 12):
        draw.point((7, y), (30, 30, 30, 255))
        draw.point((8, y), (30, 30, 30, 255))

random.seed(42)

textures = {
    "colony_banner": ("banner", None, None),
    "stockpile": ("crate", (160, 130, 80), None),
    "research_table": ("workbench", (130, 100, 70), (80, 80, 180)),
    "woodcutter_bench": ("workbench", (140, 110, 60), (180, 140, 80)),
    "forester_hut": ("workbench", (100, 140, 70), (60, 120, 40)),
    "miner_hut": ("workbench", (130, 120, 110), (120, 120, 140)),
    "farmer_hut": ("workbench", (140, 120, 70), (100, 160, 50)),
    "berry_farm": ("workbench", (130, 110, 70), (180, 50, 80)),
    "fishing_hut": ("workbench", (140, 130, 100), (60, 130, 180)),
    "water_well": ("stone", (130, 130, 140), None),
    "stove": ("furnace", (140, 130, 120), (255, 160, 40)),
    "bloomery": ("furnace", (130, 110, 100), (255, 120, 30)),
    "blast_furnace": ("furnace", (100, 100, 110), (255, 80, 20)),
    "tailor_shop": ("workbench", (150, 120, 100), (200, 80, 80)),
    "fletcher_bench": ("workbench", (140, 120, 80), (160, 160, 100)),
    "stonemason_bench": ("workbench", (140, 140, 140), (160, 160, 160)),
    "compost_bin": ("crate", (100, 130, 70), None),
    "grindstone_station": ("stone", (150, 150, 150), None),
    "alchemist_table": ("workbench", (120, 100, 80), (130, 50, 160)),
    "research_desk": ("workbench", (130, 100, 70), (60, 60, 160)),
    "guard_tower": ("tower", (130, 130, 130), None),
    "chicken_coop": ("workbench", (160, 140, 90), (200, 180, 100)),
    "beehive_station": ("workbench", (160, 140, 60), (200, 170, 40)),
    "tanners_bench": ("workbench", (140, 100, 60), (160, 120, 80)),
    "pottery_station": ("workbench", (160, 120, 80), (180, 100, 60)),
    "glass_furnace": ("furnace", (140, 150, 160), (100, 200, 255)),
}

for name, (style, base, accent) in textures.items():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    if style == "banner":
        draw_banner(img)
    elif style == "crate":
        draw_crate(img, base)
    elif style == "stone":
        draw_stone(img, base)
    elif style == "furnace":
        draw_furnace(img, base, accent)
    elif style == "tower":
        draw_tower(img, base)
    elif style == "workbench":
        draw_workbench(img, base, accent)
    img.save(os.path.join(OUT_BLOCK, name + ".png"))
    print("  block/" + name + ".png")

for name in ["guidebook", "job_assignment_book", "area_wand"]:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    if name == "guidebook":
        for x in range(3, 13):
            for y in range(2, 14):
                draw.point((x, y), (139, 90, 43, 255))
        for x in range(4, 12):
            for y in range(3, 13):
                draw.point((x, y), (222, 198, 150, 255))
        for y in range(2, 14):
            draw.point((3, y), (100, 60, 30, 255))
        for x in range(5, 11):
            draw.point((x, 5), (80, 60, 40, 255))
            draw.point((x, 7), (100, 80, 50, 255))
    elif name == "job_assignment_book":
        for x in range(3, 13):
            for y in range(2, 14):
                draw.point((x, y), (50, 120, 50, 255))
        for x in range(4, 12):
            for y in range(3, 13):
                draw.point((x, y), (200, 220, 180, 255))
        for y in range(2, 14):
            draw.point((3, y), (30, 80, 30, 255))
    elif name == "area_wand":
        handle = (100, 70, 40, 255)
        tip = (100, 200, 255, 255)
        for i in range(10):
            x, y = 4 + i, 12 - i
            draw.point((x, y), handle if i < 6 else tip)
            if i < 6:
                draw.point((x+1, y), (80, 55, 30, 255))
        draw.point((12, 2), (255, 255, 100, 255))
        draw.point((11, 1), (255, 255, 200, 255))
        draw.point((13, 1), (255, 255, 200, 255))
    img.save(os.path.join(OUT_ITEM, name + ".png"))
    print("  item/" + name + ".png")

print("Done!")
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
