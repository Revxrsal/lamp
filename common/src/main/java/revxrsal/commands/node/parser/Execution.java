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
package revxrsal.commands.node.parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.SecretCommand;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.command.*;
import revxrsal.commands.exception.ExpectedLiteralException;
import revxrsal.commands.exception.InputParseException;
import revxrsal.commands.exception.context.ErrorContext;
import revxrsal.commands.node.CommandNode;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.node.LiteralNode;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.stream.MutableStringStream;

import java.util.*;

import static revxrsal.commands.util.Collections.unmodifiableIterator;

final class Execution<A extends CommandActor> implements ExecutableCommand<A> {

    private final CommandFunction function;
    private final LinkedList<CommandNode<A>> nodes;
    private final CommandPermission<A> permission;
    private final int size;
    private int optionalParameters, requiredInput;
    private final boolean isSecret;
    private final String description, usage;

    public Execution(CommandFunction function, LinkedList<CommandNode<A>> nodes) {
        this.function = function;
        this.nodes = nodes;
        this.size = nodes.size();
        //noinspection unchecked
        this.permission = (CommandPermission<A>) function.lamp().createPermission(function.annotations());
        for (CommandNode<A> node : nodes) {
            if (isOptional(node))
                optionalParameters++;
            else
                requiredInput++;
        }
        this.isSecret = function.annotations().contains(SecretCommand.class);
        this.description = function.annotations().map(Description.class, Description::value);
        this.usage = function.annotations().mapOrGet(Usage.class, Usage::value, this::path);
    }

