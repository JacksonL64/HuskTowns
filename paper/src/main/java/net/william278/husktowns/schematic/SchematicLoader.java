/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husktowns.schematic;

import net.william278.husktowns.BukkitHuskTowns;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

import org.jnbt.*;

/**
 * Loads .schem files (Sponge Schematic Format)
 */
public class SchematicLoader {

    private final BukkitHuskTowns plugin;
    private final File schematicsFolder;
    private final Map<String, Schematic> cachedSchematics = new HashMap<>();

    public SchematicLoader(@NotNull BukkitHuskTowns plugin) {
        this.plugin = plugin;
        this.schematicsFolder = new File(plugin.getDataFolder(), "schematics");

        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }
    }

    /**
     * Loads a schematic from file
     *
     * @param name The schematic name (without .schem extension)
     * @return The loaded schematic, or null if failed
     */
    @Nullable
    public Schematic loadSchematic(@NotNull String name) {
        // Check cache first
        if (cachedSchematics.containsKey(name)) {
            return cachedSchematics.get(name);
        }

        File file = new File(schematicsFolder, name + ".schem");
        if (!file.exists()) {
            plugin.getLogger().warning("Schematic file not found: " + file.getAbsolutePath());
            return null;
        }

        try (FileInputStream fis = new FileInputStream(file);
             GZIPInputStream gis = new GZIPInputStream(fis)) {

            NBTInputStream nbtStream = new NBTInputStream(gis);
            CompoundTag schematicTag = (CompoundTag) nbtStream.readTag();
            nbtStream.close();

            Schematic schematic = parseSchematic(schematicTag);
            if (schematic != null) {
                cachedSchematics.put(name, schematic);
            }

            return schematic;

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load schematic: " + name, e);
            return null;
        }
    }

    /**
     * Parses NBT data into a Schematic object
     */
    @Nullable
    private Schematic parseSchematic(@NotNull CompoundTag tag) {
        try {
            Map<String, Tag> values = tag.getValue();

            // Get dimensions
            short width = getShort(values, "Width");
            short height = getShort(values, "Height");
            short length = getShort(values, "Length");

            // Get palette
            CompoundTag paletteTag = (CompoundTag) values.get("Palette");
            Map<Integer, Material> palette = parsePalette(paletteTag);

            // Get block data
            byte[] blockData = getByteArray(values, "BlockData");

            return new Schematic(width, height, length, palette, blockData);

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to parse schematic data", e);
            return null;
        }
    }

    /**
     * Parses the block palette from NBT
     */
    @NotNull
    private Map<Integer, Material> parsePalette(@NotNull CompoundTag paletteTag) {
        Map<Integer, Material> palette = new HashMap<>();
        Map<String, Tag> paletteMap = paletteTag.getValue();

        for (Map.Entry<String, Tag> entry : paletteMap.entrySet()) {
            String blockId = entry.getKey();
            int index = ((IntTag) entry.getValue()).getValue();

            // Parse block ID (e.g., "minecraft:stone" or "minecraft:oak_planks[axis=y]")
            String materialName = blockId.split("\\[")[0].replace("minecraft:", "").toUpperCase();
            Material material = Material.matchMaterial(materialName);

            if (material != null) {
                palette.put(index, material);
            } else {
                palette.put(index, Material.AIR);
            }
        }

        return palette;
    }

    private short getShort(@NotNull Map<String, Tag> values, @NotNull String key) {
        return ((ShortTag) values.get(key)).getValue();
    }

    private byte[] getByteArray(@NotNull Map<String, Tag> values, @NotNull String key) {
        return ((ByteArrayTag) values.get(key)).getValue();
    }

    /**
     * Gets the schematics folder
     */
    @NotNull
    public File getSchematicsFolder() {
        return schematicsFolder;
    }

    /**
     * Clears the schematic cache
     */
    public void clearCache() {
        cachedSchematics.clear();
    }
}
