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

import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Represents a loaded schematic structure
 */
public class Schematic {

    private final short width;
    private final short height;
    private final short length;
    private final Map<Integer, Material> palette;
    private final byte[] blockData;

    public Schematic(short width, short height, short length,
                     @NotNull Map<Integer, Material> palette,
                     @NotNull byte[] blockData) {
        this.width = width;
        this.height = height;
        this.length = length;
        this.palette = palette;
        this.blockData = blockData;
    }

    /**
     * Gets the material at a specific position within the schematic
     *
     * @param x X position
     * @param y Y position
     * @param z Z position
     * @return The material at that position
     */
    @NotNull
    public Material getBlockAt(int x, int y, int z) {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= length) {
            return Material.AIR;
        }

        int index = (y * width * length) + (z * width) + x;
        if (index >= blockData.length) {
            return Material.AIR;
        }

        int paletteIndex = blockData[index] & 0xFF;
        return palette.getOrDefault(paletteIndex, Material.AIR);
    }

    /**
     * Gets the world location for a specific position in the schematic
     *
     * @param origin The origin location where schematic is being placed
     * @param x      X offset within schematic
     * @param y      Y offset within schematic
     * @param z      Z offset within schematic
     * @return The world location
     */
    @NotNull
    public Location getWorldLocation(@NotNull Location origin, int x, int y, int z) {
        return origin.clone().add(x, y, z);
    }

    public short getWidth() {
        return width;
    }

    public short getHeight() {
        return height;
    }

    public short getLength() {
        return length;
    }

    public Map<Integer, Material> getPalette() {
        return palette;
    }

    public byte[] getBlockData() {
        return blockData;
    }
}
