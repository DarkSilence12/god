package bai.god.shen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShenEventAdapter {

    private static boolean registered = false;

    private static long lastTimeOfDay = -1;
    private static boolean lastRaining = false;
    private static boolean lastThundering = false;
    private static final Map<UUID, ResourceKey<Level>> lastDimensionMap = new HashMap<>();
    private static final Map<UUID, Boolean> sneakState = new HashMap<>();
    private static final Map<UUID, Boolean> groundState = new HashMap<>();

    public static void initialize() {
        if (registered) return;
        registered = true;
        MinecraftForge.EVENT_BUS.register(new ShenEventAdapter());
    }

    // ============ 实体右键 ============
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Level world = event.getLevel();
        if (world.isClientSide() || !(player instanceof ServerPlayer)) return;
        Entity entity = event.getTarget();
        ShenEventBus.fire(ShenEventType.PLAYER_RIGHT_CLICK,
            player, player.blockPosition(), world);
        if (entity instanceof LivingEntity livingEntity) {
            ShenEventBus.fire(new ShenEvent(ShenEventType.ENTITY_RIGHT_CLICK,
                player, livingEntity, entity.blockPosition(), world));
        }
    }

    // ============ 右键方块 ============
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level world = event.getLevel();
        if (world.isClientSide() || !(player instanceof ServerPlayer)) return;
        BlockPos pos = event.getPos();
        ShenEventBus.fire(ShenEventType.PLAYER_RIGHT_CLICK, player, pos, world);
        ShenEventBus.fire(ShenEventType.BLOCK_RIGHT_CLICK, player, pos, world);
        ShenEventBus.fire(ShenEventType.BLOCK_INTERACT, player, pos, world);

        if (event.getItemStack().getItem() instanceof BlockItem) {
            BlockPos placedPos = pos.relative(event.getFace());
            ShenEventBus.fire(ShenEventType.BLOCK_PLACE, player, placedPos, world);
        }
    }

    // ============ 攻击实体 ============
    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !(player instanceof ServerPlayer)) return;
        Entity entity = event.getTarget();
        if (entity instanceof LivingEntity livingTarget) {
            ShenEventBus.fire(new ShenEvent(ShenEventType.PLAYER_ATTACK,
                player, livingTarget, entity.blockPosition(), player.level()));
        }
    }

    // ============ 聊天 ============
    @SubscribeEvent
    public void onChat(ServerChatEvent event) {
        ServerPlayer sender = event.getPlayer();
        if (sender == null) return;
        String chatText = event.getMessage().getString();
        ShenEventBus.fire(new ShenEvent(ShenEventType.PLAYER_CHAT,
            sender, null, sender.blockPosition(), sender.level(), chatText));
    }

    // ============ 使用物品 / 进食 ============
    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        Level world = event.getLevel();
        if (world.isClientSide() || !(player instanceof ServerPlayer)) return;
        if (event.getItemStack().isEdible()) {
            ShenEventBus.fire(ShenEventType.PLAYER_EAT, player, player.blockPosition(), world);
        }
        ShenEventBus.fire(ShenEventType.ITEM_USE, player, player.blockPosition(), world);
    }

    // ============ 受伤 ============
    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (entity instanceof ServerPlayer player) {
            ShenEventBus.fire(ShenEventType.PLAYER_HURT, player, player.blockPosition(), player.level());
        } else {
            ShenEventBus.fire(ShenEventType.ENTITY_HURT, entity, entity.blockPosition(), entity.level());
        }
    }

    // ============ 死亡 ============
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (entity instanceof ServerPlayer player) {
            ShenEventBus.fire(ShenEventType.PLAYER_DEATH, player, player.blockPosition(), player.level());
        } else {
            ShenEventBus.fire(ShenEventType.ENTITY_DEATH, entity, entity.blockPosition(), entity.level());
        }
    }

    // ============ 无敌：免疫一切伤害（任意形式，包括指令杀 / 死亡事件） ============
    // 关键：LivingAttackEvent 是 hurt() 流程最开头的钩子（LivingEntity.hurt 与 Player.hurt 都先触发它）。
    // 在这里取消后整个伤害流程直接终止——hurtTime 红闪计时、伤害屏幕指示、击退全部不会发生，
    // 受击者完全感受不到被打中（无身体发红、无视角摇晃）。
    @SubscribeEvent
    public void onInvincibleAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (ShenEffectRegistry.isInvincible(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onInvincibleHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (ShenEffectRegistry.isInvincible(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onInvincibleDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (ShenEffectRegistry.isInvincible(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onInvincibleDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (ShenEffectRegistry.isInvincible(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    // 免疫击退：被攻击时不会后退/失衡，如同没有被打中
    @SubscribeEvent
    public void onInvincibleKnockBack(LivingKnockBackEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (ShenEffectRegistry.isInvincible(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    // 弹射物弹开：箭/雪球/火球等命中无敌实体时折返弹飞，不附着、不造成任何效果
    @SubscribeEvent
    public void onInvincibleProjectile(ProjectileImpactEvent event) {
        HitResult result = event.getRayTraceResult();
        if (!(result instanceof EntityHitResult entityHit)) return;
        Entity hit = entityHit.getEntity();
        if (hit.level().isClientSide()) return;
        if (!ShenEffectRegistry.isInvincible(hit)) return;
        // 反转弹射物运动方向（略微加速），使其被弹开折返
        event.getProjectile().setDeltaMovement(
            event.getProjectile().getDeltaMovement().scale(-1.1));
        event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
    }

    // ============ 重生 ============
    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ShenEventBus.fire(ShenEventType.PLAYER_RESPAWN, player, player.blockPosition(), player.level());
        }
    }

    // ============ 上床 / 起床 ============
    @SubscribeEvent
    public void onSleepInBed(PlayerSleepInBedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !(player instanceof ServerPlayer)) return;
        ShenEventBus.fire(ShenEventType.PLAYER_BED_ENTER, player, event.getPos(), player.level());
    }

    @SubscribeEvent
    public void onWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !(player instanceof ServerPlayer)) return;
        ShenEventBus.fire(ShenEventType.PLAYER_BED_LEAVE, player, player.blockPosition(), player.level());
    }

    // ============ 进入 / 离开世界 ============
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ShenEventBus.fire(ShenEventType.PLAYER_JOIN_WORLD, player, player.blockPosition(), player.level());
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ShenEventBus.fire(ShenEventType.PLAYER_LEAVE_WORLD, player, player.blockPosition(), player.level());
        }
    }

    // ============ 方块被破坏 ============
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player != null && player.level().isClientSide()) return;
        {
            ShenEventBus.fire(ShenEventType.BLOCK_BREAK, player, event.getPos(), (Level) event.getLevel());
        }
    }

    // ============ 实体生成（加载到世界） ============
    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (event.getLevel().isClientSide()) return;
        if (!(entity instanceof ServerPlayer) && entity instanceof LivingEntity) {
            ShenEventBus.fire(ShenEventType.ENTITY_SPAWN,
                (LivingEntity) entity, entity.blockPosition(), event.getLevel());
        }
    }

    // ============ 服务端 tick ============
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var server = event.getServer();
        if (server == null) return;

        ShenDelayManager.tick();

        if (server.getTickCount() % 20 == 0) {
            for (ServerLevel world : server.getAllLevels()) {
                long currentTime = world.getDayTime();
                long timeDiff = currentTime - lastTimeOfDay;
                if (lastTimeOfDay != -1 && Math.abs(timeDiff) >= 100) {
                    ServerPlayer nearestPlayer = null;
                    double nearestDist = Double.MAX_VALUE;
                    for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                        if (p.level() == world) {
                            double dist = p.blockPosition().distSqr(BlockPos.ZERO);
                            if (dist < nearestDist) {
                                nearestDist = dist;
                                nearestPlayer = p;
                            }
                        }
                    }
                    if (nearestPlayer != null) {
                        ShenEventBus.fire(ShenEventType.WORLD_TIME_CHANGE,
                            nearestPlayer, nearestPlayer.blockPosition(), world);
                    }
                }
                lastTimeOfDay = currentTime;

                boolean raining = world.isRaining();
                boolean thundering = world.isThundering();
                if ((raining != lastRaining || thundering != lastThundering) && lastRaining) {
                    ServerPlayer nearestPlayer = null;
                    double nearestDist = Double.MAX_VALUE;
                    for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                        if (p.level() == world) {
                            double dist = p.blockPosition().distSqr(BlockPos.ZERO);
                            if (dist < nearestDist) {
                                nearestDist = dist;
                                nearestPlayer = p;
                            }
                        }
                    }
                    if (nearestPlayer != null) {
                        ShenEventBus.fire(ShenEventType.WORLD_WEATHER_CHANGE,
                            nearestPlayer, nearestPlayer.blockPosition(), world);
                    }
                }
                lastRaining = raining;
                lastThundering = thundering;
            }

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ResourceKey<Level> currentDim = player.level().dimension();
                ResourceKey<Level> lastDim = lastDimensionMap.get(player.getUUID());
                if (lastDim != null && !lastDim.equals(currentDim)) {
                    ShenEventBus.fire(ShenEventType.PLAYER_DIMENSION_CHANGE,
                        player, player.blockPosition(), player.level());
                }
                lastDimensionMap.put(player.getUUID(), currentDim);
            }

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ItemEntity nearestItem = null;
                double nearestDist = 2.0;
                for (Entity e : player.level().getEntities(player,
                        player.getBoundingBox().inflate(2.0))) {
                    if (e instanceof ItemEntity item && e.isAlive()) {
                        double dist = e.distanceTo(player);
                        if (dist < nearestDist) {
                            nearestDist = dist;
                            nearestItem = item;
                        }
                    }
                }
                if (nearestItem != null) {
                    ShenEventBus.fire(ShenEventType.PLAYER_ITEM_PICKUP,
                        player, player.blockPosition(), player.level());
                }
            }
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            boolean isSneaking = player.isShiftKeyDown();
            Boolean wasSneaking = sneakState.get(player.getUUID());
            if (wasSneaking != null && isSneaking != wasSneaking && isSneaking) {
                ShenEventBus.fire(ShenEventType.PLAYER_SNEAK,
                    player, player.blockPosition(), player.level());
            }
            sneakState.put(player.getUUID(), isSneaking);

            boolean isOnGround = player.onGround();
            Boolean wasOnGround = groundState.get(player.getUUID());
            if (wasOnGround != null && wasOnGround && !isOnGround) {
                if (player.getDeltaMovement().y > 0) {
                    ShenEventBus.fire(ShenEventType.PLAYER_JUMP,
                        player, player.blockPosition(), player.level());
                }
            }
            groundState.put(player.getUUID(), isOnGround);

            if (server.getTickCount() % 20 == 0) {
                bai.god.shen.compat.ModAdapterManager.checkEntityNbtChanges(player);
            }
        }
    }
}
