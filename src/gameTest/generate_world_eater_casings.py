"""Pixel-authored 16px industrial plates; run from the repository root.

Original raster artwork, MIT like the project. GTCEu Industrial Steam Casing is
used as a visual reference only: no upstream image pixels are copied.
Requires Pillow for asset generation/verification, never at game runtime.
"""
import json
import os
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/gregecore'
TEXTURES = ASSETS / 'textures/block/casings/world_eater'
COLORS = {'black': (23, 23, 25), 'purple': (48, 24, 65),
          'green': (23, 47, 31), 'orange': (54, 29, 14)}
FACES = ('down', 'up', 'north', 'south', 'west', 'east')


def dump(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + '\n')


def tone(rgb, offset):
    return tuple(max(0, min(255, c + offset)) for c in rgb) + (255,)


def plate(rgb, shift):
    image = Image.new('RGBA', (16, 16), tone(rgb, -16))
    p = image.load()
    assert p is not None
    for y in range(1, 15):
        for x in range(1, 15):
            if x in (1, 14) or y in (1, 14):
                delta = 19 if x == 1 or y == 1 else -5
            elif x in (2, 13) or y in (2, 13):
                delta = -9 if x == 2 or y == 2 else 7
            else:
                # Broad hand-stepped diagonal shadows; motion stays inside plate.
                band = (x - y + shift) % 11
                delta = -7 if band in (0, 1, 2) else 0
                if (x + 3 * y) % 17 == 0:
                    delta += 2
            p[x, y] = tone(rgb, delta)
    for x, y in ((3, 3), (11, 3), (3, 11), (11, 11)):
        p[x, y] = tone(rgb, 31)
        p[x + 1, y] = tone(rgb, 20)
        p[x, y + 1] = tone(rgb, 12)
        p[x + 1, y + 1] = tone(rgb, -6)
    # Discontinuous corner bevels and two-pixel seam cuts, not smooth borders.
    for x, y in ((0, 0), (15, 0), (0, 15), (15, 15)):
        p[x, y] = tone(rgb, -19)
    return image


def connected_plate(rgb, shift, edges=(), corners=False):
    image = Image.new('RGBA', (16, 16))
    p = image.load()
    assert p is not None
    original = plate(rgb, shift)
    for y in range(16):
        for x in range(16):
            band = (x - y + shift) % 11
            delta = (-7 if band in (0, 1, 2) else 0) + (2 if (x + 3 * y) % 17 == 0 else 0)
            p[x, y] = tone(rgb, delta)
            keep = ('left' in edges and x <= 2 or 'right' in edges and x >= 13
                    or 'top' in edges and y <= 2 or 'bottom' in edges and y >= 13)
            if corners:
                keep = (x <= 2 or x >= 13) and (y <= 2 or y >= 13)
            if keep:
                pixel = original.getpixel((x, y))
                assert pixel is not None
                p[x, y] = pixel
    return image


def shimmer(rgb, frame, connected=False):
    image = Image.new('RGBA', (16, 16))
    if frame == 0:
        return image
    # Traverse the whole diagonal, rather than ending at x+y=16 in the center.
    crest = (frame - 1) * 5
    p = image.load()
    assert p is not None
    extent = range(16) if connected else range(2, 14)
    for y in extent:
        for x in extent:
            distance = x + y - crest
            if distance in (-1, 0, 1):
                brightness = 100 if distance == 0 else 48
                p[x, y] = tone(rgb, brightness)
    return image


def connected_tile(rgb, shift, layer, frame):
    image = Image.new('RGBA', (32, 32))
    # LDLib's 4x4 submap grid uses these four 16px regions. Isolated
    # quadrants are sampled from the original sprite, not this CTM sheet.
    for x, y, edges, corners in ((0, 0, (), False),
                                  (16, 0, ('left', 'right'), False),
                                  (0, 16, ('top', 'bottom'), False),
                                  (16, 16, (), True)):
        tile = connected_plate(rgb, shift, edges, corners) if layer == 'base' else shimmer(rgb, frame, True)
        image.paste(tile, (x, y))
    return image


