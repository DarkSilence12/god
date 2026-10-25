package bai.client;

import bai.god.shen.ShenNetwork;

/**
 * 言出法随 —— Forge 客户端入口。
 * <p>Forge 网络层基于 SimpleChannel 全局消息处理，客户端无需额外注册回调；
 * 此入口保留以对齐原版结构，并可在需要时挂载客户端初始化逻辑。</p>
 */
public class GodClient {

	public static void onInitializeClient() {
		ShenNetwork.registerClient();
	}
}
