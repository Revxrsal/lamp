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
package revxrsal.commands.bukkit.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.bukkit.util.BukkitVersion;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An enumeration for containing Minecraft's built-in {@link ArgumentType}s.
 * <p>
 * This is for older versions from 1.13 to 1.19.2.
 */
@SuppressWarnings("rawtypes")
public enum MinecraftArgumentType {

    /**
     * A selector, player name, or UUID.
     * <p>
     * Parameters:
     * - boolean single
     * - boolean playerOnly
     */
    ENTITY(new String[]{"ArgumentEntity", "EntityArgument"}, boolean.class, boolean.class),

    /**
     * A player, online or not. Can also use a selector, which may match one or more
     * players (but not entities).
     */
    GAME_PROFILE("ArgumentProfile", "GameProfileArgument"),

    /**
     * A chat color. One of the names from <a href="https://wiki.vg/Chat#Colors">colors</a>, or {@code reset}.
     * Case-insensitive.
     */
    COLOR("ArgumentChatFormat", "ColorArgument"),

    /**
     * A JSON Chat component.
     */
    COMPONENT("ArgumentChatComponent", "ComponentArgument"),

    /**
     * A regular message, potentially including selectors.
     */
    MESSAGE("ArgumentChat", "MessageArgument"),

    /**
     * An NBT value, parsed using JSON-NBT rules. This represents a full NBT tag.
     */
    NBT("ArgumentNBTTag", "CompoundTagArgument"),

    /**
     * Represents a partial NBT tag, usable in data modify command.
     */
    NBT_TAG("ArgumentNBTBase", "NbtTagArgument"),

    /**
     * A path within an NBT value, allowing for array and member accesses.
     */
    NBT_PATH("ArgumentNBTKey", "NbtPathArgument"),

    /**
     * A scoreboard objective.
     */
    SCOREBOARD_OBJECTIVE("ArgumentScoreboardObjective", "ObjectiveArgument"),

    /**
     * A single score criterion.
     */
    OBJECTIVE_CRITERIA("ArgumentScoreboardCriteria", "ObjectiveCriteriaArgument"),

    /**
     * A scoreboard operator.
     */
    SCOREBOARD_SLOT("ArgumentScoreboardSlot", "SlotArgument"),

    /**
     * Something that can join a team. Allows selectors and *.
     */
    SCORE_HOLDER("ArgumentScoreholder", "ScoreHolderArgument"),

    /**
     * The name of a team. Parsed as an unquoted string.
     */
    TEAM("ArgumentScoreboardTeam", "TeamArgument"),

    /**
     * A scoreboard operator.
     */
    OPERATION("ArgumentMathOperation", "OperationArgument"),

    /**
     * A particle effect (an identifier with extra information following it for
     * specific particles, mirroring the Particle packet)
     */
    PARTICLE("ArgumentParticle", "ParticleArgument"),

    /**
     * Represents an angle.
     */
    ANGLE("ArgumentAngle", "AngleArgument"),

    /**
     * A name for an inventory slot.
     */
    ITEM_SLOT("ArgumentInventorySlot", "SlotArgument"),

    /**
     * An Identifier.
     */
    RESOURCE_LOCATION("ArgumentMinecraftKeyRegistered", "ResourceLocationArgument"),

    /**
     * A potion effect.
     */
    POTION_EFFECT("ArgumentMobEffect"),

    /**
     * Represents a item enchantment.
     */
    ENCHANTMENT("ArgumentEnchantment"),

    /**
     * Represents an entity summon.
     */
    ENTITY_SUMMON("ArgumentEntitySummon"),

    /**
     * Represents a dimension.
     */
    DIMENSION("ArgumentDimension", "DimensionArgument"),

    /**
     * Represents a time duration.
     */
    TIME("ArgumentTime", "TimeArgument"),

    /**
     * Represents a UUID value.
     *
     * @since Minecraft 1.16
     */
    UUID("ArgumentUUID", "UuidArgument"),

