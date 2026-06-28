package com.cobwebalert;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.HashSet;
import java.util.Set;

/**
 * Keeps a live set of "which cobweb positions currently have a player or
 * mob standing in them". Recomputed from scratch every client tick, which
 * is cheap (a handful of entities at most, in any normal PvP situation).
 *
 * Because this runs independently on every player's own client using
 * entity positions that are already sent to them as part of normal
 * multiplayer play, everyone with the mod installed sees the same cobwebs
 * light up - including cobwebs that an opponent, not just you, is stuck in.
 */
public final class CobwebTracker {

    private static Set<BlockPos> active = new HashSet<>();

    private CobwebTracker() {
    }

    public static boolean isActive(BlockPos pos) {
        return active.contains(pos);
    }

    public static void tick(MinecraftClient client) {
        if (client.world == null) {
            return;
        }

        Set<BlockPos> updated = new HashSet<>();

        for (Entity entity : client.world.getEntities()) {
            // Only players/mobs can be "trapped" - skip arrows, dropped items, etc.
            if (!(entity instanceof LivingEntity)) {
                continue;
            }

            Box box = entity.getBoundingBox();
            BlockPos min = BlockPos.ofFloored(box.minX, box.minY, box.minZ);
            BlockPos max = BlockPos.ofFloored(box.maxX, box.maxY, box.maxZ);

            for (BlockPos pos : BlockPos.iterate(min, max)) {
                if (client.world.getBlockState(pos).isOf(Blocks.COBWEB)) {
                    // toImmutable() matters here - BlockPos.iterate() reuses
                    // the same mutable object every loop, so without this every
                    // entry in the set would end up pointing at the same position.
                    updated.add(pos.toImmutable());
                }
            }
        }

        Set<BlockPos> previous = active;

        // Any cobweb whose state flipped (became occupied, or just emptied out)
        // needs its chunk section redrawn, otherwise the new color won't show
        // up until that chunk happens to reload for some other reason.
        for (BlockPos pos : updated) {
            if (!previous.contains(pos)) {
                requestRerender(client, pos);
            }
        }
        for (BlockPos pos : previous) {
            if (!updated.contains(pos)) {
                requestRerender(client, pos);
            }
        }

        active = updated;
    }

    private static void requestRerender(MinecraftClient client, BlockPos pos) {
        // SETUP NOTE: this single call is the one piece of this mod most likely
        // to need a tiny tweak, since Mojang occasionally renames internal
        // rendering methods between versions. If "scheduleBlockRenders" shows
        // up red/underlined in IntelliJ, place your cursor on "worldRenderer."
        // and press Ctrl+Space - it'll show you the actual method available in
        // your exact build, which will do the same thing (mark this block's
        // chunk section as needing a redraw). Everything else in this file is
        // ordinary, version-stable Minecraft code.
        client.worldRenderer.scheduleBlockRenders(
                ChunkSectionPos.getSectionCoord(pos.getX()),
                ChunkSectionPos.getSectionCoord(pos.getY()),
                ChunkSectionPos.getSectionCoord(pos.getZ())
        );
    }
}
