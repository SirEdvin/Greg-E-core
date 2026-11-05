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


def interior(rgb, x, y):
    # A 16px period matches the block lattice across joins.
    x, y = x % 16, y % 16
    delta = (-7 if (x - y) % 16 < 3 else 0)
    delta += 2 if (x + 3 * y) % 17 == 0 else 0
    return tone(rgb, delta)


def plate(rgb):
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
                p[x, y] = interior(rgb, x, y)
                continue
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


def connected_corner(rgb, layer, origin, edges):
    """One native 8px submap, with only its disconnected edge profile."""
    image = Image.new('RGBA', (8, 8))
    p = image.load()
    assert p is not None
    original = plate(rgb)
    ox, oy = origin
    for ty in range(8):
        for tx in range(8):
            x, y = ox + tx, oy + ty
            distances = [(dict(left=x, right=15 - x, top=y, bottom=15 - y)[edge], edge) for edge in edges]
            # A missing diagonal is a concave corner: both distances must be
            # inside the border, and the farther distance defines its ring.
            distance, edge = max(distances) if distances else (16, '')
            if layer != 'base':
                pixel = tone(rgb, 60) if distance == 2 else (0, 0, 0, 0)
            elif distance < 3:
                # Extrude a straight edge from its midpoint. Copying (x,y)
                # would reintroduce the standalone corner/end-cap at every join.
                pixel = original.getpixel((x, 8) if edge in ('left', 'right') else (8, y))
            else:
                pixel = interior(rgb, x, y)
            assert pixel is not None
            p[tx, ty] = pixel
    return image


def glow(rgb):
    image = Image.new('RGBA', (16, 16))
    p = image.load()
    assert p is not None
    for y in range(16):
        for x in range(16):
            if min(x, y, 15 - x, 15 - y) == 2:
                p[x, y] = tone(rgb, 60)
    return image


def connected_tile(rgb, layer):
    image = Image.new('RGBA', (32, 32))
    # Author the actual 4x4 grid, not independently framed 16px quadrants.
    # LDLib 1.0.40.b samples 10/11/14/15 when
    # both orthogonal neighbours exist but their diagonal is missing.
    slots = (
        ((0, 0), ()), ((8, 0), ()), ((0, 0), ('left',)), ((8, 0), ('right',)),
        ((0, 8), ()), ((8, 8), ()), ((0, 8), ('left',)), ((8, 8), ('right',)),
        ((0, 0), ('top',)), ((8, 0), ('top',)), ((0, 0), ('top', 'left')), ((8, 0), ('top', 'right')),
        ((0, 8), ('bottom',)), ((8, 8), ('bottom',)), ((0, 8), ('bottom', 'left')), ((8, 8), ('bottom', 'right')))
    for index, (origin, edges) in enumerate(slots):
        image.paste(connected_corner(rgb, layer, origin, edges), (index % 4 * 8, index // 4 * 8))
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
        for layer in ('base', 'glow'):
            sheet = plate(rgb) if layer == 'base' else glow(rgb)
            ctm = connected_tile(rgb, layer)
            filename = f'{color}{"_emissive" if layer == "glow" else ""}'
            sheet.save(TEXTURES / f'{filename}.png')
            ctm.save(TEXTURES / f'{filename}_ctm.png')
            meta: dict = {'ldlib': {'connection': f'gregecore:block/casings/world_eater/{filename}_ctm'}}
            ctm_meta = {'ldlib': {'emissive': layer == 'glow'}}
            if layer == 'glow':
                meta['ldlib']['emissive'] = True
                # Match native GTCEu coil metadata; Shimmer remains optional.
                meta['shimmer'] = ctm_meta['shimmer'] = {'bloom': True}
            dump(TEXTURES / f'{filename}.png.mcmeta', meta)
            dump(TEXTURES / f'{filename}_ctm.png.mcmeta', ctm_meta)
        texture = f'gregecore:block/casings/world_eater/{color}'
        dump(ASSETS / f'models/block/world_eater_tamer_casing{"_" + color if color != "black" else ""}.json',
             {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout_mipped',
              'textures': {'particle': texture, 'all': texture, 'glow': texture + '_emissive'},
              'elements': [cube('#all'), cube('#glow', 0.001, True)]})
        tile = plate(rgb)
        tile.alpha_composite(glow(rgb))
        previews.append((color, tile))
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
    contact = Image.new('RGB', (4 * 192, 220), '#303038')
    draw = ImageDraw.Draw(contact)
    for index, (color, tile) in enumerate(previews):
        x, y = index * 192, 0
        contact.paste(tile.resize((176, 176), Image.Resampling.NEAREST), (x + 8, y + 26))
        draw.text((x + 8, y + 8), f'{color} static glow', fill='white')
    contact.save(out / 'pixel-contact-sheet.png')
    print(out / 'pixel-contact-sheet.png')


if __name__ == '__main__':
    generate()
