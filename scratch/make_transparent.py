from PIL import Image

def make_background_transparent(image_path, output_path):
    img = Image.open(image_path).convert("RGBA")
    data = img.load()
    width, height = img.size
    
    # We want to do a flood fill starting from all pixels on the borders that are white
    visited = set()
    queue = []
    
    # Add all border pixels that are close to white
    for x in range(width):
        for y in (0, height - 1):
            r, g, b, a = data[x, y]
            if r > 240 and g > 240 and b > 240:
                queue.append((x, y))
                visited.add((x, y))
    for y in range(height):
        for x in (0, width - 1):
            r, g, b, a = data[x, y]
            if r > 240 and g > 240 and b > 240:
                if (x, y) not in visited:
                    queue.append((x, y))
                    visited.add((x, y))
                    
    # BFS to find all connected white pixels
    while queue:
        cx, cy = queue.pop(0)
        # Set alpha to 0 for the background pixel
        r, g, b, a = data[cx, cy]
        data[cx, cy] = (r, g, b, 0)
        
        for dx, dy in [(-1,0), (1,0), (0,-1), (0,1)]:
            nx, ny = cx + dx, cy + dy
            if 0 <= nx < width and 0 <= ny < height:
                if (nx, ny) not in visited:
                    nr, ng, nb, na = data[nx, ny]
                    # If it's white/light grey, it's part of the background
                    if nr > 240 and ng > 240 and nb > 240:
                        visited.add((nx, ny))
                        queue.append((nx, ny))
                        
    img.save(output_path, "PNG")
    print(f"Processed {image_path} -> {output_path}")

make_background_transparent("image/login/mima.png", "image/login/mima.png")
make_background_transparent("image/UI2/mima.png", "image/UI2/mima.png")
