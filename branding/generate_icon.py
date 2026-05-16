"""Generate the project icon (256x256 PNG) for CurseForge.

Run: python3 branding/generate_icon.py
Output: branding/icon.png (256x256) and branding/icon_400.png (400x400)
"""

from PIL import Image, ImageDraw, ImageFilter


def _clip_to_mask(layer: Image.Image, mask: Image.Image) -> Image.Image:
    """Return a copy of layer where alpha is multiplied by mask (L)."""
    a = layer.split()[3]
    new_a = Image.new("L", layer.size, 0)
    new_a.paste(a, (0, 0), mask)
    out = layer.copy()
    out.putalpha(new_a)
    return out


def make_icon(size: int = 256) -> Image.Image:
    s = 256

    # Mask: rounded square covering the whole canvas
    r = 32
    mask = Image.new("L", (s, s), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, s - 1, s - 1), radius=r, fill=255)

    # 1. Slate background (clipped to rounded rect)
    bg = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    ImageDraw.Draw(bg).rounded_rectangle(
        (0, 0, s - 1, s - 1), radius=r, fill=(24, 28, 38, 255)
    )

    # 2. Diagonal tints: upper-left warm (Epic Fight), lower-right cool (TacZ).
    #    Drawn as full-opacity polygons on a transparent layer, then made
    #    semi-transparent globally and clipped to the rounded mask.
    tint = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    td = ImageDraw.Draw(tint)
    td.polygon([(0, 0), (s, 0), (0, s)], fill=(220, 92, 36, 255))
    td.polygon([(s, 0), (s, s), (0, s)], fill=(46, 138, 70, 255))
    # global opacity ~38%
    tint_alpha = tint.split()[3].point(lambda v: int(v * 0.38))
    tint.putalpha(tint_alpha)
    tint = _clip_to_mask(tint, mask)
    bg = Image.alpha_composite(bg, tint)

    # 3. Diagonal separator line (subtle highlight along the split)
    sep = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    ImageDraw.Draw(sep).line([(s, 0), (0, s)], fill=(255, 255, 255, 36), width=2)
    sep = _clip_to_mask(sep, mask)
    bg = Image.alpha_composite(bg, sep)

    # 4. Sword (vertical) — will rotate +45deg so blade points to upper-left.
    sword = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    sd = ImageDraw.Draw(sword)
    cx = s // 2
    silver = (232, 236, 242, 255)
    silver_edge = (168, 178, 192, 255)
    gold = (224, 176, 72, 255)
    gold_dark = (168, 124, 40, 255)
    handle_dark = (66, 44, 26, 255)
    blade_w = 22
    blade_top = 18
    blade_bot = 188
    # blade
    sd.rectangle(
        (cx - blade_w // 2, blade_top, cx + blade_w // 2, blade_bot), fill=silver
    )
    # darker edge on one side for shape
    sd.rectangle(
        (cx + 4, blade_top, cx + blade_w // 2, blade_bot), fill=silver_edge
    )
    # central fuller (groove)
    sd.line([(cx, blade_top + 6), (cx, blade_bot - 4)], fill=silver_edge, width=2)
    # tip (triangle above blade_top)
    sd.polygon(
        [
            (cx - blade_w // 2, blade_top),
            (cx + blade_w // 2, blade_top),
            (cx, blade_top - 30),
        ],
        fill=silver,
    )
    # crossguard
    sd.rectangle((cx - 60, blade_bot, cx + 60, blade_bot + 16), fill=gold)
    sd.ellipse((cx - 68, blade_bot - 2, cx - 52, blade_bot + 18), fill=gold)
    sd.ellipse((cx + 52, blade_bot - 2, cx + 68, blade_bot + 18), fill=gold)
    sd.rectangle((cx - 60, blade_bot + 12, cx + 60, blade_bot + 16), fill=gold_dark)
    # handle
    sd.rectangle((cx - 9, blade_bot + 16, cx + 9, blade_bot + 56), fill=handle_dark)
    # handle wrap rings
    for y in (blade_bot + 22, blade_bot + 36, blade_bot + 50):
        sd.line([(cx - 9, y), (cx + 9, y)], fill=(28, 18, 10, 255), width=1)
    # pommel
    sd.ellipse((cx - 15, blade_bot + 54, cx + 15, blade_bot + 80), fill=gold)
    sd.ellipse((cx - 11, blade_bot + 60, cx + 3, blade_bot + 70), fill=(248, 220, 140, 255))
    sword_rot = sword.rotate(45, resample=Image.BICUBIC)

    # 5. Rifle (horizontal) — will rotate -45deg so muzzle points to lower-right.
    rifle = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    rd = ImageDraw.Draw(rifle)
    metal = (66, 72, 82, 255)
    metal_light = (96, 104, 116, 255)
    metal_dark = (34, 38, 46, 255)
    wood = (120, 84, 46, 255)
    wood_light = (152, 108, 62, 255)
    # stock (wood, angled)
    rd.polygon(
        [(20, 120), (78, 116), (78, 156), (20, 162)], fill=wood
    )
    rd.polygon(
        [(20, 120), (78, 116), (78, 124), (24, 128)], fill=wood_light
    )
    # cheek riser
    rd.polygon([(50, 116), (80, 112), (80, 120), (52, 124)], fill=wood_light)
    # receiver
    rd.rectangle((76, 116, 138, 156), fill=metal_light)
    rd.rectangle((76, 116, 138, 122), fill=metal)
    # charging handle bump
    rd.rectangle((118, 110, 130, 118), fill=metal_dark)
    # barrel
    rd.rectangle((138, 128, 232, 140), fill=metal)
    rd.rectangle((138, 128, 232, 131), fill=metal_light)
    rd.rectangle((138, 137, 232, 140), fill=metal_dark)
    # muzzle / flash hider
    rd.rectangle((228, 122, 244, 146), fill=metal_dark)
    rd.rectangle((232, 126, 240, 142), fill=metal)
    # trigger guard
    rd.rectangle((100, 156, 124, 168), fill=metal_dark)
    rd.rectangle((104, 158, 120, 166), fill=(0, 0, 0, 0))
    # trigger
    rd.line([(112, 158), (112, 168)], fill=metal_dark, width=2)
    # magazine
    rd.rectangle((92, 156, 118, 196), fill=metal)
    rd.rectangle((92, 156, 96, 196), fill=metal_dark)
    rd.rectangle((114, 156, 118, 196), fill=metal_light)
    # scope body
    rd.rectangle((104, 100, 168, 116), fill=metal_dark)
    rd.ellipse((100, 98, 116, 118), fill=metal_dark)
    rd.ellipse((156, 98, 172, 118), fill=metal_dark)
    # scope highlight
    rd.rectangle((108, 102, 164, 106), fill=metal_light)
    # scope mounts
    rd.rectangle((114, 116, 122, 124), fill=metal_dark)
    rd.rectangle((150, 116, 158, 124), fill=metal_dark)
    rifle_rot = rifle.rotate(-45, resample=Image.BICUBIC)

    # 6. Drop shadow under the weapons
    weapons_alpha = Image.new("L", (s, s), 0)
    weapons_alpha.paste(sword_rot.split()[3], (0, 0), sword_rot.split()[3])
    weapons_alpha.paste(rifle_rot.split()[3], (0, 0), rifle_rot.split()[3])
    shadow_a = weapons_alpha.filter(ImageFilter.GaussianBlur(5))
    shadow_a = shadow_a.point(lambda v: min(150, v))
    shadow_layer = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    shadow_color = Image.new("RGBA", (s, s), (0, 0, 0, 255))
    shadow_layer.paste(shadow_color, (4, 5), shadow_a)
    shadow_layer = _clip_to_mask(shadow_layer, mask)

    # 7. Compose final image
    img = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    img = Image.alpha_composite(img, bg)
    img = Image.alpha_composite(img, shadow_layer)
    img = Image.alpha_composite(img, sword_rot)
    img = Image.alpha_composite(img, rifle_rot)

    # 8. Inner border for crisp edge
    border = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    ImageDraw.Draw(border).rounded_rectangle(
        (2, 2, s - 3, s - 3), radius=r - 2, outline=(255, 255, 255, 56), width=2
    )
    img = Image.alpha_composite(img, border)

    if size != s:
        img = img.resize((size, size), Image.LANCZOS)
    return img


if __name__ == "__main__":
    import pathlib

    out_dir = pathlib.Path(__file__).resolve().parent
    make_icon(256).save(out_dir / "icon.png", "PNG")
    make_icon(400).save(out_dir / "icon_400.png", "PNG")
    print(f"wrote {out_dir/'icon.png'} and {out_dir/'icon_400.png'}")
