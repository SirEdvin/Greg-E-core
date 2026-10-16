"""Resource contract checks; run: python -m unittest discover -s src/gameTest -p 'test_*.py'."""
import json
import unittest
from pathlib import Path
from PIL import Image

ASSETS = Path(__file__).resolve().parents[2] / 'src/main/resources/assets/gregecore'
TEXTURES = ASSETS / 'textures/block/casings/world_eater'


def render_panel(base, ctm, width, height):
    """Reconstruct LDLib's actual 8px submaps, not four independent 16px tiles."""
    image = Image.new('RGBA', (width * 16, height * 16))
    for by in range(height):
        for bx in range(width):
            for ox, oy, offset, fallback, dx, dy in (
                    (0, 0, 0, 16, -1, -1), (8, 0, 1, 17, 1, -1),
                    (0, 8, 4, 18, -1, 1), (8, 8, 5, 19, 1, 1)):
                vertical = 0 <= by + dy < height
                horizontal = 0 <= bx + dx < width
                # Filled rectangles have their diagonal whenever both sides exist.
                index = offset if vertical and horizontal else offset + 2 * vertical + 8 * horizontal
                if not (vertical or horizontal):
                    index = fallback
                if index >= 16:
                    tile = base.crop((ox, oy, ox + 8, oy + 8))
                else:
                    x, y = index % 4 * 8, index // 4 * 8
                    tile = ctm.crop((x, y, x + 8, y + 8))
                image.paste(tile, (bx * 16 + ox, by * 16 + oy))
    return image


