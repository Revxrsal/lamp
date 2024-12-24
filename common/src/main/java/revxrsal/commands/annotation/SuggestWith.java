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
package revxrsal.commands.annotation;

import revxrsal.commands.autocomplete.BaseSuggestionProvider;
import revxrsal.commands.autocomplete.SuggestionProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that instructs Lamp that certain parameters should produce
 * suggestions using a specific {@link SuggestionProvider} or a {@link SuggestionProvider.Factory}.
 * <p>
 * Note that the supplied type <em>must</em> be either:
 * <ul>
 *     <li>A class with a no-arg constructor</li>
 *     <li>An enum with at least 1 constant field</li>
 *     <li>A class or interface with static fields of the same type (i.e. INSTANCE-like fields)</li>
 *     <li>A class or interface with no-arg methods that return the same type (i.e. getInstance()-like methods)</li>
 * </ul>
 * <p>
 * The supplied class must either implement {@link SuggestionProvider}
 * or {@link SuggestionProvider.Factory}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SuggestWith {

    /**
     * The type to suggest with
     *
     * @return The type
     */
    Class<? extends BaseSuggestionProvider> value();

}