def cube(texture, inset: float = 0, emissive=False):
    element = {'from': [-inset] * 3, 'to': [16 + inset] * 3,
               'faces': {face: {'uv': [0, 0, 16, 16], 'texture': texture,
                                'cullface': face} for face in FACES}}
    if emissive:
        element.update({'shade': False, 'forge_data': {'block_light': 15, 'sky_light': 15}})
    return element


def generate():
    TEXTURES.mkdir(parents=True, exist_ok=True)
    previews = []
    for color, rgb in COLORS.items():
        times = [170, 4, 4, 4, 4, 4, 5, 5] if color == 'black' else [5] * 8
        shifts = (0,) * 8 if color == 'black' else (0, 0, 1, 1, 0, 0, -1, -1)
        for layer in ('base', 'shimmer'):
            sheet = Image.new('RGBA', (16, 128))
            ctm = Image.new('RGBA', (32, 256))
            for frame, shift in enumerate(shifts):
                tile = plate(rgb, shift) if layer == 'base' else shimmer(rgb, frame)
                sheet.paste(tile, (0, frame * 16))
                ctm.paste(connected_tile(rgb, shift, layer, frame), (0, frame * 32))
            filename = f'{color}{"_emissive" if layer == "shimmer" else ""}'
            sheet.save(TEXTURES / f'{filename}.png')
            ctm.save(TEXTURES / f'{filename}_ctm.png')
            meta = {'animation': {'interpolate': False, 'width': 16, 'height': 16,
                                 'frames': [{'index': i, 'time': t} for i, t in enumerate(times)]},
                    'ldlib': {'connection': f'gregecore:block/casings/world_eater/{filename}_ctm'}}
            if layer == 'shimmer':
                meta['ldlib']['emissive'] = True
            dump(TEXTURES / f'{filename}.png.mcmeta', meta)
            dump(TEXTURES / f'{filename}_ctm.png.mcmeta',
                 {'animation': dict(meta['animation'], width=32, height=32),
                  'ldlib': {'emissive': layer == 'shimmer'}})
        texture = f'gregecore:block/casings/world_eater/{color}'
        dump(ASSETS / f'models/block/world_eater_tamer_casing{"_" + color if color != "black" else ""}.json',
             {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout_mipped',
              'textures': {'particle': texture, 'all': texture, 'glow': texture + '_emissive'},
              'elements': [cube('#all'), cube('#glow', 0.001, True)]})
        for frame in (0, 2, 4):
            tile = plate(rgb, shifts[frame])
            tile.alpha_composite(shimmer(rgb, frame))
            previews.append((color, frame, tile))
    # Controller's native workable overlay template plus a separate casing glow.
    # Four overlay directions remain controlled by GTCEu's normal status models.
    textures = {f'overlay_{side}{suffix}': 'gtceu:block/void'
                for side in ('front', 'back', 'top', 'bottom', 'side')
                for suffix in ('', '_emissive')}
    base = cube('#all')
    base['faces'] = {face: dict(value, tintindex=1) for face, value in base['faces'].items()}
    elements = [base, cube('#glow', 0.001, True)]
    for suffix, inset in (('', 0.01), ('_emissive', 0.02)):
        element = cube('#unused', inset, bool(suffix))
        for face, alias in zip(FACES, ('bottom', 'top', 'front', 'back', 'side', 'side')):
            element['faces'][face]['texture'] = f'#overlay_{alias}{suffix}'
        elements.append(element)
    dump(ASSETS / 'models/block/world_eater_controller_template.json',
         {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout_mipped',
          'textures': dict(textures, particle='#all'), 'elements': elements})
    out = Path(os.environ['TMPDIR']) / 'world-eater-casings'
    out.mkdir(parents=True, exist_ok=True)
    contact = Image.new('RGB', (3 * 192, 4 * 220), '#303038')
    draw = ImageDraw.Draw(contact)
    for index, (color, frame, tile) in enumerate(previews):
        x, y = index % 3 * 192, index // 3 * 220
        contact.paste(tile.resize((176, 176), Image.Resampling.NEAREST), (x + 8, y + 26))
        draw.text((x + 8, y + 8), f'{color} frame {frame}', fill='white')
    contact.save(out / 'pixel-contact-sheet.png')
    print(out / 'pixel-contact-sheet.png')


if __name__ == '__main__':
    generate()
