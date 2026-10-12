
package bai.god.support.smfs;

import net.minecraft.registry.Registries;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmfsIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger("god-smfs-integration");
    private static boolean isSmfsLoaded = true;

    public static boolean hasGhostEyeEquipped(PlayerEntity player) {
        if (!isSmfsLoaded) {
            LOGGER.debug("神秘复苏模组未加载，无法检测鬼眼装备");
            return false;
        }
        try {
            if (detectGhostEyeEquipped(player)) {
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.error("检测鬼眼装备时发生错误: {}", (Object)e.getMessage());
        }
        try {
            if (bai.god.support.GoldenGhostEyePassiveHandler.isGoldenGhostEyeMain(player)) {
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static boolean isGhostDomainActive(PlayerEntity player) {
        if (!isSmfsLoaded) {
            LOGGER.debug("神秘复苏模组未加载，无法检测鬼域状态");
            return false;
        }

        try {
            Class<?> ghostDomainManagerClass = Class.forName("com.xie.smfs.manager.GhostDomainManager");
            Method getGhostDomainLevelMethod = ghostDomainManagerClass.getMethod("getGhostDomainLevel", PlayerEntity.class);
            Object result = getGhostDomainLevelMethod.invoke(null, player);
            if (result instanceof Integer) {
                int level = (Integer)result;
                LOGGER.debug("通过反射检测到鬼域等级: {}", level);
                return level > 0;
            }
        }
        catch (Exception e) {
            LOGGER.debug("反射检测鬼域状态失败: {}", e.getMessage());
        }

        try {
            NbtCompound nbt = player.writeNbt(new NbtCompound());

            String[] activePaths = {
                "smfs:ghost_domain_active", "smfs:domain_active",
                "smfs:ghost_domain_enabled", "ForgeData.smfs_ghost_domain_active"
            };

            for (String path : activePaths) {
                if (nbt.contains(path)) {
                    boolean active = nbt.getBoolean(path);
                    LOGGER.debug("通过NBT路径 {} 检测到鬼域状态: {}", path, active);
                    return active;
                }
            }

            String[] levelPaths = {
                "smfs:ghost_domain_level", "smfs:domain_level",
                "ForgeData.smfs_ghost_domain_level"
            };

            for (String path : levelPaths) {
                if (nbt.contains(path)) {
                    int level = nbt.getInt(path);
                    LOGGER.debug("通过NBT路径 {} 检测到鬼域等级: {}", path, level);
                    return level > 0;
                }
            }
        }
        catch (Exception e) {
            LOGGER.debug("NBT检测鬼域状态失败: {}", e.getMessage());
        }

        LOGGER.debug("所有检测方法失败，返回false");
        return false;
    }

    public static boolean isGhostEyeDomainActive(PlayerEntity player) {
        if (!isSmfsLoaded) {
            LOGGER.debug("神秘复苏模组未加载，返回默认鬼眼鬼域状态");
            return false;
        }
        try {
            return SmfsIntegration.detectGhostEyeDomainActive(player);
        }
        catch (Exception e) {
            LOGGER.error("检测鬼眼鬼域状态时发生错误: {}", (Object)e.getMessage());
            return false;
        }
    }

    public static int getGhostDomainLevel(PlayerEntity player) {
        if (!isSmfsLoaded) {
            LOGGER.debug("神秘复苏模组未加载，无法检测鬼域等级");
            return 0;
        }
        try {
            return SmfsIntegration.detectGhostDomainLevel(player);
        }
        catch (Exception e) {
            LOGGER.error("检测鬼域等级时发生错误: {}", (Object)e.getMessage());
            return 0;
        }
    }

    public static int getGhostEyeLevel(PlayerEntity player) {
        if (!isSmfsLoaded) {
            LOGGER.debug("神秘复苏模组未加载，无法检测鬼等级");
            return 0;
        }
        try {
            return SmfsIntegration.detectGhostEyeLevel(player);
        }
        catch (Exception e) {
            LOGGER.error("检测鬼眼等级时发生错误: {}", (Object)e.getMessage());
            return 0;
        }
    }

    public static int getGhostEyeRevivalLevel(PlayerEntity player) {
        if (!isSmfsLoaded) {
            LOGGER.debug("神秘复苏模组未加载，无法检测复苏程度");
            return 0;
        }
        try {
            return SmfsIntegration.detectGhostEyeRevivalLevel(player);
        }
        catch (Exception e) {
            LOGGER.error("检测复苏程度时发生错误: {}", (Object)e.getMessage());
            return 0;
        }
    }

    private static boolean detectGhostEyeEquipped(PlayerEntity player) {
        try {
            Class<?> ghostDomainManagerClass = Class.forName("com.xie.smfs.manager.GhostDomainManager");
            Method hasGhostEyeMethod = ghostDomainManagerClass.getMethod("hasGhostEye", PlayerEntity.class);
            Object result = hasGhostEyeMethod.invoke(null, player);
            if (result instanceof Boolean) {
                LOGGER.debug("通过反射检测到鬼眼装备状态: {}", result);
                return (Boolean)result;
            }
        }
        catch (Exception e) {
            LOGGER.debug("反射检测鬼眼装备失败: {}", (Object)e.getMessage());
        }
        try {
            String[] nbtPaths;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            String[] stringArray = nbtPaths = new String[]{"smfs:ghost_eye_equipped", "smfs:silent_ghost_equipped", "smfs:ghost_eye_active", "ForgeData.smfs_ghost_eye_equipped", "ForgeData.smfs_silent_ghost_equipped"};
            int n = stringArray.length;
            for (int i = 0; i < n; ++i) {
                String path = stringArray[i];
                if (!nbt.contains(path)) continue;
                boolean equipped = nbt.getBoolean(path);
                LOGGER.debug("通过NBT路径 {} 检测到鬼眼装备状态: {}", (Object)path, (Object)equipped);
                return equipped;
            }
        }
        catch (Exception e) {
            LOGGER.debug("NBT检测鬼眼装备失败: {}", (Object)e.getMessage());
        }
        try {
            String[] ghostEyeItems;
            for (String itemName : ghostEyeItems = new String[]{"ghost_eye", "silent_ghost_eye", "ghost_eye_item"}) {
                Identifier itemId = new Identifier("smfs", itemName);
                Item item = (Item)Registries.ITEM.get(itemId);
                if (item == null || !player.getInventory().contains(item.getDefaultStack())) continue;
                LOGGER.debug("检测到玩家物品栏中有鬼眼物品: {}", (Object)itemName);
                return true;
            }
            Identifier goldenGhostEyeId = new Identifier("god", "golden_ghost_eye");
            Item goldenGhostEyeItem = (Item)Registries.ITEM.get(goldenGhostEyeId);
            if (goldenGhostEyeItem != null && player.getInventory().contains(goldenGhostEyeItem.getDefaultStack())) {
                LOGGER.debug("检测到玩家物品栏中有金色鬼眼物品");
                return true;
            }
            if (SmfsIntegration.isGhostEyeItem(player.getMainHandStack()) || SmfsIntegration.isGhostEyeItem(player.getOffHandStack())) {
                LOGGER.debug("检测到玩家手持鬼眼物品");
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.debug("物品检测鬼眼装备失败: {}", (Object)e.getMessage());
        }
        LOGGER.debug("所有检测方法失败，返回false");
        return false;
    }

    private static boolean detectGhostEyeDomainActive(PlayerEntity player) {
        try {
            Class<?> ghostDomainManagerClass = Class.forName("com.xie.smfs.manager.GhostDomainManager");
            Method isGhostEyeDomainMethod = ghostDomainManagerClass.getMethod("isGhostEyeDomainActive", PlayerEntity.class);
            Object result = isGhostEyeDomainMethod.invoke(null, player);
            if (result instanceof Boolean) {
                LOGGER.debug("通过反射检测到鬼眼鬼域状态: {}", result);
                return (Boolean)result;
            }
        }
        catch (Exception e) {
            LOGGER.debug("反射检测鬼眼鬼域状态失败: {}", (Object)e.getMessage());
        }
        try {
            if (bai.god.support.smfs.SmfsAPIWrapper.hasGoldenGhostDomain(player)) {
                LOGGER.debug("直接检测到金色鬼域状态");
                return true;
            }
            String[] ghostEyePaths;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            for (String path : ghostEyePaths = new String[]{"smfs:ghost_eye_domain_active", "smfs:silent_ghost_eye_domain_active", "smfs:ghost_eye_active", "smfs:silent_ghost_eye_active", "ForgeData.smfs_ghost_eye_domain_active", "ForgeData.smfs_silent_ghost_eye_domain_active", "smfs_spirit_data.ghost_eye_domain_active", "smfs_spirit_data.silent_ghost_eye_domain_active"}) {
                if (!nbt.contains(path)) continue;
                boolean active = nbt.getBoolean(path);
                LOGGER.debug("通过NBT路径 {} 检测到鬼眼鬼域状态: {}", (Object)path, (Object)active);
                return active;
            }
            if (SmfsIntegration.detectGhostEyeEquipped(player)) {
                if (bai.god.support.smfs.SmfsAPIWrapper.hasRedGhostDomain(player)) {
                    LOGGER.debug("检测到鬼眼鬼域");
                    return true;
                }
            }
        }
        catch (Exception e) {
            LOGGER.debug("NBT检测鬼眼鬼域状态失败: {}", (Object)e.getMessage());
        }
        LOGGER.debug("所有检测方法失败，返回false");
        return false;
    }

    private static boolean detectGhostDomainActive(PlayerEntity player) {
        try {
            Class<?> ghostDomainManagerClass = Class.forName("com.xie.smfs.manager.GhostDomainManager");
            Method getGhostDomainLevelMethod = ghostDomainManagerClass.getMethod("getGhostDomainLevel", PlayerEntity.class);
            Object result = getGhostDomainLevelMethod.invoke(null, player);
            if (result instanceof Integer) {
                int level = (Integer)result;
                LOGGER.debug("通过反射检测到鬼域等级: {}", (Object)level);
                return level > 0;
            }
        }
        catch (Exception e) {
            LOGGER.debug("反射检测鬼域状态失败: {}", (Object)e.getMessage());
        }
        try {
            String[] levelPaths;
            String[] activePaths;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            for (String path : activePaths = new String[]{"smfs:ghost_domain_active", "smfs:domain_active", "smfs:ghost_domain_enabled", "ForgeData.smfs_ghost_domain_active"}) {
                if (!nbt.contains(path)) continue;
                boolean active = nbt.getBoolean(path);
                LOGGER.debug("通过NBT路径 {} 检测到鬼域状态: {}", (Object)path, (Object)active);
                return active;
            }
            for (String path : levelPaths = new String[]{"smfs:ghost_domain_level", "smfs:domain_level", "ForgeData.smfs_ghost_domain_level"}) {
                if (!nbt.contains(path)) continue;
                int level = nbt.getInt(path);
                LOGGER.debug("通过NBT路径 {} 检测到鬼域等级: {}", (Object)path, (Object)level);
                return level > 0;
            }
        }
        catch (Exception e) {
            LOGGER.debug("NBT检测鬼域状态失败: {}", (Object)e.getMessage());
        }
        LOGGER.debug("所有检测方法失败，返回false");
        return false;
    }

    private static int detectGhostDomainLevel(PlayerEntity player) {
        try {
            Class<?> ghostDomainManagerClass = Class.forName("com.xie.smfs.manager.GhostDomainManager");
            Method getGhostDomainLevelMethod = ghostDomainManagerClass.getMethod("getGhostDomainLevel", PlayerEntity.class);
            Object result = getGhostDomainLevelMethod.invoke(null, player);
            if (result instanceof Integer) {
                LOGGER.debug("通过反射检测到鬼域等级: {}", result);
                return (Integer)result;
            }
        }
        catch (Exception e) {
            LOGGER.debug("反射检测鬼域等级失败: {}", (Object)e.getMessage());
        }
        try {
            String[] levelPaths;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            for (String path : levelPaths = new String[]{"smfs:ghost_domain_level", "smfs:domain_level", "smfs:ghost_eye_level", "smfs:level", "ForgeData.smfs_ghost_domain_level", "ForgeData.smfs_domain_level"}) {
                if (!nbt.contains(path)) continue;
                int level = nbt.getInt(path);
                LOGGER.debug("通过NBT路径 {} 检测到鬼域等级: {}", (Object)path, (Object)level);
                return level;
            }
        }
        catch (Exception e) {
            LOGGER.debug("NBT检测鬼域等级失败: {}", (Object)e.getMessage());
        }
        LOGGER.debug("所有检测方法失败，返回0");
        return 0;
    }

    private static int detectGhostEyeLevel(PlayerEntity player) {
        LOGGER.debug("开始检测鬼眼等级...");
        try {
            Class<?> ghostDomainManagerClass = Class.forName("com.xie.smfs.manager.GhostDomainManager");
            Method getGhostDomainLevelMethod = ghostDomainManagerClass.getMethod("getGhostDomainLevel", PlayerEntity.class);
            Object result = getGhostDomainLevelMethod.invoke(null, player);
            if (result instanceof Integer) {
                int level = (Integer)result;
                LOGGER.debug("通过反射检测到鬼眼等级: {}", (Object)level);
                return level;
            }
        }
        catch (Exception e) {
            LOGGER.debug("反射检测鬼眼等级失败: {}", (Object)e.getMessage());
        }
        try {
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("玩家NBT数据: {}", (Object)nbt.toString());
            }
            if (nbt.contains("smfs_spirit_data")) {
                NbtCompound spiritData = nbt.getCompound("smfs_spirit_data");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("smfs_spirit_data内容: {}", (Object)spiritData.toString());
                }
                String[] levelPaths = new String[]{"ghost_eye_level", "silent_ghost_eye_level", "level", "ghost_domain_level", "domain_level", "ghost_level", "revival_level", "ghost_eye_revival_level", "ghostEyeLevel", "silentGhostEyeLevel", "ghostDomainLevel", "domainLevel", "ghostLevel", "revivalLevel", "ghostEyeRevivalLevel"};
                for (String path : levelPaths) {
                    if (spiritData.contains(path)) {
                        int level = spiritData.getInt(path);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("通过smfs_spirit_data路径 {} 检测到鬼眼等级: {}", (Object)path, (Object)level);
                        }
                        return level;
                    }
                }
            }
            String[] levelPaths = new String[]{"smfs:ghost_eye_level", "smfs:silent_ghost_eye_level", "smfs:level", "smfs:ghost_domain_level", "smfs:domain_level", "smfs:ghost_level", "ForgeData.smfs_ghost_eye_level", "ForgeData.smfs_silent_ghost_eye_level", "ForgeData.smfs_level", "ForgeData.smfs_domain_level", "smfs:revival_level", "smfs:ghost_eye_revival_level", "ForgeData.smfs_revival_level", "ForgeData.smfs_ghost_eye_revival_level", "smfs_revival_level", "smfs_ghost_eye_revival_level"};
            for (String path : levelPaths) {
                if (nbt.contains(path)) {
                    int level = nbt.getInt(path);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("通过NBT路径 {} 检测到鬼眼等级: {}", (Object)path, (Object)level);
                    }
                    return level;
                }
            }
            if (nbt.contains("ForgeData")) {
                NbtCompound forgeData = nbt.getCompound("ForgeData");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("ForgeData内容: {}", (Object)forgeData.toString());
                }
                if (forgeData.contains("smfs")) {
                    int level;
                    NbtCompound smfsData = forgeData.getCompound("smfs");
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("ForgeData.smfs内容: {}", (Object)smfsData.toString());
                    }
                    if (smfsData.contains("ghost_eye_level")) {
                        level = smfsData.getInt("ghost_eye_level");
                        LOGGER.debug("通过深层NBT路径检测到鬼眼等级: {}", (Object)level);
                        return level;
                    }
                    if (smfsData.contains("level")) {
                        level = smfsData.getInt("level");
                        LOGGER.debug("通过深层NBT路径检测到等级: {}", (Object)level);
                        return level;
                    }
                }
            }
        }
        catch (Exception e) {
            LOGGER.debug("NBT检测鬼眼等级失败: {}", (Object)e.getMessage());
        }
        try {
            if (SmfsIntegration.detectGhostDomainActive(player)) {
                LOGGER.debug("等级检测失败，但鬼域已开启，推断等级>=1");
                return 1;
            }
        }
        catch (Exception e) {
            LOGGER.debug("推断检测失败: {}", (Object)e.getMessage());
        }
        LOGGER.debug("所有检测方法失败，返回0");
        return 0;
    }

    private static int detectGhostEyeRevivalLevel(PlayerEntity player) {
        try {
            Class<?> mainGhostManagerClass = Class.forName("com.xie.smfs.manager.MainGhostManager");
            Method getRevivalDegreeMethod = mainGhostManagerClass.getMethod("getRevivalDegree", PlayerEntity.class);
            Object result = getRevivalDegreeMethod.invoke(null, player);
            if (result instanceof Integer) {
                int revivalLevel = (Integer)result;
                LOGGER.debug("通过反射检测到复苏程度: {}", (Object)revivalLevel);
                return revivalLevel;
            }
        }
        catch (Exception e) {
            LOGGER.debug("反射检测复苏程度失败: {}", (Object)e.getMessage());
        }
        try {
            NbtCompound smfsData;
            NbtCompound forgeData;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            String ghostEyeSlotName = findGhostEyeSlotName(nbt);

            List<String> revivalPaths = new ArrayList<>();

            // 如果找到鬼眼Slot，添加鬼眼特定路径（最高优先级）
            if (ghostEyeSlotName != null) {
                revivalPaths.add("smfs_spirit_data.GhostSlots." + ghostEyeSlotName + ".revivalDegree");
                revivalPaths.add("smfs_spirit_data.GhostSlots." + ghostEyeSlotName + ".revival_level");
                revivalPaths.add("smfs_spirit_data.GhostSlots." + ghostEyeSlotName + ".revival");
            }

            // 通用路径（回退）
            revivalPaths.addAll(Arrays.asList(
                "revivalDegree", "smfs_spirit_data.revivalDegree",
                "smfs:revival_degree", "smfs:revival_level", "smfs:ghost_eye_revival",
                "smfs:silent_ghost_revival", "smfs:revival", "smfs:level",
                "ForgeData.smfs_revival_degree", "ForgeData.smfs_revival_level", "ForgeData.smfs_revival"
            ));

            for (String path : revivalPaths) {
                if (!nbt.contains(path)) continue;
                int revival = nbt.getInt(path);
                LOGGER.debug("通过NBT路径 {} 检测到复苏程度: {}", (Object)path, (Object)revival);
                return revival;
            }
            if (nbt.contains("ForgeData") && (forgeData = nbt.getCompound("ForgeData")).contains("smfs") && (smfsData = forgeData.getCompound("smfs")).contains("revival_degree")) {
                int revival = smfsData.getInt("revival_degree");
                LOGGER.debug("通过深层NBT路径检测到复苏程度: {}", (Object)revival);
                return revival;
            }
        }
        catch (Exception e) {
            LOGGER.debug("NBT检测复苏程度失败: {}", (Object)e.getMessage());
        }
        try {
            int ghostEyeLevel = SmfsIntegration.detectGhostEyeLevel(player);
            if (ghostEyeLevel > 0) {
                LOGGER.debug("复苏程度检测失败，使用鬼眼等级作为替代: {}", (Object)ghostEyeLevel);
                return ghostEyeLevel;
            }
        }
        catch (Exception e) {
            LOGGER.debug("替代检测失败: {}", (Object)e.getMessage());
        }
        LOGGER.debug("所有检测方法失败，返回0");
        return 0;
    }

    private static boolean isGhostEyeItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        try {
            Identifier itemId = Registries.ITEM.getId(stack.getItem());
            String namespace = itemId.getNamespace();
            String path = itemId.getPath();
            if ("smfs".equals(namespace)) {
                String[] ghostEyeItems = new String[]{"ghost_eye", "silent_ghost_eye", "ghost_eye_item", "silent_ghost"};
                for (String itemName : ghostEyeItems) {
                    if (itemName.equals(path)) {
                        return true;
                    }
                }
            }
            if ("guiyu".equals(namespace)) {
                if (path.contains("ghost_eye")) {
                    return true;
                }
            }
        }
        catch (Exception e) {
            LOGGER.debug("检测鬼眼物品失败: {}", (Object)e.getMessage());
        }
        return false;
    }

    public static int getGhostDomainRange(PlayerEntity player) {
        if (!isSmfsLoaded) {
            LOGGER.debug("神秘复苏模组未加载，使用默认鬼域范围");
            return 3;
        }
        try {
            return SmfsIntegration.detectGhostDomainRange(player);
        }
        catch (Exception e) {
            LOGGER.error("检测鬼域范围时发生错误: {}", (Object)e.getMessage());
            return 3;
        }
    }

    public static boolean increaseGhostEyeRevivalValue(PlayerEntity player, int amount) {
        if (!isSmfsLoaded) {
            LOGGER.debug("神秘复苏模组未加载，无法增加复苏值");
            return false;
        }
        try {
            return SmfsIntegration.detectAndIncreaseRevivalValue(player, amount);
        }
        catch (Exception e) {
            LOGGER.error("增加复苏值时发生错误: {}", (Object)e.getMessage());
            return false;
        }
    }

    private static int detectGhostDomainRange(PlayerEntity player) {
        try {
            Class<?> ghostDomainManagerClass = Class.forName("com.xie.smfs.manager.GhostDomainManager");
            Method getGhostDomainLevelMethod = ghostDomainManagerClass.getMethod("getGhostDomainLevel", PlayerEntity.class);
            Object result = getGhostDomainLevelMethod.invoke(null, player);
            if (result instanceof Integer) {
                int level = (Integer)result;
                int range = Math.max(1, level);
                LOGGER.debug("通过反射检测到鬼域范围: {} (基于等级 {})", (Object)range, (Object)level);
                return range;
            }
        }
        catch (Exception e) {
            LOGGER.debug("反射检测鬼域范围失败: {}", (Object)e.getMessage());
        }
        try {
            String[] rangePaths;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            for (String path : rangePaths = new String[]{"smfs:ghost_domain_range", "smfs:domain_range", "smfs:ghost_domain_size", "ForgeData.smfs_ghost_domain_range"}) {
                if (!nbt.contains(path)) continue;
                int range = nbt.getInt(path);
                LOGGER.debug("通过NBT路径 {} 检测到鬼域范围: {}", (Object)path, (Object)range);
                return Math.max(1, range);
            }
            int level = SmfsIntegration.detectGhostDomainLevel(player);
            int range = Math.max(1, level);
            LOGGER.debug("根据等级 {} 计算鬼域范围: {}", (Object)level, (Object)range);
            return range;
        }
        catch (Exception e) {
            LOGGER.debug("NBT检测鬼域范围失败: {}", (Object)e.getMessage());
            LOGGER.debug("所有检测方法失败，返回默认范围3");
            return 3;
        }
    }

    private static boolean detectAndIncreaseRevivalValue(PlayerEntity player, int amount) {
        try {
            Class<?> mainGhostManagerClass = Class.forName("com.xie.smfs.manager.MainGhostManager");
            Method increaseRevivalDegreeMethod = mainGhostManagerClass.getMethod("increaseRevivalDegree", PlayerEntity.class, Integer.TYPE);
            Object result = increaseRevivalDegreeMethod.invoke(null, player, amount);
            if (result instanceof Boolean) {
                LOGGER.debug("通过反射增加复苏值: {}点，结果: {}", (Object)amount, result);
                return (Boolean)result;
            }
        }
        catch (Exception e) {
            LOGGER.debug("反射增加复苏值失败: {}", (Object)e.getMessage());
        }
        try {
            String[] revivalPaths;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            for (String path : revivalPaths = new String[]{"smfs:revival_degree", "smfs:revival_level", "smfs:ghost_eye_revival", "ForgeData.smfs_revival_degree"}) {
                if (!nbt.contains(path)) continue;
                int currentRevival = nbt.getInt(path);
                int newRevival = currentRevival + amount;
                nbt.putInt(path, newRevival);
                player.readNbt(nbt);
                LOGGER.debug("通过NBT路径 {} 增加复苏值: {} -> {}", new Object[]{path, currentRevival, newRevival});
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.debug("NBT增加复苏值失败: {}", (Object)e.getMessage());
        }
        LOGGER.debug("所有增加复苏值方法失败");
        return false;
    }

    public static boolean isSmfsLoaded() {
        return isSmfsLoaded;
    }

    public static int getSpiritualStrength(PlayerEntity player) {
        try {
            return SmfsIntegration.detectSpiritualStrength(player);
        }
        catch (Exception e) {
            LOGGER.error("获取灵异强度时发生错误: {}", e.getMessage());
            try {
                NbtCompound nbt = player.writeNbt(new NbtCompound());
                String[] strengthPaths = {
                    "smfs:spiritual_strength", "smfs:strength", "smfs:spirit_strength",
                    "ForgeData.smfs_spiritual_strength", "smfs_spirit_data.spiritual_strength"
                };

                for (String path : strengthPaths) {
                    if (nbt.contains(path)) {
                        int strength = nbt.getInt(path);
                        LOGGER.debug("通过NBT路径 {} 检测到灵异强度: {}", path, strength);
                        return strength;
                    }
                }

                LOGGER.error("无法获取灵异强度，所有方法都失败");
                throw new RuntimeException("无法获取玩家灵异强度数值，请确保神秘复苏模组正确加载");
            } catch (Exception ex) {
                LOGGER.error("备选获取方法也失败: {}", ex.getMessage());
                throw new RuntimeException("无法获取玩家灵异强度数值，请确保神秘复苏模组正确加载");
            }
        }
    }

    public static int getMaxSpiritualStrength(PlayerEntity player) {
        try {
            return SmfsIntegration.detectMaxSpiritualStrength(player);
        }
        catch (Exception e) {
            LOGGER.error("获取最大灵异强度时发生错误: {}", e.getMessage());
            try {
                NbtCompound nbt = player.writeNbt(new NbtCompound());
                String[] maxStrengthPaths = {
                    "smfs:max_spiritual_strength", "smfs:max_strength", "smfs:max_spirit_strength",
                    "ForgeData.smfs_max_spiritual_strength", "smfs_spirit_data.max_spiritual_strength"
                };

                for (String path : maxStrengthPaths) {
                    if (nbt.contains(path)) {
                        int maxStrength = nbt.getInt(path);
                        LOGGER.debug("通过NBT路径 {} 检测到最大灵异强度: {}", path, maxStrength);
                        return maxStrength;
                    }
                }

                LOGGER.error("无法获取最大灵异强度，所有方法都失败");
                throw new RuntimeException("无法获取玩家最大灵异强度数值，请确保神秘复苏模组正确加载");
            } catch (Exception ex) {
                LOGGER.error("备选获取方法也失败: {}", ex.getMessage());
                throw new RuntimeException("无法获取玩家最大灵异强度数值，请确保神秘复苏模组正确加载");
            }
        }
    }

    public static int getSpiritualResistance(PlayerEntity player) {
        if (!isSmfsLoaded) {
            LOGGER.debug("神秘复苏模组未加载，返回默认灵异抗性");
            return 500;
        }
        try {
            return SmfsIntegration.detectSpiritualResistance(player);
        }
        catch (Exception e) {
            LOGGER.error("获取灵异抗性时发生错误: {}", (Object)e.getMessage());
            return 500;
        }
    }

    public static int getSpiritualPower(PlayerEntity player) {
        try {
            return SmfsIntegration.detectSpiritualPower(player);
        }
        catch (Exception e) {
            LOGGER.error("获取灵异力量时发生错误: {}", e.getMessage());
            try {
                NbtCompound nbt = player.writeNbt(new NbtCompound());
                String[] powerPaths = {
                    "smfs:spiritual_power", "smfs:power", "smfs:spirit_power",
                    "ForgeData.smfs_spiritual_power", "smfs_spirit_data.spiritual_power"
                };

                for (String path : powerPaths) {
                    if (nbt.contains(path)) {
                        int power = nbt.getInt(path);
                        LOGGER.debug("通过NBT路径 {} 检测到灵异力量: {}", path, power);
                        return power;
                    }
                }

                LOGGER.warn("无法获取灵异力量，使用灵异强度作为备选");
                return SmfsIntegration.getSpiritualStrength(player);
            } catch (Exception ex) {
                LOGGER.error("备选获取方法也失败: {}", ex.getMessage());
                throw new RuntimeException("无法获取玩家灵异力量数值，请确保神秘复苏模组正确加载");
            }
        }
    }

    public static int getSanityValue(PlayerEntity player) {
        if (!isSmfsLoaded) {
            LOGGER.debug("神秘复苏模组未加载，返回默认理智值");
            return 100;
        }
        try {
            return SmfsIntegration.detectSanityValue(player);
        }
        catch (Exception e) {
            LOGGER.error("获取理智值时发生错误: {}", (Object)e.getMessage());
            return 100;
        }
    }

    public static boolean setSpiritualStrength(PlayerEntity player, double value) {
        if (!isSmfsLoaded) {
            LOGGER.debug("神秘复苏模组未加载，无法设置灵异强度");
            return false;
        }
        try {
            return SmfsIntegration.setSpiritualStrengthValueDouble(player, value);
        }
        catch (Exception e) {
            LOGGER.error("设置灵异强度时发生错误: {}", (Object)e.getMessage());
            return false;
        }
    }

    public static boolean setSpiritualResistance(PlayerEntity player, double value) {
        if (!isSmfsLoaded) {
            LOGGER.debug("神秘复苏模组未加载，无法设置灵异抗性");
            return false;
        }
        try {
            return SmfsIntegration.setSpiritualResistanceValueDouble(player, value);
        }
        catch (Exception e) {
            LOGGER.error("设置灵异抗性时发生错误: {}", (Object)e.getMessage());
            return false;
        }
    }

    public static boolean setSpiritualPower(PlayerEntity player, double value) {
        if (!isSmfsLoaded) {
            LOGGER.debug("神秘复苏模组未加载，无法设置灵异力量");
            return false;
        }
        try {
            return SmfsIntegration.setSpiritualPowerValueDouble(player, value);
        }
        catch (Exception e) {
            LOGGER.error("设置灵异力量时发生错误: {}", (Object)e.getMessage());
            return false;
        }
    }

    public static boolean setSanityValue(PlayerEntity player, int value) {
        if (!isSmfsLoaded) {
            LOGGER.debug("神秘复苏模组未加载，无法设置理智值");
            return false;
        }
        try {
            return SmfsIntegration.setSanityValueValue(player, value);
        }
        catch (Exception e) {
            LOGGER.error("设置理智值时发生错误: {}", (Object)e.getMessage());
            return false;
        }
    }

    public static boolean setGhostEyeRevivalValue(PlayerEntity player, int value) {
        if (!isSmfsLoaded) {
            LOGGER.debug("神秘复苏模组未加载，无法设置复苏值");
            return false;
        }
        try {
            return SmfsIntegration.setRevivalValue(player, value);
        }
        catch (Exception e) {
            LOGGER.error("设置复苏值时发生错误: {}", (Object)e.getMessage());
            return false;
        }
    }

    public static boolean setRevivalFactor(PlayerEntity player, int value) {
        if (!isSmfsLoaded) {
            LOGGER.debug("神秘复苏模组未加载，无法设置复苏因子");
            return false;
        }
        try {
            return SmfsIntegration.setRevivalFactorValue(player, value);
        }
        catch (Exception e) {
            LOGGER.error("设置复苏因子时发生错误: {}", (Object)e.getMessage());
            return false;
        }
    }

    private static int detectSpiritualStrength(PlayerEntity player) {
        try {
            Class<?> attributeManagerClass = Class.forName("com.xie.smfs.manager.AttributeManager");
            Method getStrengthMethod = attributeManagerClass.getMethod("getSpiritualStrength", PlayerEntity.class);
            Object result = getStrengthMethod.invoke(null, player);
            if (result instanceof Integer) {
                LOGGER.debug("通过反射检测到灵异强度: {}", result);
                return (Integer)result;
            }
        }
        catch (Exception e) {
            LOGGER.debug("反射检测灵异强度失败: {}", (Object)e.getMessage());
        }
        try {
            String[] strengthPaths;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            for (String path : strengthPaths = new String[]{"smfs:spiritual_strength", "smfs:strength", "smfs:ghost_strength", "ForgeData.smfs_spiritual_strength", "smfs_spiritual_strength", "spiritual_strength", "ghost_strength", "strength", "smfs.strength", "smfs.spiritual_strength", "smfs.ghost_strength", "player.smfs.strength", "player.smfs.spiritual_strength"}) {
                if (!nbt.contains(path)) continue;
                int strength = nbt.getInt(path);
                LOGGER.debug("通过NBT路径 {} 检测到灵异强度: {}", (Object)path, (Object)strength);
                return strength;
            }
            return SmfsIntegration.searchNbtForStrength(nbt);
        }
        catch (Exception e) {
            LOGGER.debug("NBT检测灵异强度失败: {}", (Object)e.getMessage());
            LOGGER.debug("灵异强度检测失败，返回默认值1000");
            return 1000;
        }
    }

    private static int searchNbtForStrength(NbtCompound nbt) {
        try {
            for (String key : nbt.getKeys()) {
                int value;
                if (!key.toLowerCase().contains("strength") && !key.toLowerCase().contains("spiritual") && !key.toLowerCase().contains("ghost") || !nbt.contains(key) || (value = nbt.getInt(key)) <= 0 || value >= 100000) continue;
                LOGGER.debug("通过关键词搜索检测到灵异强度: {} = {}", (Object)key, (Object)value);
                return value;
            }
            for (String key : nbt.getKeys()) {
                NbtCompound subNbt;
                int result;
                if (!(nbt.get(key) instanceof NbtCompound) || (result = SmfsIntegration.searchNbtForStrength(subNbt = nbt.getCompound(key))) == 1000) continue;
                return result;
            }
        }
        catch (Exception e) {
            LOGGER.debug("深度搜索NBT结构失败: {}", (Object)e.getMessage());
        }
        return 1000;
    }

    private static int detectMaxSpiritualStrength(PlayerEntity player) {
        try {
            Class<?> attributeManagerClass = Class.forName("com.xie.smfs.manager.AttributeManager");
            Method getMaxStrengthMethod = attributeManagerClass.getMethod("getMaxSpiritualStrength", PlayerEntity.class);
            Object result = getMaxStrengthMethod.invoke(null, player);
            if (result instanceof Integer) {
                int strength = (Integer)result;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("通过反射检测到最大灵异强度: {}", (Object)strength);
                }
                return strength;
            }
        }
        catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("反射检测最大灵异强度失败: {}", (Object)e.getMessage());
            }
        }
        try {
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("玩家NBT数据完整内容: {}", (Object)nbt.toString());
            }
            if (nbt.contains("smfs_spirit_data")) {
                NbtCompound spiritData = nbt.getCompound("smfs_spirit_data");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("smfs_spirit_data内容: {}", (Object)spiritData.toString());
                }
                String[] maxStrengthPaths = new String[]{"maxSpirit", "max_spirit", "maxSpiritualStrength", "max_spiritual_strength", "max_strength", "max_ghost_strength", "maximum_spiritual_strength", "maximum_strength", "spiritual_strength_max", "strength_max", "ghost_strength_max", "maxStrength", "maxGhostStrength", "maximumSpiritualStrength", "maximumStrength", "spiritualStrengthMax", "strengthMax", "ghostStrengthMax"};
                for (String path : maxStrengthPaths) {
                    if (spiritData.contains(path)) {
                        int maxStrength = spiritData.getInt(path);
                        return maxStrength;
                    }
                }
                for (String path : new String[]{"currentSpirit", "current_spirit", "spiritual_strength", "strength", "ghost_strength", "spiritualStrength", "ghostStrength", "current_spiritual_strength", "current_strength", "current_ghost_strength"}) {
                    if (spiritData.contains(path)) {
                        int currentStrength = spiritData.getInt(path);
                        return currentStrength;
                    }
                }
            }
            String[] maxStrengthPaths = new String[]{"smfs:max_spiritual_strength", "smfs:max_strength", "smfs:max_ghost_strength", "ForgeData.smfs_max_spiritual_strength", "smfs_max_spiritual_strength", "max_spiritual_strength", "max_ghost_strength", "max_strength", "smfs.max_strength", "smfs.max_spiritual_strength", "smfs.max_ghost_strength", "player.smfs.max_strength", "player.smfs.max_spiritual_strength", "smfs.maximum_strength", "maximum_spiritual_strength", "smfs:maximum_spiritual_strength", "ForgeData.smfs_maximum_spiritual_strength", "smfs_maximum_spiritual_strength"};
            for (String path : maxStrengthPaths) {
                if (nbt.contains(path)) {
                    int maxStrength = nbt.getInt(path);
                    return maxStrength;
                }
            }
            for (String key : nbt.getKeys()) {
                if (!key.toLowerCase().contains("strength") && !key.toLowerCase().contains("spiritual") || !nbt.contains(key)) continue;
                int value = nbt.getInt(key);
                if (value <= 0 || value >= 100000) continue;
                return value;
            }
            int currentStrength = SmfsIntegration.detectSpiritualStrength(player);
            return currentStrength;
        }
        catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("NBT检测最大灵异强度失败: {}", (Object)e.getMessage());
            }
            return 7000;
        }
    }

    private static int detectSpiritualResistance(PlayerEntity player) {
        try {
            Class<?> attributeManagerClass = Class.forName("com.xie.smfs.manager.AttributeManager");
            Method getResistanceMethod = attributeManagerClass.getMethod("getSpiritualResistance", PlayerEntity.class);
            Object result = getResistanceMethod.invoke(null, player);
            if (result instanceof Integer) {
                LOGGER.debug("通过反射检测到灵异抗性: {}", result);
                return (Integer)result;
            }
        }
        catch (Exception e) {
            LOGGER.debug("反射检测灵异抗性失败: {}", (Object)e.getMessage());
        }
        try {
            String[] resistancePaths;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            for (String path : resistancePaths = new String[]{"smfs:spiritual_resistance", "smfs:resistance", "smfs:ghost_resistance", "ForgeData.smfs_spiritual_resistance"}) {
                if (!nbt.contains(path)) continue;
                int resistance = nbt.getInt(path);
                LOGGER.debug("通过NBT路径 {} 检测到灵异抗性: {}", (Object)path, (Object)resistance);
                return resistance;
            }
        }
        catch (Exception e) {
            LOGGER.debug("NBT检测灵异抗性失败: {}", (Object)e.getMessage());
        }
        LOGGER.debug("灵异抗性检测失败，返回默认值500");
        return 500;
    }

    private static int detectSpiritualPower(PlayerEntity player) {
        try {
            Class<?> attributeManagerClass = Class.forName("com.xie.smfs.manager.AttributeManager");
            Method getPowerMethod = attributeManagerClass.getMethod("getSpiritualPower", PlayerEntity.class);
            Object result = getPowerMethod.invoke(null, player);
            if (result instanceof Integer) {
                LOGGER.debug("通过反射检测到灵异力量: {}", result);
                return (Integer)result;
            }
        }
        catch (Exception e) {
            LOGGER.debug("反射检测灵异力量失败: {}", (Object)e.getMessage());
        }
        try {
            String[] powerPaths;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            for (String path : powerPaths = new String[]{"smfs:spiritual_power", "smfs:power", "smfs:ghost_power", "ForgeData.smfs_spiritual_power"}) {
                if (!nbt.contains(path)) continue;
                int power = nbt.getInt(path);
                LOGGER.debug("通过NBT路径 {} 检测到灵异力量: {}", (Object)path, (Object)power);
                return power;
            }
        }
        catch (Exception e) {
            LOGGER.debug("NBT检测灵异力量失败: {}", (Object)e.getMessage());
        }
        LOGGER.debug("灵异力量检测失败，返回默认值2000");
        return 2000;
    }

    private static int detectSanityValue(PlayerEntity player) {
        try {
            Class<?> sanityManagerClass = Class.forName("com.xie.smfs.manager.SanityManager");
            Method getSanityMethod = sanityManagerClass.getMethod("getSanity", PlayerEntity.class);
            Object result = getSanityMethod.invoke(null, player);
            if (result instanceof Integer) {
                LOGGER.debug("通过反射检测到理智值: {}", result);
                return (Integer)result;
            }
        }
        catch (Exception e) {
            LOGGER.debug("反射检测理智值失败: {}", (Object)e.getMessage());
        }
        try {
            String[] sanityPaths;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            for (String path : sanityPaths = new String[]{"smfs:sanity", "smfs:sanity_value", "smfs:mental_health", "ForgeData.smfs_sanity"}) {
                if (!nbt.contains(path)) continue;
                int sanity = nbt.getInt(path);
                LOGGER.debug("通过NBT路径 {} 检测到理智值: {}", (Object)path, (Object)sanity);
                return sanity;
            }
        }
        catch (Exception e) {
            LOGGER.debug("NBT检测理智值失败: {}", (Object)e.getMessage());
        }
        LOGGER.debug("理智值检测失败，返回默认值100");
        return 100;
    }

    private static boolean setSpiritualStrengthValue(PlayerEntity player, int value) {
        try {
            Class<?> attributeManagerClass = Class.forName("com.xie.smfs.manager.AttributeManager");
            Method setStrengthMethod = attributeManagerClass.getMethod("setSpiritualStrength", PlayerEntity.class, Integer.TYPE);
            Object result = setStrengthMethod.invoke(null, player, value);
            if (result instanceof Boolean) {
                LOGGER.debug("通过反射设置灵异强度: {}，结果: {}", (Object)value, result);
                return (Boolean)result;
            }
        }
        catch (Exception e) {
            LOGGER.debug("反射设置灵异强度失败: {}", (Object)e.getMessage());
        }
        try {
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            if (nbt.contains("smfs_spirit_data")) {
                NbtCompound spiritData = nbt.getCompound("smfs_spirit_data");
                spiritData.putInt("currentSpirit", value);
                int maxStrength = spiritData.getInt("maxSpirit");
                if (value > maxStrength) {
                    spiritData.putInt("maxSpirit", value);
                }
                nbt.put("smfs_spirit_data", (NbtElement)spiritData);
                player.readNbt(nbt);
                LOGGER.debug("通过smfs_spirit_data路径设置灵异强度: 当前强度={}, 最大强度={}", (Object)value, (Object)maxStrength);
                return true;
            }
            int n = 0;
            String[] strengthPaths = new String[]{"currentSpirit", "smfs:spiritual_strength", "smfs:strength", "ForgeData.smfs_spiritual_strength"};
            String[] stringArray = strengthPaths;
            int n2 = stringArray.length;
            if (n < n2) {
                String path = stringArray[n];
                nbt.putInt(path, value);
                player.readNbt(nbt);
                LOGGER.debug("通过NBT路径 {} 设置灵异强度: {}", (Object)path, (Object)value);
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.debug("NBT设置灵异强度失败: {}", (Object)e.getMessage());
        }
        LOGGER.debug("设置灵异强度失败");
        return false;
    }

    private static boolean setSpiritualResistanceValue(PlayerEntity player, int value) {
        try {
            Class<?> attributeManagerClass = Class.forName("com.xie.smfs.manager.AttributeManager");
            Method setResistanceMethod = attributeManagerClass.getMethod("setSpiritualResistance", PlayerEntity.class, Integer.TYPE);
            Object result = setResistanceMethod.invoke(null, player, value);
            if (result instanceof Boolean) {
                LOGGER.debug("通过反射设置灵异抗性: {}，结果: {}", (Object)value, result);
                return (Boolean)result;
            }
        }
        catch (Exception e) {
            LOGGER.debug("反射设置灵异抗性失败: {}", (Object)e.getMessage());
        }
        try {
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            if (nbt.contains("smfs_spirit_data")) {
                NbtCompound spiritData = nbt.getCompound("smfs_spirit_data");
                spiritData.putInt("spiritResistance", value);
                nbt.put("smfs_spirit_data", (NbtElement)spiritData);
                player.readNbt(nbt);
                LOGGER.debug("通过smfs_spirit_data路径设置灵异抗性: {}", (Object)value);
                return true;
            }
            int n = 0;
            String[] resistancePaths = new String[]{"spiritResistance", "spirit_resistance", "spiritual_resistance", "smfs:spiritual_resistance", "smfs:resistance", "ForgeData.smfs_spiritual_resistance", "smfs_spirit_data.spiritResistance", "smfs_spirit_data.spirit_resistance"};
            String[] stringArray = resistancePaths;
            int n2 = stringArray.length;
            if (n < n2) {
                String path = stringArray[n];
                if (path.contains(".")) {
                    String[] pathParts = path.split("\\.");
                    if (nbt.contains(pathParts[0])) {
                        NbtCompound compound = nbt.getCompound(pathParts[0]);
                        compound.putInt(pathParts[1], value);
                        nbt.put(pathParts[0], (NbtElement)compound);
                        player.readNbt(nbt);
                        LOGGER.debug("通过嵌套NBT路径 {} 设置灵异抗性: {}", (Object)path, (Object)value);
                        return true;
                    }
                } else {
                    nbt.putInt(path, value);
                    player.readNbt(nbt);
                    LOGGER.debug("通过NBT路径 {} 设置灵异抗性: {}", (Object)path, (Object)value);
                }
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.debug("NBT设置灵异抗性失败: {}", (Object)e.getMessage());
        }
        LOGGER.debug("设置灵异抗性失败");
        return false;
    }

    private static boolean setSpiritualPowerValue(PlayerEntity player, int value) {
        try {
            Class<?> attributeManagerClass = Class.forName("com.xie.smfs.manager.AttributeManager");
            Method setPowerMethod = attributeManagerClass.getMethod("setSpiritualPower", PlayerEntity.class, Integer.TYPE);
            Object result = setPowerMethod.invoke(null, player, value);
            if (result instanceof Boolean) {
                LOGGER.debug("通过反射设置灵异力量: {}，结果: {}", (Object)value, result);
                return (Boolean)result;
            }
        }
        catch (Exception e) {
            LOGGER.debug("反射设置灵异力量失败: {}", (Object)e.getMessage());
        }
        try {
            String[] powerPaths;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            if (nbt.contains("smfs_spirit_data")) {
                NbtCompound spiritData = nbt.getCompound("smfs_spirit_data");
                spiritData.putInt("spiritDamage", value);
                nbt.put("smfs_spirit_data", (NbtElement)spiritData);
                player.readNbt(nbt);
                LOGGER.debug("通过smfs_spirit_data路径设置灵异力量: {}", (Object)value);
                return true;
            }
            for (String path : powerPaths = new String[]{"spiritDamage", "spirit_damage", "spiritual_power", "smfs:spiritual_power", "smfs:power", "ForgeData.smfs_spiritual_power", "smfs_spirit_data.spiritDamage", "smfs_spirit_data.spirit_damage"}) {
                if (path.contains(".")) {
                    String[] pathParts = path.split("\\.");
                    if (!nbt.contains(pathParts[0])) continue;
                    NbtCompound compound = nbt.getCompound(pathParts[0]);
                    compound.putInt(pathParts[1], value);
                    nbt.put(pathParts[0], (NbtElement)compound);
                    player.readNbt(nbt);
                    LOGGER.debug("通过嵌套NBT路径 {} 设置灵异力量: {}", (Object)path, (Object)value);
                    return true;
                }
                nbt.putInt(path, value);
                player.readNbt(nbt);
                LOGGER.debug("通过NBT路径 {} 设置灵异力量: {}", (Object)path, (Object)value);
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.debug("NBT设置灵异力量失败: {}", (Object)e.getMessage());
        }
        LOGGER.debug("设置灵异力量失败");
        return false;
    }

    private static boolean writeDoubleToSpiritData(PlayerEntity player, String nbtKey, double value, boolean alsoSetMaxSpirit) {
        try {
            Class<?> playerEventsClass = Class.forName("com.xie.smfs.common.events.PlayerEvents");
            java.lang.reflect.Field cacheField = playerEventsClass.getField("PLAYER_DATA_CACHE");
            Object cache = cacheField.get(null);
            if (cache instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<UUID, Object> dataCache = (java.util.Map<UUID, Object>) cache;
                Object cached = dataCache.get(player.getUuid());
                if (cached instanceof NbtCompound) {
                    NbtCompound spiritData = (NbtCompound) cached;
                    spiritData.putDouble(nbtKey, value);
                    if (alsoSetMaxSpirit) {
                        double current = spiritData.getDouble(nbtKey);
                        double max = spiritData.getDouble("maxSpirit");
                        if (current > max) {
                            spiritData.putDouble("maxSpirit", current);
                        }
                    }
                    java.lang.reflect.Method setMethod = playerEventsClass.getMethod("setSpiritAttributes", PlayerEntity.class, NbtCompound.class);
                    setMethod.invoke(null, player, spiritData);
                    LOGGER.debug("通过SMFS缓存设置 {} = {}", (Object)nbtKey, (Object)value);
                    return true;
                }
            }
        }
        catch (Exception e) {
            LOGGER.debug("SMFS缓存写入失败，尝试NBT路径: {}", (Object)e.getMessage());
        }
        try {
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            if (nbt.contains("smfs_spirit_data")) {
                NbtCompound spiritData = nbt.getCompound("smfs_spirit_data");
                spiritData.putDouble(nbtKey, value);
                if (alsoSetMaxSpirit) {
                    double current = spiritData.getDouble(nbtKey);
                    double max = spiritData.getDouble("maxSpirit");
                    if (current > max) {
                        spiritData.putDouble("maxSpirit", current);
                    }
                }
                nbt.put("smfs_spirit_data", (NbtElement)spiritData);
                player.readNbt(nbt);
                LOGGER.debug("通过NBT路径设置 {} = {}", (Object)nbtKey, (Object)value);
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.debug("NBT设置 {} 失败: {}", (Object)nbtKey, (Object)e.getMessage());
        }
        LOGGER.debug("设置 {} 失败", (Object)nbtKey);
        return false;
    }

    private static boolean setSpiritualStrengthValueDouble(PlayerEntity player, double value) {
        return writeDoubleToSpiritData(player, "currentSpirit", value, true);
    }

    private static boolean setSpiritualResistanceValueDouble(PlayerEntity player, double value) {
        return writeDoubleToSpiritData(player, "spiritResistance", value, false);
    }

    private static boolean setSpiritualPowerValueDouble(PlayerEntity player, double value) {
        return writeDoubleToSpiritData(player, "spiritDamage", value, false);
    }

    private static boolean setMaxSpiritualStrengthValueDouble(PlayerEntity player, double value) {
        return writeDoubleToSpiritData(player, "maxSpirit", value, false);
    }

    public static boolean hasSilenceEffect(PlayerEntity player) {
        if (SmfsIntegration.hasSilenceImmunity(player) || SmfsIntegration.hasNegativeEffectImmunity(player)) {
            LOGGER.debug("玩家 {} 拥有免疫，不受沉寂效果影响", (Object)player.getName().getString());
            return false;
        }
        if (!isSmfsLoaded) {
            LOGGER.debug("神秘复苏模组未加载，无法检测沉寂效果");
            return false;
        }
        try {
            Class<?> effectManagerClass = Class.forName("com.xie.smfs.manager.EffectManager");
            Method hasSilenceMethod = effectManagerClass.getMethod("hasSilenceEffect", PlayerEntity.class);
            Object result = hasSilenceMethod.invoke(null, player);
            if (result instanceof Boolean) {
                LOGGER.debug("玩家 {} 通过反射检测到沉寂效果状态: {}", (Object)player.getName().getString(), result);
                return (Boolean)result;
            }
        }
        catch (Exception e) {
            LOGGER.debug("玩家 {} 反射检测沉寂效果失败: {}", (Object)player.getName().getString(), (Object)e.getMessage());
        }
        try {
            String[] silencePaths;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            for (String path : silencePaths = new String[]{"smfs:silence_effect", "smfs:silence", "ForgeData.smfs_silence_effect", "silence_effect", "silence"}) {
                if (!nbt.contains(path)) continue;
                boolean hasSilence = nbt.getBoolean(path);
                LOGGER.debug("玩家 {} 通过NBT路径 {} 检测到沉寂效果状态: {}", new Object[]{player.getName().getString(), path, hasSilence});
                return hasSilence;
            }
        }
        catch (Exception e) {
            LOGGER.debug("玩家 {} NBT检测沉寂效果失败: {}", (Object)player.getName().getString(), (Object)e.getMessage());
        }
        LOGGER.debug("玩家 {} 所有检测方法失败，返回false", (Object)player.getName().getString());
        return false;
    }

    public static boolean hasNegativeEffectImmunity(PlayerEntity player) {
        try {
            NbtCompound forgeData;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            String immunityKey = "guiyu:negative_effect_immunity";
            if (nbt.contains("ForgeData") && (forgeData = nbt.getCompound("ForgeData")).contains(immunityKey)) {
                boolean immune = forgeData.getBoolean(immunityKey);
                LOGGER.debug("通过ForgeData检测到负面效果免疫状态: {}", (Object)immune);
                return immune;
            }
            if (nbt.contains(immunityKey)) {
                boolean immune = nbt.getBoolean(immunityKey);
                LOGGER.debug("通过根级别检测到负面效果免疫状态: {}", (Object)immune);
                return immune;
            }
            LOGGER.debug("玩家 {} 没有负面效果免疫", (Object)player.getName().getString());
            return false;
        }
        catch (Exception e) {
            LOGGER.error("检测玩家 {} 的负面效果免疫状态时发生错误: {}", (Object)player.getName().getString(), (Object)e.getMessage());
            return false;
        }
    }

    public static boolean hasSilenceImmunity(PlayerEntity player) {
        try {
            NbtCompound forgeData;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            String immunityKey = "guiyu:silence_immunity";
            if (nbt.contains("ForgeData") && (forgeData = nbt.getCompound("ForgeData")).contains(immunityKey)) {
                boolean immune = forgeData.getBoolean(immunityKey);
                LOGGER.debug("通过ForgeData检测到沉寂免疫状态: {}", (Object)immune);
                return immune;
            }
            if (nbt.contains(immunityKey)) {
                boolean immune = nbt.getBoolean(immunityKey);
                LOGGER.debug("通过根级别检测到沉寂免疫状态: {}", (Object)immune);
                return immune;
            }
            LOGGER.debug("玩家 {} 没有沉寂免疫", (Object)player.getName().getString());
            return false;
        }
        catch (Exception e) {
            LOGGER.error("检测玩家 {} 的沉寂免疫状态时发生错误: {}", (Object)player.getName().getString(), (Object)e.getMessage());
            return false;
        }
    }

    public static boolean removeSilenceEffect(PlayerEntity player) {
        if (!isSmfsLoaded) {
            LOGGER.debug("玩家 {} 神秘复苏模组未加载，无法移除沉寂效果", (Object)player.getName().getString());
            return false;
        }
        try {
            Class<?> effectManagerClass = Class.forName("com.xie.smfs.manager.EffectManager");
            Method removeSilenceMethod = effectManagerClass.getMethod("removeSilenceEffect", PlayerEntity.class);
            Object result = removeSilenceMethod.invoke(null, player);
            if (result instanceof Boolean) {
                LOGGER.debug("玩家 {} 通过反射移除沉寂效果，结果: {}", (Object)player.getName().getString(), result);
                return (Boolean)result;
            }
        }
        catch (Exception e) {
            LOGGER.debug("玩家 {} 反射移除沉寂效果失败: {}", (Object)player.getName().getString(), (Object)e.getMessage());
        }
        try {
            String[] silencePaths;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            for (String path : silencePaths = new String[]{"smfs:silence_effect", "smfs:silence", "ForgeData.smfs_silence_effect", "silence_effect", "silence"}) {
                if (!nbt.contains(path)) continue;
                nbt.remove(path);
                player.readNbt(nbt);
                LOGGER.debug("玩家 {} 通过NBT路径 {} 移除沉寂效果", (Object)player.getName().getString(), (Object)path);
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.debug("玩家 {} NBT移除沉寂效果失败: {}", (Object)player.getName().getString(), (Object)e.getMessage());
        }
        LOGGER.debug("玩家 {} 所有移除方法失败，返回false", (Object)player.getName().getString());
        return false;
    }

    public static boolean setSilenceImmunity(PlayerEntity player, boolean immune) {
        try {
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            String immunityKey = "guiyu:silence_immunity";
            if (!nbt.contains("ForgeData")) {
                nbt.put("ForgeData", (NbtElement)new NbtCompound());
            }
            NbtCompound forgeData = nbt.getCompound("ForgeData");
            forgeData.putBoolean(immunityKey, immune);
            player.readNbt(nbt);
            LOGGER.debug("玩家 {} 的沉寂免疫状态已设置为: {}", (Object)player.getName().getString(), (Object)immune);
            return true;
        }
        catch (Exception e) {
            LOGGER.error("设置玩家 {} 的沉寂免疫状态时发生错误: {}", (Object)player.getName().getString(), (Object)e.getMessage());
            return false;
        }
    }

    public static boolean setNegativeEffectImmunity(PlayerEntity player, boolean immune) {
        try {
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            String immunityKey = "guiyu:negative_effect_immunity";
            if (!nbt.contains("ForgeData")) {
                nbt.put("ForgeData", (NbtElement)new NbtCompound());
            }
            NbtCompound forgeData = nbt.getCompound("ForgeData");
            forgeData.putBoolean(immunityKey, immune);
            player.readNbt(nbt);
            LOGGER.debug("玩家 {} 的负面效果免疫状态已设置为: {}", (Object)player.getName().getString(), (Object)immune);
            return true;
        }
        catch (Exception e) {
            LOGGER.error("设置玩家 {} 的负面效果免疫状态时发生错误: {}", (Object)player.getName().getString(), (Object)e.getMessage());
            return false;
        }
    }

    public static boolean removeSilenceImmunity(PlayerEntity player) {
        try {
            NbtCompound forgeData;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            String immunityKey = "guiyu:silence_immunity";
            if (nbt.contains("ForgeData") && (forgeData = nbt.getCompound("ForgeData")).contains(immunityKey)) {
                forgeData.remove(immunityKey);
                player.readNbt(nbt);
                LOGGER.debug("玩家 {} 的沉寂免疫状态已移除", (Object)player.getName().getString());
                return true;
            }
            if (nbt.contains(immunityKey)) {
                nbt.remove(immunityKey);
                player.readNbt(nbt);
                LOGGER.debug("玩家 {} 的沉寂免疫状态已移除", (Object)player.getName().getString());
                return true;
            }
            LOGGER.debug("玩家 {} 没有沉寂免疫状态可移除", (Object)player.getName().getString());
            return false;
        }
        catch (Exception e) {
            LOGGER.error("移除玩家 {} 的沉寂免疫状态时发生错误: {}", (Object)player.getName().getString(), (Object)e.getMessage());
            return false;
        }
    }

    public static boolean hasSpiritualResistance(LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            try {
                Class<?> attributeManagerClass = Class.forName("com.xie.smfs.manager.AttributeManager");
                Method getResistanceMethod = attributeManagerClass.getMethod("getSpiritualResistance", PlayerEntity.class);
                Object result = getResistanceMethod.invoke(null, player);
                if (result instanceof Integer) {
                    int resistance = (Integer)result;
                    LOGGER.debug("通过反射检测到玩家 {} 的灵异抗性: {}", (Object)player.getName().getString(), (Object)resistance);
                    return resistance > 0;
                }
            }
            catch (Exception e) {
                LOGGER.debug("反射检测玩家灵异抗性失败: {}", (Object)e.getMessage());
            }
            try {
                NbtCompound spiritData;
                NbtCompound nbt = player.writeNbt(new NbtCompound());
                if (nbt.contains("smfs_spirit_data") && (spiritData = nbt.getCompound("smfs_spirit_data")).contains("spiritResistance")) {
                    int resistance = spiritData.getInt("spiritResistance");
                    LOGGER.debug("通过smfs_spirit_data检测到玩家 {} 的灵异抗性: {}", (Object)player.getName().getString(), (Object)resistance);
                    return resistance > 0;
                }
                String[] resistancePaths = new String[]{"smfs:spiritual_resistance", "smfs:resistance", "smfs:ghost_resistance", "ForgeData.smfs_spiritual_resistance"};
                for (String path : resistancePaths) {
                    if (!nbt.contains(path)) continue;
                    int resistance = nbt.getInt(path);
                    LOGGER.debug("通过NBT路径 {} 检测到玩家 {} 的灵异抗性: {}", new Object[]{path, player.getName().getString(), resistance});
                    return resistance > 0;
                }
            }
            catch (Exception e) {
                LOGGER.debug("NBT检测玩家灵异抗性失败: {}", (Object)e.getMessage());
            }
        }
        try {
            String[] resistanceTags;
            String entityType = entity.getType().toString();
            LOGGER.debug("检测非玩家实体类型: {}", (Object)entityType);
            NbtCompound entityNbt = new NbtCompound();
            entity.writeNbt(entityNbt);
            for (String tag : resistanceTags = new String[]{"smfs:spiritual_resistance", "smfs:resistance", "smfs:ghost_resistance", "spiritual_resistance", "ghost_resistance"}) {
                if (!entityNbt.contains(tag)) continue;
                int resistance = entityNbt.getInt(tag);
                LOGGER.debug("通过NBT标签 {} 检测到实体的灵异抗性: {}", (Object)tag, (Object)resistance);
                return resistance > 0;
            }
        }
        catch (Exception e) {
            LOGGER.debug("检测非玩家实体灵异抗性失败: {}", (Object)e.getMessage());
        }
        LOGGER.debug("实体 {} 没有检测到灵异抗性", (Object)entity.getName().getString());
        return false;
    }

    public static boolean removeNegativeEffectImmunity(PlayerEntity player) {
        try {
            NbtCompound forgeData;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            String immunityKey = "guiyu:negative_effect_immunity";
            if (nbt.contains("ForgeData") && (forgeData = nbt.getCompound("ForgeData")).contains(immunityKey)) {
                forgeData.remove(immunityKey);
                player.readNbt(nbt);
                LOGGER.debug("玩家 {} 的负面效果免疫状态已移除", (Object)player.getName().getString());
                return true;
            }
            if (nbt.contains(immunityKey)) {
                nbt.remove(immunityKey);
                player.readNbt(nbt);
                LOGGER.debug("玩家 {} 的负面效果免疫状态已移除", (Object)player.getName().getString());
                return true;
            }
            LOGGER.debug("玩家 {} 没有负面效果免疫状态可移除", (Object)player.getName().getString());
            return false;
        }
        catch (Exception e) {
            LOGGER.error("移除玩家 {} 的负面效果免疫状态时发生错误: {}", (Object)player.getName().getString(), (Object)e.getMessage());
            return false;
        }
    }

    public static boolean hasCurseEffect(PlayerEntity player) {
        if (SmfsIntegration.hasNegativeEffectImmunity(player)) {
            LOGGER.debug("玩家 {} 拥有免疫，不受诅咒效果影响", (Object)player.getName().getString());
            return false;
        }
        if (!isSmfsLoaded) {
            LOGGER.debug("神秘复苏模组未加载，无法检测诅咒效果");
            return false;
        }
        try {
            Class<?> effectManagerClass = Class.forName("com.xie.smfs.manager.EffectManager");
            Method hasCurseMethod = effectManagerClass.getMethod("hasCurseEffect", PlayerEntity.class);
            Object result = hasCurseMethod.invoke(null, player);
            if (result instanceof Boolean) {
                LOGGER.debug("玩家 {} 通过反射检测到诅咒效果状态: {}", (Object)player.getName().getString(), result);
                return (Boolean)result;
            }
        }
        catch (Exception e) {
            LOGGER.debug("玩家 {} 反射检测诅咒效果失败: {}", (Object)player.getName().getString(), (Object)e.getMessage());
        }
        try {
            String[] cursePaths;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            for (String path : cursePaths = new String[]{"smfs:curse_effect", "smfs:curse", "ForgeData.smfs_curse_effect", "curse_effect", "curse"}) {
                if (!nbt.contains(path)) continue;
                boolean hasCurse = nbt.getBoolean(path);
                LOGGER.debug("玩家 {} 通过NBT路径 {} 检测到诅咒效果状态: {}", new Object[]{player.getName().getString(), path, hasCurse});
                return hasCurse;
            }
        }
        catch (Exception e) {
            LOGGER.debug("玩家 {} NBT检测诅咒效果失败: {}", (Object)player.getName().getString(), (Object)e.getMessage());
        }
        LOGGER.debug("玩家 {} 所有检测方法失败，返回false", (Object)player.getName().getString());
        return false;
    }

    public static boolean hasFearEffect(PlayerEntity player) {
        if (SmfsIntegration.hasNegativeEffectImmunity(player)) {
            LOGGER.debug("玩家 {} 拥有免疫，不受恐惧效果影响", (Object)player.getName().getString());
            return false;
        }
        if (!isSmfsLoaded) {
            LOGGER.debug("神秘复苏模组未加载，无法检测恐惧效果");
            return false;
        }
        try {
            Class<?> effectManagerClass = Class.forName("com.xie.smfs.manager.EffectManager");
            Method hasFearMethod = effectManagerClass.getMethod("hasFearEffect", PlayerEntity.class);
            Object result = hasFearMethod.invoke(null, player);
            if (result instanceof Boolean) {
                LOGGER.debug("玩家 {} 通过反射检测到恐惧效果状态: {}", (Object)player.getName().getString(), result);
                return (Boolean)result;
            }
        }
        catch (Exception e) {
            LOGGER.debug("玩家 {} 反射检测恐惧效果失败: {}", (Object)player.getName().getString(), (Object)e.getMessage());
        }
        try {
            String[] fearPaths;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            for (String path : fearPaths = new String[]{"smfs:fear_effect", "smfs:fear", "ForgeData.smfs_fear_effect", "fear_effect", "fear"}) {
                if (!nbt.contains(path)) continue;
                boolean hasFear = nbt.getBoolean(path);
                LOGGER.debug("玩家 {} 通过NBT路径 {} 检测到恐惧效果状态: {}", new Object[]{player.getName().getString(), path, hasFear});
                return hasFear;
            }
        }
        catch (Exception e) {
            LOGGER.debug("玩家 {} NBT检测恐惧效果失败: {}", (Object)player.getName().getString(), (Object)e.getMessage());
        }
        LOGGER.debug("玩家 {} 所有检测方法失败，返回false", (Object)player.getName().getString());
        return false;
    }

    public static boolean hasAnyNegativeEffect(PlayerEntity player) {
        return SmfsIntegration.hasSilenceEffect(player) || SmfsIntegration.hasCurseEffect(player) || SmfsIntegration.hasFearEffect(player);
    }

    public static boolean setMaxSpiritualStrength(PlayerEntity player, double value) {
        try {
            return SmfsIntegration.setMaxSpiritualStrengthValueDouble(player, value);
        }
        catch (Exception e) {
            LOGGER.debug("设置最大灵异强度失败: {}", (Object)e.getMessage());
        }
        return false;
    }

    private static boolean setSanityValueValue(PlayerEntity player, int value) {
        try {
            Class<?> sanityManagerClass = Class.forName("com.xie.smfs.manager.SanityManager");
            Method setSanityMethod = sanityManagerClass.getMethod("setSanity", PlayerEntity.class, Integer.TYPE);
            Object result = setSanityMethod.invoke(null, player, value);
            if (result instanceof Boolean) {
                LOGGER.debug("通过反射设置理智值: {}，结果: {}", (Object)value, result);
                return (Boolean)result;
            }
        }
        catch (Exception e) {
            LOGGER.debug("反射设置理智值失败: {}", (Object)e.getMessage());
        }
        try {
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            if (nbt.contains("smfs_spirit_data")) {
                NbtCompound spiritData = nbt.getCompound("smfs_spirit_data");
                spiritData.putInt("sanity", value);
                int maxSanity = spiritData.getInt("maxSanity");
                if (value > maxSanity) {
                    spiritData.putInt("maxSanity", value);
                }
                nbt.put("smfs_spirit_data", (NbtElement)spiritData);
                player.readNbt(nbt);
                LOGGER.debug("通过smfs_spirit_data路径设置理智值: 当前理智={}, 最大理智={}", (Object)value, (Object)maxSanity);
                return true;
            }
            int n = 0;
            String[] sanityPaths = new String[]{"sanity", "maxSanity", "smfs:sanity", "smfs:sanity_value", "ForgeData.smfs_sanity"};
            String[] stringArray = sanityPaths;
            int n2 = stringArray.length;
            if (n < n2) {
                String path = stringArray[n];
                nbt.putInt(path, value);
                player.readNbt(nbt);
                LOGGER.debug("通过NBT路径 {} 设置理智值: {}", (Object)path, (Object)value);
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.debug("NBT设置理智值失败: {}", (Object)e.getMessage());
        }
        LOGGER.debug("设置理智值失败");
        return false;
    }

    private static boolean setRevivalValue(PlayerEntity player, int value) {
        try {
            Class<?> revivalManagerClass = Class.forName("com.xie.smfs.manager.RevivalManager");
            Method setRevivalMethod = revivalManagerClass.getMethod("setRevivalValue", PlayerEntity.class, Integer.TYPE);
            Object result = setRevivalMethod.invoke(null, player, value);
            if (result instanceof Boolean) {
                LOGGER.debug("通过反射设置复苏值: {}，结果: {}", (Object)value, result);
                return (Boolean)result;
            }
        }
        catch (Exception e) {
            LOGGER.debug("反射设置复苏值失败: {}", (Object)e.getMessage());
        }
        try {
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            if (nbt.contains("smfs_spirit_data")) {
                NbtCompound spiritData = nbt.getCompound("smfs_spirit_data");
                spiritData.putInt("revival", value);
                nbt.put("smfs_spirit_data", (NbtElement)spiritData);
                player.readNbt(nbt);
                LOGGER.debug("通过smfs_spirit_data路径设置复苏值: {}", (Object)value);
                return true;
            }
            int n = 0;
            String[] revivalPaths = new String[]{"revival", "MainGhostSlot", "smfs:revival_value", "ForgeData.smfs_revival_value"};
            String[] stringArray = revivalPaths;
            int n2 = stringArray.length;
            if (n < n2) {
                String path = stringArray[n];
                nbt.putInt(path, value);
                player.readNbt(nbt);
                LOGGER.debug("通过NBT路径 {} 设置复苏值: {}", (Object)path, (Object)value);
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.debug("NBT设置复苏值失败: {}", (Object)e.getMessage());
        }
        LOGGER.debug("设置复苏值失败");
        return false;
    }

    private static boolean setRevivalFactorValue(PlayerEntity player, int value) {
        try {
            Class<?> revivalManagerClass = Class.forName("com.xie.smfs.manager.RevivalManager");
            Method setRevivalFactorMethod = revivalManagerClass.getMethod("setRevivalFactor", PlayerEntity.class, Integer.TYPE);
            Object result = setRevivalFactorMethod.invoke(null, player, value);
            if (result instanceof Boolean) {
                LOGGER.debug("通过反射设置复苏因子: {}，结果: {}", (Object)value, result);
                return (Boolean)result;
            }
        }
        catch (Exception e) {
            LOGGER.debug("反射设置复苏因子失败: {}", (Object)e.getMessage());
        }
        try {
            String[] revivalFactorPaths;
            NbtCompound nbt = player.writeNbt(new NbtCompound());
            String[] stringArray = revivalFactorPaths = new String[]{"revivalFactor", "smfs:revival_factor", "ForgeData.smfs_revival_factor"};
            int n = stringArray.length;
            int n2 = 0;
            if (n2 < n) {
                String path = stringArray[n2];
                nbt.putInt(path, value);
                player.readNbt(nbt);
                LOGGER.debug("通过NBT路径 {} 设置复苏因子: {}", (Object)path, (Object)value);
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.debug("NBT设置复苏因子失败: {}", (Object)e.getMessage());
        }
        LOGGER.debug("设置复苏因子失败");
        return false;
    }

    static {
        String[] smfsClasses = new String[]{"com.xie.smfs.manager.GhostDomainManager", "com.xie.smfs.manager.AttributeManager", "com.xie.smfs.manager.MainGhostManager", "com.xie.smfs.SmfsMod", "com.xie.smfs.MysteriousRevival", "com.xie.smfs.manager.PlayerDataManager", "com.xie.smfs.manager.SkillManager", "com.xie.smfs.manager.GhostManager"};

        boolean detectedSmfs = false;
        for (String className : smfsClasses) {
            try {
                Class.forName(className);
                detectedSmfs = true;
                LOGGER.info("神秘复苏模组已加载（通过类 {}），启用完全兼容性集成", className);
                break;
            }
            catch (ClassNotFoundException e) {
                LOGGER.debug("无法加载神秘复苏模组类: {}", className);
            }
        }

        if (detectedSmfs) {
            LOGGER.info("神秘复苏模组检测成功，启用高级兼容性功能");
        }
    }

    private static String findGhostEyeSlotName(NbtCompound nbt) {
        try {
            if (!nbt.contains("smfs_spirit_data")) {
                return null;
            }

            NbtCompound spiritData = nbt.getCompound("smfs_spirit_data");
            if (!spiritData.contains("GhostSlots")) {
                return null;
            }

            NbtCompound ghostSlots = spiritData.getCompound("GhostSlots");

            for (String slotName : ghostSlots.getKeys()) {
                if (slotName.startsWith("Slot")) {
                    NbtCompound slot = ghostSlots.getCompound(slotName);

                    if (slot.contains("item")) {
                        NbtCompound item = slot.getCompound("item");

                        if (item.contains("id")) {
                            String itemId = item.getString("id");

                            if ("smfs:silent_ghost_eye".equals(itemId)) {
                                LOGGER.debug("找到鬼眼Slot: {}", slotName);
                                return slotName;
                            }
                        }
                    }
                }
            }

            LOGGER.debug("未找到包含鬼眼的Slot");
            return null;

        } catch (Exception e) {
            LOGGER.error("查找鬼眼Slot时发生错误: {}", e.getMessage());
            return null;
        }
    }

    // 新增方法：用于API包装类的备用方案

    /**
     * 通过NBT设置灵异伤害（备用方案）
     */
    public static boolean setSpiritualDamageToNBT(PlayerEntity player, int damage) {
        try {
            NbtCompound nbt = player.writeNbt(new NbtCompound());

            String[] damagePaths = {
                "smfs_spirit_data.spiritDamage",
                "smfs:spiritual_damage",
                "smfs:damage",
                "ForgeData.smfs_spiritual_damage"
            };

            for (String path : damagePaths) {
                if (path.contains(".")) {
                    String[] parts = path.split("\\.");
                    if (parts.length == 2 && nbt.contains(parts[0])) {
                        NbtCompound compound = nbt.getCompound(parts[0]);
                        compound.putInt(parts[1], damage);
                        nbt.put(parts[0], compound);
                        player.readNbt(nbt);
                        LOGGER.debug("通过嵌套NBT路径 {} 设置灵异伤害: {}", path, damage);
                        return true;
                    }
                } else {
                    nbt.putInt(path, damage);
                    player.readNbt(nbt);
                    LOGGER.debug("通过NBT路径 {} 设置灵异伤害: {}", path, damage);
                    return true;
                }
            }

            LOGGER.warn("无法找到合适的NBT路径设置灵异伤害");
            return false;

        } catch (Exception e) {
            LOGGER.error("通过NBT设置灵异伤害失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 通过NBT设置灵异力量（备用方案）
     */
    public static boolean setSpiritualStrengthToNBT(PlayerEntity player, int power) {
        try {
            NbtCompound nbt = player.writeNbt(new NbtCompound());

            String[] powerPaths = {
                "smfs_spirit_data.currentSpirit",
                "smfs:spiritual_strength",
                "smfs:strength",
                "ForgeData.smfs_spiritual_strength"
            };

            for (String path : powerPaths) {
                if (path.contains(".")) {
                    String[] parts = path.split("\\.");
                    if (parts.length == 2 && nbt.contains(parts[0])) {
                        NbtCompound compound = nbt.getCompound(parts[0]);
                        compound.putInt(parts[1], power);
                        nbt.put(parts[0], compound);
                        player.readNbt(nbt);
                        LOGGER.debug("通过嵌套NBT路径 {} 设置灵异力量: {}", path, power);
                        return true;
                    }
                } else {
                    nbt.putInt(path, power);
                    player.readNbt(nbt);
                    LOGGER.debug("通过NBT路径 {} 设置灵异力量: {}", path, power);
                    return true;
                }
            }

            LOGGER.warn("无法找到合适的NBT路径设置灵异力量");
            return false;

        } catch (Exception e) {
            LOGGER.error("通过NBT设置灵异力量失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 通过NBT检查驾驭状态（备用方案）
     */
    public static boolean hasTamedGhostInNBT(PlayerEntity player, String ghostType) {
        try {
            NbtCompound nbt = player.writeNbt(new NbtCompound());

            String tamedKey = "guiyu:tamed_" + ghostType;
            if (nbt.contains(tamedKey)) {
                return nbt.getBoolean(tamedKey);
            }

            if (nbt.contains("smfs_spirit_data")) {
                NbtCompound spiritData = nbt.getCompound("smfs_spirit_data");
                if (spiritData.contains("GhostSlots")) {
                    NbtCompound ghostSlots = spiritData.getCompound("GhostSlots");
                    for (String slotName : ghostSlots.getKeys()) {
                        if (slotName.startsWith("Slot")) {
                            NbtCompound slot = ghostSlots.getCompound(slotName);
                            if (slot.contains("item")) {
                                NbtCompound item = slot.getCompound("item");
                                if (item.contains("id")) {
                                    String itemId = item.getString("id");
                                    if (itemId.contains(ghostType)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return false;

        } catch (Exception e) {
            LOGGER.error("通过NBT检查驾驭状态失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 通过NBT注册驾驭物品（备用方案）
     */
    public static boolean registerTameableItemInNBT(PlayerEntity player, String ghostType,
                                                    int spiritBonus, int powerBonus, int damageBonus,
                                                    int resistanceBonus, int sanityBonus, double revivalFactor) {
        try {
            NbtCompound nbt = player.writeNbt(new NbtCompound());

            String tamedKey = "guiyu:tamed_" + ghostType;
            nbt.putBoolean(tamedKey, true);

            nbt.putInt("guiyu:" + ghostType + "_spirit_bonus", spiritBonus);
            nbt.putInt("guiyu:" + ghostType + "_power_bonus", powerBonus);
            nbt.putInt("guiyu:" + ghostType + "_damage_bonus", damageBonus);
            nbt.putInt("guiyu:" + ghostType + "_resistance_bonus", resistanceBonus);
            nbt.putInt("guiyu:" + ghostType + "_sanity_bonus", sanityBonus);
            nbt.putDouble("guiyu:" + ghostType + "_revival_factor", revivalFactor);

            player.readNbt(nbt);
            LOGGER.debug("通过NBT注册驾驭物品: {}", ghostType);
            return true;

        } catch (Exception e) {
            LOGGER.error("通过NBT注册驾驭物品失败: {}", e.getMessage());
            return false;
        }
    }

    public static boolean hasGhostBlood(PlayerEntity player) {
        if (!isSmfsLoaded) return false;
        try {
            Class<?> playerEventsClass = Class.forName("com.xie.smfs.common.events.PlayerEvents");
            Method getGhostSlotItemMethod = playerEventsClass.getMethod("getGhostSlotItem", PlayerEntity.class, int.class);
            for (int i = 0; i < 10; i++) {
                Object itemStack = getGhostSlotItemMethod.invoke(null, player, i);
                if (itemStack instanceof ItemStack stack && !stack.isEmpty()) {
                    Identifier id = Registries.ITEM.getId(stack.getItem());
                    if (id.getPath().contains("ghost_blood")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("检测鬼血驾驭状态失败: {}", e.getMessage());
        }
        return false;
    }

    public static boolean hasGhostBloodLake(PlayerEntity player) {
        if (!isSmfsLoaded) return false;
        try {
            Class<?> playerEventsClass = Class.forName("com.xie.smfs.common.events.PlayerEvents");
            Method getGhostTypeMethod = playerEventsClass.getMethod("getGhostTypeInSlot", PlayerEntity.class, int.class);
            for (int i = 0; i < 10; i++) {
                Object ghostType = getGhostTypeMethod.invoke(null, player, i);
                if ("ghost_blood_lake".equals(ghostType) || "ghost_lake".equals(ghostType)) {
                    return true;
                }
            }
        } catch (Exception e) {
            LOGGER.debug("检测鬼血湖驾驭状态失败: {}", e.getMessage());
        }
        return false;
    }

    public static int countOccupiedGhostSlots(PlayerEntity player) {
        if (!isSmfsLoaded) return 0;
        try {
            Class<?> playerEventsClass = Class.forName("com.xie.smfs.common.events.PlayerEvents");
            Method countMethod = playerEventsClass.getMethod("countOccupiedGhostSlots", PlayerEntity.class);
            Object result = countMethod.invoke(null, player);
            return (int) result;
        } catch (Exception e) {
            LOGGER.debug("统计驾驭鬼数量失败: {}", e.getMessage());
        }
        return 0;
    }

    public static int getMaxAvailableLayer(PlayerEntity player) {
        int maxStrength = getMaxSpiritualStrength(player);
        int ghostCount = countOccupiedGhostSlots(player);
        boolean hasBlood = hasGhostBlood(player) || hasGhostBloodLake(player);

        if (maxStrength >= 9000 && hasBlood && ghostCount >= 6) {
            return 9;
        }
        if (maxStrength >= 7000) return 8;
        if (maxStrength >= 5500) return 7;
        if (maxStrength >= 3500) return 6;
        if (maxStrength >= 2600) return 5;
        if (maxStrength >= 1200) return 4;
        if (maxStrength >= 500) return 3;
        if (maxStrength >= 400) return 2;
        return 1;
    }

    public static boolean safeRemoveMusicBoxCurse(net.minecraft.server.network.ServerPlayerEntity player) {
        try {
            Class<?> musicBoxCurseClass = Class.forName("com.xie.smfs.effect.MusicBoxCurseEffect");
            Field safeRemovalField = musicBoxCurseClass.getDeclaredField("SAFE_REMOVAL_PLAYERS");
            safeRemovalField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Set<UUID> safeRemovalSet = (Set<UUID>) safeRemovalField.get(null);
            safeRemovalSet.add(player.getUuid());
            net.minecraft.entity.effect.StatusEffect effect = Registries.STATUS_EFFECT.get(new Identifier("smfs", "music_box_curse"));
            if (effect != null && player.hasStatusEffect(effect)) {
                player.removeStatusEffect(effect);
            }
            return true;
        } catch (Exception e) {
            LOGGER.debug("安全移除八音盒诅咒失败: {}", e.getMessage());
            return false;
        }
    }

    public static boolean safeRemoveGhostSuppression(net.minecraft.server.network.ServerPlayerEntity player) {
        try {
            Class<?> ghostSuppressionClass = Class.forName("com.xie.smfs.effect.GhostSuppressionEffect");
            Field safeRemovalField = ghostSuppressionClass.getDeclaredField("SAFE_REMOVAL_PLAYERS");
            safeRemovalField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Set<UUID> safeRemovalSet = (Set<UUID>) safeRemovalField.get(null);
            safeRemovalSet.add(player.getUuid());
            net.minecraft.entity.effect.StatusEffect effect = Registries.STATUS_EFFECT.get(new Identifier("smfs", "ghost_suppression"));
            if (effect != null && player.hasStatusEffect(effect)) {
                player.removeStatusEffect(effect);
            }
            return true;
        } catch (Exception e) {
            LOGGER.debug("安全移除鬼中药失败: {}", e.getMessage());
            return false;
        }
    }
}