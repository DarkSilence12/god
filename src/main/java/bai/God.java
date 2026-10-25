package bai;

import bai.god.shen.ShenCommand;
import bai.god.shen.ShenServerNetwork;
import bai.god.shen.ShenSystem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 言出法随 —— Forge 服务端入口。
 *
 * <p>从鬼域（guiyu）模组中独立移植出来的完整「言出法随」系统：
 * 规则 = 事件 + 条件 + 目标 + 效果，可自由组合成自定义法则。
 * 支持第三方模组内容自动适配（实体/物品/效果/NBT 路径），
 * 并对神秘复苏（smfs）做了深度集成。</p>
 */
@Mod(God.MOD_ID)
public class God {
	public static final String MOD_ID = "god";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public God() {
		ShenSystem.initialize();
		LOGGER.info("言出法随系统初始化完成");

		ShenServerNetwork.registerServer();

		MinecraftForge.EVENT_BUS.register(this);
	}

	// 注册 /god shen 命令（无权限要求，任何玩家可用）
	@SubscribeEvent
	public void onRegisterCommands(RegisterCommandsEvent event) {
		ShenCommand.register(event.getDispatcher());
	}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			player.server.execute(() -> ShenSystem.loadPlayerLaws(player));
		}
	}

	@SubscribeEvent
	public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			player.server.execute(() -> ShenSystem.unloadPlayerLaws(player));
		}
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
