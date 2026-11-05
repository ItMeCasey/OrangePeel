"""Draws the OrangePeel icon as 32x32 pixel art and writes it at several sizes.

Run from the repository root: python branding/source/make_icon.py
Writes branding/icon/icon_<size>.png, branding/icon/icon.svg and orangepeel_logo.png (for the jar).

The picture: an orange with a strip of peel curling off it and a leaf on top, on the studio's night tile.
"""
import math
import random
from pathlib import Path

from PIL import Image

N = 32
OUT = Path(__file__).resolve().parent.parent / "icon"

# Studio palette
BG = (24, 22, 38)
BG_EDGE = (46, 42, 70)
BG_TOP = (58, 54, 88)
GOLD = (242, 179, 61)
GOLD_HI = (255, 226, 140)
GOLD_LO = (178, 116, 32)
# Orange
ORANGE = [(250, 140, 26), (240, 124, 18), (255, 156, 40)]
ORANGE_LO = (196, 90, 12)
ORANGE_HI = (255, 196, 96)
PITH = (255, 236, 176)
PITH_LO = (240, 206, 120)
SEG = (250, 168, 52)
SEG_LINE = (222, 120, 24)
LEAF = (98, 176, 64)
LEAF_LO = (64, 130, 44)
STEM = (96, 62, 34)

random.seed(11)
px = [[None] * N for _ in range(N)]


def put(x, y, c):
    if 0 <= x < N and 0 <= y < N:
        px[y][x] = c


# Background tile with stepped rounded corners.
R = 4
for y in range(N):
    for x in range(N):
        cx = min(max(x, R - 0.5), N - R - 0.5)
        cy = min(max(y, R - 0.5), N - R - 0.5)
        if math.hypot(x - cx, y - cy) <= R:
            px[y][x] = BG
for y in range(N):
    for x in range(N):
        if px[y][x] is None:
            continue
        edge = any(
            not (0 <= x + dx < N and 0 <= y + dy < N) or px[y + dy][x + dx] is None
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))
        )
        if edge:
            px[y][x] = BG_TOP if y < N // 2 else BG_EDGE

# The orange: a disc with the lower right wedge peeled back to the pith and segments.
FX, FY, FR = 14.5, 17.5, 10.2
for y in range(N):
    for x in range(N):
        fx, fy = x + 0.5, y + 0.5
        d = math.hypot(fx - FX, fy - FY)
        if d > FR:
            continue
        ang = math.degrees(math.atan2(fy - FY, fx - FX)) % 360  # 0 = right, 90 = down
        peeled = 10 <= ang <= 100 and d > 3.0 or (d <= 3.0 and 10 <= ang <= 100)
        if peeled:
            if d > FR - 1.6:
                c = PITH_LO
            elif d > FR - 3.2:
                c = PITH
            else:
                # segments: alternating bands around the center
                band = int((ang - 10) / 15)
                c = SEG if band % 2 == 0 else (255, 182, 70)
                if abs(((ang - 10) % 15) - 0) < 1.6 or abs(((ang - 10) % 15) - 15) < 1.6:
                    c = SEG_LINE
            put(x, y, c)
        else:
            light = -((fx - FX) + (fy - FY)) / (FR * 1.4)
            if light > 0.45:
                c = ORANGE_HI
            elif light < -0.45:
                c = ORANGE_LO
            else:
                c = random.choice(ORANGE)
            put(x, y, c)

# Rind dimples (tiny darker dots) on the intact skin.
for x, y in ((8, 14), (11, 11), (6, 19), (10, 23), (15, 9), (4, 16)):
    if px[y][x] in ORANGE or px[y][x] == ORANGE_HI:
        put(x, y, ORANGE_LO)

# Peel ribbon curling out of the wedge toward the lower right.
for t in range(0, 101):
    u = t / 100.0
    ang = math.radians(100 - 85 * u)
    rad = FR - 0.5 + 6.0 * u
    cxp = FX + math.cos(ang) * rad * 0.95 + 9.0 * u
    cyp = FY + math.sin(ang) * rad * 0.95 + 1.0 * u * u
    for dx in (-1, 0, 1):
        for dy in (0, 1):
            x, y = int(cxp) + dx, int(cyp) + dy
            if 0 <= x < N and 0 <= y < N and (px[y][x] is None or px[y][x] in (BG, BG_EDGE, BG_TOP)):
                put(x, y, ORANGE_LO if dy == 1 else ORANGE[0])
# Highlight on the ribbon
for t in range(0, 101, 3):
    u = t / 100.0
    ang = math.radians(100 - 85 * u)
    rad = FR - 0.5 + 6.0 * u
    x = int(FX + math.cos(ang) * rad * 0.95 + 9.0 * u)
    y = int(FY + math.sin(ang) * rad * 0.95 + 1.0 * u * u)
    if 0 <= x < N and 0 <= y < N and px[y][x] in ORANGE:
        put(x, y, ORANGE_HI)

# Stem and leaf.
for y in (6, 7):
    put(14, y, STEM)
leaf = [
    "..XXX.",
    ".XXXXX",
    "XXXXX.",
    ".XXX..",
]
for j, row in enumerate(leaf):
    for i, ch in enumerate(row):
        if ch == "X":
            put(15 + i, 4 + j, LEAF if (i + j) % 3 else LEAF_LO)

# Gold spark, upper right.
for x, y, c in ((26, 5, GOLD_HI), (25, 5, GOLD), (27, 5, GOLD), (26, 4, GOLD), (26, 6, GOLD), (28, 9, GOLD_HI)):
    put(x, y, c)

img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
for y in range(N):
    for x in range(N):
        if px[y][x] is not None:
            img.putpixel((x, y), px[y][x] + (255,))

OUT.mkdir(parents=True, exist_ok=True)
for size in (32, 64, 128, 256, 512, 1024):
    img.resize((size, size), Image.NEAREST).save(OUT / f"icon_{size}.png", optimize=True)
img.resize((512, 512), Image.NEAREST).save(OUT / "orangepeel_logo.png", optimize=True)

rects = []
for y in range(N):
    for x in range(N):
        c = px[y][x]
        if c is not None:
            rects.append(f'<rect x="{x}" y="{y}" width="1.02" height="1.02" fill="#{c[0]:02x}{c[1]:02x}{c[2]:02x}"/>')
(OUT / "icon.svg").write_text(
    f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {N} {N}" shape-rendering="crispEdges">'
    + "".join(rects)
    + "</svg>\n",
    encoding="utf-8",
)
print("wrote", OUT)
