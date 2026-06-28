package com.cobwebalert;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;

/**
 * Entry point for the client side of the mod.
 * This mod has NO server-side code at all - it just watches entity
 * positions (which every client already receives normally in multiplayer)
 * and recolors cobwebs locally. That means it only needs to be installed
 * on whichever client wants to see the red highlight; it does not need to
 * be installed on the server, and it does not change how cobwebs behave.
 */
public final class CobwebAlertClient implements ClientModInitializer {

    // Tweak these two if you want a different look.
    private static final int ALERT_COLOR = 0xFF3B3B;  // red, shown while occupied
    private static final int NORMAL_COLOR = 0xFFFFFF;  // white = "no tint", vanilla look

    @Override
    public void onInitializeClient() {
        // This is what actually makes the block render red or not. Fabric calls
        // this every time a cobweb is drawn, and we just look up whether that
        // exact position is currently marked "occupied" by CobwebTracker.
        ColorProviderRegistry.BLOCK.register(
                (state, world, pos, tintIndex) ->
                        (pos != null && CobwebTracker.isActive(pos)) ? ALERT_COLOR : NORMAL_COLOR,
                Blocks.COBWEB
        );

        // The cobweb ITEM (the one you hold/see in your inventory) now shares
        // a model with a tint slot too, so we explicitly keep it untinted.
        // Without this it would still probably look fine, but this guarantees it.
        ColorProviderRegistry.ITEM.register(
                (stack, tintIndex) -> NORMAL_COLOR,
                Items.COBWEB
        );

        // Re-check which cobwebs are occupied once every client tick (20x/second).
        ClientTickEvents.END_CLIENT_TICK.register(CobwebTracker::tick);
    }
}
