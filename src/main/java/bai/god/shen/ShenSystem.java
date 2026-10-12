package bai.god.shen;

import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ShenSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger("god-shen");
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;
        initialized = true;

        ShenEventAdapter.initialize();

        bai.god.shen.compat.smfs.SmfsAdapter.initialize();
        bai.god.shen.compat.smfs.SmfsIntegration.initialize();
        bai.god.shen.compat.ModAdapterManager.loadAllAdapters();

        bai.god.shen.compat.smfs.SmfsIntegration.registerEventListeners();

        LOGGER.info("言出法随系统已激活");
    }

    /**
     * 言出法随对全部玩家开放，不再依赖鬼域模组的权限体系。
     */
    public static boolean canUseShen(ServerPlayerEntity player) {
        return player != null;
    }

    public static void loadPlayerLaws(ServerPlayerEntity player) {
        if (!canUseShen(player)) return;
        List<ShenLaw> laws = ShenDataManager.loadLaws(player);
        for (ShenLaw law : laws) {
            ShenEventBus.registerLaw(law);
        }
    }

    public static void unloadPlayerLaws(ServerPlayerEntity player) {
        List<ShenLaw> laws = ShenDataManager.loadLaws(player);
        for (ShenLaw law : laws) {
            ShenEventBus.unregisterLaw(law);
        }
    }
}