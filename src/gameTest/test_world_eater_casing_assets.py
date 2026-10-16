"""Resource contract checks; run: python -m unittest discover -s src/gameTest -p 'test_*.py'."""
import json
import unittest
from pathlib import Path
from PIL import Image

ASSETS = Path(__file__).resolve().parents[2] / 'src/main/resources/assets/gregecore'
TEXTURES = ASSETS / 'textures/block/casings/world_eater'


class CasingAssetsTest(unittest.TestCase):
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
