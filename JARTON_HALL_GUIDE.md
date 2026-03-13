# Jarton Hall Town Creation System

This guide explains the new GUI-based town creation system with schematic placement functionality.

## ✨ Platform Support

- ✅ **Paper** (Recommended - fully supported)
- ✅ **Bukkit** (fully supported)
- ✅ **Spigot** (fully supported via Bukkit)

All features work identically across all platforms!

## Overview

The town creation system has been enhanced with:
- Interactive GUI for town creation
- Jarton Hall schematic placement with 10x10 building
- Real-time placement visualizer
- Claim validation (only place in claimed land)
- Item-based placement system

## How It Works

### 1. Starting Town Creation

Run the command:
```
/town create
```

This will open a GUI with a player head in the center that says **"Click Here to Start Your Town!"**

### 2. Town Options GUI

After clicking the player head, you'll see two options:

1. **Click Here to Name Your Town!** (Book icon)
   - Click this to name your town via chat
   - Type your town name in chat when prompted

2. **Click Here to Place Your Jarton Hall!** (Brick/Gray pane icon)
   - **LOCKED** until you claim at least one chunk
   - Once unlocked, click to receive the "Jarton Hall Placer" item

### 3. Claiming Land

Before you can place the Jarton Hall, you must claim at least one chunk:
```
/town claim
```

Once you have at least one claimed chunk, the Jarton Hall option will unlock.

### 4. Placing the Jarton Hall

When you receive the **Jarton Hall Placer** item:

1. **Visualizer**: While holding the item, you'll see a particle outline showing where the building will be placed
   - **GREEN particles** = Valid placement location (inside your claims)
   - **RED particles** = Invalid placement location (outside your claims)

2. **Movement**: The visualizer follows your view as you look around and move

3. **Placement**: Right-click to place the Jarton Hall
   - The schematic must be entirely within your claimed land
   - The item will be consumed upon successful placement

## Setting Up the Schematic

### Creating the Schematic File

1. Build your Jarton Hall structure in-game (10x10 recommended size)

2. Use WorldEdit or a similar plugin to save it as a schematic:
   ```
   //copy
   //schem save jartonhall
   ```

3. Place the `jartonhall.schem` file in:
   ```
   plugins/HuskTowns/schematics/jartonhall.schem
   ```

### Schematic Requirements

- **Format**: Sponge Schematic Format (.schem)
- **Name**: Must be named exactly `jartonhall.schem`
- **Size**: Recommended 10x10, but can be any size
- **Location**: `plugins/HuskTowns/schematics/` folder

### Example Schematic Creation with WorldEdit

```
1. Build your Jarton Hall structure
2. Stand at one corner and run: //pos1
3. Stand at the opposite corner and run: //pos2
4. Run: //copy
5. Run: //schem save jartonhall
6. Move the file from WorldEdit's schematics folder to HuskTowns/schematics/
```

## Technical Details

### Files Created

- `TownCreationGUI.java` - Handles the inventory GUI
- `SchematicLoader.java` - Loads .schem files
- `Schematic.java` - Represents a loaded schematic
- `SchematicVisualizer.java` - Particle visualization system
- `SchematicPlacer.java` - Places schematics with claim validation
- `TownCreationListener.java` - Event handlers for GUI and placement
- `org.jnbt.*` - NBT parsing library for .schem files

### Claim Validation

The system ensures:
- All chunks the schematic overlaps must be claimed by the player's town
- If any part extends into unclaimed land, placement is denied
- Visual feedback via particle color (green = valid, red = invalid)

### Permissions

Uses existing HuskTowns permissions:
- `husktowns.command.town.create` - Create towns and access GUI

## Troubleshooting

**"Schematic not found" error:**
- Ensure `jartonhall.schem` is in `plugins/HuskTowns/schematics/`
- Check file name is exactly `jartonhall.schem` (case-sensitive on Linux)
- Verify the file is in Sponge Schematic Format (.schem), not legacy .schematic

**Jarton Hall button is locked:**
- You must claim at least one chunk first: `/town claim`
- You must be in a town (or creating one)

**Can't place Jarton Hall:**
- The entire schematic must fit within your claimed chunks
- Make sure you're looking at claimed land
- Check the particle color - green means valid, red means invalid

**Particles not showing:**
- Make sure you're holding the "Jarton Hall Placer" item
- Check your particle settings in Minecraft
- Try moving or looking around to update the visualizer

## Customization

To change the schematic:
1. Create a new .schem file with your desired structure
2. Replace `plugins/HuskTowns/schematics/jartonhall.schem`
3. Reload/restart the server (or use `/town admin reload` if available)

The schematic can be any size, but ensure players have enough claimed chunks to place it!

## Commands Summary

| Command | Description |
|---------|-------------|
| `/town create` | Opens the town creation GUI |
| `/town create <name>` | Creates a town directly (traditional method) |
| `/town claim` | Claims the chunk you're standing in |

## Future Enhancements

Potential future additions:
- Multiple building types beyond Jarton Hall
- Building upgrades
- Custom schematic configuration
- Different schematics per town level
- Building management GUI

---

*For issues or questions, please refer to the main HuskTowns documentation or open an issue on GitHub.*