    /**
     * A location, represented as 3 numbers (which must be integers). May use relative locations
     * with ~
     */
    BLOCK_POS("coordinates.ArgumentPosition", "coordinates.BlockPosArgument"),

    /**
     * A column location, represented as 2 numbers (which must be integers). May use relative locations
     * with ~.
     */
    COLUMN_POS("coordinates.ArgumentVec2I", "coordinates.ColumnPosArgument"),

    /**
     * A location, represented as 3 numbers (which may have a decimal point, but will be moved to the
     * center of a block if none is specified). May use relative locations with ~.
     */
    VECTOR_3("coordinates.ArgumentVec3", "coordinates.Vec3Argument"),

    /**
     * A location, represented as 2 numbers (which may have a decimal point, but will be moved to the center
     * of a block if none is specified). May use relative locations with ~.
     */
    VECTOR_2("coordinates.ArgumentVec2", "coordinates.Vec2Argument"),

    /**
     * An angle, represented as 2 numbers (which may have a decimal point, but will be moved to the
     * center of a block if none is specified). May use relative locations with ~.
     */
    ROTATION("coordinates.ArgumentRotation", "coordinates.RotationArgument"),

    /**
     * A collection of up to 3 axes.
     */
    SWIZZLE("coordinates.ArgumentRotationAxis", "coordinates.SwizzleArgument"),

    /**
     * A block state, optionally including NBT and state information.
     */
    BLOCK_STATE("blocks.ArgumentTile", "blocks.BlockStateArgument"),

    /**
     * A block, or a block tag.
     */
    BLOCK_PREDICATE("blocks.ArgumentBlockPredicate", "blocks.BlockPredicateArgument"),

    /**
     * An item, optionally including NBT.
     */
    ITEM_STACK("item.ArgumentItemStack", "item.ItemArgument"),

    /**
     * An item, or an item tag.
     */
    ITEM_PREDICATE("item.ArgumentItemPredicate", "item.ItemPredicateArgument"),

    /**
     * A function.
     */
    FUNCTION("item.ArgumentTag", "item.FunctionArgument"),

    /**
     * The entity anchor related to the facing argument in the teleport command,
     * is feet or eyes.
     */
    ENTITY_ANCHOR("ArgumentAnchor", "EntityAnchorArgument"),

    /**
     * An integer range of values with a min and a max.
     */
    INT_RANGE("ArgumentCriterionValue$b", "RangeArgument$Ints"),

    /**
     * A floating-point range of values with a min and a max.
     */
    FLOAT_RANGE("ArgumentCriterionValue$a", "RangeArgument$Floats"),

    /**
     * Template mirror
     *
     * @since Minecraft 1.19
     */
    TEMPLATE_MIRROR("TemplateMirrorArgument"),

    /**
     * Template rotation
     *
     * @since Minecraft 1.19
     */
    TEMPLATE_ROTATION("TemplateRotationArgument");

    private final Class<?>[] parameters;
    private @Nullable ArgumentType<?> argumentType;
    private @Nullable Constructor<? extends ArgumentType> argumentConstructor;

    MinecraftArgumentType(String... names) {
        this(names, new Class[0]);
    }

    MinecraftArgumentType(String[] names, Class<?>... parameters) {
        Class<?> argumentClass = null;
        for (String name : names) {
            argumentClass = resolveArgumentClass(name);
            if (argumentClass != null)
                break;
        }
        this.parameters = parameters;
        if (argumentClass == null) {
            argumentType = null;
            argumentConstructor = null;
            return;
        }
        try {
            argumentConstructor = argumentClass.asSubclass(ArgumentType.class).getDeclaredConstructor(parameters);
            if (!argumentConstructor.isAccessible())
                argumentConstructor.setAccessible(true);
            if (parameters.length == 0) {
                argumentType = argumentConstructor.newInstance();
            } else {
                argumentType = null;
            }
        } catch (Throwable e) {
            argumentType = null;
            argumentConstructor = null;
        }
    }

