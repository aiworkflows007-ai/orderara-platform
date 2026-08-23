import os
import math
from PIL import Image, ImageDraw, ImageFilter

DENSITIES = [
    (48, "mipmap-mdpi"),
    (72, "mipmap-hdpi"),
    (96, "mipmap-xhdpi"),
    (144, "mipmap-xxhdpi"),
    (192, "mipmap-xxxhdpi"),
    (512, "playstore")
]

def draw_restaurant_building_icon(is_partner=False, size=512):
    """Draws a 3D isometric/modern restaurant building storefront."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    padding = size * 0.04
    radius = size * 0.26
    
    # 1. Background Gradient Container
    if not is_partner:
        # Customer: Vibrant Sunset Flame Orange to Crimson Ember
        top_color = (255, 90, 10, 255)
        bottom_color = (215, 20, 10, 255)
        border_color = (255, 255, 255, 90)
    else:
        # Partner: Dark Merchant Obsidian & Slate with Emerald/Gold trim
        top_color = (28, 38, 54, 255)
        bottom_color = (12, 18, 28, 255)
        border_color = (255, 120, 30, 200)

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
    
    # Border Rim
    draw.rounded_rectangle(
        [padding + 2, padding + 2, size - padding - 2, size - padding - 2],
        radius=radius - 2,
        outline=border_color,
        width=int(size * 0.015)
    )

    cx, cy = size / 2, size / 2

    # --- RESTAURANT BUILDING DRAWING ---
    b_w = size * 0.62   # Building width
    b_h = size * 0.44   # Building height
    b_left = cx - b_w / 2
    b_right = cx + b_w / 2
    b_bottom = cy + size * 0.28
    b_top = b_bottom - b_h

    # 2. Ground Foundation Steps
    step_h = size * 0.035
    draw.rounded_rectangle(
        [cx - size*0.36, b_bottom, cx + size*0.36, b_bottom + step_h],
        radius=step_h/2,
        fill=(220, 225, 235, 255) if not is_partner else (51, 65, 85, 255)
    )
    draw.rounded_rectangle(
        [cx - size*0.33, b_bottom + step_h, cx + size*0.33, b_bottom + step_h*1.6],
        radius=step_h/2,
        fill=(180, 190, 205, 255) if not is_partner else (30, 41, 59, 255)
    )

    # 3. Main Building Walls (Warm Stone White / Slate)
    wall_color = (255, 255, 255, 255) if not is_partner else (241, 245, 249, 255)
    draw.rounded_rectangle(
        [b_left, b_top, b_right, b_bottom],
        radius=size * 0.02,
        fill=wall_color
    )

    # 4. Rooftop Signboard / Parapet
    roof_h = size * 0.09
    roof_w = b_w + size * 0.04
    roof_color = (245, 130, 32, 255) if not is_partner else (16, 185, 129, 255) # Orange for Customer, Emerald for Partner
    draw.rounded_rectangle(
        [cx - roof_w/2, b_top - roof_h*0.7, cx + roof_w/2, b_top + size*0.01],
        radius=size * 0.02,
        fill=roof_color
    )

    # Restaurant Emblem on Rooftop (Mini Fork & Knife or Chef Hat)
    emblem_w = size * 0.22
    emblem_h = size * 0.045
    draw.rounded_rectangle(
        [cx - emblem_w/2, b_top - roof_h*0.5, cx + emblem_w/2, b_top - roof_h*0.5 + emblem_h],
        radius=emblem_h/2,
        fill=(255, 255, 255, 255)
    )
    # Text placeholder / golden star
    draw.ellipse([cx - size*0.018, b_top - roof_h*0.48, cx + size*0.018, b_top - roof_h*0.48 + size*0.036], fill=(245, 158, 11, 255))

    # 5. Striped Awning (Canopy) with Scalloped Edge
    awning_top = b_top + size * 0.03
    awning_h = size * 0.13
    awning_bottom = awning_top + awning_h
    awning_w = b_w + size * 0.03
    awning_left = cx - awning_w / 2
    awning_right = cx + awning_w / 2

    # Draw 6 striped awning segments (Alternating Orange/Ruby or Red/White)
    num_stripes = 6
    stripe_w = awning_w / num_stripes
    stripe_colors = [
        (239, 68, 68, 255) if not is_partner else (249, 115, 22, 255),  # Crimson / Orange
        (255, 255, 255, 255)                                            # White
    ]

    for i in range(num_stripes):
        sx1 = awning_left + i * stripe_w
        sx2 = sx1 + stripe_w
        col = stripe_colors[i % 2]
        
        # Awning trapezoid
        draw.polygon(
            [
                (sx1 + size*0.01, awning_top),
                (sx2 - size*0.01, awning_top),
                (sx2, awning_bottom),
                (sx1, awning_bottom)
            ],
            fill=col
        )
        # Scallop circle at bottom of stripe
        draw.pieslice(
            [sx1, awning_bottom - stripe_w*0.4, sx2, awning_bottom + stripe_w*0.4],
            start=0,
            end=180,
            fill=col
        )

    # 6. Restaurant Glass Windows with Warm Glow (Left & Right)
    win_w = size * 0.13
    win_h = size * 0.15
    win_y = awning_bottom + size * 0.04
    win_color = (254, 240, 138, 255) # Glowing warm yellow light
    frame_color = (51, 65, 85, 255)  # Dark frame

    # Left Window
    lx = b_left + size * 0.035
    draw.rounded_rectangle([lx, win_y, lx + win_w, win_y + win_h], radius=size*0.015, fill=win_color, outline=frame_color, width=int(size*0.01))
    # Window panes (cross)
    draw.line([(lx, win_y + win_h/2), (lx + win_w, win_y + win_h/2)], fill=frame_color, width=int(size*0.008))
    draw.line([(lx + win_w/2, win_y), (lx + win_w/2, win_y + win_h)], fill=frame_color, width=int(size*0.008))

    # Right Window
    rx = b_right - size * 0.035 - win_w
    draw.rounded_rectangle([rx, win_y, rx + win_w, win_y + win_h], radius=size*0.015, fill=win_color, outline=frame_color, width=int(size*0.01))
    draw.line([(rx, win_y + win_h/2), (rx + win_w, win_y + win_h/2)], fill=frame_color, width=int(size*0.008))
    draw.line([(rx + win_w/2, win_y), (rx + win_w/2, win_y + win_h)], fill=frame_color, width=int(size*0.008))

    # 7. Planter Boxes below windows with green plants
    plant_h = size * 0.025
    draw.rounded_rectangle([lx - size*0.01, win_y + win_h, lx + win_w + size*0.01, win_y + win_h + plant_h], radius=plant_h/2, fill=(16, 185, 129, 255))
    draw.rounded_rectangle([rx - size*0.01, win_y + win_h, rx + win_w + size*0.01, win_y + win_h + plant_h], radius=plant_h/2, fill=(16, 185, 129, 255))

    # 8. Glass Entrance Door in Center
    door_w = size * 0.18
    door_h = size * 0.21
    door_x = cx - door_w / 2
    door_y = b_bottom - door_h
    door_color = (219, 234, 254, 255) # Glass blue

    draw.rounded_rectangle(
        [door_x, door_y, door_x + door_w, b_bottom],
        radius=size * 0.015,
        fill=door_color,
        outline=frame_color,
        width=int(size * 0.012)
    )
    # Center Door Divider
    draw.line([(cx, door_y), (cx, b_bottom)], fill=frame_color, width=int(size*0.008))
    # Golden Handles
    handle_y = door_y + door_h * 0.48
    draw.ellipse([cx - size*0.02, handle_y, cx - size*0.008, handle_y + size*0.03], fill=(245, 158, 11, 255))
    draw.ellipse([cx + size*0.008, handle_y, cx + size*0.02, handle_y + size*0.03], fill=(245, 158, 11, 255))

    return img


def generate_all_restaurant_building_icons():
    # 1. Customer App (Restaurant Building Icon)
    customer_res_dir = "/home/ashok/Projects/restaurant/customer-app/app/src/main/res"
    customer_512 = draw_restaurant_building_icon(is_partner=False, size=512)
    
    for size, folder in DENSITIES:
        if folder == "playstore":
            target_file = "/home/ashok/Projects/restaurant/customer-app/playstore_icon.png"
            customer_512.save(target_file, "PNG")
            print(f"Saved Customer Play Store icon: {target_file}")
            continue
            
        target_dir = os.path.join(customer_res_dir, folder)
        os.makedirs(target_dir, exist_ok=True)
        scaled_img = customer_512.resize((size, size), Image.Resampling.LANCZOS)
        
        for name in ["ic_launcher.png", "ic_launcher_round.png"]:
            path = os.path.join(target_dir, name)
            scaled_img.save(path, "PNG")
        print(f"Saved Customer building icon ({size}x{size}) in {folder}")

    # 2. Partner App (Restaurant Building Icon)
    partner_res_dir = "/home/ashok/Projects/restaurant/partner-app/app/src/main/res"
    partner_512 = draw_restaurant_building_icon(is_partner=True, size=512)
    
    for size, folder in DENSITIES:
        if folder == "playstore":
            target_file = "/home/ashok/Projects/restaurant/partner-app/playstore_icon.png"
            partner_512.save(target_file, "PNG")
            print(f"Saved Partner Play Store icon: {target_file}")
            continue
            
        target_dir = os.path.join(partner_res_dir, folder)
        os.makedirs(target_dir, exist_ok=True)
        scaled_img = partner_512.resize((size, size), Image.Resampling.LANCZOS)
        
        for name in ["ic_launcher.png", "ic_launcher_round.png"]:
            path = os.path.join(target_dir, name)
            scaled_img.save(path, "PNG")
        print(f"Saved Partner building icon ({size}x{size}) in {folder}")

if __name__ == "__main__":
    generate_all_restaurant_building_icons()
