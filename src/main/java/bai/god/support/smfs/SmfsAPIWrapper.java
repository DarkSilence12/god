package bai.god.support.smfs;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 神秘复苏API包装类
 * 提供简化的API调用，避免编译时依赖问题
 */
public class SmfsAPIWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger("god-smfs-wrapper");

    public static boolean isSmfsLoaded() {
        try {
            Class.forName("com.xie.smfs.api.GhostAPI");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 获取玩家的灵异伤害值
     * 从PlayerEvents的缓存数据中获取，与神秘复苏本体保持一致
     */
    public static int getSpiritualPower(PlayerEntity player) {
        if (!isSmfsLoaded()) {
            LOGGER.debug("神秘复苏模组未加载，使用NBT检测灵异伤害");
            return SmfsIntegration.getSpiritualPower(player);
        }

        try {
            Class<?> playerEventsClass = Class.forName("com.xie.smfs.common.events.PlayerEvents");
            java.lang.reflect.Method getSpiritAttributesMethod = playerEventsClass.getMethod("getSpiritAttributes", PlayerEntity.class);
            Object spiritAttributes = getSpiritAttributesMethod.invoke(null, player);

            if (spiritAttributes instanceof net.minecraft.nbt.NbtCompound) {
                net.minecraft.nbt.NbtCompound nbt = (net.minecraft.nbt.NbtCompound) spiritAttributes;

                if (nbt.contains("spiritDamage")) {
                    if (nbt.contains("spiritDamage", net.minecraft.nbt.NbtElement.DOUBLE_TYPE)) {
                        return (int) nbt.getDouble("spiritDamage");
                    } else if (nbt.contains("spiritDamage", net.minecraft.nbt.NbtElement.INT_TYPE)) {
                        return nbt.getInt("spiritDamage");
                    }
                }

                if (nbt.contains("currentSpirit")) {
                    if (nbt.contains("currentSpirit", net.minecraft.nbt.NbtElement.DOUBLE_TYPE)) {
                        return (int) nbt.getDouble("currentSpirit");
                    } else if (nbt.contains("currentSpirit", net.minecraft.nbt.NbtElement.INT_TYPE)) {
                        return nbt.getInt("currentSpirit");
                    }
                }

                if (nbt.contains("maxSpirit")) {
                    if (nbt.contains("maxSpirit", net.minecraft.nbt.NbtElement.DOUBLE_TYPE)) {
                        return (int) nbt.getDouble("maxSpirit");
                    } else if (nbt.contains("maxSpirit", net.minecraft.nbt.NbtElement.INT_TYPE)) {
                        return nbt.getInt("maxSpirit");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("通过PlayerEvents获取灵异伤害失败: {}", e.getMessage());
        }

        // 备用方案：使用NBT检测
        return SmfsIntegration.getSpiritualPower(player);
    }

    public static int getSpiritualResistance(PlayerEntity player) {
        if (!isSmfsLoaded()) {
            return SmfsIntegration.getSpiritualResistance(player);
        }

        try {
            Class<?> playerEventsClass = Class.forName("com.xie.smfs.common.events.PlayerEvents");
            java.lang.reflect.Method getSpiritAttributesMethod = playerEventsClass.getMethod("getSpiritAttributes", PlayerEntity.class);
            Object spiritAttributes = getSpiritAttributesMethod.invoke(null, player);

            if (spiritAttributes instanceof net.minecraft.nbt.NbtCompound) {
                net.minecraft.nbt.NbtCompound nbt = (net.minecraft.nbt.NbtCompound) spiritAttributes;

                if (nbt.contains("spiritResistance")) {
                    if (nbt.contains("spiritResistance", net.minecraft.nbt.NbtElement.DOUBLE_TYPE)) {
                        return (int) nbt.getDouble("spiritResistance");
                    } else if (nbt.contains("spiritResistance", net.minecraft.nbt.NbtElement.INT_TYPE)) {
                        return nbt.getInt("spiritResistance");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("通过PlayerEvents获取灵异抗性失败: {}", e.getMessage());
        }

        return SmfsIntegration.getSpiritualResistance(player);
    }

    public static int getSpiritualStrength(PlayerEntity player) {
        if (!isSmfsLoaded()) {
            return SmfsIntegration.getSpiritualStrength(player);
        }

        try {
            Class<?> playerEventsClass = Class.forName("com.xie.smfs.common.events.PlayerEvents");
            java.lang.reflect.Method getSpiritAttributesMethod = playerEventsClass.getMethod("getSpiritAttributes", PlayerEntity.class);
            Object spiritAttributes = getSpiritAttributesMethod.invoke(null, player);

            if (spiritAttributes instanceof net.minecraft.nbt.NbtCompound) {
                net.minecraft.nbt.NbtCompound nbt = (net.minecraft.nbt.NbtCompound) spiritAttributes;

                if (nbt.contains("currentSpirit")) {
                    if (nbt.contains("currentSpirit", net.minecraft.nbt.NbtElement.DOUBLE_TYPE)) {
                        return (int) nbt.getDouble("currentSpirit");
                    } else if (nbt.contains("currentSpirit", net.minecraft.nbt.NbtElement.INT_TYPE)) {
                        return nbt.getInt("currentSpirit");
                    }
                }

                if (nbt.contains("maxSpirit")) {
                    if (nbt.contains("maxSpirit", net.minecraft.nbt.NbtElement.DOUBLE_TYPE)) {
                        return (int) nbt.getDouble("maxSpirit");
                    } else if (nbt.contains("maxSpirit", net.minecraft.nbt.NbtElement.INT_TYPE)) {
                        return nbt.getInt("maxSpirit");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("通过PlayerEvents获取灵异强度失败: {}", e.getMessage());
        }

        return SmfsIntegration.getSpiritualStrength(player);
    }

    public static boolean setSpiritualPower(PlayerEntity player, int power) {
        if (!isSmfsLoaded()) {
            LOGGER.debug("神秘复苏模组未加载，使用NBT设置灵异力量");
            return SmfsIntegration.setSpiritualPower(player, power);
        }

        try {
            Class<?> ghostAPIClass = Class.forName("com.xie.smfs.api.GhostAPI");
            java.lang.reflect.Method getInstanceMethod = ghostAPIClass.getMethod("getInstance");
            Object ghostAPI = getInstanceMethod.invoke(null);

            java.lang.reflect.Method setSpiritualStrengthMethod = ghostAPIClass.getMethod("setSpiritualStrength", PlayerEntity.class, Integer.TYPE);
            Object result = setSpiritualStrengthMethod.invoke(ghostAPI, player, power);

            if (result instanceof Boolean) {
                return (Boolean) result;
            }
        } catch (Exception e) {
            LOGGER.warn("通过反射API设置灵异力量失败: {}", e.getMessage());
        }

        // 备用方案：使用NBT设置
        return SmfsIntegration.setSpiritualPower(player, power);
    }

    public static DamageSource getGhostDamageSource(World world) {
        if (!isSmfsLoaded()) {
            LOGGER.debug("神秘复苏模组未加载");
            return world.getDamageSources().magic();
        }

        try {
            Class<?> modDamageSourcesClass = Class.forName("com.xie.smfs.damage.ModDamageSources");
            java.lang.reflect.Method ghostMethod = modDamageSourcesClass.getMethod("ghost", World.class);
            Object damageSource = ghostMethod.invoke(null, world);

            if (damageSource instanceof DamageSource) {
                return (DamageSource) damageSource;
            }
        } catch (Exception e) {
            LOGGER.warn("通过反射获取灵异伤害源失败: {}", e.getMessage());
        }

        // 备用方案：使用魔法伤害源
        return world.getDamageSources().magic();
    }

    public static boolean hasTamedGhost(PlayerEntity player, String ghostType) {
        if (!isSmfsLoaded()) {
            LOGGER.debug("神秘复苏模组未加载，使用NBT检查驾驭状态");
            return SmfsIntegration.hasTamedGhostInNBT(player, ghostType);
        }

        try {
            Class<?> tameableItemAPIClass = Class.forName("com.xie.smfs.api.TameableItemAPI");
            java.lang.reflect.Method getInstanceMethod = tameableItemAPIClass.getMethod("getInstance");
            Object tameableAPI = getInstanceMethod.invoke(null);

            java.lang.reflect.Method hasTamedGhostMethod = tameableItemAPIClass.getMethod("hasTamedGhost", PlayerEntity.class, String.class);
            Object result = hasTamedGhostMethod.invoke(tameableAPI, player, ghostType);

            if (result instanceof Boolean) {
                return (Boolean) result;
            }
        } catch (Exception e) {
            LOGGER.warn("通过反射API检查驾驭状态失败: {}", e.getMessage());
        }

        // 备用方案：检查NBT标记
        return SmfsIntegration.hasTamedGhostInNBT(player, ghostType);
    }

    public static boolean registerTameableItem(PlayerEntity player, String ghostType,
                                                int spiritBonus, int powerBonus, int damageBonus,
                                                int resistanceBonus, int sanityBonus, double revivalFactor) {
        try {
            Class<?> tameableItemAPIClass = Class.forName("com.xie.smfs.api.TameableItemAPI");

            java.lang.reflect.Method getInstanceMethod = tameableItemAPIClass.getMethod("getInstance");
            Object apiInstance = getInstanceMethod.invoke(null);

            java.lang.reflect.Method setGhostSlotItemMethod = tameableItemAPIClass.getMethod(
                "setGhostSlotItem", PlayerEntity.class, int.class, ItemStack.class);

            for (int i = 0; i < 10; i++) {
                java.lang.reflect.Method isGhostSlotOccupiedMethod = tameableItemAPIClass.getMethod(
                    "isGhostSlotOccupied", PlayerEntity.class, int.class);

                boolean occupied = (boolean) isGhostSlotOccupiedMethod.invoke(apiInstance, player, i);

                if (!occupied) {
                    ItemStack ghostFurnaceFireStack = new ItemStack(
                        net.minecraft.registry.Registries.ITEM.get(new Identifier("guiyu", "ghost_furnace_fire")));
                    setGhostSlotItemMethod.invoke(apiInstance, player, i, ghostFurnaceFireStack);

                    setGhostSlotAttributes(apiInstance, player, i, spiritBonus, powerBonus, damageBonus, resistanceBonus, revivalFactor);

                    LOGGER.debug("成功将鬼炉火添加到槽位: {}", i);
                    return true;
                }
            }

            LOGGER.warn("没有找到空槽位");
            return false;

        } catch (Exception e) {
            LOGGER.error("注册驾驭物品失败: {}", e.getMessage());
            LOGGER.debug("详细错误信息:", e);
            return false;
        }
    }

    private static void setGhostSlotAttributes(Object apiInstance, PlayerEntity player, int slotIndex,
                                              int spiritBonus, int powerBonus, int damageBonus, int resistanceBonus, double revivalFactor) {
        try {
            java.lang.reflect.Method setSpiritStrengthMethod = apiInstance.getClass().getMethod(
                "setGhostSlotSpiritStrength", PlayerEntity.class, int.class, int.class);
            setSpiritStrengthMethod.invoke(apiInstance, player, slotIndex, spiritBonus);

            java.lang.reflect.Method setSpiritPowerMethod = apiInstance.getClass().getMethod(
                "setGhostSlotSpiritPower", PlayerEntity.class, int.class, int.class);
            setSpiritPowerMethod.invoke(apiInstance, player, slotIndex, powerBonus);

            java.lang.reflect.Method setSpiritDamageMethod = apiInstance.getClass().getMethod(
                "setGhostSlotSpiritDamage", PlayerEntity.class, int.class, int.class);
            setSpiritDamageMethod.invoke(apiInstance, player, slotIndex, damageBonus);

            java.lang.reflect.Method setSpiritResistanceMethod = apiInstance.getClass().getMethod(
                "setGhostSlotSpiritResistance", PlayerEntity.class, int.class, int.class);
            setSpiritResistanceMethod.invoke(apiInstance, player, slotIndex, resistanceBonus);

            java.lang.reflect.Method setRevivalFactorMethod = apiInstance.getClass().getMethod(
                "setGhostSlotRevivalFactor", PlayerEntity.class, int.class, double.class);
            setRevivalFactorMethod.invoke(apiInstance, player, slotIndex, revivalFactor);

            LOGGER.debug("成功设置槽位{}的属性", slotIndex);
        } catch (Exception e) {
            LOGGER.warn("设置槽位属性失败: {}", e.getMessage());
        }
    }

    public static boolean isGhostDomainActive(PlayerEntity player) {
        return SmfsIntegration.isGhostDomainActive(player);
    }

    public static boolean hasRedGhostDomain(PlayerEntity player) {
        return hasStatusEffect(player, "com.xie.smfs.effect.RedGhostDomainEffect") ||
               hasStatusEffect(player, "RED_GHOST_DOMAIN") ||
               hasStatusEffect(player, "RED_GHOST_DOMAIN_TARGET");
    }

    public static boolean hasGreenGhostDomain(PlayerEntity player) {
        return hasStatusEffect(player, "com.xie.smfs.effect.GreenGhostDomainEffect") ||
               hasStatusEffect(player, "GREEN_GHOST_DOMAIN") ||
               hasStatusEffect(player, "GREEN_GHOST_DOMAIN_TARGET");
    }

    public static boolean hasBlueGhostDomain(PlayerEntity player) {
        return hasStatusEffect(player, "com.xie.smfs.effect.BlueGhostDomainEffect") ||
               hasStatusEffect(player, "BLUE_GHOST_DOMAIN") ||
               hasStatusEffect(player, "BLUE_GHOST_DOMAIN_TARGET");
    }

    public static boolean hasGrayGhostDomain(PlayerEntity player) {
        return hasStatusEffect(player, "com.xie.smfs.effect.GrayGhostDomainEffect") ||
               hasStatusEffect(player, "GRAY_GHOST_DOMAIN") ||
               hasStatusEffect(player, "GRAY_GHOST_DOMAIN_TARGET");
    }

    public static boolean hasGoldenGhostDomain(PlayerEntity player) {
        return hasStatusEffect(player, "com.xie.smfs.effect.GoldenGhostDomainEffect") ||
               hasStatusEffect(player, "GOLDEN_GHOST_DOMAIN") ||
               hasStatusEffect(player, "GOLDEN_GHOST_DOMAIN_TARGET");
    }

    public static boolean hasPurpleGhostDomain(PlayerEntity player) {
        return hasStatusEffect(player, "com.xie.smfs.effect.PurpleGhostDomainEffect") ||
               hasStatusEffect(player, "PURPLE_GHOST_DOMAIN") ||
               hasStatusEffect(player, "PURPLE_GHOST_DOMAIN_TARGET") ||
               hasStatusEffect(player, "PURPLE_GHOST_DOMAIN_VISUAL");
    }

    public static boolean hasBlackGhostDomain(PlayerEntity player) {
        return hasStatusEffect(player, "com.xie.smfs.effect.BlackGhostDomainEffect") ||
               hasStatusEffect(player, "BLACK_GHOST_DOMAIN") ||
               hasStatusEffect(player, "BLACK_GHOST_DOMAIN_TARGET");
    }

    public static boolean hasCyanGhostDomain(PlayerEntity player) {
        return hasStatusEffect(player, "com.xie.smfs.effect.CyanGhostDomainEffect") ||
               hasStatusEffect(player, "CYAN_GHOST_DOMAIN") ||
               hasStatusEffect(player, "CYAN_GHOST_DOMAIN_TARGET");
    }

    private static boolean hasStatusEffect(PlayerEntity player, String effectName) {
        if (player == null || !isSmfsLoaded()) {
            return false;
        }

        try {
            net.minecraft.registry.Registries.STATUS_EFFECT.getIds().forEach(id -> {
                if (id.getPath().equalsIgnoreCase(effectName) ||
                    id.getPath().toLowerCase().contains(effectName.toLowerCase())) {
                }
            });

            Class<?> modEffectsClass = Class.forName("com.xie.smfs.registry.ModEffects");
            java.lang.reflect.Field field = null;

            String[] possibleNames = {
                effectName.toUpperCase(),
                effectName.toLowerCase(),
                effectName.replace("GHOST_DOMAIN", "_GHOST_DOMAIN"),
                effectName.replace("_", "")
            };

            for (String name : possibleNames) {
                try {
                    field = modEffectsClass.getField(name);
                    break;
                } catch (NoSuchFieldException e) {
                }
            }

            if (field != null) {
                Object effect = field.get(null);
                if (effect instanceof net.minecraft.entity.effect.StatusEffect) {
                    return player.hasStatusEffect((net.minecraft.entity.effect.StatusEffect) effect);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("检查状态效果失败: {} - {}", effectName, e.getMessage());
        }

        return false;
    }

    public static int getGhostDomainLevel(PlayerEntity player) {
        return SmfsIntegration.getGhostDomainLevel(player);
    }

    public static int getGhostEyeLevel(PlayerEntity player) {
        return SmfsIntegration.getGhostEyeLevel(player);
    }

    public static void dealSpiritDamage(LivingEntity target, float amount, LivingEntity attacker) {
        World world = target.getWorld();
        DamageSource source = getGhostDamageSource(world);

        if (target instanceof PlayerEntity targetPlayer) {
            handleSpiritDamage(targetPlayer, amount, amount, source);
        } else {
            try {
                Class<?> ghostClass = Class.forName("com.xie.smfs.entity.GhostEntity");
                if (ghostClass.isInstance(target)) {
                    dealGhostDamage(target, amount, attacker);
                    return;
                }
            } catch (ClassNotFoundException ignored) {}

            try {
                Class<?> ghostMasterClass = Class.forName("com.xie.smfs.entity.GhostMasterEntity");
                if (ghostMasterClass.isInstance(target)) {
                    dealGhostMasterDamage(target, amount, source);
                    return;
                }
            } catch (ClassNotFoundException ignored) {}

            target.damage(source, amount);
        }
    }

    private static void dealGhostDamage(LivingEntity target, float amount, LivingEntity attacker) {
        try {
            float resistance = 0;
            try {
                java.lang.reflect.Method getResistance = target.getClass().getMethod("getSpiritResistance");
                Object res = getResistance.invoke(target);
                resistance = (res instanceof Number) ? ((Number) res).floatValue() : 0;
            } catch (Exception ignored) {}

            float actual = (amount * amount) / (amount + resistance);

            if (actual > 0) {
                java.lang.reflect.Method getStrength = target.getClass().getMethod("getSpiritualStrength");
                java.lang.reflect.Method setStrength = target.getClass().getMethod("setSpiritualStrength", int.class);
                int current = (int) getStrength.invoke(target);
                int newStr = Math.max(0, current - (int) actual);
                setStrength.invoke(target, newStr);

                if (newStr == 0) {
                    try {
                        java.lang.reflect.Method isDeadlocked = target.getClass().getMethod("isDeadlocked");
                        java.lang.reflect.Method setDeadlocked = target.getClass().getMethod("setDeadlocked", boolean.class);
                        if (!(boolean) isDeadlocked.invoke(target)) {
                            setDeadlocked.invoke(target, true);
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            LOGGER.warn("处理鬼实体灵异伤害失败: {}", e.getMessage());
            target.damage(getGhostDamageSource(target.getWorld()), amount);
        }
    }

    private static void dealGhostMasterDamage(LivingEntity target, float amount, DamageSource source) {
        try {
            java.lang.reflect.Method getResistance = target.getClass().getMethod("getSpiritualResistance");
            Object res = getResistance.invoke(target);
            float resistance = (res instanceof Number) ? ((Number) res).floatValue() : 0;

            Class<?> playerEventsClass = Class.forName("com.xie.smfs.common.events.PlayerEvents");
            java.lang.reflect.Method calculateMethod = playerEventsClass.getMethod(
                "calculateSpiritDamage", float.class, float.class);
            float actual = (float) calculateMethod.invoke(null, amount, resistance);

            Class<?> modEventsClass = Class.forName("com.xie.smfs.event.ModEvents");
            java.lang.reflect.Field processingField = modEventsClass.getField("processingSpiritDamage");
            Object threadLocal = processingField.get(null);
            java.lang.reflect.Method setMethod = threadLocal.getClass().getMethod("set", Boolean.TYPE);
            setMethod.invoke(threadLocal, true);
            try {
                target.damage(source, actual);
            } finally {
                setMethod.invoke(threadLocal, false);
            }
        } catch (Exception e) {
            LOGGER.warn("处理驭鬼者NPC灵异伤害失败: {}", e.getMessage());
            target.damage(source, amount);
        }
    }

    public static void handleSpiritDamage(PlayerEntity target, float spiritDamageAmount, float baseAmount, DamageSource source) {
        if (!isSmfsLoaded()) {
            // 如果神秘复苏未加载，直接造成伤害
            target.damage(source, spiritDamageAmount);
            return;
        }

        try {
            Class<?> playerEventsClass = Class.forName("com.xie.smfs.common.events.PlayerEvents");
            java.lang.reflect.Method handleSpiritDamageMethod = playerEventsClass.getMethod(
                "handleSpiritDamage",
                PlayerEntity.class,
                float.class,
                float.class,
                net.minecraft.entity.damage.DamageSource.class
            );
            handleSpiritDamageMethod.invoke(null, target, spiritDamageAmount, baseAmount, source);

            LOGGER.debug("成功调用神秘复苏的handleSpiritDamage方法");
        } catch (Exception e) {
            LOGGER.warn("调用handleSpiritDamage失败: {}", e.getMessage());
            // 备用方案：直接造成伤害
            target.damage(source, spiritDamageAmount);
        }
    }
}