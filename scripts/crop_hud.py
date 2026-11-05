"""Crops and zooms the hunger bar area (or with --tooltip the drawn tooltip) of tour screenshots into one stacked image for review.

Usage: python scripts/crop_hud.py [--tooltip] <out.png> <shot.png> [<shot.png> ...]
"""
import sys

from PIL import Image, ImageDraw

args = [a for a in sys.argv[1:] if a != "--tooltip"]
tip = "--tooltip" in sys.argv
out, shots = args[0], args[1:]
rows = []
for path in shots:
    im = Image.open(path).convert("RGB")
    w, h = im.size
    scale = h / 1041.0 if h else 1
    if tip:
        box = (int(w / 2 - 170 * scale), int(h / 2 - 270 * scale), int(w / 2 + 520 * scale), int(h / 2 + 180 * scale))
        zoom = 2
    else:
        box = (int(w / 2 - 300 * scale), int(h - 135 * scale), int(w / 2 + 300 * scale), int(h - 60 * scale))
        zoom = 3
    c = im.crop(box)
    c = c.resize((c.width * zoom, c.height * zoom), Image.NEAREST)
    ImageDraw.Draw(c).text((4, 2), path.replace("\\", "/").split("/")[-1], fill=(255, 255, 0))
    rows.append(c)
sheet = Image.new("RGB", (max(r.width for r in rows), sum(r.height for r in rows)))
y = 0
for r in rows:
    sheet.paste(r, (0, y))
    y += r.height
sheet.save(out)
print("wrote", out, sheet.size)
