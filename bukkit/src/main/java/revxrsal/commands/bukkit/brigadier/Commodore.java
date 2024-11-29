/*
 * This file is part of commodore, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
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

package revxrsal.commands.bukkit.brigadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class Commodore {

    /**
     * Gets the aliases known for the given command.
     *
     * <p>This will include the main label, as well as defined aliases, and
     * aliases including the fallback prefix added by Bukkit.</p>
     *
     * @param command the command
     * @return the aliases
     */
    protected static Collection<String> getAliases(Command command) {
        Objects.requireNonNull(command, "command");

        Stream<String> aliasesStream = Stream.concat(
                Stream.of(command.getLabel()),
                command.getAliases().stream()
        );

        if (command instanceof PluginCommand) {
            String fallbackPrefix = ((PluginCommand) command).getPlugin().getName().toLowerCase().trim();
            aliasesStream = aliasesStream.flatMap(alias -> Stream.of(
                    alias,
                    fallbackPrefix + ":" + alias
            ));
        }

        return aliasesStream.distinct().collect(Collectors.toList());
    }

    /**
     * Registers the provided argument data to the dispatcher, against all
     * aliases defined for the {@code command}.
     *
     * <p>Additionally applies the CraftBukkit {@link SuggestionProvider}
     * to all arguments within the node, so ASK_SERVER suggestions can continue
     * to function for the command.</p>
     *
     * @param command the command to read aliases from
     * @param node    the argument data
     */
    abstract void register(Command command, LiteralCommandNode<?> node);

    /**
     * Registers the provided argument data to the dispatcher.
     *
     * <p>Equivalent to calling
     * {@link CommandDispatcher#register(LiteralArgumentBuilder)}.</p>
     *
     * <p>Prefer using {@link #register(Command, LiteralCommandNode)}.</p>
     *
     * @param node the argument data
     */
    abstract void register(LiteralCommandNode<?> node);

}