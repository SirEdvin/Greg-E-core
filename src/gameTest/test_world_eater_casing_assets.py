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
                for frame in range(source.height // 16):
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
                for frame in range(source.height // 16):
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
        for color in ('black', 'purple', 'green', 'orange'):
            with Image.open(TEXTURES / f'{color}.png') as source, Image.open(TEXTURES / f'{color}_ctm.png') as connected:
                for frame in range(source.height // 16):
                    base = source.crop((0, frame * 16, 16, (frame + 1) * 16))
                    ctm = connected.crop((0, frame * 32, 32, (frame + 1) * 32))
                    panel = render_panel(base, ctm, 4, 2)
                    pixel = base.getpixel((6, 8))
                    assert isinstance(pixel, tuple)
                    rgb = pixel[:3]
                    for y in range(8, 24):
                        for x in range(8, 56):
                            delta = (-7 if (x - y) % 16 < 3 else 0)
                            delta += 2 if (x % 16 + 3 * (y % 16)) % 17 == 0 else 0
                            self.assertEqual(panel.getpixel((x, y)), tuple(c + delta for c in rgb) + (255,),
                                             f'{color} frame {frame}, joined interior ({x}, {y})')

    def test_native_connection_metadata_and_optional_bloom(self):
        for color in ('black', 'purple', 'green', 'orange'):
            for suffix in ('', '_emissive'):
                name = color + suffix
                meta = json.loads((TEXTURES / f'{name}.png.mcmeta').read_text())
                self.assertEqual(meta.get('ldlib', {}).get('connection'),
                                 f'gregecore:block/casings/world_eater/{name}_ctm')
                path = TEXTURES / f'{name}_ctm.png'
                with Image.open(path) as image:
                    self.assertEqual(image.size, (32, 32))
                ctm = json.loads(path.with_suffix('.png.mcmeta').read_text())
                for data in (meta, ctm):
                    self.assertEqual(data.get('ldlib', {}).get('emissive', False), bool(suffix))
                    self.assertEqual(data.get('shimmer', {}).get('bloom', False), bool(suffix))

    def test_glow_keeps_only_connected_outer_edges(self):
        for color in ('black', 'purple', 'green', 'orange'):
            with Image.open(TEXTURES / f'{color}_emissive.png') as base, Image.open(TEXTURES / f'{color}_emissive_ctm.png') as ctm:
                for width, height in ((4, 2), (4, 1), (1, 4), (3, 3)):
                    alpha = render_panel(base, ctm, width, height).getchannel('A')
                    for y in range(height * 16):
                        for x in range(width * 16):
                            distance = min(x, y, width * 16 - 1 - x, height * 16 - 1 - y)
                            self.assertEqual(alpha.getpixel((x, y)) == 255, distance == 2,
                                             f'{color} joined glow ({x}, {y}) has no interior seams')

    def test_every_texture_is_single_frame_without_animation(self):
        for color in ('black', 'purple', 'green', 'orange'):
            for suffix in ('', '_emissive', '_ctm', '_emissive_ctm'):
                with self.subTest(color=color, layer=suffix):
                    path = TEXTURES / f'{color}{suffix}.png'
                    with Image.open(path) as image:
                        size = 32 if suffix.endswith('_ctm') else 16
                        self.assertEqual(image.size, (size, size))
                        data = json.loads(path.with_suffix('.png.mcmeta').read_text())
                        self.assertNotIn('animation', data)

    def test_base_remains_shaded_and_controller_keeps_status_overlays(self):
        for color in ('black', 'purple', 'green', 'orange'):
            suffix = '' if color == 'black' else '_' + color
            model = json.loads((ASSETS / f'models/block/world_eater_tamer_casing{suffix}.json').read_text())
            self.assertNotIn('forge_data', model['elements'][0])
            self.assertTrue(model['elements'][0].get('shade', True))
        controller = json.loads((ASSETS / 'models/block/world_eater_controller_template.json').read_text())
        self.assertEqual(len(controller['elements']), 4)
        self.assertEqual(controller['elements'][1]['faces']['north']['texture'], '#glow')
        self.assertEqual(controller['elements'][2]['faces']['north']['texture'], '#overlay_front')
        self.assertEqual(controller['elements'][3]['faces']['north']['texture'], '#overlay_front_emissive')

    def test_emissive_is_transparent_detail_not_full_plate(self):
        for color in ('black', 'purple', 'green', 'orange'):
            with Image.open(TEXTURES / f'{color}_emissive.png') as image:
                count = 256 - image.crop((0, 0, 16, 16)).getchannel('A').histogram()[0]
                self.assertGreater(count, 0)
                self.assertLess(count, 64)
            suffix = '' if color == 'black' else '_' + color
            model = json.loads((ASSETS / f'models/block/world_eater_tamer_casing{suffix}.json').read_text())
            self.assertEqual(model['elements'][1]['forge_data'], {'block_light': 15, 'sky_light': 15})
            self.assertNotIn('forge_data', model['elements'][0])


    def test_controller_color_states_preserve_workable_overlays(self):
        path = Path(__file__).resolve().parents[2] / 'src/generated/resources/assets/gregecore/models/block/machine/incomplete_world_eater_tamer.json'
        model = json.loads(path.read_text())
        expected = {(color, formed, status) for color in ('black', 'purple', 'green', 'orange')
                    for formed in ('false', 'true') for status in ('idle', 'suspend', 'waiting', 'working')}
        actual = set()
        for key, variant in model['variants'].items():
            state = dict(part.split('=') for part in key.split(','))
            self.assertIn('casing_color', state)
            color = state['casing_color'] if state['is_formed'] == 'true' else 'black'
            textures = variant['model']['textures']
            base = f'gregecore:block/casings/world_eater/{color}'
            self.assertEqual(textures['all'], base)
            self.assertEqual(textures['glow'], base + '_emissive')
            active = '_active' if state['recipe_logic_status'] in ('waiting', 'working') else ''
            overlay = 'gtceu:block/multiblock/research_station/overlay_front' + active
            self.assertEqual(textures['overlay_front'], overlay)
            self.assertEqual(textures['overlay_front_emissive'], overlay + '_emissive')
            actual.add((state['casing_color'], state['is_formed'], state['recipe_logic_status']))
        self.assertEqual(actual, expected)
        self.assertEqual(model['dynamic_renders'], [{'type': 'gregecore:world_eater_parts'}])


if __name__ == '__main__':
    unittest.main()
