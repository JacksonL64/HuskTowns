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

package net.william278.husktowns.menu;

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.user.BukkitUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Interactive GUI for town creation with schematic placement
 */
public class TownCreationGUI {

    private final BukkitHuskTowns plugin;
    private final Map<UUID, GUIState> playerStates = new HashMap<>();

    public TownCreationGUI(@NotNull BukkitHuskTowns plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the initial town creation GUI for a player
     *
     * @param player The player to show the GUI to
     */
    public void openInitialGUI(@NotNull Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Create Your Town");

        // Player head in center (slot 13)
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            skullMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Click Here to Start Your Town!");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Begin your town creation journey");
            skullMeta.setLore(lore);
            playerHead.setItemMeta(skullMeta);
        }

        gui.setItem(13, playerHead);

        player.openInventory(gui);
        playerStates.put(player.getUniqueId(), GUIState.INITIAL);
    }

    /**
     * Opens the main options GUI (name town + place Jarton Hall)
     *
     * @param player The player to show the GUI to
     */
    public void openOptionsGUI(@NotNull Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Town Creation Options");

        // Name town option (slot 11)
        ItemStack nameBook = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta nameMeta = nameBook.getItemMeta();
        if (nameMeta != null) {
            nameMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Click Here to Name Your Town!");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Choose a unique name for your town");
            nameMeta.setLore(lore);
            nameBook.setItemMeta(nameMeta);
        }
        gui.setItem(11, nameBook);

        // Jarton Hall placement option (slot 15)
        boolean hasClaimedChunk = hasPlayerClaimedChunk(player);
        ItemStack jartonHall = new ItemStack(hasClaimedChunk ? Material.BRICK : Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta hallMeta = jartonHall.getItemMeta();
        if (hallMeta != null) {
            hallMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Click Here to Place Your Jarton Hall!");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.RED + "" + ChatColor.ITALIC + "You can only place this in claimed land");

            if (!hasClaimedChunk) {
                lore.add("");
                lore.add(ChatColor.RED + "LOCKED: Claim at least one chunk first!");
                hallMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                hallMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } else {
                lore.add("");
                lore.add(ChatColor.GREEN + "Ready to place!");
            }

            hallMeta.setLore(lore);
            jartonHall.setItemMeta(hallMeta);
        }
        gui.setItem(15, jartonHall);

        player.openInventory(gui);
        playerStates.put(player.getUniqueId(), GUIState.OPTIONS);
    }

    /**
     * Gives the player the Jarton Hall Placer item
     *
     * @param player The player to give the item to
     */
    public void giveJartonHallPlacer(@NotNull Player player) {
        ItemStack placer = new ItemStack(Material.BRICK);
        ItemMeta meta = placer.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Jarton Hall Placer");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Right-click to place your Jarton Hall");
            lore.add(ChatColor.GRAY + "10x10 schematic structure");
            lore.add(ChatColor.RED + "Can only be placed in your claimed land");
            meta.setLore(lore);
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            placer.setItemMeta(meta);
        }

        player.getInventory().addItem(placer);
        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "You received the Jarton Hall Placer! Right-click to place it.");
    }

    /**
     * Handles a click in the GUI
     *
     * @param player The player who clicked
     * @param slot   The slot that was clicked
     */
    public void handleClick(@NotNull Player player, int slot) {
        GUIState state = playerStates.getOrDefault(player.getUniqueId(), GUIState.INITIAL);

        switch (state) {
            case INITIAL:
                if (slot == 13) { // Player head clicked
                    openOptionsGUI(player);
                }
                break;

            case OPTIONS:
                if (slot == 11) { // Name town clicked
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "Please type your town name in chat:");
                    playerStates.put(player.getUniqueId(), GUIState.AWAITING_NAME);
                } else if (slot == 15) { // Jarton Hall clicked
                    if (hasPlayerClaimedChunk(player)) {
                        giveJartonHallPlacer(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "You must claim at least one chunk before placing a Jarton Hall!");
                    }
                }
                break;
        }
    }

    /**
     * Handles chat input for naming the town
     *
     * @param player   The player
     * @param townName The town name they entered
     */
    public void handleTownNameInput(@NotNull Player player, @NotNull String townName) {
        if (playerStates.get(player.getUniqueId()) == GUIState.AWAITING_NAME) {
            BukkitUser user = BukkitUser.adapt(player, plugin);

            // Use the existing town creation logic
            plugin.getManager().towns().createTown(user, townName);

            // Clean up state
            playerStates.remove(player.getUniqueId());

            // Reopen the options GUI to allow Jarton Hall placement
            Bukkit.getScheduler().runTaskLater(plugin, () -> openOptionsGUI(player), 20L);
        }
    }

    /**
     * Checks if the player has claimed at least one chunk
     *
     * @param player The player to check
     * @return true if they have claimed a chunk
     */
    private boolean hasPlayerClaimedChunk(@NotNull Player player) {
        BukkitUser user = BukkitUser.adapt(player, plugin);
        return plugin.getUserTown(user)
                .map(member -> member.town().getClaimCount() > 0)
                .orElse(false);
    }

    /**
     * Gets the current GUI state for a player
     *
     * @param playerUuid The player's UUID
     * @return The GUI state, or null if not in GUI
     */
    public GUIState getPlayerState(@NotNull UUID playerUuid) {
        return playerStates.get(playerUuid);
    }

    /**
     * Removes a player's GUI state
     *
     * @param playerUuid The player's UUID
     */
    public void removePlayerState(@NotNull UUID playerUuid) {
        playerStates.remove(playerUuid);
    }

    /**
     * Enum representing the different states of the GUI
     */
    public enum GUIState {
        INITIAL,
        OPTIONS,
        AWAITING_NAME
    }
}
