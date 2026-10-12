package bai.god.shen;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import bai.god.support.ForceTeleportHelper;
import bai.god.support.smfs.SmfsAPIWrapper;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import net.minecraft.world.GameRules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ShenEffectRegistry {
    private static final Map<ShenEffectType, BiConsumer<ShenEvent, List<LivingEntity>>> EXECUTORS = new HashMap<>();

    static {
        EXECUTORS.put(ShenEffectType.MODIFY_HEALTH, ShenEffectRegistry::executeModifyHealth);
        EXECUTORS.put(ShenEffectType.DEAL_DAMAGE, ShenEffectRegistry::executeDealDamage);
        EXECUTORS.put(ShenEffectType.APPLY_EFFECT, ShenEffectRegistry::executeApplyEffect);
        EXECUTORS.put(ShenEffectType.CLEAR_EFFECT, ShenEffectRegistry::executeClearEffect);
        EXECUTORS.put(ShenEffectType.MODIFY_NBT, ShenEffectRegistry::executeModifyNbt);
        EXECUTORS.put(ShenEffectType.MODIFY_AI, ShenEffectRegistry::executeModifyAi);
        EXECUTORS.put(ShenEffectType.KILL_ENTITY, ShenEffectRegistry::executeKillEntity);
        EXECUTORS.put(ShenEffectType.SUMMON_ENTITY, ShenEffectRegistry::executeSummonEntity);
        EXECUTORS.put(ShenEffectType.TELEPORT, ShenEffectRegistry::executeTeleport);
        EXECUTORS.put(ShenEffectType.SET_FIRE, ShenEffectRegistry::executeSetFire);
        EXECUTORS.put(ShenEffectType.EXPLOSION, ShenEffectRegistry::executeExplosion);
        EXECUTORS.put(ShenEffectType.GIVE_ITEM, ShenEffectRegistry::executeGiveItem);
        EXECUTORS.put(ShenEffectType.REMOVE_ITEM, ShenEffectRegistry::executeRemoveItem);
        EXECUTORS.put(ShenEffectType.MODIFY_ITEM_NBT, ShenEffectRegistry::executeModifyItemNbt);
        EXECUTORS.put(ShenEffectType.COPY_ITEM, ShenEffectRegistry::executeCopyItem);
        EXECUTORS.put(ShenEffectType.PLACE_BLOCK, ShenEffectRegistry::executePlaceBlock);
        EXECUTORS.put(ShenEffectType.BREAK_BLOCK, ShenEffectRegistry::executeBreakBlock);
        EXECUTORS.put(ShenEffectType.MODIFY_BLOCK_STATE, ShenEffectRegistry::executeModifyBlockState);
        EXECUTORS.put(ShenEffectType.MODIFY_TIME, ShenEffectRegistry::executeModifyTime);
        EXECUTORS.put(ShenEffectType.MODIFY_WEATHER, ShenEffectRegistry::executeModifyWeather);
        EXECUTORS.put(ShenEffectType.MODIFY_GAME_RULE, ShenEffectRegistry::executeModifyGameRule);
        EXECUTORS.put(ShenEffectType.SPAWN_PARTICLE, ShenEffectRegistry::executeSpawnParticle);
        EXECUTORS.put(ShenEffectType.PLAY_SOUND, ShenEffectRegistry::executePlaySound);
        EXECUTORS.put(ShenEffectType.SEND_MESSAGE, ShenEffectRegistry::executeSendMessage);
        EXECUTORS.put(ShenEffectType.EXECUTE_COMMAND, ShenEffectRegistry::executeCommand);
        EXECUTORS.put(ShenEffectType.REPLACE_ITEM, ShenEffectRegistry::executeReplaceItem);
        EXECUTORS.put(ShenEffectType.ENCHANT_ITEM, ShenEffectRegistry::executeEnchantItem);
        EXECUTORS.put(ShenEffectType.SPAWN_EXPERIENCE, ShenEffectRegistry::executeSpawnExperience);
        EXECUTORS.put(ShenEffectType.RUN_OTHER_RULE, ShenEffectRegistry::executeRunOtherRule);
        EXECUTORS.put(ShenEffectType.SET_VARIABLE, ShenEffectRegistry::executeSetVariable);
        EXECUTORS.put(ShenEffectType.MODIFY_VARIABLE, ShenEffectRegistry::executeModifyVariable);
        EXECUTORS.put(ShenEffectType.SET_GAMEMODE, ShenEffectRegistry::executeSetGamemode);
        EXECUTORS.put(ShenEffectType.CLEAR_INVENTORY, ShenEffectRegistry::executeClearInventory);
        EXECUTORS.put(ShenEffectType.CLEAR_ITEM, ShenEffectRegistry::executeClearItem);
        EXECUTORS.put(ShenEffectType.LOOP, ShenEffectRegistry::executeLoop);
        EXECUTORS.put(ShenEffectType.SET_FLYING, ShenEffectRegistry::executeSetFlying);
        EXECUTORS.put(ShenEffectType.SET_WALK_SPEED, ShenEffectRegistry::executeSetWalkSpeed);
        EXECUTORS.put(ShenEffectType.SET_FLY_SPEED, ShenEffectRegistry::executeSetFlySpeed);
        EXECUTORS.put(ShenEffectType.MODIFY_MAX_HEALTH, ShenEffectRegistry::executeModifyMaxHealth);
        EXECUTORS.put(ShenEffectType.REPAIR_ITEM, ShenEffectRegistry::executeRepairItem);
        EXECUTORS.put(ShenEffectType.SPAWN_LIGHTNING, ShenEffectRegistry::executeSpawnLightning);
        EXECUTORS.put(ShenEffectType.SET_INVISIBLE, ShenEffectRegistry::executeSetInvisible);
        EXECUTORS.put(ShenEffectType.SATURATE_FOOD, ShenEffectRegistry::executeSaturateFood);
        EXECUTORS.put(ShenEffectType.SET_STEP_HEIGHT, ShenEffectRegistry::executeSetStepHeight);
        EXECUTORS.put(ShenEffectType.SET_RESISTANCE, ShenEffectRegistry::executeSetResistance);
        EXECUTORS.put(ShenEffectType.SWAP_HAND_ITEMS, ShenEffectRegistry::executeSwapHandItems);
        EXECUTORS.put(ShenEffectType.RANDOM_TELEPORT, ShenEffectRegistry::executeRandomTeleport);
        EXECUTORS.put(ShenEffectType.SET_AIR, ShenEffectRegistry::executeSetAir);
        EXECUTORS.put(ShenEffectType.MODIFY_ATTRIBUTE, ShenEffectRegistry::executeModifyAttribute);
        EXECUTORS.put(ShenEffectType.FREEZE_ENTITY, ShenEffectRegistry::executeFreezeEntity);
        EXECUTORS.put(ShenEffectType.PLACE_STRUCTURE, ShenEffectRegistry::executePlaceStructure);
        EXECUTORS.put(ShenEffectType.MASS_REPLACE_BLOCKS, ShenEffectRegistry::executeMassReplaceBlocks);
        EXECUTORS.put(ShenEffectType.MOD_ADAPTER_EFFECT, ShenEffectRegistry::executeModAdapterEffect);

        EXECUTORS.put(ShenEffectType.SMFS_SET_SPIRIT_RESISTANCE, ShenEffectRegistry::executeSmfsSetSpiritResistance);
        EXECUTORS.put(ShenEffectType.SMFS_SET_SPIRIT_DAMAGE, ShenEffectRegistry::executeSmfsSetSpiritDamage);
        EXECUTORS.put(ShenEffectType.SMFS_SET_CURRENT_SPIRIT, ShenEffectRegistry::executeSmfsSetCurrentSpirit);
        EXECUTORS.put(ShenEffectType.SMFS_SET_MAX_SPIRIT, ShenEffectRegistry::executeSmfsSetMaxSpirit);
        EXECUTORS.put(ShenEffectType.SMFS_SET_SANITY, ShenEffectRegistry::executeSmfsSetSanity);
        EXECUTORS.put(ShenEffectType.SMFS_SET_REVIVAL_FACTOR, ShenEffectRegistry::executeSmfsSetRevivalFactor);
        EXECUTORS.put(ShenEffectType.SMFS_SET_MAIN_GHOST_SLOT, ShenEffectRegistry::executeSmfsSetMainGhostSlot);
        EXECUTORS.put(ShenEffectType.SMFS_CYCLE_MAIN_GHOST, ShenEffectRegistry::executeSmfsCycleMainGhost);
        EXECUTORS.put(ShenEffectType.SMFS_SET_NBT_ATTRIBUTE, ShenEffectRegistry::executeSmfsSetNbtAttribute);

        EXECUTORS.put(ShenEffectType.SMFS_ACTIVATE_DOMAIN, ShenEffectRegistry::executeSmfsActivateDomain);
        EXECUTORS.put(ShenEffectType.SMFS_SET_DOMAIN_TYPE, ShenEffectRegistry::executeSmfsSetDomainType);
        EXECUTORS.put(ShenEffectType.SMFS_SET_DOMAIN_LEVEL, ShenEffectRegistry::executeSmfsSetDomainLevel);

        EXECUTORS.put(ShenEffectType.SMFS_SET_GHOST_EYE_LEVEL, ShenEffectRegistry::executeSmfsSetGhostEyeLevel);
        EXECUTORS.put(ShenEffectType.SMFS_TOGGLE_GHOST_EYE, ShenEffectRegistry::executeSmfsToggleGhostEye);

        EXECUTORS.put(ShenEffectType.SMFS_LOCK_SLOT, ShenEffectRegistry::executeSmfsLockSlot);
        EXECUTORS.put(ShenEffectType.SMFS_CLEAR_SLOT, ShenEffectRegistry::executeSmfsClearSlot);
        EXECUTORS.put(ShenEffectType.SMFS_SWAP_SLOTS, ShenEffectRegistry::executeSmfsSwapSlots);
        EXECUTORS.put(ShenEffectType.SMFS_SET_SLOT_GHOST, ShenEffectRegistry::executeSmfsSetSlotGhost);
    }

    public static void execute(ShenEffectType type, ShenEvent event, List<LivingEntity> targets, ShenRule rule) {
        BiConsumer<ShenEvent, List<LivingEntity>> executor = EXECUTORS.get(type);
        if (executor != null) {
            event.rule = rule;
            executor.accept(event, targets);
            event.rule = null;
        }
    }

    public static void execute(ShenRule.ShenEffectEntry entry, ShenEvent event, List<LivingEntity> targets) {
        BiConsumer<ShenEvent, List<LivingEntity>> executor = EXECUTORS.get(entry.effectType);
        if (executor != null) {
            // 创建临时规则，使用效果步骤的参数
            ShenRule tempRule = new ShenRule();
            tempRule.params = entry.params;
            event.rule = tempRule;
            executor.accept(event, targets);
            event.rule = null;
        }
    }

    private static void executeModifyHealth(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        String targetType = rule.getParam("target", "current");
        for (LivingEntity target : targets) {
            float amount = (float) rule.getParamDouble("amount", target.getMaxHealth());
            boolean percent = rule.getParamBool("percent", false);
            float finalAmount = percent ? target.getMaxHealth() * amount / 100f : amount;

            if ("max".equals(targetType)) {
                net.minecraft.entity.attribute.EntityAttribute maxHealthAttr =
                    net.minecraft.registry.Registries.ATTRIBUTE.get(
                        net.minecraft.util.Identifier.tryParse("minecraft:generic.max_health"));
                if (maxHealthAttr != null) {
                    net.minecraft.entity.attribute.EntityAttributeInstance attr = target.getAttributeInstance(maxHealthAttr);
                    if (attr != null) {
                        float clamped = Math.max(0, Math.min(finalAmount, 100000));
                        attr.setBaseValue(clamped);
                        if (target.getHealth() > target.getMaxHealth()) {
                            target.setHealth(target.getMaxHealth());
                        }
                    }
                }
            } else {
                target.setHealth(Math.max(0, Math.min(target.getMaxHealth(), finalAmount)));
            }
        }
    }

    private static void executeDealDamage(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        for (LivingEntity target : targets) {
            float amount = (float) rule.getParamDouble("amount", 10.0);
            String damageTypeId = rule.getParam("damageType", "minecraft:generic");
            Identifier dtId = Identifier.tryParse(damageTypeId);
            if (dtId == null) dtId = Identifier.of("minecraft", "generic");

            if ("smfs:ghost".equals(dtId.toString())) {
                LivingEntity attacker = event.source != null ? event.source : target;
                SmfsAPIWrapper.dealSpiritDamage(target, amount, attacker);
                continue;
            }

            try {
                RegistryKey<DamageType> dtKey = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, dtId);
                var registryEntry = ((net.minecraft.server.world.ServerWorld)target.getWorld())
                    .getRegistryManager()
                    .get(RegistryKeys.DAMAGE_TYPE)
                    .entryOf(dtKey);
                DamageSource source = new DamageSource(registryEntry, event.source, event.source);
                target.damage(source, amount);
            } catch (Exception e) {
                // 安全兜底 — 如果是灵异伤害兜底也走灵异通道
                if ("smfs:ghost".equals(dtId.toString())) {
                    LivingEntity attacker = event.source != null ? event.source : target;
                    SmfsAPIWrapper.dealSpiritDamage(target, amount, attacker);
                    continue;
                }
                DamageSource fallback = (event.source != null)
                    ? target.getDamageSources().mobAttack(event.source)
                    : target.getDamageSources().generic();
                target.damage(fallback, amount);
            }
        }
    }

    private static void executeApplyEffect(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        String effectId = rule.getParam("effect", "minecraft:glowing");
        int duration = rule.getParamInt("duration", 200);
        int amplifier = rule.getParamInt("amplifier", 0);
        boolean showParticles = rule.getParamBool("showParticles", true);

        StatusEffect effect = Registries.STATUS_EFFECT.get(new Identifier(effectId));
        if (effect == null) {
            effect = Registries.STATUS_EFFECT.get(new Identifier("minecraft:glowing"));
        }
        for (LivingEntity target : targets) {
            target.addStatusEffect(new StatusEffectInstance(effect, duration, amplifier, false, showParticles));
        }
    }

    private static void executeClearEffect(ShenEvent event, List<LivingEntity> targets) {
        for (LivingEntity target : targets) {
            target.clearStatusEffects();
        }
    }

    private static void executeModifyNbt(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        String nbtMode = rule.getParam("nbtMode", "modify");
        String nbtKey = rule.getParam("nbtKey", "");
        String nbtValue = rule.getParam("nbtValue", "");
        String nbtType = rule.getParam("nbtType", "string");

        for (LivingEntity target : targets) {
            NbtCompound nbt = new NbtCompound();
            target.writeNbt(nbt);

            if (!nbtKey.isEmpty()) {
                if ("delete".equals(nbtMode)) {
                    nbt.remove(nbtKey);
                } else {
                    switch (nbtType) {
                        case "int":
                            try { nbt.putInt(nbtKey, Integer.parseInt(nbtValue)); } catch (NumberFormatException ignored) {}
                            break;
                        case "double":
                            try { nbt.putDouble(nbtKey, Double.parseDouble(nbtValue)); } catch (NumberFormatException ignored) {}
                            break;
                        case "boolean":
                            nbt.putBoolean(nbtKey, "true".equalsIgnoreCase(nbtValue));
                            break;
                        case "compound":
                            try {
                                NbtCompound parsed = StringNbtReader.parse(nbtValue);
                                nbt.put(nbtKey, parsed);
                            } catch (Exception ignored) {}
                            break;
                        default:
                            nbt.putString(nbtKey, nbtValue);
                            break;
                    }
                }
            }
            target.readNbt(nbt);
        }
    }

    private static void executeModifyAi(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        boolean setAi = rule.getParamBool("ai", false);
        for (LivingEntity target : targets) {
            NbtCompound nbt = new NbtCompound();
            target.writeNbt(nbt);
            nbt.putBoolean("NoAI", !setAi);
            target.readNbt(nbt);
        }
    }

    private static void executeKillEntity(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        boolean discard = rule.getParamBool("discard", false);
        for (LivingEntity target : targets) {
            if (discard) {
                target.discard();
            } else {
                target.kill();
            }
        }
    }

    private static void executeSummonEntity(ShenEvent event, List<LivingEntity> targets) {
        if (!(event.world instanceof ServerWorld serverWorld)) return;
        ShenRule rule = event.rule;
        String entityTypeId = rule.getParam("entityType", "minecraft:lightning_bolt");
        int count = rule.getParamInt("count", 1);

        EntityType<?> entityType = Registries.ENTITY_TYPE.get(new Identifier(entityTypeId));
        if (entityType == null) return;

        for (LivingEntity target : targets) {
            Vec3d centerPos = target.getPos();
            for (int i = 0; i < count; i++) {
                Entity entity = entityType.create(serverWorld);
                if (entity == null) continue;
                double offsetX = (serverWorld.random.nextDouble() - 0.5) * 2;
                double offsetZ = (serverWorld.random.nextDouble() - 0.5) * 2;
                entity.setPos(centerPos.x + offsetX, centerPos.y, centerPos.z + offsetZ);
                serverWorld.spawnEntity(entity);
            }
        }
    }

    private static void executeTeleport(ShenEvent event, List<LivingEntity> targets) {
        if (!(event.world instanceof ServerWorld serverWorld)) return;
        ShenRule rule = event.rule;
        double x = rule.getParamDouble("x", 0);
        double y = rule.getParamDouble("y", 64);
        double z = rule.getParamDouble("z", 0);
        boolean relative = rule.getParamBool("relative", false);
        boolean switchDimension = rule.getParamBool("switchDimension", false);
        String dimensionId = rule.getParam("dimension", "minecraft:overworld");

        ServerWorld targetWorld = serverWorld;
        if (switchDimension && !dimensionId.isEmpty()) {
            net.minecraft.registry.RegistryKey<net.minecraft.world.World> dimKey =
                net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, new Identifier(dimensionId));
            net.minecraft.server.world.ServerWorld dimWorld = serverWorld.getServer().getWorld(dimKey);
            if (dimWorld != null) {
                targetWorld = dimWorld;
            }
        }

        for (LivingEntity target : targets) {
            double tx = relative ? target.getX() + x : x;
            double ty = relative ? target.getY() + y : y;
            double tz = relative ? target.getZ() + z : z;
            if (target instanceof ServerPlayerEntity player) {
                ForceTeleportHelper.setForceTeleport(true);
                player.teleport(targetWorld, tx, ty, tz, player.getYaw(), player.getPitch());
                ForceTeleportHelper.setForceTeleport(false);
            } else {
                target.teleport(tx, ty, tz);
            }
        }
    }

    private static void executeSetFire(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        for (LivingEntity target : targets) {
            int ticks = (int) rule.resolveNumber("ticks", 100, target.getFireTicks());
            target.setOnFireFor(ticks);
        }
    }

    private static void executeExplosion(ShenEvent event, List<LivingEntity> targets) {
        if (!(event.world instanceof ServerWorld serverWorld)) return;
        ShenRule rule = event.rule;
        float power = (float) rule.getParamDouble("power", 4.0);
        boolean destroyBlocks = rule.getParamBool("destroyBlocks", true);
        boolean createFire = rule.getParamBool("createFire", false);
        boolean atImpact = rule.getParamBool("atImpact", false);

        var explosionType = destroyBlocks
            ? net.minecraft.world.World.ExplosionSourceType.TNT
            : net.minecraft.world.World.ExplosionSourceType.NONE;

        if (atImpact) {
            BlockPos eventPos = event.pos;
            serverWorld.createExplosion(event.source, eventPos.getX() + 0.5, eventPos.getY(), eventPos.getZ() + 0.5,
                power, createFire, explosionType);
            return;
        }

        for (LivingEntity target : targets) {
            Vec3d pos = target.getPos();
            serverWorld.createExplosion(
                event.source, pos.x, pos.y, pos.z, power, createFire, explosionType);
        }
    }

    private static void executeGiveItem(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        String itemId = rule.getParam("item", "minecraft:diamond");
        int count = rule.getParamInt("count", 1);
        String nbtStr = rule.getParam("itemNbt", "");

        ItemStack stack = new ItemStack(Registries.ITEM.get(new Identifier(itemId)), count);
        if (!nbtStr.isEmpty()) {
            try {
                stack.setNbt(StringNbtReader.parse(nbtStr));
            } catch (Exception ignored) {}
        }

        for (LivingEntity target : targets) {
            if (target instanceof ServerPlayerEntity player) {
                player.getInventory().offerOrDrop(stack.copy());
            }
        }
    }

    private static void executeRemoveItem(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        String itemId = rule.getParam("item", "");
        int count = rule.getParamInt("count", 1);

        for (LivingEntity target : targets) {
            if (target instanceof ServerPlayerEntity player) {
                if (itemId.isEmpty()) {
                    player.getMainHandStack().decrement(count);
                } else {
                    net.minecraft.item.Item targetItem = Registries.ITEM.get(new Identifier(itemId));
                    if (targetItem != null) {
                        for (int i = 0; i < player.getInventory().size(); i++) {
                            ItemStack stack = player.getInventory().getStack(i);
                            if (stack.isOf(targetItem)) {
                                stack.decrement(count);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private static void executeModifyItemNbt(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        String nbtKey = rule.getParam("nbtKey", "");
        String nbtValue = rule.getParam("nbtValue", "");
        String nbtType = rule.getParam("nbtType", "string");

        for (LivingEntity target : targets) {
            if (target instanceof ServerPlayerEntity player) {
                ItemStack stack = player.getMainHandStack();
                if (!stack.isEmpty() && !nbtKey.isEmpty()) {
                    NbtCompound nbt = stack.getOrCreateNbt();
                    switch (nbtType) {
                        case "int":
                            try { nbt.putInt(nbtKey, Integer.parseInt(nbtValue)); } catch (NumberFormatException ignored) {}
                            break;
                        case "double":
                            try { nbt.putDouble(nbtKey, Double.parseDouble(nbtValue)); } catch (NumberFormatException ignored) {}
                            break;
                        case "boolean":
                            nbt.putBoolean(nbtKey, "true".equalsIgnoreCase(nbtValue));
                            break;
                        case "compound":
                            try { nbt.put(nbtKey, StringNbtReader.parse(nbtValue)); } catch (Exception ignored) {}
                            break;
                        default:
                            nbt.putString(nbtKey, nbtValue);
                            break;
                    }
                }
            }
        }
    }

    private static void executeCopyItem(ShenEvent event, List<LivingEntity> targets) {
        for (LivingEntity target : targets) {
            if (target instanceof ServerPlayerEntity player) {
                ItemStack stack = player.getMainHandStack().copy();
                if (!stack.isEmpty()) {
                    player.getInventory().offerOrDrop(stack);
                }
            }
        }
    }

    private static void executePlaceBlock(ShenEvent event, List<LivingEntity> targets) {
        if (event.world == null) return;
        ShenRule rule = event.rule;
        String blockId = rule.getParam("block", "minecraft:stone");

        net.minecraft.block.Block block = Registries.BLOCK.get(new Identifier(blockId));
        if (block == null) block = Registries.BLOCK.get(new Identifier("minecraft:stone"));

        if (event.pos != null) {
            event.world.setBlockState(event.pos, block.getDefaultState());
        }
    }

    private static void executeBreakBlock(ShenEvent event, List<LivingEntity> targets) {
        if (event.world != null && event.pos != null) {
            ShenRule rule = event.rule;
            boolean dropItems = rule.getParamBool("dropItems", true);
            event.world.breakBlock(event.pos, dropItems);
        }
    }

    private static void executeModifyBlockState(ShenEvent event, List<LivingEntity> targets) {
        if (event.world == null || event.pos == null) return;
        ShenRule rule = event.rule;
        String blockId = rule.getParam("block", "minecraft:stone");

        net.minecraft.block.Block block = Registries.BLOCK.get(new Identifier(blockId));
        if (block != null) {
            event.world.setBlockState(event.pos, block.getDefaultState());
        }
    }

    private static void executeModifyTime(ShenEvent event, List<LivingEntity> targets) {
        if (event.world instanceof ServerWorld serverWorld) {
            ShenRule rule = event.rule;
            long time = (long) rule.getParamDouble("time", 1000);
            boolean absolute = rule.getParamBool("absolute", false);
            if (absolute) {
                serverWorld.setTimeOfDay(time);
            } else {
                serverWorld.setTimeOfDay(serverWorld.getTimeOfDay() + time);
            }
        }
    }

    private static void executeModifyWeather(ShenEvent event, List<LivingEntity> targets) {
        if (event.world instanceof ServerWorld serverWorld) {
            ShenRule rule = event.rule;
            String weather = rule.getParam("weather", "clear");
            int duration = rule.getParamInt("duration", 6000);

            switch (weather) {
                case "rain":
                    serverWorld.setWeather(0, duration, true, false);
                    break;
                case "thunder":
                    serverWorld.setWeather(0, duration, true, true);
                    break;
                default:
                    serverWorld.setWeather(duration, 0, false, false);
                    break;
            }
        }
    }

    private static void executeModifyGameRule(ShenEvent event, List<LivingEntity> targets) {
        if (event.world instanceof ServerWorld serverWorld) {
            ShenRule rule = event.rule;
            String gameRuleName = rule.getParam("gameRule", "doDaylightCycle");
            boolean value = rule.getParamBool("value", true);

            GameRules gameRules = serverWorld.getGameRules();
            GameRules.accept(new GameRules.Visitor() {
                @Override
                public void visitInt(GameRules.Key<GameRules.IntRule> key, GameRules.Type<GameRules.IntRule> type) {
                    if (gameRuleName.equals(key.getName())) {
                        if (value) {
                            gameRules.get(key).set(1, serverWorld.getServer());
                        } else {
                            gameRules.get(key).set(0, serverWorld.getServer());
                        }
                    }
                }

                @Override
                public void visitBoolean(GameRules.Key<GameRules.BooleanRule> key, GameRules.Type<GameRules.BooleanRule> type) {
                    if (gameRuleName.equals(key.getName())) {
                        gameRules.get(key).set(value, serverWorld.getServer());
                    }
                }
            });
        }
    }

    private static void executeSpawnParticle(ShenEvent event, List<LivingEntity> targets) {
        if (!(event.world instanceof ServerWorld serverWorld)) return;
        ShenRule rule = event.rule;
        String particleId = rule.getParam("particle", "minecraft:flame");
        int count = rule.getParamInt("count", 20);
        double spread = rule.getParamDouble("spread", 0.5);

        String colorStr = rule.getParam("color", "");
        int colorR, colorG, colorB;
        boolean hasExplicitColor = !colorStr.isEmpty();
        if (hasExplicitColor) {
            String[] parts = colorStr.split(",");
            colorR = parts.length > 0 ? clampInt(parts[0], 255) : 255;
            colorG = parts.length > 1 ? clampInt(parts[1], 0) : 0;
            colorB = parts.length > 2 ? clampInt(parts[2], 0) : 0;
        } else {
            // 兼容旧规则
            colorR = rule.getParamInt("colorR", 255);
            colorG = rule.getParamInt("colorG", 0);
            colorB = rule.getParamInt("colorB", 0);
        }
        float dustSize = (float) rule.getParamDouble("dustSize", 1.0);
        boolean isDefaultColor = "255,0,0".equals(colorStr) && dustSize == 1.0;
        boolean hasCustomColor = (hasExplicitColor && !isDefaultColor)
            || (colorR != 255 || colorG != 0 || colorB != 0 || dustSize != 1.0);

        ParticleEffect particle;

        if (hasCustomColor) {
            float r = Math.min(1.0f, Math.max(0, colorR / 255.0f));
            float g = Math.min(1.0f, Math.max(0, colorG / 255.0f));
            float b = Math.min(1.0f, Math.max(0, colorB / 255.0f));
            particle = new DustParticleEffect(new Vector3f(r, g, b), dustSize);
        } else {
            particle = ParticleTypes.FLAME;
            try {
                Object raw = Registries.PARTICLE_TYPE.get(new Identifier(particleId));
                if (raw instanceof ParticleEffect pe) {
                    particle = pe;
                }
            } catch (Exception ignored) {}
        }

        for (LivingEntity target : targets) {
            serverWorld.spawnParticles(particle,
                target.getX(), target.getY() + 1, target.getZ(),
                count, spread, spread, spread, 0.02);
        }
    }

    private static int clampInt(String str, int defaultVal) {
        try {
            return Math.max(0, Math.min(255, Integer.parseInt(str.trim())));
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private static void executePlaySound(ShenEvent event, List<LivingEntity> targets) {
        if (event.world == null) return;
        ShenRule rule = event.rule;
        String soundId = rule.getParam("sound", "minecraft:entity.experience_orb.pickup");
        float volume = (float) rule.getParamDouble("volume", 1.0);
        float pitch = (float) rule.getParamDouble("pitch", 1.0);

        SoundEvent sound = Registries.SOUND_EVENT.get(new Identifier(soundId));
        if (sound == null) sound = SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;

        if (event.pos != null) {
            event.world.playSound(null, event.pos, sound, SoundCategory.PLAYERS, volume, pitch);
        } else {
            for (LivingEntity target : targets) {
                event.world.playSound(null, target.getBlockPos(), sound, SoundCategory.PLAYERS, volume, pitch);
            }
        }
    }

    private static void executeSendMessage(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        String message = rule.getParam("message", "§6[言出法随] §e规则已触发！");
        boolean broadcastToAll = rule.getParamBool("broadcast", false);

        if (broadcastToAll) {
            if (event.world != null && event.world.getServer() != null) {
                for (ServerPlayerEntity player : event.world.getServer().getPlayerManager().getPlayerList()) {
                    player.sendMessage(Text.literal(message), false);
                }
            }
        } else {
            for (LivingEntity target : targets) {
                if (target instanceof ServerPlayerEntity player) {
                    player.sendMessage(Text.literal(message), false);
                }
            }
        }
    }

    private static void executeCommand(ShenEvent event, List<LivingEntity> targets) {
        if (!(event.world instanceof ServerWorld serverWorld)) return;
        ShenRule rule = event.rule;
        String command = rule.getParam("command", "say 言出法随!");
        boolean executeForEach = rule.getParamBool("executeForEach", false);

        ServerCommandSource baseSource = serverWorld.getServer().getCommandSource()
            .withPosition(event.pos != null ? Vec3d.ofCenter(event.pos) : Vec3d.ZERO)
            .withWorld(serverWorld);

        if (executeForEach) {
            for (LivingEntity target : targets) {
                ServerCommandSource source = baseSource.withEntity(target);
                serverWorld.getServer().getCommandManager().executeWithPrefix(source, command);
            }
        } else {
            if (event.source != null) {
                baseSource = baseSource.withEntity(event.source);
            }
            serverWorld.getServer().getCommandManager().executeWithPrefix(baseSource, command);
        }
    }

    private static void executeReplaceItem(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        String removeItemId = rule.getParam("removeItem", "");
        int removeCount = rule.getParamInt("removeCount", 1);
        String giveItemId = rule.getParam("giveItem", "minecraft:diamond");
        int giveCount = rule.getParamInt("giveCount", 1);

        for (LivingEntity target : targets) {
            if (target instanceof ServerPlayerEntity player) {
                if (!removeItemId.isEmpty()) {
                    net.minecraft.item.Item removeItem = Registries.ITEM.get(new Identifier(removeItemId));
                    if (removeItem != null) {
                        for (int i = 0; i < player.getInventory().size(); i++) {
                            ItemStack stack = player.getInventory().getStack(i);
                            if (stack.isOf(removeItem)) {
                                stack.decrement(removeCount);
                                break;
                            }
                        }
                    }
                }
                ItemStack giveStack = new ItemStack(Registries.ITEM.get(new Identifier(giveItemId)), giveCount);
                player.getInventory().offerOrDrop(giveStack);
            }
        }
    }

    private static void executeEnchantItem(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        String enchantId = rule.getParam("enchantment", "minecraft:sharpness");
        int level = rule.getParamInt("level", 1);

        net.minecraft.enchantment.Enchantment enchantment = Registries.ENCHANTMENT.get(new Identifier(enchantId));
        if (enchantment == null) return;

        for (LivingEntity target : targets) {
            if (target instanceof ServerPlayerEntity player) {
                ItemStack stack = player.getMainHandStack();
                if (!stack.isEmpty()) {
                    stack.addEnchantment(enchantment, level);
                }
            }
        }
    }

    private static void executeSpawnExperience(ShenEvent event, List<LivingEntity> targets) {
        if (!(event.world instanceof ServerWorld serverWorld)) return;
        ShenRule rule = event.rule;
        int amount = rule.getParamInt("amount", 100);

        for (LivingEntity target : targets) {
            net.minecraft.entity.ExperienceOrbEntity.spawn(serverWorld, target.getPos(), amount);
        }
    }

    private static void executeRunOtherRule(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        String targetRuleName = rule.getParam("targetRuleName", "");

        if (targetRuleName.isEmpty()) return;

        for (ShenLaw law : ShenEventBus.getActiveLaws()) {
            for (ShenRule targetRule : law.rules) {
                if (targetRule.name.equals(targetRuleName) || targetRule.ruleId.equals(targetRuleName)) {
                    try {
                        ShenRuleExecutor.execute(targetRule, event);
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    private static void executeSetVariable(ShenEvent event, List<LivingEntity> targets) {
        if (event.world == null || event.world.getServer() == null) return;
        ShenRule rule = event.rule;
        String varKey = rule.getParam("varKey", "default");
        String varValue = rule.getParam("varValue", "1");
        String scope = rule.getParam("scope", "player");

        ShenVariableStore store = ShenVariableStore.get(event.world.getServer());
        if ("global".equals(scope)) {
            store.setGlobal(varKey, varValue);
        } else {
            for (LivingEntity target : targets) {
                if (target instanceof ServerPlayerEntity player) {
                    store.setPlayer(player.getUuid(), varKey, varValue);
                }
            }
        }
    }

    private static void executeModifyVariable(ShenEvent event, List<LivingEntity> targets) {
        if (event.world == null || event.world.getServer() == null) return;
        ShenRule rule = event.rule;
        String varKey = rule.getParam("varKey", "default");
        double amount = rule.getParamDouble("amount", 1.0);
        String scope = rule.getParam("scope", "player");

        ShenVariableStore store = ShenVariableStore.get(event.world.getServer());
        if ("global".equals(scope)) {
            store.modifyGlobal(varKey, amount);
        } else {
            for (LivingEntity target : targets) {
                if (target instanceof ServerPlayerEntity player) {
                    store.modifyPlayer(player.getUuid(), varKey, amount);
                }
            }
        }
    }

    private static void executeSetGamemode(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        String modeName = rule.getParam("mode", "survival");

        for (LivingEntity target : targets) {
            if (target instanceof ServerPlayerEntity player) {
                net.minecraft.world.GameMode mode = net.minecraft.world.GameMode.byName(modeName);
                if (mode == null) mode = net.minecraft.world.GameMode.SURVIVAL;
                player.changeGameMode(mode);
            }
        }
    }

    private static void executeClearInventory(ShenEvent event, List<LivingEntity> targets) {
        for (LivingEntity target : targets) {
            if (target instanceof ServerPlayerEntity player) {
                player.getInventory().clear();
            }
        }
    }

    private static void executeClearItem(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        String itemId = rule.getParam("item", "minecraft:stone");
        int maxCount = rule.getParamInt("maxCount", -1);

        for (LivingEntity target : targets) {
            if (target instanceof ServerPlayerEntity player) {
                int removed = 0;
                for (int i = 0; i < player.getInventory().size(); i++) {
                    ItemStack stack = player.getInventory().getStack(i);
                    if (stack.getItem().toString().contains(itemId)) {
                        if (maxCount > 0 && removed >= maxCount) break;
                        int toRemove = Math.min(stack.getCount(), maxCount > 0 ? maxCount - removed : stack.getCount());
                        stack.decrement(toRemove);
                        removed += toRemove;
                    }
                }
            }
        }
    }

    private static void executeLoop(ShenEvent event, List<LivingEntity> targets) {
        // 只有首次触发时才启动循环，防止无限递归
        if (event.loopCount > 0) return;

        ShenRule rule = event.rule;
        int times = rule.getParamInt("times", 3);
        int intervalTicks = rule.getParamInt("interval", 20);

        for (int i = 1; i <= times; i++) {
            int delayTicks = intervalTicks * i;
            ShenDelayManager.schedule(delayTicks, () -> {
                if (event.source != null && !event.source.isRemoved()) {
                    // 创建带循环标记的事件，防止再次触发LOOP
                    ShenEvent loopEvent = new ShenEvent(event.type, event.source, event.target, event.pos, event.world, event.chatMessage);
                    loopEvent.loopCount = 1;
                    ShenEventBus.fire(loopEvent);
                }
            });
        }
    }

    private static void executeSetFlying(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        boolean canFly = rule.getParamBool("value", true);
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.server.network.ServerPlayerEntity player) {
                player.getAbilities().allowFlying = canFly;
                player.sendAbilitiesUpdate();
            }
        }
    }

    private static void executeSetWalkSpeed(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.server.network.ServerPlayerEntity player) {
                float current = player.getAbilities().getWalkSpeed();
                float speed = (float) rule.resolveNumber("value", 0.1, current);
                player.getAbilities().setWalkSpeed(speed);
                player.sendAbilitiesUpdate();
            }
        }
    }

    private static void executeSetFlySpeed(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.server.network.ServerPlayerEntity player) {
                float current = player.getAbilities().getFlySpeed();
                float speed = (float) rule.resolveNumber("value", 0.05, current);
                player.getAbilities().setFlySpeed(speed);
                player.sendAbilitiesUpdate();
            }
        }
    }

    private static void executeModifyMaxHealth(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        net.minecraft.entity.attribute.EntityAttribute maxHealthAttr = net.minecraft.registry.Registries.ATTRIBUTE.get(
            net.minecraft.util.Identifier.tryParse("minecraft:generic.max_health"));
        if (maxHealthAttr == null) return;
        for (LivingEntity target : targets) {
            net.minecraft.entity.attribute.EntityAttributeInstance attr = target.getAttributeInstance(maxHealthAttr);
            if (attr == null) continue;
            double currentBase = attr.getBaseValue();
            double newBase = rule.resolveNumber("amount", 40.0, currentBase);
            // 减法模式保底 1 点血
            if ("subtract".equals(rule.getParam("amount_op", "set"))) {
                newBase = Math.max(1.0, newBase);
            }
            attr.setBaseValue(newBase);
            if (target.getHealth() > target.getMaxHealth()) {
                target.setHealth(target.getMaxHealth());
            }
        }
    }

    private static void executeRepairItem(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        int damage = rule.getParamInt("damage", 0);
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                net.minecraft.item.ItemStack stack = player.getMainHandStack();
                if (!stack.isEmpty()) {
                    if (damage <= 0) {
                        stack.setDamage(0);
                    } else {
                        stack.setDamage(Math.max(0, stack.getDamage() - damage));
                    }
                }
            }
        }
    }

    private static void executeSpawnLightning(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        boolean effectOnly = rule.getParamBool("effectOnly", false);
        if (event.world == null) return;
        for (LivingEntity target : targets) {
            net.minecraft.entity.LightningEntity lightning =
                net.minecraft.entity.EntityType.LIGHTNING_BOLT.create(event.world);
            if (lightning != null) {
                lightning.setPosition(target.getX(), target.getY(), target.getZ());
                if (effectOnly) {
                    lightning.setCosmetic(true);
                }
                event.world.spawnEntity(lightning);
            }
        }
    }

    private static void executeSetInvisible(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        boolean invisible = rule.getParamBool("value", true);
        for (LivingEntity target : targets) {
            target.setInvisible(invisible);
        }
    }

    private static void executeSaturateFood(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        int food = rule.getParamInt("food", 20);
        float saturation = (float) rule.getParamDouble("saturation", 5.0f);
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                player.getHungerManager().add(food, saturation);
            }
        }
    }

    private static void executeSetStepHeight(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        for (LivingEntity target : targets) {
            net.minecraft.nbt.NbtCompound nbt = target.writeNbt(new net.minecraft.nbt.NbtCompound());
            float current = nbt.contains("StepHeight") ? nbt.getFloat("StepHeight") : 0.6f;
            double height = rule.resolveNumber("height", 1.0, current);
            nbt.putFloat("StepHeight", (float) Math.max(0, height));
            target.readNbt(nbt);
        }
    }

    private static void executeSetResistance(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        int amplifier = rule.getParamInt("amplifier", 0);
        int duration = rule.getParamInt("duration", 200);
        for (LivingEntity target : targets) {
            net.minecraft.entity.effect.StatusEffect resistance = net.minecraft.registry.Registries.STATUS_EFFECT.get(
                net.minecraft.util.Identifier.tryParse("minecraft:resistance"));
            if (resistance != null) {
                target.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(resistance, duration, amplifier));
            }
        }
    }

    private static void executeSwapHandItems(ShenEvent event, List<LivingEntity> targets) {
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                net.minecraft.item.ItemStack main = player.getMainHandStack();
                net.minecraft.item.ItemStack off = player.getOffHandStack();
                player.getInventory().main.set(player.getInventory().selectedSlot, off);
                player.getInventory().offHand.set(0, main);
            }
        }
    }

    private static void executeRandomTeleport(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        double range = rule.getParamDouble("range", 50.0);
        java.util.Random rand = new java.util.Random();
        for (LivingEntity target : targets) {
            if (event.world == null) continue;
            double x = target.getX() + (rand.nextDouble() - 0.5) * range * 2;
            double z = target.getZ() + (rand.nextDouble() - 0.5) * range * 2;
            double y = target.getY();
            net.minecraft.util.math.BlockPos tpPos = net.minecraft.util.math.BlockPos.ofFloored(x, y, z);
            tpPos = event.world.getTopPosition(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, tpPos);
            target.teleport(tpPos.getX() + 0.5, tpPos.getY(), tpPos.getZ() + 0.5);
        }
    }

    private static void executeSetAir(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        for (LivingEntity target : targets) {
            int air = (int) rule.resolveNumber("air", 300, target.getAir());
            target.setAir(Math.max(0, Math.min(300, air)));
        }
    }

    private static void executeModifyAttribute(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        String attrId = rule.getParam("attribute", "minecraft:generic.movement_speed");
        net.minecraft.entity.attribute.EntityAttribute attr = net.minecraft.registry.Registries.ATTRIBUTE.get(
            net.minecraft.util.Identifier.tryParse(attrId));
        if (attr == null) return;
        for (LivingEntity target : targets) {
            net.minecraft.entity.attribute.EntityAttributeInstance instance = target.getAttributeInstance(attr);
            if (instance != null) {
                double current = instance.getBaseValue();
                double value = rule.resolveNumber("value", 0.1, current);
                instance.setBaseValue(value);
            }
        }
    }

    private static void executeFreezeEntity(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        for (LivingEntity target : targets) {
            int ticks = (int) rule.resolveNumber("ticks", 100, target.getFrozenTicks());
            target.setFrozenTicks(ticks);
        }
    }

    private static void executePlaceStructure(ShenEvent event, List<LivingEntity> targets) {
        if (!(event.world instanceof ServerWorld serverWorld)) return;
        ShenRule rule = event.rule;
        String structureId = rule.getParam("structureId", "minecraft:village_plains");
        boolean useTargetPos = rule.getParamBool("useTargetPos", true);
        boolean ignoreEntities = rule.getParamBool("ignoreEntities", true);
        int integrity = rule.getParamInt("integrity", 100);
        long seed = rule.getParamInt("seed", (int)(serverWorld.getRandom().nextLong() & 0x7FFFFFFF));

        for (LivingEntity target : targets) {
            BlockPos placePos = useTargetPos ? target.getBlockPos() : event.pos;
            if (placePos == null) placePos = BlockPos.ORIGIN;

            try {
                net.minecraft.util.Identifier structId = net.minecraft.util.Identifier.tryParse(structureId);
                if (structId == null) continue;

                java.util.Optional<net.minecraft.structure.StructureTemplate> templateOpt =
                    serverWorld.getStructureTemplateManager().getTemplate(structId);

                if (templateOpt.isPresent()) {
                    net.minecraft.structure.StructureTemplate template = templateOpt.get();
                    net.minecraft.structure.StructurePlacementData placementData =
                        new net.minecraft.structure.StructurePlacementData()
                            .setIgnoreEntities(ignoreEntities)
                            .setPosition(new BlockPos(0, 0, 0));

                    template.place(serverWorld, placePos, placePos, placementData,
                        net.minecraft.util.math.random.Random.create(seed),
                        net.minecraft.block.Block.NOTIFY_ALL);
                } else {
                    String cmd = "place structure " + structureId + " " + placePos.getX() + " " + placePos.getY() + " " + placePos.getZ();
                    serverWorld.getServer().getCommandManager().executeWithPrefix(
                        serverWorld.getServer().getCommandSource().withSilent(), cmd);
                }
            } catch (Exception e) {
                if (event.source instanceof ServerPlayerEntity player) {
                    player.sendMessage(Text.literal("§c放置结构失败: " + e.getMessage()), false);
                }
            }
        }
    }

    private static void executeMassReplaceBlocks(ShenEvent event, List<LivingEntity> targets) {
        if (!(event.world instanceof ServerWorld serverWorld)) return;
        ShenRule rule = event.rule;
        String sourceBlockId = rule.getParam("sourceBlock", "minecraft:stone");
        String targetBlockId = rule.getParam("targetBlock", "minecraft:air");
        int radiusX = rule.getParamInt("radiusX", 5);
        int radiusY = rule.getParamInt("radiusY", 5);
        int radiusZ = rule.getParamInt("radiusZ", 5);
        String shape = rule.getParam("shape", "cube");
        boolean useTargetPos = rule.getParamBool("useTargetPos", true);

        net.minecraft.block.Block sourceBlock = Registries.BLOCK.get(new Identifier(sourceBlockId));
        net.minecraft.block.Block targetBlock = Registries.BLOCK.get(new Identifier(targetBlockId));
        if (sourceBlock == null || targetBlock == null) return;

        for (LivingEntity target : targets) {
            BlockPos center = useTargetPos ? target.getBlockPos() : event.pos;
            if (center == null) center = BlockPos.ORIGIN;
            int count = 0;

            for (int dx = -radiusX; dx <= radiusX; dx++) {
                for (int dy = -radiusY; dy <= radiusY; dy++) {
                    for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                        BlockPos checkPos = center.add(dx, dy, dz);

                        if ("sphere".equals(shape)) {
                            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                            if (dist > radiusX) continue;
                        }
                        if ("cylinder".equals(shape)) {
                            double dist = Math.sqrt(dx * dx + dz * dz);
                            if (dist > radiusX || Math.abs(dy) > radiusY) continue;
                        }

                        if (serverWorld.getBlockState(checkPos).isOf(sourceBlock)) {
                            serverWorld.setBlockState(checkPos, targetBlock.getDefaultState(), 3);
                            count++;
                        }
                    }
                }
            }
        }
    }

    private static void executeModAdapterEffect(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        String effId = rule.getParam("adapterEffectId", "");
        if (effId.isEmpty()) return;
        String value = rule.getParam("value", "0");
        for (LivingEntity target : targets) {
            bai.god.shen.compat.ModAdapterManager.executeEffect(effId, value, target);
        }
    }

    private static void executeSmfsSetSpiritResistance(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        float value = (float) rule.getParamDouble("value", 0);
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                bai.god.shen.compat.smfs.SmfsIntegration.setSpiritResistance(player, value);
            }
        }
    }

    private static void executeSmfsSetSpiritDamage(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        float value = (float) rule.getParamDouble("value", 0);
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                bai.god.shen.compat.smfs.SmfsIntegration.setSpiritDamage(player, value);
            }
        }
    }

    private static void executeSmfsSetCurrentSpirit(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        float value = (float) rule.getParamDouble("value", 0);
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                bai.god.shen.compat.smfs.SmfsIntegration.setCurrentSpirit(player, value);
            }
        }
    }

    private static void executeSmfsSetMaxSpirit(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        float value = (float) rule.getParamDouble("value", 0);
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                bai.god.shen.compat.smfs.SmfsIntegration.setMaxSpirit(player, value);
            }
        }
    }

    private static void executeSmfsSetSanity(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        float value = (float) rule.getParamDouble("value", 100);
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                bai.god.shen.compat.smfs.SmfsIntegration.setSanity(player, value);
            }
        }
    }

    private static void executeSmfsSetRevivalFactor(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        double value = rule.getParamDouble("value", 0);
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                bai.god.shen.compat.smfs.SmfsIntegration.setRevivalFactor(player, value);
            }
        }
    }

    private static void executeSmfsSetMainGhostSlot(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        int slotIndex = rule.getParamInt("slotIndex", 0);
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                bai.god.shen.compat.smfs.SmfsIntegration.setMainGhostSlot(player, slotIndex);
            }
        }
    }

    private static void executeSmfsCycleMainGhost(ShenEvent event, List<LivingEntity> targets) {
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                bai.god.shen.compat.smfs.SmfsIntegration.cycleMainGhost(player);
            }
        }
    }

    private static void executeSmfsSetNbtAttribute(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        String key = rule.getParam("attributeKey", "");
        double value = rule.getParamDouble("value", 0);
        if (key.isEmpty()) return;
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                bai.god.shen.compat.smfs.SmfsIntegration.setNbtAttribute(player, key, value);
            }
        }
    }

    private static void executeSmfsActivateDomain(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        boolean active = rule.getParamBool("value", true);
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                bai.god.shen.compat.smfs.SmfsIntegration.setGhostDomainActive(player, active);
            }
        }
    }

    private static void executeSmfsSetDomainType(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        int color = rule.getParamInt("color", 0);
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                bai.god.shen.compat.smfs.SmfsIntegration.setGhostDomainColor(player, color);
            }
        }
    }

    private static void executeSmfsSetDomainLevel(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        int level = rule.getParamInt("level", 1);
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                bai.god.shen.compat.smfs.SmfsIntegration.setGhostDomainLevel(player, level);
            }
        }
    }

    private static void executeSmfsSetGhostEyeLevel(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        int level = rule.getParamInt("level", 1);
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                bai.god.shen.compat.smfs.SmfsIntegration.setGhostEyeLevel(player, level);
            }
        }
    }

    private static void executeSmfsToggleGhostEye(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        boolean active = rule.getParamBool("value", true);
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                bai.god.shen.compat.smfs.SmfsIntegration.setGhostEyeActive(player, active);
            }
        }
    }

    private static void executeSmfsLockSlot(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        int slotIndex = rule.getParamInt("slotIndex", 0);
        boolean locked = rule.getParamBool("locked", true);
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                bai.god.shen.compat.smfs.SmfsIntegration.setSlotLocked(player, slotIndex, locked);
            }
        }
    }

    private static void executeSmfsClearSlot(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        int slotIndex = rule.getParamInt("slotIndex", 0);
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                bai.god.shen.compat.smfs.SmfsIntegration.clearSlot(player, slotIndex);
            }
        }
    }

    private static void executeSmfsSwapSlots(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        int slotA = rule.getParamInt("slotA", 0);
        int slotB = rule.getParamInt("slotB", 1);
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                bai.god.shen.compat.smfs.SmfsIntegration.swapSlots(player, slotA, slotB);
            }
        }
    }

    private static void executeSmfsSetSlotGhost(ShenEvent event, List<LivingEntity> targets) {
        ShenRule rule = event.rule;
        int slotIndex = rule.getParamInt("slotIndex", 0);
        String ghostItemId = rule.getParam("ghostItemId", "");
        if (ghostItemId.isEmpty()) return;
        for (LivingEntity target : targets) {
            if (target instanceof net.minecraft.entity.player.PlayerEntity player) {
                bai.god.shen.compat.smfs.SmfsIntegration.setGhostInSlot(player, slotIndex, ghostItemId);
            }
        }
    }
}