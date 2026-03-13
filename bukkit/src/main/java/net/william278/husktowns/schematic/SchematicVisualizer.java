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
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Visualizes schematic placement boundaries using particles
 */
public class SchematicVisualizer {

    private final BukkitHuskTowns plugin;
    private final Map<UUID, VisualizerData> activeVisualizers = new HashMap<>();

    public SchematicVisualizer(@NotNull BukkitHuskTowns plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts visualizing a schematic for a player
     *
     * @param player    The player
     * @param schematic The schematic to visualize
     * @param location  The origin location
     * @param canPlace  Whether the schematic can be placed here
     */
    public void startVisualization(@NotNull Player player, @NotNull Schematic schematic,
                                    @NotNull Location location, boolean canPlace) {
        // Stop existing visualization
        stopVisualization(player);

        VisualizerData data = new VisualizerData(schematic, location, canPlace);

        // Schedule repeating task to show particles
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || !player.getItemInHand().hasItemMeta() ||
                    !player.getItemInHand().getItemMeta().getDisplayName().contains("Jarton Hall Placer")) {
                stopVisualization(player);
                return;
            }

            showParticles(player, data);
        }, 0L, 5L); // Update every 5 ticks (0.25 seconds)

        data.task = task;
        activeVisualizers.put(player.getUniqueId(), data);
    }

    /**
     * Updates the visualization location
     */
    public void updateLocation(@NotNull Player player, @NotNull Location location, boolean canPlace) {
        VisualizerData data = activeVisualizers.get(player.getUniqueId());
        if (data != null) {
            data.location = location;
            data.canPlace = canPlace;
        }
    }

    /**
     * Stops visualization for a player
     */
    public void stopVisualization(@NotNull Player player) {
        VisualizerData data = activeVisualizers.remove(player.getUniqueId());
        if (data != null && data.task != null) {
            data.task.cancel();
        }
    }

    /**
     * Shows particle outline of the schematic
     */
    private void showParticles(@NotNull Player player, @NotNull VisualizerData data) {
        Location origin = data.location;
        Schematic schematic = data.schematic;

        // Choose color based on whether placement is valid
        Particle.DustOptions dustOptions = new Particle.DustOptions(
                data.canPlace ? Color.GREEN : Color.RED,
                1.0f
        );

        int width = schematic.getWidth();
        int height = schematic.getHeight();
        int length = schematic.getLength();

        // Draw corners and edges
        // Bottom corners
        spawnParticle(player, origin.clone(), dustOptions);
        spawnParticle(player, origin.clone().add(width, 0, 0), dustOptions);
        spawnParticle(player, origin.clone().add(0, 0, length), dustOptions);
        spawnParticle(player, origin.clone().add(width, 0, length), dustOptions);

        // Top corners
        spawnParticle(player, origin.clone().add(0, height, 0), dustOptions);
        spawnParticle(player, origin.clone().add(width, height, 0), dustOptions);
        spawnParticle(player, origin.clone().add(0, height, length), dustOptions);
        spawnParticle(player, origin.clone().add(width, height, length), dustOptions);

        // Draw edges
        drawLine(player, origin.clone(), origin.clone().add(width, 0, 0), dustOptions);
        drawLine(player, origin.clone(), origin.clone().add(0, 0, length), dustOptions);
        drawLine(player, origin.clone().add(width, 0, 0), origin.clone().add(width, 0, length), dustOptions);
        drawLine(player, origin.clone().add(0, 0, length), origin.clone().add(width, 0, length), dustOptions);

        // Vertical edges
        drawLine(player, origin.clone(), origin.clone().add(0, height, 0), dustOptions);
        drawLine(player, origin.clone().add(width, 0, 0), origin.clone().add(width, height, 0), dustOptions);
        drawLine(player, origin.clone().add(0, 0, length), origin.clone().add(0, height, length), dustOptions);
        drawLine(player, origin.clone().add(width, 0, length), origin.clone().add(width, height, length), dustOptions);

        // Top edges
        drawLine(player, origin.clone().add(0, height, 0), origin.clone().add(width, height, 0), dustOptions);
        drawLine(player, origin.clone().add(0, height, 0), origin.clone().add(0, height, length), dustOptions);
        drawLine(player, origin.clone().add(width, height, 0), origin.clone().add(width, height, length), dustOptions);
        drawLine(player, origin.clone().add(0, height, length), origin.clone().add(width, height, length), dustOptions);
    }

    /**
     * Draws a line of particles between two points
     */
    private void drawLine(@NotNull Player player, @NotNull Location start, @NotNull Location end,
                          @NotNull Particle.DustOptions options) {
        double distance = start.distance(end);
        int particles = (int) (distance * 2); // 2 particles per block

        for (int i = 0; i <= particles; i++) {
            double t = (double) i / particles;
            Location point = start.clone().add(
                    (end.getX() - start.getX()) * t,
                    (end.getY() - start.getY()) * t,
                    (end.getZ() - start.getZ()) * t
            );
            spawnParticle(player, point, options);
        }
    }

    /**
     * Spawns a dust particle visible only to the player
     */
    private void spawnParticle(@NotNull Player player, @NotNull Location location,
                                @NotNull Particle.DustOptions options) {
        player.spawnParticle(Particle.REDSTONE, location, 1, 0, 0, 0, 0, options);
    }

    /**
     * Checks if a player has an active visualization
     */
    public boolean hasActiveVisualization(@NotNull Player player) {
        return activeVisualizers.containsKey(player.getUniqueId());
    }

    /**
     * Data class for storing visualization information
     */
    private static class VisualizerData {
        Schematic schematic;
        Location location;
        boolean canPlace;
        BukkitTask task;

        VisualizerData(Schematic schematic, Location location, boolean canPlace) {
            this.schematic = schematic;
            this.location = location;
            this.canPlace = canPlace;
        }
    }
}