class CasingAssetsTest(unittest.TestCase):
    def test_missing_diagonal_uses_concave_not_standalone_corners(self):
        for color in ('black', 'purple', 'green', 'orange'):
            with Image.open(TEXTURES / f'{color}.png') as source, Image.open(TEXTURES / f'{color}_ctm.png') as connected:
                for frame in range(8):
                    base = source.crop((0, frame * 16, 16, (frame + 1) * 16))
                    ctm = connected.crop((0, frame * 32, 32, (frame + 1) * 32))
                    for index, ox, oy, right, bottom in ((10, 0, 0, False, False), (11, 8, 0, True, False),
                                                       (14, 0, 8, False, True), (15, 8, 8, True, True)):
                        for dx in range(4):
                            for dy in range(4):
                                x, y = (15 - dx if right else dx), (15 - dy if bottom else dy)
                                expected = {ctm.getpixel((x, y))}
                                if max(dx, dy) < 3:
                                    # Equal-distance diagonal pixels may use either
                                    # edge's light direction; off-diagonal pixels
                                    # must use the farther ring, not an outer cap.
                                    expected = {base.getpixel((8, y))} if dy > dx else {base.getpixel((x, 8))}
                                    if dx == dy:
                                        expected.add(base.getpixel((8, y)))
                                self.assertIn(ctm.getpixel((index % 4 * 8 + x - ox, index // 4 * 8 + y - oy)), expected)

    def test_connected_perimeter_has_no_standalone_corner_endcaps(self):
        # Includes the user's 4x2 wall, single-row/column strips and the old fixture.
        for color in ('black', 'purple', 'green', 'orange'):
            with Image.open(TEXTURES / f'{color}.png') as source, Image.open(TEXTURES / f'{color}_ctm.png') as connected:
                for frame in range(8):
                    base = source.crop((0, frame * 16, 16, (frame + 1) * 16))
                    ctm = connected.crop((0, frame * 32, 32, (frame + 1) * 32))
                    for width, height in ((4, 2), (4, 1), (1, 4), (3, 3)):
                        panel = render_panel(base, ctm, width, height)
                        with self.subTest(color=color, frame=frame, shape=(width, height)):
                            for y in (0, 1, 2, height * 16 - 3, height * 16 - 2, height * 16 - 1):
                                expected = base.getpixel((8, y % 16))
                                self.assertTrue(all(panel.getpixel((x, y)) == expected for x in range(3, width * 16 - 3)),
                                                f'Horizontal perimeter interrupted at row {y}')
                            for x in (0, 1, 2, width * 16 - 3, width * 16 - 2, width * 16 - 1):
                                expected = base.getpixel((x % 16, 8))
                                self.assertTrue(all(panel.getpixel((x, y)) == expected for y in range(3, height * 16 - 3)),
                                                f'Vertical perimeter interrupted at column {x}')

    def test_connected_shadow_is_one_periodic_field_across_block_joins(self):
        shifts = (0, 0, 1, 1, 0, 0, -1, -1)
        for color in ('black', 'purple', 'green', 'orange'):
            with Image.open(TEXTURES / f'{color}.png') as source, Image.open(TEXTURES / f'{color}_ctm.png') as connected:
                for frame in range(8):
                    base = source.crop((0, frame * 16, 16, (frame + 1) * 16))
                    ctm = connected.crop((0, frame * 32, 32, (frame + 1) * 32))
                    panel = render_panel(base, ctm, 4, 2)
                    pixel = base.getpixel((6, 8))
                    assert isinstance(pixel, tuple)
                    rgb = pixel[:3]
                    shift = 0 if color == 'black' else shifts[frame]
                    for y in range(8, 24):
                        for x in range(8, 56):
                            delta = (-7 if (x - y + shift) % 16 < 3 else 0)
                            delta += 2 if (x % 16 + 3 * (y % 16)) % 17 == 0 else 0
                            self.assertEqual(panel.getpixel((x, y)), tuple(c + delta for c in rgb) + (255,),
                                             f'{color} frame {frame}, joined interior ({x}, {y})')

    def test_native_connection_metadata_and_synchronized_sheets(self):
        for color in ('black', 'purple', 'green', 'orange'):
            for suffix in ('', '_emissive'):
                name = color + suffix
                meta = json.loads((TEXTURES / f'{name}.png.mcmeta').read_text())
                self.assertEqual(meta.get('ldlib', {}).get('connection'),
                                 f'gregecore:block/casings/world_eater/{name}_ctm')
                path = TEXTURES / f'{name}_ctm.png'
                with Image.open(path) as image:
                    self.assertEqual(image.size, (32, 256))
                ctm = json.loads(path.with_suffix('.png.mcmeta').read_text())
                self.assertEqual((ctm['animation']['width'], ctm['animation']['height']), (32, 32))
                self.assertEqual(ctm['animation']['frames'], meta['animation']['frames'])
                if suffix:
                    self.assertTrue(meta['ldlib']['emissive'])
                    self.assertTrue(ctm['ldlib']['emissive'])
                    with Image.open(path) as image:
                        alpha = [image.crop((0, i * 32, 16, i * 32 + 16)).getchannel('A') for i in range(8)]
                        self.assertTrue(any(frame.getpixel((0, 0)) != 0 for frame in alpha))
                        self.assertTrue(any(frame.getpixel((15, 15)) != 0 for frame in alpha))
                        self.assertEqual(alpha[0].getbbox(), None)

    def test_shimmer_reaches_both_corners_before_reset(self):
        for color in ('black', 'purple', 'green', 'orange'):
            with Image.open(TEXTURES / f'{color}_emissive.png') as image:
                alpha = [image.crop((0, i * 16, 16, (i + 1) * 16)).getchannel('A') for i in range(8)]
                self.assertTrue(any(frame.getpixel((2, 2)) != 0 for frame in alpha), 'Sweep begins at near corner')
                self.assertTrue(any(frame.getpixel((13, 13)) != 0 for frame in alpha), 'Sweep must reach far corner, not stop in center')
                self.assertEqual(alpha[0].getbbox(), None, 'Quiet reset frame is transparent')

    def test_animation_bounds_and_timing(self):
        for color in ('black', 'purple', 'green', 'orange'):
            loops = []
            for suffix in ('', '_emissive'):
                with self.subTest(color=color, layer=suffix):
                    path = TEXTURES / f'{color}{suffix}.png'
                    with Image.open(path) as image:
                        self.assertEqual(image.size, (16, 128))
                        data = json.loads(path.with_suffix('.png.mcmeta').read_text())
                        animation = data['animation']
                        self.assertEqual((animation['width'], animation['height']), (16, 16))
                        self.assertFalse(animation['interpolate'])
                        self.assertTrue(all(0 <= f['index'] < 8 and f['time'] > 0 for f in animation['frames']))
                        loops.append(animation['frames'])
                        self.assertEqual(sum(f['time'] for f in animation['frames']), 200 if color == 'black' else 40)
                        self.assertEqual(data.get('ldlib', {}).get('emissive', False), suffix == '_emissive')
            self.assertEqual(loops[0], loops[1])

    def test_shadow_motion_and_fixed_rivets(self):
        for color in ('black', 'purple', 'green', 'orange'):
            with Image.open(TEXTURES / f'{color}.png') as image:
                frames = [image.crop((0, i * 16, 16, (i + 1) * 16)).convert('RGBA') for i in range(8)]
                protected = {(x, y) for x in range(16) for y in range(16) if x <= 2 or x >= 13 or y <= 2 or y >= 13}
                for x, y in ((3, 3), (11, 3), (3, 11), (11, 11)):
                    protected.update((x + dx, y + dy) for dx in (0, 1) for dy in (0, 1))
                for frame in frames:
                    self.assertTrue(all(frame.getpixel(p) == frames[0].getpixel(p) for p in protected))
                unique = {frame.tobytes() for frame in frames}
                self.assertEqual(len(unique), 1 if color == 'black' else 3)

    def test_emissive_is_transparent_detail_not_full_plate(self):
        for color in ('black', 'purple', 'green', 'orange'):
            with Image.open(TEXTURES / f'{color}_emissive.png') as image:
                counts = [256 - image.crop((0, i * 16, 16, (i + 1) * 16)).getchannel('A').histogram()[0] for i in range(8)]
                self.assertEqual(counts[0], 0)
                self.assertGreater(max(counts), 0)
                self.assertLess(max(counts), 40)
            suffix = '' if color == 'black' else '_' + color
            model = json.loads((ASSETS / f'models/block/world_eater_tamer_casing{suffix}.json').read_text())
            self.assertEqual(model['elements'][1]['forge_data'], {'block_light': 15, 'sky_light': 15})
            self.assertNotIn('forge_data', model['elements'][0])


if __name__ == '__main__':
    unittest.main()
