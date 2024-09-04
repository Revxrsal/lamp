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
package revxrsal.commands.bukkit;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.brigadier.types.ArgumentTypes;
import revxrsal.commands.bukkit.actor.ActorFactory;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.brigadier.BukkitArgumentTypes;
import revxrsal.commands.bukkit.util.BukkitVersion;
import revxrsal.commands.util.Lazy;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static revxrsal.commands.bukkit.BukkitVisitors.*;
import static revxrsal.commands.util.Preconditions.notNull;

/**
 * A collective object that contains all Bukkit-only properties and allows for
 * easy customizing and chaining using a builder
 *
 * @param <A> The actor type.
 */
public final class BukkitLampConfig<A extends BukkitCommandActor> implements LampBuilderVisitor<A> {

    private final ActorFactory<A> actorFactory;
    private final Supplier<ArgumentTypes<A>> argumentTypes;
    private final JavaPlugin plugin;
    private final boolean disableBrigadier;

    private BukkitLampConfig(ActorFactory<A> actorFactory, Supplier<ArgumentTypes<A>> argumentTypes, JavaPlugin plugin, boolean disableBrigadier) {
        this.actorFactory = actorFactory;
        this.argumentTypes = argumentTypes;
        this.plugin = plugin;
        this.disableBrigadier = disableBrigadier;
    }

    /**
     * Returns a new {@link Builder} with the given plugin.
     *
     * @param plugin Plugin to create for
     * @param <A>    The actor type
     * @return The {@link Builder}
     */
    public static <A extends BukkitCommandActor> Builder<A> builder(@NotNull JavaPlugin plugin) {
        notNull(plugin, "plugin");
        return new Builder<>(plugin);
    }

    /**
     * Returns a new {@link BukkitLampConfig} with the given plugin,
     * containing the default settings.
     *
     * @param plugin Plugin to create for
     * @return The {@link Builder}
     */
    public static BukkitLampConfig<BukkitCommandActor> createDefault(@NotNull JavaPlugin plugin) {
        notNull(plugin, "plugin");
        return new BukkitLampConfig<>(ActorFactory.defaultFactory(), () -> BukkitArgumentTypes.<BukkitCommandActor>builder().build(), plugin, false);
    }

    @Override public void visit(Lamp.@NotNull Builder<A> builder) {
        builder.accept(legacyColorCodes())
                .accept(bukkitSenderResolver())
                .accept(bukkitParameterTypes())
                .accept(bukkitExceptionHandler())
                .accept(bukkitPermissions())
                .accept(registrationHooks(plugin))
                .accept(pluginContextParameters(plugin));
        if (BukkitVersion.isBrigadierSupported() && !disableBrigadier)
            builder.accept(brigadier(plugin, argumentTypes.get(), actorFactory));
    }

    /**
     * Represents a builder for {@link BukkitLampConfig}
     *
     * @param <A> The actor type
     */
    public static class Builder<A extends BukkitCommandActor> {

        private final Supplier<ArgumentTypes.Builder<A>> argumentTypes = Lazy.of(() -> BukkitArgumentTypes.builder());
        private final @NotNull JavaPlugin plugin;
        private ActorFactory<A> actorFactory = (ActorFactory<A>) ActorFactory.defaultFactory();
        private boolean disableBrigadier;

        Builder(@NotNull JavaPlugin plugin) {
            this.plugin = plugin;
        }

        /**
         * Sets the {@link ActorFactory}. This allows to supply custom implementations for
         * the {@link BukkitLampConfig} interface.
         *
         * @param actorFactory The actor factory
         * @return This builder
         * @see ActorFactory
         */
        public @NotNull Builder<A> actorFactory(@NotNull ActorFactory<A> actorFactory) {
            this.actorFactory = actorFactory;
            return this;
        }

        /**
         * Returns the {@link ArgumentTypes.Builder} of this builder
         *
         * @return The builder
         */
        public @NotNull ArgumentTypes.Builder<A> argumentTypes() {
            return argumentTypes.get();
        }

        /**
         * Applies the given {@link Consumer} on the {@link #argumentTypes()} instance.
         * This allows for easy chaining of the builder instances
         *
         * @param consumer Consumer to apply
         * @return This builder
         */
        public @NotNull Builder<A> argumentTypes(@NotNull Consumer<ArgumentTypes.Builder<A>> consumer) {
            consumer.accept(argumentTypes.get());
            return this;
        }

        /**
         * Disables brigadier integration
         *
         * @return This builder
         */
        public @NotNull Builder<A> disableBrigadier() {
            return disableBrigadier(true);
        }

        /**
         * Disables brigadier integration
         *
         * @return This builder
         */
        public @NotNull Builder<A> disableBrigadier(boolean disabled) {
            this.disableBrigadier = disabled;
            return this;
        }

        /**
         * Returns a new {@link BukkitLampConfig} from this builder
         *
         * @return The newly created config
         */
        @Contract("-> new")
        public @NotNull BukkitLampConfig<A> build() {
            return new BukkitLampConfig<>(this.actorFactory, Lazy.of(() -> argumentTypes.get().build()), this.plugin, disableBrigadier);
        }
    }
}