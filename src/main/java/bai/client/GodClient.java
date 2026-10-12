package bai.client;

import bai.god.shen.ShenNetwork;
import net.fabricmc.api.ClientModInitializer;

/**
 * 言出法随 —— 客户端入口。
 */
public class GodClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ShenNetwork.registerClient();
	}
}
