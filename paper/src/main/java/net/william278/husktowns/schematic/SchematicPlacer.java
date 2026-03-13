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
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.user.BukkitUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles placement of schematics with claim validation
 */
public class SchematicPlacer {

    private final BukkitHuskTowns plugin;

    public SchematicPlacer(@NotNull BukkitHuskTowns plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if a schematic can be placed at the given location
     *
     * @param player    The player attempting to place
     * @param schematic The schematic to place
     * @param origin    The origin location
     * @return true if the schematic can be placed
     */
    public boolean canPlaceSchematic(@NotNull Player player, @NotNull Schematic schematic,
                                      @NotNull Location origin) {
        BukkitUser user = BukkitUser.adapt(player, plugin);

        // Get all chunks the schematic would occupy
        Set<Chunk> affectedChunks = getAffectedChunks(origin, schematic);

        // Check if player's town owns all affected chunks
        return plugin.getUserTown(user).map(member -> {
            World world = World.of(origin.getWorld().getUID(), origin.getWorld().getName(),
                    origin.getWorld().getEnvironment().name());

            for (Chunk chunk : affectedChunks) {
                // Check if this chunk is claimed by the player's town
                boolean isClaimed = plugin.getClaimWorld(world)
                        .flatMap(claimWorld -> claimWorld.getClaimAt(chunk, plugin))
                        .map(claim -> claim.town().equals(member.town()))
                        .orElse(false);

                if (!isClaimed) {
                    return false;
                }
            }
            return true;
        }).orElse(false);
    }

    /**
     * Places a schematic at the given location
     *
     * @param player    The player placing the schematic
     * @param schematic The schematic to place
     * @param origin    The origin location
     */
    public void placeSchematic(@NotNull Player player, @NotNull Schematic schematic,
                                @NotNull Location origin) {
        if (!canPlaceSchematic(player, schematic, origin)) {
            player.sendMessage(ChatColor.RED + "You can only place the Jarton Hall in your claimed land!");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Placing Jarton Hall...");

        // Place blocks asynchronously to avoid lag
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int blocksPlaced = 0;

            for (int y = 0; y < schematic.getHeight(); y++) {
                for (int z = 0; z < schematic.getLength(); z++) {
                    for (int x = 0; x < schematic.getWidth(); x++) {
                        Material material = schematic.getBlockAt(x, y, z);

                        if (material != Material.AIR) {
                            Location blockLoc = schematic.getWorldLocation(origin, x, y, z);

                            // Place block on main thread
                            final int finalX = x;
                            final int finalY = y;
                            final int finalZ = z;
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                Block block = blockLoc.getBlock();
                                block.setType(material);
                            });

                            blocksPlaced++;
                        }
                    }
                }
            }

            final int totalBlocks = blocksPlaced;
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(ChatColor.GREEN + "Jarton Hall placed successfully! (" + totalBlocks + " blocks)");
            });
        });
    }

    /**
     * Gets all chunks that a schematic would affect
     *
     * @param origin    The origin location
     * @param schematic The schematic
     * @return Set of affected chunks
     */
    @NotNull
    private Set<Chunk> getAffectedChunks(@NotNull Location origin, @NotNull Schematic schematic) {
        Set<Chunk> chunks = new HashSet<>();

        int minX = origin.getBlockX();
        int maxX = origin.getBlockX() + schematic.getWidth();
        int minZ = origin.getBlockZ();
        int maxZ = origin.getBlockZ() + schematic.getLength();

        // Calculate chunk coordinates
        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                chunks.add(Chunk.at(chunkX, chunkZ));
            }
        }

        return chunks;
    }

    /**
     * Gets the affected chunks for visualization updates
     */
    @NotNull
    public Set<Chunk> getAffectedChunksForLocation(@NotNull Location origin, @NotNull Schematic schematic) {
        return getAffectedChunks(origin, schematic);
    }
}