    @Override
    public @NotNull Lamp<A> lamp() {
        return function.lamp();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int optionalParameters() {
        return optionalParameters;
    }

    @Override
    public int requiredInput() {
        return requiredInput;
    }

    @Override
    public @NotNull String path() {
        StringJoiner joiner = new StringJoiner(" ");
        for (CommandNode<A> n : nodes) {
            if (n instanceof ParameterNodeImpl)
                if (((ParameterNode<? extends CommandActor, ?>) n).isOptional())
                    joiner.add("[" + n.name() + "]");
                else
                    joiner.add("<" + n.name() + ">");
            else
                joiner.add(n.name());
        }
        return joiner.toString();
    }

    @Override
    public @NotNull String usage() {
        return usage;
    }

    @Override
    public @NotNull CommandPermission<A> permission() {
        return permission;
    }

    @Override
    public @NotNull CommandFunction function() {
        return function;
    }

    @Override
    public @NotNull CommandNode<A> lastNode() {
        return nodes.getLast();
    }

    @Override
    public @NotNull LiteralNodeImpl<A> firstNode() {
        return ((LiteralNodeImpl<A>) nodes.getFirst());
    }

    @Override
    public @NotNull Potential<A> test(@NotNull A actor, @NotNull MutableStringStream input) {
        return new ParseResult<>(this, actor, input);
    }

    @Override
    public void unregister() {
        lamp().unregister(this);
    }

    @Override
    public boolean isSecret() {
        return isSecret;
    }

    @Override
    public String toString() {
        return "ExecutableCommand(path='" + path() + "')";
    }

    @Override
    public @Nullable String description() {
        return description;
    }

    @Override
    public @NotNull @Unmodifiable List<CommandNode<A>> nodes() {
        return Collections.unmodifiableList(nodes);
    }

    @Override
    public int compareTo(@NotNull ExecutableCommand<A> o) {
        if (!(o instanceof Execution<A> exec)) {
            return 0;
        }
        if (size - requiredInput == exec.size) {
            if (isOptional(lastNode()) != isOptional(o.lastNode()))
                return isOptional(lastNode()) ? 1 : -1;
        }
        // notice that we do exec.size first, then our size. this
        // reverses the order which is mostly what we want
        // (higher size = higher priority)
        int result = Integer.compare(exec.size, size);
        if (result == 0) {
            CommandNode<A> ourLeaf = lastNode();
            CommandNode<A> theirs = o.lastNode();

            return ourLeaf.compareTo(theirs);
        }
        return result;
    }

    private static boolean isOptional(@NotNull CommandNode<? extends CommandActor> node) {
        return node instanceof ParameterNodeImpl && ((ParameterNode<? extends CommandActor, ?>) node).isOptional();
    }

    @Override
    public Iterator<CommandNode<A>> iterator() {
        return unmodifiableIterator(nodes.iterator());
    }

    static final class ParseResult<A extends CommandActor> implements Potential<A> {
        private final Execution<A> execution;
        private final BasicExecutionContext<A> context;
        private final MutableStringStream input;
        private boolean consumedAllInput = false;
        private final boolean testResult;
        private @Nullable Throwable error;
        private @Nullable ErrorContext<A> errorContext;

        public ParseResult(Execution<A> execution, A actor, MutableStringStream input) {
            this.execution = execution;
            this.context = new BasicExecutionContext<>(execution.function.lamp(), execution, actor);
            this.input = input;
            this.testResult = test();
        }

        private boolean test() {
            for (CommandNode<A> node : execution.nodes) {
                if (!tryParse(node, input, context)) {
                    context.clearResolvedArguments();
                    return false;
                }
            }
            consumedAllInput = input.hasFinished();
            return true;
        }

        @Override
        public boolean successful() {
            return testResult;
        }

        @Override
        public @NotNull ExecutionContext<A> context() {
            return context;
        }

        @Override
        public boolean failed() {
            return !testResult;
        }

        @Override
        public void handleException() {
            if (error != null && errorContext != null)
                context().lamp().handleException(error, errorContext);
        }

        @Override
        public @Nullable Throwable error() {
            return error;
        }

        @Override
        public @Nullable ErrorContext<A> errorContext() {
            return errorContext;
        }

        @Override
        public void execute() {
            if (error == null) {
                if (!execution.lamp().hooks().onCommandExecuted(execution, context))
                    execution.lastNode().execute(context, input);
            }
        }

        @Override
        public int compareTo(@NotNull Potential<A> o) {
            if (o.getClass() != getClass())
                return 0;
            ParseResult<A> result = ((ParseResult<A>) o);
            if (consumedAllInput != result.consumedAllInput)
                return consumedAllInput ? -1 : 1;
            return execution.compareTo(result.execution);
        }

        private boolean tryParse(
                CommandNode<A> node,
                MutableStringStream input,
                BasicExecutionContext<A> context
        ) {
            if (input.hasRemaining() && input.peek() == ' ')
                input.moveForward();
            int pos = input.position();
            if (node instanceof LiteralNodeImpl<A> l) {
                String value = input.readUnquotedString();
                if (node.name().equalsIgnoreCase(value)) {
                    checkForSpace(input);
                    return true;
                }
                input.setPosition(pos);
                error = new ExpectedLiteralException(value, (LiteralNode<CommandActor>) l);
                errorContext = ErrorContext.parsingLiteral(context, l);
                return false;
            }
            ParameterNodeImpl<A, Object> parameter = (ParameterNodeImpl<A, Object>) node;
            try {
                Object value = parameter.parse(input, context);
                Lamp<A> lamp = execution.function().lamp();
                lamp.validate(context.actor(), value, parameter);
                context.addResoledArgument(parameter.name(), value);
                checkForSpace(input);
                return true;
            } catch (Throwable t) {
                input.setPosition(pos);
                error = t;
                errorContext = ErrorContext.parsingParameter(context, parameter, input);
                return false;
            }
        }

        private void checkForSpace(MutableStringStream input) {
            if (input.hasRemaining() && input.peek() != ' ')
                throw new InputParseException(InputParseException.Cause.EXPECTED_WHITESPACE);
        }

        @Override
        public String toString() {
            if (successful())
                return "Potential(path=" + execution.path() + ", success=true)";
            else
                return "Potential(path=" + execution.path() + ", success=false, error=" + error + ")";
        }
    }
}