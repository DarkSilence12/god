package bai;

import bai.god.shen.ShenCommand;
import bai.god.shen.ShenServerNetwork;
import bai.god.shen.ShenSystem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 言出法随 —— 服务端入口。
 *
 * <p>从鬼域（guiyu）模组中独立移植出来的完整「言出法随」系统：
 * 规则 = 事件 + 条件 + 目标 + 效果，可自由组合成自定义法则。
 * 支持第三方模组内容自动适配（实体/物品/效果/NBT 路径），
 * 并对神秘复苏（smfs）做了深度集成。</p>
 */
public class God implements ModInitializer {
	public static final String MOD_ID = "god";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ShenSystem.initialize();
		LOGGER.info("言出法随系统初始化完成");

		ShenServerNetwork.registerServer();

		// 注册 /god shen 命令（无权限要求，任何玩家可用）
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			ShenCommand.register(dispatcher, registryAccess, environment));

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
			server.execute(() -> ShenSystem.loadPlayerLaws(handler.player)));

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
			server.execute(() -> ShenSystem.unloadPlayerLaws(handler.player)));
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
