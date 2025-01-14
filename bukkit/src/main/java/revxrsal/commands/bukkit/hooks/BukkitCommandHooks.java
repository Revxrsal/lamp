/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package revxrsal.commands.bukkit.hooks;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.actor.ActorFactory;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.FallbackPrefix;
import revxrsal.commands.bukkit.util.PluginCommands;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.hook.CancelHandle;
import revxrsal.commands.hook.CommandRegisteredHook;
import revxrsal.commands.hook.CommandUnregisteredHook;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static revxrsal.commands.bukkit.util.PluginCommands.getCommand;

public final class BukkitCommandHooks<A extends BukkitCommandActor> implements CommandRegisteredHook<A>,
        CommandUnregisteredHook<A> {

    private final Set<String> registeredRootNames = new HashSet<>();

    private final JavaPlugin plugin;
    private final ActorFactory<A> actorFactory;
    private final String defaultFallbackPrefix;

    public BukkitCommandHooks(JavaPlugin plugin, ActorFactory<A> actorFactory, @NotNull String defaultFallbackPrefix) {
        this.plugin = plugin;
        this.actorFactory = actorFactory;
        this.defaultFallbackPrefix = defaultFallbackPrefix;
    }

    @Override
    public void onRegistered(@NotNull ExecutableCommand<A> command, @NotNull CancelHandle cancelHandle) {
        String name = command.firstNode().name();
        if (registeredRootNames.add(name)) {
            // command wasn't registered before. register it.
            String fallbackPrefix = command.annotations().mapOr(FallbackPrefix.class, FallbackPrefix::value, defaultFallbackPrefix);
            PluginCommand cmd = PluginCommands.create(fallbackPrefix, command.firstNode().name(), plugin);

            LampCommandExecutor<A> executor = new LampCommandExecutor<>(command.lamp(), actorFactory);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);

            if (cmd.getDescription().isEmpty() && command.description() != null)
                cmd.setDescription(Objects.requireNonNull(command.description()));
            if (cmd.getUsage().isEmpty())
                cmd.setUsage(command.usage());
        }
    }

    @Override public void onUnregistered(@NotNull ExecutableCommand<A> command, @NotNull CancelHandle cancelHandle) {
        String label = command.firstNode().name();
        String fallbackPrefix = fallbackPrefix(command);
        PluginCommand cmd = Bukkit.getServer().getPluginCommand(fallbackPrefix + ':' + label);
        // check there's no other '/fallback_prefix:label' command. if so, unregister.
        if (!command.lamp().registry().any(c -> c != command && c.firstNode().name().equals(label) && fallbackPrefix(c).equals(fallbackPrefix)))
            if (cmd != null)
                PluginCommands.unregister(cmd, plugin);

        // check there's no other '/label' command. if so, unregister.
        if (!command.lamp().registry().any(c -> c != command && c.firstNode().name().equals(label))) {
            cmd = getCommand(plugin, label);
            if (cmd != null)
                PluginCommands.unregister(cmd, plugin);
        }
    }

    private @NotNull String fallbackPrefix(@NotNull ExecutableCommand<A> command) {
        return command.annotations().mapOr(FallbackPrefix.class, FallbackPrefix::value, defaultFallbackPrefix);
    }
}
