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

package net.william278.husktowns.listener;

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.menu.TownCreationGUI;
import net.william278.husktowns.schematic.Schematic;
import net.william278.husktowns.schematic.SchematicLoader;
import net.william278.husktowns.schematic.SchematicPlacer;
import net.william278.husktowns.schematic.SchematicVisualizer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Handles events related to town creation GUI and Jarton Hall placement
 */
public class TownCreationListener implements Listener {

    private final BukkitHuskTowns plugin;
    private final TownCreationGUI gui;
    private final SchematicLoader schematicLoader;
    private final SchematicPlacer schematicPlacer;
    private final SchematicVisualizer visualizer;

    public TownCreationListener(@NotNull BukkitHuskTowns plugin,
                                  @NotNull TownCreationGUI gui,
                                  @NotNull SchematicLoader schematicLoader,
                                  @NotNull SchematicPlacer schematicPlacer,
                                  @NotNull SchematicVisualizer visualizer) {
        this.plugin = plugin;
        this.gui = gui;
        this.schematicLoader = schematicLoader;
        this.schematicPlacer = schematicPlacer;
        this.visualizer = visualizer;
    }

    /**
     * Handles GUI clicks
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();
        if (title.contains("Create Your Town") || title.contains("Town Creation Options")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }

            gui.handleClick(player, event.getSlot());
        }
    }

    /**
     * Handles inventory close - clean up GUI state
     */
    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();
        if (title.contains("Create Your Town") || title.contains("Town Creation Options")) {
            TownCreationGUI.GUIState state = gui.getPlayerState(player.getUniqueId());
            if (state != TownCreationGUI.GUIState.AWAITING_NAME) {
                gui.removePlayerState(player.getUniqueId());
            }
        }
    }

    /**
     * Handles town name input via chat
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(@NotNull AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (gui.getPlayerState(player.getUniqueId()) == TownCreationGUI.GUIState.AWAITING_NAME) {
            event.setCancelled(true);

            String townName = event.getMessage();

            // Run on main thread
            plugin.getServer().getScheduler().runTask(plugin, () ->
                    gui.handleTownNameInput(player, townName)
            );
        }
    }

    /**
     * Handles Jarton Hall Placer item usage
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) {
            return;
        }

        String displayName = item.getItemMeta().getDisplayName();
        if (!displayName.contains("Jarton Hall Placer")) {
            return;
        }

        // Right-click to place
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);

            Location targetLocation = player.getTargetBlock(null, 5).getLocation();
            Schematic schematic = schematicLoader.loadSchematic("jartonhall");

            if (schematic == null) {
                player.sendMessage(ChatColor.RED + "Error: Jarton Hall schematic not found!");
                player.sendMessage(ChatColor.RED + "Please place 'jartonhall.schem' in the schematics folder.");
                return;
            }

            // Check if placement is valid
            boolean canPlace = schematicPlacer.canPlaceSchematic(player, schematic, targetLocation);

            if (canPlace) {
                // Place the schematic
                schematicPlacer.placeSchematic(player, schematic, targetLocation);

                // Remove the placer item
                item.setAmount(item.getAmount() - 1);

                // Stop visualization
                visualizer.stopVisualization(player);
            } else {
                player.sendMessage(ChatColor.RED + "You can only place the Jarton Hall in your claimed land!");
            }
        }
    }

    /**
     * Updates visualization when player moves while holding Jarton Hall Placer
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        if (item == null || !item.hasItemMeta()) {
            if (visualizer.hasActiveVisualization(player)) {
                visualizer.stopVisualization(player);
            }
            return;
        }

        String displayName = item.getItemMeta().getDisplayName();
        if (!displayName.contains("Jarton Hall Placer")) {
            if (visualizer.hasActiveVisualization(player)) {
                visualizer.stopVisualization(player);
            }
            return;
        }

        // Load schematic
        Schematic schematic = schematicLoader.loadSchematic("jartonhall");
        if (schematic == null) {
            return;
        }

        Location targetLocation = player.getTargetBlock(null, 5).getLocation();
        boolean canPlace = schematicPlacer.canPlaceSchematic(player, schematic, targetLocation);

        if (!visualizer.hasActiveVisualization(player)) {
            visualizer.startVisualization(player, schematic, targetLocation, canPlace);
        } else {
            visualizer.updateLocation(player, targetLocation, canPlace);
        }
    }

    /**
     * Cleanup on player quit
     */
    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        gui.removePlayerState(player.getUniqueId());
        visualizer.stopVisualization(player);
    }
}
