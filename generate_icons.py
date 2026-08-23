import os
import math
from PIL import Image, ImageDraw, ImageFilter, ImageFont

# Density dimensions (size, folder_name)
DENSITIES = [
    (48, "mipmap-mdpi"),
    (72, "mipmap-hdpi"),
    (96, "mipmap-xhdpi"),
    (144, "mipmap-xxhdpi"),
    (192, "mipmap-xxxhdpi"),
    (512, "playstore")
]

def draw_customer_icon(size=512):
    """Draws the ultra-attractive Customer App icon (Flame Orange / Cloche / Fork & Spoon)."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # 1. Base Rounded Square / Circle with Gradient
    padding = size * 0.04
    radius = size * 0.28
    
    # Radial/Linear multi-stop gradient (Flame Orange to Crimson Ember)
    top_color = (255, 107, 0, 255)      # Bright Flame Orange #FF6B00
    bottom_color = (230, 30, 10, 255)   # Deep Ruby Ember #E61E0A
    
    mask = Image.new("L", (size, size), 0)
    mask_draw = ImageDraw.Draw(mask)
    mask_draw.rounded_rectangle(
        [padding, padding, size - padding, size - padding],
        radius=radius,
        fill=255
    )
    
    # Draw vertical gradient on a base layer
    gradient = Image.new("RGBA", (size, size))
    for y in range(size):
        t = y / size
        r = int(top_color[0] * (1 - t) + bottom_color[0] * t)
        g = int(top_color[1] * (1 - t) + bottom_color[1] * t)
        b = int(top_color[2] * (1 - t) + bottom_color[2] * t)
        for x in range(size):
            gradient.putpixel((x, y), (r, g, b, 255))
            
    img.paste(gradient, (0, 0), mask)
    
    # 2. Inner Glow & Ambient Rim
    glow_draw = ImageDraw.Draw(img)
    glow_draw.rounded_rectangle(
        [padding + 2, padding + 2, size - padding - 2, size - padding - 2],
        radius=radius - 2,
        outline=(255, 255, 255, 70),
        width=int(size * 0.015)
    )
    
    # 3. Soft Background Pattern / Ambient Plate Ring
    cx, cy = size / 2, size / 2
    plate_r = size * 0.32
    glow_draw.ellipse(
        [cx - plate_r, cy - plate_r + size*0.04, cx + plate_r, cy + plate_r + size*0.04],
        fill=(0, 0, 0, 35)
    )
    glow_draw.ellipse(
        [cx - plate_r, cy - plate_r, cx + plate_r, cy + plate_r],
        outline=(255, 255, 255, 90),
        width=int(size * 0.02)
    )
    
    # 4. Icon Foreground Motif: Cloche (Food Cover) & Steam / Sparkles
    # Cloche Dome
    dome_w = size * 0.44
    dome_h = size * 0.26
    dome_top = cy - dome_h * 0.6
    dome_bottom = cy + size * 0.10
    
    # Cloche base plate
    base_h = size * 0.035
    base_w = size * 0.48
    glow_draw.rounded_rectangle(
        [cx - base_w/2, dome_bottom, cx + base_w/2, dome_bottom + base_h],
        radius=base_h/2,
        fill=(255, 255, 255, 255)
    )
    
    # Cloche Semi-Circle Dome
    glow_draw.pieslice(
        [cx - dome_w/2, dome_top - dome_h*0.3, cx + dome_w/2, dome_bottom + dome_h*0.7],
        start=180,
        end=360,
        fill=(255, 255, 255, 255)
    )
    
    # Cloche Handle Knob on Top
    knob_r = size * 0.042
    glow_draw.ellipse(
        [cx - knob_r, dome_top - dome_h*0.3 - knob_r*1.2, cx + knob_r, dome_top - dome_h*0.3 + knob_r*0.8],
        fill=(255, 230, 180, 255)
    )
    
    # Steam Swirls / Aroma Vapor
    for offset_x in [-size*0.08, 0, size*0.08]:
        sx = cx + offset_x
        sy = dome_top - size * 0.12
        glow_draw.arc(
            [sx - size*0.03, sy - size*0.05, sx + size*0.03, sy + size*0.05],
            start=45,
            end=225,
            fill=(255, 255, 255, 200),
            width=int(size * 0.015)
        )
        
    return img


def draw_partner_icon(size=512):
    """Draws the ultra-attractive Partner App icon (Obsidian Slate / Chef Hat & POS Store)."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # 1. Base Rounded Square / Circle with Dark Obsidian / Merchant Navy Gradient
    padding = size * 0.04
    radius = size * 0.28
    
    top_color = (30, 41, 59, 255)      # Slate 800 #1E293B
    bottom_color = (15, 23, 42, 255)   # Slate 900 #0F172A
    
    mask = Image.new("L", (size, size), 0)
    mask_draw = ImageDraw.Draw(mask)
    mask_draw.rounded_rectangle(
        [padding, padding, size - padding, size - padding],
        radius=radius,
        fill=255
    )
    
    gradient = Image.new("RGBA", (size, size))
    for y in range(size):
        t = y / size
        r = int(top_color[0] * (1 - t) + bottom_color[0] * t)
        g = int(top_color[1] * (1 - t) + bottom_color[1] * t)
        b = int(top_color[2] * (1 - t) + bottom_color[2] * t)
        for x in range(size):
            gradient.putpixel((x, y), (r, g, b, 255))
            
    img.paste(gradient, (0, 0), mask)
    
    # 2. Golden / Flame Orange Neon Border Accent
    glow_draw = ImageDraw.Draw(img)
    glow_draw.rounded_rectangle(
        [padding + 2, padding + 2, size - padding - 2, size - padding - 2],
        radius=radius - 2,
        outline=(255, 107, 0, 180),
        width=int(size * 0.018)
    )
    
    cx, cy = size / 2, size / 2
    
    # 3. Chef Toque Hat & Order Ticket Badge
    # Chef Hat Puffs
    hat_y = cy - size * 0.05
    puff_r = size * 0.12
    # Left puff
    glow_draw.ellipse([cx - puff_r*1.6, hat_y - puff_r*1.1, cx - puff_r*0.2, hat_y + puff_r*0.7], fill=(255, 255, 255, 240))
    # Right puff
    glow_draw.ellipse([cx + puff_r*0.2, hat_y - puff_r*1.1, cx + puff_r*1.6, hat_y + puff_r*0.7], fill=(255, 255, 255, 240))
    # Middle puff (taller)
    glow_draw.ellipse([cx - puff_r*0.9, hat_y - puff_r*1.5, cx + puff_r*0.9, hat_y + puff_r*0.5], fill=(255, 255, 255, 255))
    
    # Chef Hat Band
    band_w = size * 0.36
    band_h = size * 0.10
    glow_draw.rounded_rectangle(
        [cx - band_w/2, hat_y + size*0.02, cx + band_w/2, hat_y + size*0.02 + band_h],
        radius=band_h*0.3,
        fill=(255, 107, 0, 255) # Orange Band
    )
    
    # Merchant Star / Fork Icon on Band
    star_r = size * 0.025
    glow_draw.ellipse(
        [cx - star_r, hat_y + size*0.04, cx + star_r, hat_y + size*0.04 + star_r*2],
        fill=(255, 255, 255, 255)
    )
    
    # 4. KDS Order Ticket Base Pedestal
    pedestal_w = size * 0.44
    pedestal_h = size * 0.04
    glow_draw.rounded_rectangle(
        [cx - pedestal_w/2, cy + size*0.18, cx + pedestal_w/2, cy + size*0.18 + pedestal_h],
        radius=pedestal_h/2,
        fill=(16, 185, 129, 255) # Emerald Green base
    )
    
    return img