    static class Data {
        private static final List<String> POSSIBLE_CLASS_NAMES = Arrays.asList(
                "net.minecraft.server.{name}",
                "net.minecraft.server.{version}.{name}",
                "net.minecraft.commands.arguments.{name}",
                "net.minecraft.server.{version}.{stripped_name}"
        );
    }

    private static @Nullable Class<?> resolveArgumentClass(String name) {
        String strippedName;
        if (name.lastIndexOf('.') != -1)
            strippedName = name.substring(name.lastIndexOf('.') + 1);
        else
            strippedName = name;
        for (String s : Data.POSSIBLE_CLASS_NAMES) {
            String className = s
                    .replace("{version}", BukkitVersion.version())
                    .replace("{name}", name)
                    .replace("{stripped_name}", strippedName);
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }

    /**
     * Checks if this argument type is supported in this Minecraft version
     *
     * @return If this is supported
     */
    public boolean isSupported() {
        return argumentConstructor != null;
    }

    /**
     * Checks if this argument type requires parameters
     *
     * @return If this requires parameters
     */
    public boolean requiresParameters() {
        return parameters.length != 0;
    }

    /**
     * Returns the argument type represented by this enum value, otherwise
     * throws an exception
     *
     * @param <T> The argument type
     * @return The argument type
     * @throws IllegalArgumentException if not supported in this version
     * @throws IllegalArgumentException if this argument requires arguments. See {@link #create(Object...)}
     */
    public @NotNull <T> ArgumentType<T> get() {
        if (argumentConstructor == null)
            throw new IllegalArgumentException("Argument type '" + name().toLowerCase() + "' is not available on this version.");
        if (argumentType != null)
            return (ArgumentType<T>) argumentType;
        throw new IllegalArgumentException("This argument type requires " + parameters.length + " parameter(s) of type(s) " +
                Arrays.stream(parameters).map(Class::getName).collect(Collectors.joining(", ")) + ". Use #create() instead.");
    }

    /**
     * Creates an instance of this argument type
     *
     * @param arguments Arguments to construct the argument type with
     * @param <T>       The argument ttype
     * @return The created argument type.
     * @throws IllegalArgumentException if not supported in this version
     */
    @SneakyThrows
    public @NotNull <T> ArgumentType<T> create(Object... arguments) {
        if (argumentConstructor == null)
            throw new IllegalArgumentException("Argument type '" + name().toLowerCase() + "' is not available on this version.");
        if (argumentType != null && arguments.length == 0)
            return (ArgumentType<T>) argumentType;
        return argumentConstructor.newInstance(arguments);
    }

    /**
     * Returns the argument type represented by this enum value, wrapped
     * inside an {@link Optional}
     *
     * @param <T> The argument type
     * @return The argument type optional
     * @throws IllegalArgumentException if this argument requires arguments. See {@link #createIfPresent(Object...)}
     */
    public @NotNull <T> Optional<ArgumentType<T>> getIfPresent() {
        if (argumentConstructor == null)
            return Optional.empty();
        if (argumentType != null)
            return Optional.of((ArgumentType<T>) argumentType);
        throw new IllegalArgumentException("This argument type requires " + parameters.length + " parameter(s) of type(s) " +
                Arrays.stream(parameters).map(Class::getName).collect(Collectors.joining(", ")) + ". Use #create() instead.");
    }

    /**
     * Creates an instance of this argument type, wrapped in an optional.
     *
     * @param arguments Arguments to construct the argument type with
     * @param <T>       The argument ttype
     * @return The created argument type optional.
     */
    @SneakyThrows
    public @NotNull <T> Optional<ArgumentType<T>> createIfPresent(Object... arguments) {
        if (argumentConstructor == null)
            return Optional.empty();
        if (argumentType != null && arguments.length == 0)
            return Optional.of((ArgumentType<T>) argumentType);
        return Optional.of(argumentConstructor.newInstance(arguments));
    }
}
