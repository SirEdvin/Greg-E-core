"""Generate the static, transparent ME demand hatch front overlay."""
from pathlib import Path
from PIL import Image, ImageDraw

OUTPUT = Path(__file__).resolve().parents[1] / 'main/resources/assets/gregecore/textures/block/overlay/me_demand.png'


def generate():
    image = Image.new('RGBA', (16, 16))
    draw = ImageDraw.Draw(image)
    draw.rectangle((2, 2, 13, 13), fill='#24152f', outline='#9173b0')
    draw.rectangle((4, 4, 11, 6), fill='#58c9a4')
    draw.polygon([(6, 7), (9, 7), (9, 9), (11, 9), (8, 12), (4, 9), (6, 9)], fill='#e3c8ff')
    corner = image.getpixel((0, 0))
    assert image.getbbox() == (2, 2, 14, 14) and isinstance(corner, tuple) and corner[3] == 0
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    image.save(OUTPUT)


if __name__ == '__main__':
    generate()
