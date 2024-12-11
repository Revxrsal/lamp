/*
 * This file is part of sweeper, licensed under the MIT License.
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
package revxrsal.commands.node;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.*;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Represents a parameter node. This node has a specific {@link #parameterType()} that
 * can parse the content of {@link MutableStringStream} and generate the
 * appropriate values.
 * <p>
 * Note that this node will always correspond to a reflective {@link java.lang.reflect.Parameter} through
 * the {@link #parameter()} type.
 *
 * @param <A> The actor type
 * @param <T> The parameter type, for type-safety.
 */
public interface ParameterNode<A extends CommandActor, T> extends CommandNode<A>, RequiresPermission<A>, HasDescription {

    /**
     * Parses the given input and produces the appropriate value
     * for the input.
     * <p>
     * If this parameter was optional, or has a default value, it will be
     * used if the input is not sufficient.
     * <p>
     * This may produce {@code null} values in case of {@link #isOptional()} parameters
     * as well as {@link ParameterType}s that may return a {@code null} value
     *
     * @param input   The input type
     * @param context The execution context
     * @return The resolved type.
     */
    @Nullable T parse(MutableStringStream input, ExecutionContext<A> context);

    /**
     * Returns the annotations present on this {@link ParameterNode}.
     *
     * @return The annotations on this node.
     */
    @NotNull AnnotationList annotations();

    /**
     * Tests whether is this parameter optional or not.
     * <p>
     * This will return true in these cases:
     * <ol>
     *     <li>It is marked with {@link Optional @Optional}</li>
     *     <li>It has a default value from {@link Default @Default}</li>
     *     <li>It has {@link Sized @Sized} with {@code min()} == 0</li>
     * </ol>
     *
     * @return if this parameter is optional
     */
    boolean isOptional();


    /**
     * Tests whether is this parameter required or not.
     * <p>
     * This will return false in these cases:
     * <ol>
     *     <li>It is marked with {@link Optional @Optional}</li>
     *     <li>It has a default value from {@link Default @Default}</li>
     *     <li>It has {@link Sized @Sized} with {@code min()} == 0</li>
     * </ol>
     *
     * @return if this parameter is required
     */
    default boolean isRequired() {
        return !isOptional();
    }

    /**
     * Returns the {@link ParameterType} of this parameter that will be
     * used to parse the input.
     *
     * @return The parameter type
     */
    @NotNull ParameterType<A, T> parameterType();

    /**
     * Returns the suggestion provider for this parameter node. This
     * node would either be generated by relevant {@link SuggestionProvider.Factory factories}
     * or have the default suggestions from {@link ParameterType#defaultSuggestions()}.
     *
     * @return The suggestion provider of this node
     */
    @NotNull SuggestionProvider<A> suggestions();

    /**
     * Returns the underlying {@link CommandParameter} of this parameter
     * node
     *
     * @return The underlying {@link CommandParameter}.
     */
    @NotNull CommandParameter parameter();

    /**
     * Tests whether is this parameter greedy or not
     *
     * @return if this parameter is greedy
     */
    boolean isGreedy();

    /**
     * Provides suggestions for the given user input.
     *
     * @param context The execution context.
     * @return The
     */
    @Contract(pure = true)
    @NotNull Collection<String> complete(@NotNull ExecutionContext<A> context);

    /**
     * Returns the parameter Java type
     *
     * @return The parameter type
     */
    default @NotNull Class<?> type() {
        return parameter().type();
    }

    /**
     * Returns the parameter Java type
     *
     * @return The parameter type
     */
    default @NotNull Type fullType() {
        return parameter().fullType();
    }

    /**
     * Tests whether this node is a {@link LiteralNode}. This will
     * always return false
     *
     * @return if this node is literal
     */
    @Override
    default boolean isLiteral() {
        return false;
    }

    /**
     * Tests whether this node is a {@link ParameterNode}. This
     * will always return true
     *
     * @return if this node is a parameter
     */
    @Override
    default boolean isParameter() {
        return true;
    }

    /**
     * Requires this node to be a {@link LiteralNode}. This will throw
     * a {@link IllegalStateException}.
     *
     * @return never as it always fails
     * @throws IllegalStateException always
     */
    @Override
    @Contract("-> fail")
    default @NotNull LiteralNode<A> requireLiteralNode() {
        throw new IllegalStateException("Expected a LiteralNode, found a ParameterNode");
    }

    /**
     * Tests whether this parameter is a flag
     *
     * @return if this parameter is a flag
     * @see Flag
     */
    boolean isFlag();

    /**
     * Tests whether this parameter is a switch
     *
     * @return if this parameter is a switch
     * @see Switch
     */
    boolean isSwitch();

    /**
     * Gets the shorthand defined from either {@link Flag#shorthand()} or {@link Switch#shorthand()}.
     * This may return null if this parameter is neither a flag nor a switch.
     *
     * @return The shorthand, or {@code null} if not defined.
     */
    @Nullable @Contract(pure = true) Character shorthand();

    /**
     * Gets the switch name defined in {@link Switch#value()}.
     * This may return null if this parameter is not a switch.
     *
     * @return The switch name, or {@code null} if not defined.
     */
    @Nullable @Contract(pure = true) String switchName();

    /**
     * Gets the flag name defined in {@link Flag#value()}.
     * This may return null if this parameter is not a flag.
     *
     * @return The switch name, or {@code null} if not defined.
     */
    @Nullable @Contract(pure = true) String flagName();
}
