package io.papermc.paper.plugin.lifecycle.event.handler.configuration;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

/**
 * Handler configuration for event types that allow "monitor" handlers.
 *
 * @param <O> the required owner type
 */
@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface MonitorLifecycleEventHandlerConfiguration<O> extends LifecycleEventHandlerConfiguration<O> {

    /**
     * Sets this handler configuration to be considered a "monitor".
     * These handlers will run last and should only be used by plugins
     * to observe changes from previously run handlers.
     *
     * @return this configuration for chaining
     */
    @Contract("-> this")
    MonitorLifecycleEventHandlerConfiguration<O> monitor();
}