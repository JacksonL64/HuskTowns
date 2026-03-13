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

package net.william278.husktowns.user;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

public interface CommandUser {

    @NotNull
    Audience getAudience();

    boolean hasPermission(@NotNull String permission);

    default void sendMessage(@NotNull Component component) {
        if (component == null) {
            return;
        }
        if (trySendDirectly(component)) {
            return;
        }
        getAudience().sendMessage(component);
    }

    default void sendMessage(@NotNull MineDown mineDown) {
        this.sendMessage(mineDown.toComponent());
    }

    private boolean trySendDirectly(@NotNull Component component) {
        try {
            final java.lang.reflect.Method getPlayer = this.getClass().getMethod("getPlayer");
            final Object player = getPlayer.invoke(this);
            if (player != null) {
                try {
                    final java.lang.reflect.Method richSendMessage = player.getClass().getMethod("sendMessage", Component.class);
                    richSendMessage.invoke(player, component);
                    return true;
                } catch (ReflectiveOperationException ignored) {
                }

                final java.lang.reflect.Method sendMessage = player.getClass().getMethod("sendMessage", String.class);
                sendMessage.invoke(player, PlainTextComponentSerializer.plainText().serialize(component));
                return true;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return false;
    }

}
