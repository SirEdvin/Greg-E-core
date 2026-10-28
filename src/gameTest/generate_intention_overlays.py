"""Generate the two static, transparent intention hatch front overlays."""
from pathlib import Path
from PIL import Image, ImageDraw

OUTPUT = Path(__file__).resolve().parents[1] / 'main/resources/assets/gregecore/textures/block/overlay/intention'


def generate():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    for name in ('safety', 'bravery'):
        image = Image.new('RGBA', (16, 16))
        draw = ImageDraw.Draw(image)
        if name == 'safety':
            draw.polygon([(3, 3), (8, 2), (12, 3), (12, 8), (10, 11), (8, 13), (5, 11), (3, 8)], fill='#152a24')
            draw.polygon([(4, 4), (8, 3), (11, 4), (11, 8), (8, 11), (4, 8)], fill='#53c797')
            draw.line([(5, 7), (7, 9), (10, 5)], fill='#eefaf1', width=1)
        else:
            draw.polygon([(8, 2), (11, 6), (9, 6), (13, 11), (10, 14), (5, 14), (2, 10), (6, 5), (5, 8)], fill='#5a281c')
            draw.polygon([(8, 3), (10, 6), (8, 6), (12, 11), (9, 13), (5, 13), (3, 10), (6, 7), (6, 10)], fill='#f18439')
            draw.polygon([(8, 8), (10, 11), (8, 13), (6, 11)], fill='#ffe3a0')
        corner = image.getpixel((0, 0))
        assert image.getbbox() and isinstance(corner, tuple) and corner[3] == 0
        image.save(OUTPUT / f'{name}.png')


if __name__ == '__main__':
    generate()
