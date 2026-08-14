"""Generates Android launcher icon assets (legacy + adaptive) and the TV banner from the
single 768x768 TokVision logo source. Run once whenever graphics/tokvision-icon-source.png
changes; outputs are committed to the repo like any other resource, no build-time step.
"""
from PIL import Image
import os

ROOT = r"c:\Users\owner\Development\TokVision"
SRC = os.path.join(ROOT, "graphics", "tokvision-icon-source.png")
RES = os.path.join(ROOT, "app", "src", "main", "res")

src = Image.open(SRC).convert("RGBA")

# --- Legacy launcher icon (pre-Android 8 launchers, and the fallback square shown by
# some launchers even on newer Android): the source already reads as a complete icon
# (rounded card + logo), so it's used as-is, just resized per density bucket. ---
legacy_sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}
for folder, size in legacy_sizes.items():
    resized = src.resize((size, size), Image.LANCZOS)
    out_dir = os.path.join(RES, folder)
    os.makedirs(out_dir, exist_ok=True)
    resized.save(os.path.join(out_dir, "ic_launcher.png"))
    resized.save(os.path.join(out_dir, "ic_launcher_round.png"))

# --- Adaptive icon foreground (Android 8+): launchers mask this into a circle/squircle/
# rounded-square and can also crop up to ~33% at the edges for parallax, so real content
# must stay within an inner "safe zone" (~66% of the canvas). The source fills its own
# canvas edge-to-edge, so it's scaled down to ~62% and centered on a transparent canvas
# before masking, instead of being used at full bleed (which would get clipped). ---
adaptive_sizes = {
    "mipmap-mdpi": 108,
    "mipmap-hdpi": 162,
    "mipmap-xhdpi": 216,
    "mipmap-xxhdpi": 324,
    "mipmap-xxxhdpi": 432,
}
SAFE_ZONE_RATIO = 0.62
for folder, canvas_size in adaptive_sizes.items():
    content_size = round(canvas_size * SAFE_ZONE_RATIO)
    resized_logo = src.resize((content_size, content_size), Image.LANCZOS)
    canvas = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    offset = ((canvas_size - content_size) // 2, (canvas_size - content_size) // 2)
    canvas.paste(resized_logo, offset, resized_logo)
    out_dir = os.path.join(RES, folder)
    canvas.save(os.path.join(out_dir, "ic_launcher_foreground.png"))

# --- Android TV launcher banner (recommended 320x180dp, 16:9). The logo is square, so
# it's letterboxed on the sampled dark background color, keeping the whole mark visible
# instead of cropping it into a wide rectangle. ---
BANNER_BG = (10, 10, 14, 255)  # sampled from the logo's own background
banner_w, banner_h = 640, 360  # 320x180dp at xhdpi (2x)
banner = Image.new("RGBA", (banner_w, banner_h), BANNER_BG)
logo_h = banner_h  # fill the full height, center horizontally
logo_resized = src.resize((logo_h, logo_h), Image.LANCZOS)
banner.paste(logo_resized, ((banner_w - logo_h) // 2, 0), logo_resized)
banner_dir = os.path.join(RES, "drawable-xhdpi")
os.makedirs(banner_dir, exist_ok=True)
banner.save(os.path.join(banner_dir, "tv_banner.png"))

print("Done: legacy icons, adaptive foreground, and TV banner generated.")
