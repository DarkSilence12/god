package bai.god.shen;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class ShenEventAdapter {

    private static long lastTimeOfDay = -1;
    private static boolean lastRaining = false;
    private static boolean lastThundering = false;
    private static final java.util.Map<java.util.UUID, net.minecraft.registry.RegistryKey<net.minecraft.world.World>> lastDimensionMap = new java.util.HashMap<>();

    public static void initialize() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient() && player instanceof ServerPlayerEntity) {
                ShenEventBus.fire(ShenEventType.PLAYER_RIGHT_CLICK,
                    (LivingEntity) player, player.getBlockPos(), world);
                if (entity instanceof LivingEntity livingEntity) {
                    ShenEventBus.fire(new ShenEvent(ShenEventType.ENTITY_RIGHT_CLICK,
                        (LivingEntity) player, livingEntity, entity.getBlockPos(), world));
                }
            }
            return ActionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient() && player instanceof ServerPlayerEntity) {
                ShenEventBus.fire(ShenEventType.PLAYER_RIGHT_CLICK,
                    (LivingEntity) player, hitResult.getBlockPos(), world);
                ShenEventBus.fire(ShenEventType.BLOCK_RIGHT_CLICK,
                    (LivingEntity) player, hitResult.getBlockPos(), world);
            }
            return ActionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient() && player instanceof ServerPlayerEntity && entity instanceof LivingEntity livingTarget) {
                ShenEventBus.fire(new ShenEvent(ShenEventType.PLAYER_ATTACK,
                    (LivingEntity) player, livingTarget, entity.getBlockPos(), world));
            }
            return ActionResult.PASS;
        });

        net.fabricmc.fabric.api.message.v1.ServerMessageEvents.CHAT_MESSAGE.register(
            (message, sender, params) -> {
                String chatText = message.getContent().getString();
                ShenEventBus.fire(new ShenEvent(ShenEventType.PLAYER_CHAT,
                    sender, null, sender.getBlockPos(), sender.getWorld(), chatText));
            });

        net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient() && player instanceof ServerPlayerEntity) {
                if (player.getStackInHand(hand).isFood()) {
                    ShenEventBus.fire(ShenEventType.PLAYER_EAT,
                        (LivingEntity) player, player.getBlockPos(), world);
                }
            }
            return net.minecraft.util.TypedActionResult.pass(player.getStackInHand(hand));
        });

        net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient() && player instanceof ServerPlayerEntity) {
                ShenEventBus.fire(ShenEventType.ITEM_USE,
                    (LivingEntity) player, player.getBlockPos(), world);
            }
            return net.minecraft.util.TypedActionResult.pass(player.getStackInHand(hand));
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player && !entity.getWorld().isClient()) {
                ShenEventBus.fire(ShenEventType.PLAYER_HURT, player, player.getBlockPos(), player.getWorld());
            }
            return true;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayerEntity player && !entity.getWorld().isClient()) {
                ShenEventBus.fire(ShenEventType.PLAYER_DEATH, player, player.getBlockPos(), player.getWorld());
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            ShenEventBus.fire(ShenEventType.PLAYER_RESPAWN,
                newPlayer, newPlayer.getBlockPos(), newPlayer.getWorld());
        });

        EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
            if (entity instanceof ServerPlayerEntity player && !entity.getWorld().isClient()) {
                ShenEventBus.fire(ShenEventType.PLAYER_BED_ENTER,
                    player, sleepingPos, player.getWorld());
            }
        });

        EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
            if (entity instanceof ServerPlayerEntity player && !entity.getWorld().isClient()) {
                BlockPos wakePos = player.getBlockPos();
                ShenEventBus.fire(ShenEventType.PLAYER_BED_LEAVE,
                    player, wakePos, player.getWorld());
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ShenEventBus.fire(ShenEventType.PLAYER_JOIN_WORLD,
                handler.player, handler.player.getBlockPos(), handler.player.getWorld());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ShenEventBus.fire(ShenEventType.PLAYER_LEAVE_WORLD,
                handler.player, handler.player.getBlockPos(), handler.player.getWorld());
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof ServerPlayerEntity) && !entity.getWorld().isClient()) {
                ShenEventBus.fire(ShenEventType.ENTITY_DEATH,
                    entity, entity.getBlockPos(), entity.getWorld());
            }
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayerEntity) && !entity.getWorld().isClient()) {
                ShenEventBus.fire(ShenEventType.ENTITY_HURT,
                    entity, entity.getBlockPos(), entity.getWorld());
            }
            return true;
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            ShenEventBus.fire(ShenEventType.BLOCK_BREAK,
                (LivingEntity) player, pos, world);
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient() && player instanceof ServerPlayerEntity) {
                ShenEventBus.fire(ShenEventType.BLOCK_INTERACT,
                    (LivingEntity) player, hitResult.getBlockPos(), world);
            }
            return ActionResult.PASS;
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof ServerPlayerEntity) && entity instanceof LivingEntity && !world.isClient()) {
                ShenEventBus.fire(ShenEventType.ENTITY_SPAWN,
                    (LivingEntity) entity, entity.getBlockPos(), world);
            }
        });

        // 方块放置的专用事件在 Fabric API 中没有直接支持，这里通过右键方块 + 手持方块物品来近似
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient() && player instanceof ServerPlayerEntity) {
                if (player.getStackInHand(hand).getItem() instanceof net.minecraft.item.BlockItem) {
                    BlockPos placedPos = hitResult.getBlockPos().offset(hitResult.getSide());
                    ShenEventBus.fire(ShenEventType.BLOCK_PLACE,
                        (LivingEntity) player, placedPos, world);
                }
            }
            return ActionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ShenDelayManager.tick();

            if (server.getTicks() % 20 == 0) {
                for (net.minecraft.server.world.ServerWorld world : server.getWorlds()) {
                    long currentTime = world.getTimeOfDay();
                    long timeDiff = currentTime - lastTimeOfDay;
                    if (lastTimeOfDay != -1 && Math.abs(timeDiff) >= 100) {
                        ServerPlayerEntity nearestPlayer = null;
                        double nearestDist = Double.MAX_VALUE;
                        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                            if (p.getWorld() == world) {
                                double dist = p.getBlockPos().getSquaredDistance(BlockPos.ORIGIN);
                                if (dist < nearestDist) {
                                    nearestDist = dist;
                                    nearestPlayer = p;
                                }
                            }
                        }
                        if (nearestPlayer != null) {
                            ShenEventBus.fire(ShenEventType.WORLD_TIME_CHANGE,
                                nearestPlayer, nearestPlayer.getBlockPos(), world);
                        }
                    }
                    lastTimeOfDay = currentTime;

                    boolean raining = world.isRaining();
                    boolean thundering = world.isThundering();
                    if ((raining != lastRaining || thundering != lastThundering) && lastRaining) {
                        ServerPlayerEntity nearestPlayer = null;
                        double nearestDist = Double.MAX_VALUE;
                        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                            if (p.getWorld() == world) {
                                double dist = p.getBlockPos().getSquaredDistance(BlockPos.ORIGIN);
                                if (dist < nearestDist) {
                                    nearestDist = dist;
                                    nearestPlayer = p;
                                }
                            }
                        }
                        if (nearestPlayer != null) {
                            ShenEventBus.fire(ShenEventType.WORLD_WEATHER_CHANGE,
                                nearestPlayer, nearestPlayer.getBlockPos(), world);
                        }
                    }
                    lastRaining = raining;
                    lastThundering = thundering;
                }

                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    net.minecraft.registry.RegistryKey<net.minecraft.world.World> currentDim = player.getWorld().getRegistryKey();
                    net.minecraft.registry.RegistryKey<net.minecraft.world.World> lastDim = lastDimensionMap.get(player.getUuid());
                    if (lastDim != null && lastDim != currentDim) {
                        ShenEventBus.fire(ShenEventType.PLAYER_DIMENSION_CHANGE,
                            player, player.getBlockPos(), player.getWorld());
                    }
                    lastDimensionMap.put(player.getUuid(), currentDim);
                }

                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    net.minecraft.entity.ItemEntity nearestItem = null;
                    double nearestDist = 2.0;
                    for (net.minecraft.entity.Entity e : player.getWorld().getOtherEntities(player,
                            player.getBoundingBox().expand(2.0))) {
                        if (e instanceof net.minecraft.entity.ItemEntity item && e.isAlive()) {
                            double dist = e.distanceTo(player);
                            if (dist < nearestDist) {
                                nearestDist = dist;
                                nearestItem = item;
                            }
                        }
                    }
                    if (nearestItem != null) {
                        ShenEventBus.fire(ShenEventType.PLAYER_ITEM_PICKUP,
                            player, player.getBlockPos(), player.getWorld());
                    }
                }
            }
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                boolean isSneaking = player.isSneaking();
                Boolean wasSneaking = sneakState.get(player.getUuid());
                if (wasSneaking != null && isSneaking != wasSneaking && isSneaking) {
                    ShenEventBus.fire(ShenEventType.PLAYER_SNEAK,
                        player, player.getBlockPos(), player.getWorld());
                }
                sneakState.put(player.getUuid(), isSneaking);

                boolean isOnGround = player.isOnGround();
                Boolean wasOnGround = groundState.get(player.getUuid());
                if (wasOnGround != null && wasOnGround && !isOnGround) {
                    if (player.getVelocity().y > 0) {
                        ShenEventBus.fire(ShenEventType.PLAYER_JUMP,
                            player, player.getBlockPos(), player.getWorld());
                    }
                }
                groundState.put(player.getUuid(), isOnGround);

                if (server.getTicks() % 20 == 0) {
                    bai.god.shen.compat.ModAdapterManager.checkEntityNbtChanges(player);
                }
            }
        });
    }

    private static final java.util.Map<java.util.UUID, Boolean> sneakState = new java.util.HashMap<>();
    private static final java.util.Map<java.util.UUID, Boolean> groundState = new java.util.HashMap<>();
}