package bai.god.shen.ui;

import bai.god.shen.ShenLaw;
import bai.god.shen.ShenNetwork;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 客户端专用网络处理器。
 * <p>只处理服务端发往客户端的 S2C 消息（打开界面 / 同步法则）。
 * 本类引用 net.minecraft.client 下的类，因此<b>绝不能</b>被专用服务器加载；
 * 服务端加载的 {@link ShenNetwork} 仅通过名称引用本类（mod 自身类，无 @OnlyIn 标注），
 * 由 Forge DistExecutor 保证仅在 Dist.CLIENT 上实例化，从而绕开
 * RuntimeDistCleaner 的 dist 校验。</p>
 */
public final class ShenClientPacketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("god-shen-network");

    private ShenClientPacketHandler() {
    }

    /**
     * 处理客户端收到的 S2C 消息。只在客户端侧被调用。
     */
    public static void handle(ShenNetwork.ShenMessage msg) {
        Minecraft client = Minecraft.getInstance();
        switch (msg.type) {
            case ShenNetwork.OPEN_SCREEN -> {
                boolean limited = Boolean.parseBoolean(msg.payload);
                client.execute(() -> {
                    LOGGER.info("收到打开言出法随界面请求{}", limited ? "（阉割版）" : "");
                    if (client.screen == null) {
                        client.setScreen(new ShenScreen(limited));
                        LOGGER.info("成功打开言出法随界面");
                    }
                });
            }
            case ShenNetwork.SYNC_LAWS_S2C -> {
                String json = msg.payload;
                List<ShenLaw> laws = ShenNetwork.GSON.fromJson(json, new TypeToken<List<ShenLaw>>() {
                }.getType());
                client.execute(() -> {
                    if (client.screen instanceof ShenScreen) {
                        ((ShenScreen) client.screen).onSyncLaws(laws);
                    }
                });
            }
        }
    }
}
