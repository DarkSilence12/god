package bai.god.shen;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ShenRuleExecutor {

    public static void execute(ShenRule rule, ShenEvent event) {
        if (!checkConditions(rule, event)) {
            return;
        }

        List<LivingEntity> targets = resolveAllTargets(rule, event);

        executeEffectChain(rule, event, targets, 0);
    }

    private static List<LivingEntity> resolveAllTargets(ShenRule rule, ShenEvent event) {
        Set<LivingEntity> merged = new HashSet<>();
        for (ShenTargetType tt : rule.targetTypes) {
            merged.addAll(ShenTargetResolver.resolve(tt, event, rule));
        }
        // 如果 targetTypes 为空，fallback 到旧的 targetType
        if (merged.isEmpty() && rule.targetType != null) {
            merged.addAll(ShenTargetResolver.resolve(rule.targetType, event, rule));
        }
        return new ArrayList<>(merged);
    }

    private static void executeEffectChain(ShenRule rule, ShenEvent event, List<LivingEntity> targets, int index) {
        if (index >= rule.effects.size()) return;
        ShenRule.ShenEffectEntry entry = rule.effects.get(index);

        Runnable executeStep = () -> {
            ShenEffectRegistry.execute(entry, event, targets);
            executeEffectChain(rule, event, targets, index + 1);
        };

        if (entry.delayTicks > 0) {
            ShenDelayManager.schedule(entry.delayTicks, executeStep);
        } else {
            executeStep.run();
        }
    }

    private static boolean checkConditions(ShenRule rule, ShenEvent event) {
        if (rule.conditions == null || rule.conditions.isEmpty()) {
            return true;
        }

        boolean isOr = rule.conditionLogic;

        if (isOr) {
            for (ShenRule.ShenCondition condition : rule.conditions) {
                if (checkSingleCondition(condition, event)) {
                    return true;
                }
            }
            return false;
        } else {
            // AND逻辑：所有条件必须满足
            for (ShenRule.ShenCondition condition : rule.conditions) {
                if (!checkSingleCondition(condition, event)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean checkSingleCondition(ShenRule.ShenCondition condition, ShenEvent event) {
        switch (condition.type) {
            case HEALTH_COMPARE:
                return checkHealthCompare(condition, event);
            case EXPERIENCE_COMPARE:
                return checkExperienceCompare(condition, event);
            case NBT_COMPARE:
                return checkNbtCompare(condition, event);
            case ITEM_IN_HAND:
                return checkItemInHand(condition, event);
            case ITEM_IN_INVENTORY:
                return checkItemInInventory(condition, event);
            case HAS_EFFECT:
                return checkHasEffect(condition, event);
            case IN_DIMENSION:
                return checkInDimension(condition, event);
            case RANDOM_CHANCE:
                return checkRandomChance(condition, event);
            case CHAT_MESSAGE:
                return checkChatMessage(condition, event);
            case TIME_RANGE:
                return checkTimeRange(condition, event);
            case WEATHER_CONDITION:
                return checkWeatherCondition(condition, event);
            case BIOME:
                return checkBiome(condition, event);
            case LIGHT_LEVEL:
                return checkLightLevel(condition, event);
            case IS_SNEAKING:
                return checkIsSneaking(condition, event);
            case IS_SPRINTING:
                return checkIsSprinting(condition, event);
            case ITEM_HAS_ENCHANTMENT:
                return checkItemHasEnchantment(condition, event);
            case IS_ON_FIRE:
                return checkIsOnFire(condition, event);
            case IS_IN_WATER:
                return checkIsInWater(condition, event);
            case IS_IN_LAVA:
                return checkIsInLava(condition, event);
            case PLAYER_DISTANCE:
                return checkPlayerDistance(condition, event);
            case VARIABLE_COMPARE:
                return checkVariableCompare(condition, event);
            case GAMEMODE:
                return checkGamemode(condition, event);
            case FOOD_LEVEL:
                return checkFoodLevel(condition, event);
            case PLAYER_LEVEL:
                return checkPlayerLevel(condition, event);
            case ENTITY_TYPE:
                return checkEntityType(condition, event);
            case ENTITY_EXISTS:
                return checkEntityExists(condition, event);
            case IS_ELYTRA_FLYING:
                return checkElytraFlying(condition, event);
            case IS_GLIDING:
                return checkGliding(condition, event);
            case IS_SWIMMING:
                return checkSwimming(condition, event);
            case IS_RIDING:
                return checkRiding(condition, event);
            case IS_WEARING_ARMOR:
                return checkWearingArmor(condition, event);
            case IS_WEARING_ITEM:
                return checkWearingItem(condition, event);
            case BLOCK_BELOW:
                return checkBlockBelow(condition, event);
            case BLOCK_ABOVE:
                return checkBlockAbove(condition, event);
            case PERMISSION_LEVEL:
                return checkPermissionLevel(condition, event);
            case ATTRIBUTE_VALUE:
                return checkAttributeValue(condition, event);
            case BLOCK_IN_HAND:
                return checkBlockInHand(condition, event);
            case ITEM_NAME:
                return checkItemName(condition, event);
            case MOON_PHASE:
                return checkMoonPhase(condition, event);
            case DIFFICULTY:
                return checkDifficulty(condition, event);
            case IN_STRUCTURE:
                return checkInStructure(condition, event);
            case STRUCTURE_DISTANCE:
                return checkStructureDistance(condition, event);
            case MOD_ADAPTER_CONDITION:
                return bai.god.shen.compat.ModAdapterManager.evaluateCondition(
                    condition.value, condition.operator,
                    condition.params.getOrDefault("compareValue", "0"), event.source);
            case SPECIFIC_PLAYER:
                return checkSpecificPlayer(condition, event);
            case IS_ATTACKING:
                return checkIsAttacking(condition, event);
            case NONE:
                return true;
            case SMFS_HAS_GHOST_TYPE:
                return checkSmfsHasGhostType(condition, event);
            case SMFS_SLOT_OCCUPIED:
                return checkSmfsSlotOccupied(condition, event);
            case SMFS_HAS_MAIN_GHOST:
                return checkSmfsHasMainGhost(condition, event);
            case SMFS_SPIRIT_ATTR_COMPARE:
                return checkSmfsSpiritAttrCompare(condition, event);
            case SMFS_GHOST_POSSESSION:
                return checkSmfsGhostPossession(condition, event);
            case SMFS_MAIN_GHOST_TYPE:
                return checkSmfsMainGhostType(condition, event);
            case SMFS_HAS_GHOST_DOMAIN:
                return checkSmfsHasGhostDomain(condition, event);
            case SMFS_GHOST_DOMAIN_TYPE:
                return checkSmfsGhostDomainType(condition, event);
            case SMFS_GHOST_DOMAIN_LEVEL:
                return checkSmfsGhostDomainLevel(condition, event);
            case SMFS_HAS_GHOST_EYE:
                return checkSmfsHasGhostEye(condition, event);
            case SMFS_GHOST_EYE_LEVEL:
                return checkSmfsGhostEyeLevel(condition, event);
            case SMFS_SLOT_LEVEL_COMPARE:
                return checkSmfsSlotLevelCompare(condition, event);
            case SMFS_SLOT_COUNT_COMPARE:
                return checkSmfsSlotCountCompare(condition, event);
            default:
                return true;
        }
    }

    private static boolean checkHealthCompare(ShenRule.ShenCondition condition, ShenEvent event) {
        if (event.source == null) return false;
        float value = Float.parseFloat(condition.value);
        float health = event.source.getHealth();
        switch (condition.operator) {
            case ">=": return health >= value;
            case "<=": return health <= value;
            case "==": return health == value;
            case ">": return health > value;
            case "<": return health < value;
            default: return false;
        }
    }

    private static boolean checkExperienceCompare(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof ServerPlayerEntity player)) return false;
        int value = Integer.parseInt(condition.value);
        int exp = player.totalExperience;
        switch (condition.operator) {
            case ">=": return exp >= value;
            case "<=": return exp <= value;
            case "==": return exp == value;
            case ">": return exp > value;
            case "<": return exp < value;
            default: return false;
        }
    }

    private static boolean checkNbtCompare(ShenRule.ShenCondition condition, ShenEvent event) {
        if (event.source == null) return false;
        String key = condition.params.getOrDefault("nbtKey", "");
        String expectedValue = condition.value;
        net.minecraft.nbt.NbtCompound nbt = new net.minecraft.nbt.NbtCompound();
        event.source.writeNbt(nbt);
        if (nbt.contains(key)) {
            return nbt.get(key).toString().contains(expectedValue);
        }
        return false;
    }

    private static boolean checkItemInHand(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof PlayerEntity player)) return false;
        String itemId = condition.value;
        net.minecraft.item.Item targetItem = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.tryParse(itemId));
        if (targetItem != null) {
            return player.getMainHandStack().isOf(targetItem);
        }
        return player.getMainHandStack().getItem().toString().contains(itemId);
    }

    private static boolean checkItemInInventory(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof PlayerEntity player)) return false;
        String itemId = condition.value;
        net.minecraft.item.Item targetItem = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.tryParse(itemId));
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (targetItem != null) {
                if (player.getInventory().getStack(i).isOf(targetItem)) {
                    return true;
                }
            } else {
                if (player.getInventory().getStack(i).getItem().toString().contains(itemId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean checkHasEffect(ShenRule.ShenCondition condition, ShenEvent event) {
        if (event.source == null) return false;
        String effectId = condition.value;
        net.minecraft.entity.effect.StatusEffect targetEffect = net.minecraft.registry.Registries.STATUS_EFFECT.get(net.minecraft.util.Identifier.tryParse(effectId));
        if (targetEffect != null) {
            return event.source.hasStatusEffect(targetEffect);
        }
        return event.source.getActiveStatusEffects().keySet().stream()
            .anyMatch(effect -> effect.toString().contains(effectId));
    }

    private static boolean checkInDimension(ShenRule.ShenCondition condition, ShenEvent event) {
        if (event.source == null || event.world == null) return false;
        String dimensionId = condition.value;
        net.minecraft.util.Identifier dimId = net.minecraft.util.Identifier.tryParse(dimensionId);
        if (dimId != null) {
            return event.world.getRegistryKey().getValue().equals(dimId);
        }
        return event.world.getRegistryKey().getValue().toString().contains(dimensionId);
    }

    private static boolean checkRandomChance(ShenRule.ShenCondition condition, ShenEvent event) {
        int chance = Integer.parseInt(condition.value);
        return Math.random() * 100 < chance;
    }

    private static boolean checkChatMessage(ShenRule.ShenCondition condition, ShenEvent event) {
        if (condition.value == null || condition.value.isEmpty()) return true;
        return event.chatMessage != null && event.chatMessage.contains(condition.value);
    }

    private static boolean checkTimeRange(ShenRule.ShenCondition condition, ShenEvent event) {
        if (event.world == null) return false;
        long time = event.world.getTimeOfDay() % 24000;
        long target = Long.parseLong(condition.value);
        String op = condition.operator != null ? condition.operator : "==";
        switch (op) {
            case ">=": return time >= target;
            case "<=": return time <= target;
            case "==": return Math.abs(time - target) < 1000;
            default: return false;
        }
    }

    private static boolean checkWeatherCondition(ShenRule.ShenCondition condition, ShenEvent event) {
        if (event.world == null) return false;
        switch (condition.value) {
            case "clear": return !event.world.isRaining();
            case "rain": return event.world.isRaining() && !event.world.isThundering();
            case "thunder": return event.world.isThundering();
            default: return false;
        }
    }

    private static boolean checkBiome(ShenRule.ShenCondition condition, ShenEvent event) {
        if (event.source == null) return false;
        Identifier biomeId = Identifier.tryParse(condition.value);
        if (biomeId == null) return false;
        return event.source.getWorld().getBiome(event.source.getBlockPos()).matchesId(biomeId);
    }

    private static boolean checkLightLevel(ShenRule.ShenCondition condition, ShenEvent event) {
        if (event.source == null) return false;
        int level = event.source.getWorld().getLightLevel(event.source.getBlockPos());
        int target = Integer.parseInt(condition.value);
        String op = condition.operator != null ? condition.operator : ">=";
        switch (op) {
            case ">=": return level >= target;
            case "<=": return level <= target;
            case "==": return level == target;
            default: return false;
        }
    }

    private static boolean checkIsSneaking(ShenRule.ShenCondition condition, ShenEvent event) {
        return event.source instanceof PlayerEntity && ((PlayerEntity) event.source).isSneaking();
    }

    private static boolean checkIsSprinting(ShenRule.ShenCondition condition, ShenEvent event) {
        return event.source instanceof PlayerEntity && ((PlayerEntity) event.source).isSprinting();
    }

    private static boolean checkItemHasEnchantment(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof PlayerEntity player)) return false;
        String enchantId = condition.value;
        if (enchantId == null || enchantId.isEmpty()) return false;
        net.minecraft.nbt.NbtList enchantments = player.getMainHandStack().getEnchantments();
        for (int i = 0; i < enchantments.size(); i++) {
            net.minecraft.nbt.NbtCompound compound = enchantments.getCompound(i);
            String id = compound.getString("id");
            if (enchantId.equals(id)) return true;
        }
        return false;
    }

    private static boolean checkIsOnFire(ShenRule.ShenCondition condition, ShenEvent event) {
        return event.source != null && event.source.isOnFire();
    }

    private static boolean checkIsInWater(ShenRule.ShenCondition condition, ShenEvent event) {
        return event.source != null && event.source.isTouchingWater();
    }

    private static boolean checkIsInLava(ShenRule.ShenCondition condition, ShenEvent event) {
        return event.source != null && event.source.isInLava();
    }

    private static boolean checkPlayerDistance(ShenRule.ShenCondition condition, ShenEvent event) {
        if (event.source == null || event.target == null) return false;
        double distance = event.source.distanceTo(event.target);
        double value = Double.parseDouble(condition.value);
        switch (condition.operator) {
            case ">=": return distance >= value;
            case "<=": return distance <= value;
            case "==": return Math.abs(distance - value) < 0.5;
            case ">": return distance > value;
            case "<": return distance < value;
            default: return false;
        }
    }

    private static boolean checkVariableCompare(ShenRule.ShenCondition condition, ShenEvent event) {
        if (event.world == null || event.world.getServer() == null) return false;
        String varKey = condition.params.getOrDefault("varKey", "default");
        String scope = condition.params.getOrDefault("scope", "player");
        ShenVariableStore store = ShenVariableStore.get(event.world.getServer());
        String varValue;
        if ("global".equals(scope)) {
            varValue = store.getGlobal(varKey);
        } else {
            if (event.source instanceof ServerPlayerEntity player) {
                varValue = store.getPlayer(player.getUuid(), varKey);
            } else {
                return false;
            }
        }
        double varNum = Double.parseDouble(varValue);
        double compareNum = Double.parseDouble(condition.value);
        switch (condition.operator) {
            case ">=": return varNum >= compareNum;
            case "<=": return varNum <= compareNum;
            case "==": return Math.abs(varNum - compareNum) < 0.001;
            case ">": return varNum > compareNum;
            case "<": return varNum < compareNum;
            default: return false;
        }
    }

    private static boolean checkGamemode(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof ServerPlayerEntity player)) return false;
        String mode = condition.value;
        return player.interactionManager.getGameMode().getName().equalsIgnoreCase(mode);
    }

    private static boolean checkFoodLevel(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof PlayerEntity player)) return false;
        int value = Integer.parseInt(condition.value);
        int food = player.getHungerManager().getFoodLevel();
        switch (condition.operator) {
            case ">=": return food >= value;
            case "<=": return food <= value;
            case "==": return food == value;
            case ">": return food > value;
            case "<": return food < value;
            default: return false;
        }
    }

    private static boolean checkPlayerLevel(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof PlayerEntity player)) return false;
        int value = Integer.parseInt(condition.value);
        int level = player.experienceLevel;
        switch (condition.operator) {
            case ">=": return level >= value;
            case "<=": return level <= value;
            case "==": return level == value;
            case ">": return level > value;
            case "<": return level < value;
            default: return false;
        }
    }

    private static boolean checkEntityType(ShenRule.ShenCondition condition, ShenEvent event) {
        if (event.source == null) return false;
        String typeId = condition.value;
        return event.source.getType().getUntranslatedName().contains(typeId)
            || net.minecraft.registry.Registries.ENTITY_TYPE.getId(event.source.getType()).toString().contains(typeId);
    }

    private static boolean checkEntityExists(ShenRule.ShenCondition condition, ShenEvent event) {
        if (event.source == null) return false;
        return !event.source.isRemoved();
    }

    private static boolean checkElytraFlying(ShenRule.ShenCondition condition, ShenEvent event) {
        return event.source instanceof net.minecraft.entity.player.PlayerEntity p
            && p.isFallFlying();
    }

    private static boolean checkGliding(ShenRule.ShenCondition condition, ShenEvent event) {
        // 1.20.1中isGliding()方法不存在，通过isFallFlying()替代（鞘翅飞行时滑翔）
        return event.source != null && event.source.isFallFlying();
    }

    private static boolean checkSwimming(ShenRule.ShenCondition condition, ShenEvent event) {
        return event.source != null && event.source.isSwimming();
    }

    private static boolean checkRiding(ShenRule.ShenCondition condition, ShenEvent event) {
        return event.source != null && event.source.hasVehicle();
    }

    private static boolean checkWearingArmor(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof net.minecraft.entity.player.PlayerEntity p)) return false;
        String slot = condition.value;
        if ("any".equals(slot)) {
            for (net.minecraft.item.ItemStack armor : p.getArmorItems()) {
                if (!armor.isEmpty()) return true;
            }
            return false;
        }
        net.minecraft.item.ItemStack stack = switch (slot) {
            case "head" -> p.getInventory().armor.get(3);
            case "chest" -> p.getInventory().armor.get(2);
            case "legs" -> p.getInventory().armor.get(1);
            case "feet" -> p.getInventory().armor.get(0);
            default -> net.minecraft.item.ItemStack.EMPTY;
        };
        return !stack.isEmpty();
    }

    private static boolean checkWearingItem(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof net.minecraft.entity.player.PlayerEntity p)) return false;
        String itemId = condition.value;
        for (net.minecraft.item.ItemStack armor : p.getArmorItems()) {
            if (!armor.isEmpty() && net.minecraft.registry.Registries.ITEM.getId(armor.getItem()).toString().equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkBlockBelow(ShenRule.ShenCondition condition, ShenEvent event) {
        if (event.world == null || event.pos == null) return false;
        String blockId = condition.value;
        net.minecraft.util.math.BlockPos below = event.pos.down();
        net.minecraft.block.Block block = event.world.getBlockState(below).getBlock();
        return net.minecraft.registry.Registries.BLOCK.getId(block).toString().equals(blockId);
    }

    private static boolean checkBlockAbove(ShenRule.ShenCondition condition, ShenEvent event) {
        if (event.world == null || event.pos == null) return false;
        String blockId = condition.value;
        net.minecraft.util.math.BlockPos above = event.pos.up();
        net.minecraft.block.Block block = event.world.getBlockState(above).getBlock();
        return net.minecraft.registry.Registries.BLOCK.getId(block).toString().equals(blockId);
    }

    private static boolean checkPermissionLevel(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof net.minecraft.server.network.ServerPlayerEntity p)) return false;
        int required = Integer.parseInt(condition.value);
        return p.server.getPermissionLevel(p.getGameProfile()) >= required;
    }

    private static boolean checkAttributeValue(ShenRule.ShenCondition condition, ShenEvent event) {
        if (event.source == null) return false;
        try {
            String attrId = condition.value;
            net.minecraft.entity.attribute.EntityAttribute attr = net.minecraft.registry.Registries.ATTRIBUTE.get(
                net.minecraft.util.Identifier.tryParse(attrId));
            if (attr == null) return false;
            double actualValue = event.source.getAttributeValue(attr);
            double targetValue = Double.parseDouble(condition.params.getOrDefault("compareValue", "0"));
            switch (condition.operator) {
                case ">=": return actualValue >= targetValue;
                case "<=": return actualValue <= targetValue;
                case "==": return actualValue == targetValue;
                case ">": return actualValue > targetValue;
                case "<": return actualValue < targetValue;
                default: return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkBlockInHand(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof net.minecraft.entity.player.PlayerEntity p)) return false;
        String blockId = condition.value;
        net.minecraft.item.ItemStack main = p.getMainHandStack();
        net.minecraft.block.Block block = net.minecraft.block.Block.getBlockFromItem(main.getItem());
        if (block != null && !block.getDefaultState().isAir()) {
            return net.minecraft.registry.Registries.BLOCK.getId(block).toString().equals(blockId);
        }
        return false;
    }

    private static boolean checkItemName(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof net.minecraft.entity.player.PlayerEntity p)) return false;
        String keyword = condition.params.getOrDefault("nameKeyword", condition.value);
        if (keyword.isEmpty()) return false;
        net.minecraft.item.ItemStack stack = p.getMainHandStack();
        if (stack.isEmpty()) return false;
        String itemName = stack.getName().getString();
        boolean matchMode = "exact".equals(condition.params.getOrDefault("nameMatchMode", "contains"));
        return matchMode ? itemName.equals(keyword) : itemName.contains(keyword);
    }

    private static boolean checkSpecificPlayer(ShenRule.ShenCondition condition, ShenEvent event) {
        String playerName = condition.params.getOrDefault("playerName", "");
        String playerUuid = condition.params.getOrDefault("playerUuid", "");
        if (playerName.isEmpty() && playerUuid.isEmpty()) return false;
        if (!(event.source instanceof net.minecraft.entity.player.PlayerEntity source)) return false;
        net.minecraft.server.MinecraftServer server = source.getServer();
        if (server == null) return false;
        for (net.minecraft.server.network.ServerPlayerEntity online : server.getPlayerManager().getPlayerList()) {
            if (!playerName.isEmpty() && online.getName().getString().equalsIgnoreCase(playerName)) return true;
            if (!playerUuid.isEmpty() && online.getUuid().toString().equalsIgnoreCase(playerUuid)) return true;
        }
        return false;
    }

    private static boolean checkMoonPhase(ShenRule.ShenCondition condition, ShenEvent event) {
        if (event.world == null) return false;
        int phase = event.world.getMoonPhase();
        return String.valueOf(phase).equals(condition.value);
    }

    private static boolean checkDifficulty(ShenRule.ShenCondition condition, ShenEvent event) {
        if (event.world == null) return false;
        return event.world.getDifficulty().getName().equals(condition.value);
    }

    private static boolean checkInStructure(ShenRule.ShenCondition condition, ShenEvent event) {
        if (event.source == null || event.world == null) return false;
        String structureId = condition.value;
        if (structureId == null || structureId.isEmpty()) return false;
        BlockPos pos = event.source.getBlockPos();
        net.minecraft.util.Identifier expectedId = net.minecraft.util.Identifier.tryParse(structureId);

        if (event.world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            net.minecraft.util.math.ChunkPos chunkPos = new net.minecraft.util.math.ChunkPos(pos);
            net.minecraft.world.chunk.Chunk chunk = serverWorld.getChunk(chunkPos.x, chunkPos.z);
            var starts = chunk.getStructureStarts();

            for (net.minecraft.world.gen.structure.Structure s : starts.keySet()) {
                net.minecraft.util.Identifier id = serverWorld.getRegistryManager()
                    .get(net.minecraft.registry.RegistryKeys.STRUCTURE).getId(s);
                if (expectedId != null && id != null && id.equals(expectedId)) {
                    return true;
                }
                if (id != null && id.toString().contains(structureId)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private static boolean checkStructureDistance(ShenRule.ShenCondition condition, ShenEvent event) {
        if (event.source == null || event.world == null) return false;
        if (!(event.world instanceof net.minecraft.server.world.ServerWorld serverWorld)) return false;
        String structureId = condition.value;
        double maxDistance = Double.parseDouble(condition.params.getOrDefault("maxDistance", "100"));
        BlockPos pos = event.source.getBlockPos();
        net.minecraft.util.Identifier expectedId = net.minecraft.util.Identifier.tryParse(structureId);

        int range = (int) Math.ceil(maxDistance);
        double closestDistance = Double.MAX_VALUE;

        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                BlockPos chunkPos = pos.add(dx * 16, 0, dz * 16);
                net.minecraft.world.chunk.Chunk chunk = serverWorld.getChunk(chunkPos);
                var starts = chunk.getStructureStarts();
                for (var entry : starts.entrySet()) {
                    net.minecraft.world.gen.structure.Structure structure = entry.getKey();
                    net.minecraft.util.Identifier id = serverWorld.getRegistryManager()
                        .get(net.minecraft.registry.RegistryKeys.STRUCTURE).getId(structure);
                    if (expectedId != null && id != null && id.equals(expectedId)) {
                        var start = entry.getValue();
                        double dist = Math.sqrt(start.getBoundingBox().getCenter()
                            .getSquaredDistance(pos));
                        if (dist < closestDistance) {
                            closestDistance = dist;
                        }
                    } else if (id != null && id.toString().contains(structureId)) {
                        var start = entry.getValue();
                        double dist = Math.sqrt(start.getBoundingBox().getCenter()
                            .getSquaredDistance(pos));
                        if (dist < closestDistance) {
                            closestDistance = dist;
                        }
                    }
                }
            }
        }

        double compareValue = Double.parseDouble(condition.value);
        String op = condition.operator != null ? condition.operator : "<=";
        switch (op) {
            case ">=": return closestDistance >= compareValue;
            case "<=": return closestDistance <= compareValue;
            case "==": return Math.abs(closestDistance - compareValue) < 1;
            case ">": return closestDistance > compareValue;
            case "<": return closestDistance < compareValue;
            default: return false;
        }
    }

    private static boolean checkSmfsHasGhostType(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof net.minecraft.entity.player.PlayerEntity player)) return false;
        if (!bai.god.shen.compat.smfs.SmfsIntegration.isSmfsLoaded()) return false;
        String ghostTypeId = condition.value;
        if (ghostTypeId.isEmpty()) return false;
        return bai.god.shen.compat.smfs.SmfsIntegration.hasGhostType(player, ghostTypeId);
    }

    private static boolean checkSmfsSlotOccupied(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof net.minecraft.entity.player.PlayerEntity player)) return false;
        if (!bai.god.shen.compat.smfs.SmfsIntegration.isSmfsLoaded()) return false;
        int slotIndex = Integer.parseInt(condition.params.getOrDefault("slotIndex", "0"));
        boolean expectOccupied = "true".equalsIgnoreCase(condition.value);
        return bai.god.shen.compat.smfs.SmfsIntegration.isSlotOccupied(player, slotIndex) == expectOccupied;
    }

    private static boolean checkSmfsHasMainGhost(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof net.minecraft.entity.player.PlayerEntity player)) return false;
        if (!bai.god.shen.compat.smfs.SmfsIntegration.isSmfsLoaded()) return false;
        boolean expected = "true".equalsIgnoreCase(condition.value);
        return bai.god.shen.compat.smfs.SmfsIntegration.hasMainGhost(player) == expected;
    }

    private static boolean checkSmfsSpiritAttrCompare(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof net.minecraft.entity.player.PlayerEntity player)) return false;
        if (!bai.god.shen.compat.smfs.SmfsIntegration.isSmfsLoaded()) return false;
        String attrKey = condition.params.getOrDefault("spiritAttr", "spiritResistance");
        double attrValue;
        switch (attrKey) {
            case "spiritResistance":
                attrValue = bai.god.shen.compat.smfs.SmfsIntegration.getSpiritResistance(player);
                break;
            case "spiritDamage":
                attrValue = bai.god.shen.compat.smfs.SmfsIntegration.getSpiritDamage(player);
                break;
            case "currentSpirit":
                attrValue = bai.god.shen.compat.smfs.SmfsIntegration.getCurrentSpirit(player);
                break;
            case "maxSpirit":
                attrValue = bai.god.shen.compat.smfs.SmfsIntegration.getMaxSpirit(player);
                break;
            case "sanity":
                attrValue = bai.god.shen.compat.smfs.SmfsIntegration.getSanity(player);
                break;
            case "revivalFactor":
                attrValue = bai.god.shen.compat.smfs.SmfsIntegration.getRevivalFactor(player);
                break;
            default:
                attrValue = bai.god.shen.compat.smfs.SmfsIntegration.getNbtAttribute(player, attrKey, 0);
                break;
        }
        double compareValue = Double.parseDouble(condition.params.getOrDefault("compareValue", "0"));
        String op = condition.operator != null ? condition.operator : ">=";
        switch (op) {
            case ">=": return attrValue >= compareValue;
            case "<=": return attrValue <= compareValue;
            case "==": return Math.abs(attrValue - compareValue) < 0.001;
            case ">": return attrValue > compareValue;
            case "<": return attrValue < compareValue;
            case "!=": return Math.abs(attrValue - compareValue) >= 0.001;
            default: return false;
        }
    }

    private static boolean checkSmfsGhostPossession(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof net.minecraft.entity.player.PlayerEntity player)) return false;
        if (!bai.god.shen.compat.smfs.SmfsIntegration.isSmfsLoaded()) return false;
        int occupied = bai.god.shen.compat.smfs.SmfsIntegration.countOccupiedSlots(player);
        int total = bai.god.shen.compat.smfs.SmfsIntegration.getTotalSlotCount();
        double percent = total > 0 ? (double) occupied / total : 0;
        double comparePercent = Double.parseDouble(condition.params.getOrDefault("compareValue", "50")) / 100.0;
        String op = condition.operator != null ? condition.operator : ">=";
        switch (op) {
            case ">=": return percent >= comparePercent;
            case "<=": return percent <= comparePercent;
            case "==": return Math.abs(percent - comparePercent) < 0.01;
            case ">": return percent > comparePercent;
            case "<": return percent < comparePercent;
            default: return false;
        }
    }

    private static boolean checkSmfsMainGhostType(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof net.minecraft.entity.player.PlayerEntity player)) return false;
        if (!bai.god.shen.compat.smfs.SmfsIntegration.isSmfsLoaded()) return false;
        if (!bai.god.shen.compat.smfs.SmfsIntegration.hasMainGhost(player)) return false;
        int mainSlot = bai.god.shen.compat.smfs.SmfsIntegration.getMainGhostSlot(player);
        String mainType = bai.god.shen.compat.smfs.SmfsIntegration.getGhostTypeInSlot(player, mainSlot);
        String expectedType = condition.value;
        if (mainType == null || expectedType.isEmpty()) return false;
        String op = condition.operator != null ? condition.operator : "==";
        if ("==".equals(op)) return mainType.equals(expectedType);
        if ("!=".equals(op)) return !mainType.equals(expectedType);
        return mainType.contains(expectedType);
    }

    private static boolean checkSmfsHasGhostDomain(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof net.minecraft.entity.player.PlayerEntity player)) return false;
        if (!bai.god.shen.compat.smfs.SmfsIntegration.isSmfsLoaded()) return false;
        boolean expected = "true".equalsIgnoreCase(condition.value);
        return bai.god.shen.compat.smfs.SmfsIntegration.isGhostDomainActive(player) == expected;
    }

    private static boolean checkSmfsGhostDomainType(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof net.minecraft.entity.player.PlayerEntity player)) return false;
        if (!bai.god.shen.compat.smfs.SmfsIntegration.isSmfsLoaded()) return false;
        int expectedColor = Integer.parseInt(condition.value);
        return bai.god.shen.compat.smfs.SmfsIntegration.getGhostDomainColor(player) == expectedColor;
    }

    private static boolean checkSmfsGhostDomainLevel(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof net.minecraft.entity.player.PlayerEntity player)) return false;
        if (!bai.god.shen.compat.smfs.SmfsIntegration.isSmfsLoaded()) return false;
        int level = bai.god.shen.compat.smfs.SmfsIntegration.getGhostDomainLevel(player);
        int compareLevel = Integer.parseInt(condition.params.getOrDefault("compareValue", "1"));
        String op = condition.operator != null ? condition.operator : ">=";
        switch (op) {
            case ">=": return level >= compareLevel;
            case "<=": return level <= compareLevel;
            case "==": return level == compareLevel;
            case ">": return level > compareLevel;
            case "<": return level < compareLevel;
            case "!=": return level != compareLevel;
            default: return false;
        }
    }

    private static boolean checkSmfsHasGhostEye(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof net.minecraft.entity.player.PlayerEntity player)) return false;
        if (!bai.god.shen.compat.smfs.SmfsIntegration.isSmfsLoaded()) return false;
        boolean expected = "true".equalsIgnoreCase(condition.value);
        return bai.god.shen.compat.smfs.SmfsIntegration.isGhostEyeActive(player) == expected;
    }

    private static boolean checkSmfsGhostEyeLevel(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof net.minecraft.entity.player.PlayerEntity player)) return false;
        if (!bai.god.shen.compat.smfs.SmfsIntegration.isSmfsLoaded()) return false;
        int level = bai.god.shen.compat.smfs.SmfsIntegration.getGhostEyeLevel(player);
        int compareLevel = Integer.parseInt(condition.params.getOrDefault("compareValue", "1"));
        String op = condition.operator != null ? condition.operator : ">=";
        switch (op) {
            case ">=": return level >= compareLevel;
            case "<=": return level <= compareLevel;
            case "==": return level == compareLevel;
            case ">": return level > compareLevel;
            case "<": return level < compareLevel;
            case "!=": return level != compareLevel;
            default: return false;
        }
    }

    private static boolean checkSmfsSlotLevelCompare(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof net.minecraft.entity.player.PlayerEntity player)) return false;
        if (!bai.god.shen.compat.smfs.SmfsIntegration.isSmfsLoaded()) return false;
        int slotIndex = Integer.parseInt(condition.params.getOrDefault("slotIndex", "0"));
        int level = bai.god.shen.compat.smfs.SmfsIntegration.getSlotLevel(player, slotIndex);
        int compareLevel = Integer.parseInt(condition.params.getOrDefault("compareValue", "1"));
        String op = condition.operator != null ? condition.operator : ">=";
        switch (op) {
            case ">=": return level >= compareLevel;
            case "<=": return level <= compareLevel;
            case "==": return level == compareLevel;
            case ">": return level > compareLevel;
            case "<": return level < compareLevel;
            default: return false;
        }
    }

    private static boolean checkSmfsSlotCountCompare(ShenRule.ShenCondition condition, ShenEvent event) {
        if (!(event.source instanceof net.minecraft.entity.player.PlayerEntity player)) return false;
        if (!bai.god.shen.compat.smfs.SmfsIntegration.isSmfsLoaded()) return false;
        int occupied = bai.god.shen.compat.smfs.SmfsIntegration.countOccupiedSlots(player);
        int compareCount = Integer.parseInt(condition.params.getOrDefault("compareValue", "1"));
        String op = condition.operator != null ? condition.operator : ">=";
        switch (op) {
            case ">=": return occupied >= compareCount;
            case "<=": return occupied <= compareCount;
            case "==": return occupied == compareCount;
            case ">": return occupied > compareCount;
            case "<": return occupied < compareCount;
            case "!=": return occupied != compareCount;
            default: return false;
        }
    }

    private static boolean checkIsAttacking(ShenRule.ShenCondition condition, ShenEvent event) {
        return event.source != null && event.source.getAttacking() != null;
    }
}