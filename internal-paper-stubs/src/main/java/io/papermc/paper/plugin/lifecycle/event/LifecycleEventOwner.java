package io.papermc.paper.plugin.lifecycle.event;

import io.papermc.paper.plugin.configuration.PluginMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Implemented by types that are considered owners
 * of registered handlers for lifecycle events. Generally
 * the types that implement this interface also provide
 * a {@link LifecycleEventManager} where you can register
 * event handlers.
 */
@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface LifecycleEventOwner {

    /**
     * Get the plugin meta for this plugin.
     *
     * @return the plugin meta
     */
    @NotNull PluginMeta getPluginMeta();
}