def generate_all_app_icons():
    # 1. Customer App Icon Generation
    customer_res_dir = "/home/ashok/Projects/restaurant/customer-app/app/src/main/res"
    customer_512 = draw_customer_icon(512)
    
    for size, folder in DENSITIES:
        target_dir = os.path.join(customer_res_dir, folder)
        if folder == "playstore":
            target_dir = "/home/ashok/Projects/restaurant/customer-app"
            target_file = os.path.join(target_dir, "playstore_icon.png")
            customer_512.save(target_file, "PNG")
            print(f"Saved Customer Play Store icon: {target_file}")
            continue
            
        os.makedirs(target_dir, exist_ok=True)
        scaled_img = customer_512.resize((size, size), Image.Resampling.LANCZOS)
        
        # Save standard and round launcher icons
        for name in ["ic_launcher.png", "ic_launcher_round.png"]:
            path = os.path.join(target_dir, name)
            scaled_img.save(path, "PNG")
        print(f"Saved Customer icons ({size}x{size}) in {folder}")

    # 2. Partner App Icon Generation
    partner_res_dir = "/home/ashok/Projects/restaurant/partner-app/app/src/main/res"
    partner_512 = draw_partner_icon(512)
    
    for size, folder in DENSITIES:
        target_dir = os.path.join(partner_res_dir, folder)
        if folder == "playstore":
            target_dir = "/home/ashok/Projects/restaurant/partner-app"
            target_file = os.path.join(target_dir, "playstore_icon.png")
            partner_512.save(target_file, "PNG")
            print(f"Saved Partner Play Store icon: {target_file}")
            continue
            
        os.makedirs(target_dir, exist_ok=True)
        scaled_img = partner_512.resize((size, size), Image.Resampling.LANCZOS)
        
        # Save standard and round launcher icons
        for name in ["ic_launcher.png", "ic_launcher_round.png"]:
            path = os.path.join(target_dir, name)
            scaled_img.save(path, "PNG")
        print(f"Saved Partner icons ({size}x{size}) in {folder}")

if __name__ == "__main__":
    generate_all_app_icons()
