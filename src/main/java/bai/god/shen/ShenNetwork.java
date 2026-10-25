package bai.god.shen;

import bai.god.shen.ui.ShenClientPacketHandler;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.ConnectionData;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

/**
 * 言出法随 Forge 网络层。
 * <p>原 Fabric 版本有 5 个网络通道，这里统一收敛到一个 SimpleChannel（god:main），
 * 用一条 ShenMessage 消息携带「类型标记 + JSON 字符串」来区分 5 种消息：</p>
 * <ul>
 *   <li>open_screen   (S2C)</li>
 *   <li>request_laws  (C2S)</li>
 *   <li>sync_laws     (S2C)</li>
 *   <li>save_law      (C2S)</li>
 *   <li>delete_law    (C2S)</li>
 * </ul>
 */
public class ShenNetwork {
    private static final Logger LOGGER = LoggerFactory.getLogger("god-shen-network");
    public static final Gson GSON = new Gson();

    public static final String OPEN_SCREEN = "open_shen_screen";
    public static final String REQUEST_LAWS_C2S = "request_shen_laws";
    public static final String SYNC_LAWS_S2C = "sync_shen_laws";
    public static final String SAVE_LAW_C2S = "save_shen_law";
    public static final String DELETE_LAW_C2S = "delete_shen_law";

    private static final String PROTOCOL_VERSION = "1";

    /**
     * 通道名：god:main。客户端若未安装本模组，则握手时该通道以 ABSENT 上报；
     * 因此版本谓词必须使用 {@link NetworkRegistry#acceptMissingOr}，
     * 同时接受协议版本 "1"、对端缺失（ABSENT）与原版连接（ALLOWVANILLA），
     * 从而允许「服务端装模组、客户端不装」也能进入服务器。
     */
    public static final ResourceLocation CHANNEL_NAME = new ResourceLocation("god", "main");

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        CHANNEL_NAME,
        () -> PROTOCOL_VERSION,
        NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION),
        NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION)
    );

    static {
        CHANNEL.messageBuilder(ShenMessage.class, 0)
            .encoder(ShenMessage::encode)
            .decoder(ShenMessage::decode)
            .consumerMainThread(ShenNetwork::handle)
            .add();
    }

    public static class ShenMessage {
        public final String type;
        public final String payload;

        public ShenMessage(String type, String payload) {
            this.type = type;
            this.payload = payload;
        }

        public static void encode(ShenMessage msg, FriendlyByteBuf buf) {
            buf.writeUtf(msg.type);
            buf.writeUtf(msg.payload);
        }

        public static ShenMessage decode(FriendlyByteBuf buf) {
            return new ShenMessage(buf.readUtf(32767), buf.readUtf(32767));
        }
    }

    public static void handle(ShenMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getDirection().getReceptionSide().isServer()) {
                handleServer(msg, ctx);
            } else {
                // 客户端分支委托给客户端专用处理器；DistExecutor 保证仅客户端执行，
                // 且 ShenNetwork 字节码不再直接引用任何 net.minecraft.client 类，
                // 服务端可安全加载（避开 RuntimeDistCleaner 的 dist 校验）。
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ShenClientPacketHandler.handle(msg));
            }
        });
        ctx.setPacketHandled(true);
    }

    private static void handleServer(ShenMessage msg, NetworkEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player == null) return;
        switch (msg.type) {
            case REQUEST_LAWS_C2S -> {
                List<ShenLaw> laws = ShenDataManager.loadLaws(player);
                sendSyncLaws(player, laws);
            }
            case SAVE_LAW_C2S -> {
                ShenLaw law = GSON.fromJson(msg.payload, ShenLaw.class);
                player.server.execute(() -> {
                    ShenDataManager.saveLaw(player, law);
                    ShenEventBus.registerLaw(law);
                    List<ShenLaw> allLaws = ShenDataManager.loadLaws(player);
                    sendSyncLaws(player, allLaws);
                    LOGGER.info("已保存法则 {} 给玩家 {}", law.name, player.getName().getString());
                });
            }
            case DELETE_LAW_C2S -> {
                String lawName = msg.payload;
                player.server.execute(() -> {
                    List<ShenLaw> laws = ShenDataManager.loadLaws(player);
                    laws.removeIf(l -> l.name.equals(lawName));
                    ShenDataManager.saveLaws(player, laws);
                    ShenEventBus.unregisterByName(lawName);
                    List<ShenLaw> allLaws = ShenDataManager.loadLaws(player);
                    sendSyncLaws(player, allLaws);
                    LOGGER.info("已删除法则 {} 给玩家 {}", lawName, player.getName().getString());
                });
            }
        }
    }

    // ============ 服务端发送 ============

    /**
     * 仅当该玩家已安装本模组（连接注册了 god:main 通道）时才发送。
     * 未安装的客户端在握手时被放行进入，但不会收到任何言出法随包。
     */
    private static boolean hasGodChannel(ServerPlayer player) {
        if (player == null) return false;
        ConnectionData data = NetworkHooks.getConnectionData(player.connection.connection);
        return data != null && data.getChannels().containsKey(CHANNEL_NAME);
    }

    public static void sendOpenScreen(ServerPlayer player) {
        sendOpenScreen(player, false);
    }

    public static void sendOpenScreenLimited(ServerPlayer player) {
        sendOpenScreen(player, true);
    }

    private static void sendOpenScreen(ServerPlayer player, boolean limited) {
        if (!hasGodChannel(player)) return;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
            new ShenMessage(OPEN_SCREEN, String.valueOf(limited)));
    }

    private static void sendSyncLaws(ServerPlayer player, List<ShenLaw> laws) {
        if (!hasGodChannel(player)) return;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
            new ShenMessage(SYNC_LAWS_S2C, GSON.toJson(laws)));
    }

    // ============ 客户端发送 ============
    public static void sendRequestLaws() {
        CHANNEL.sendToServer(new ShenMessage(REQUEST_LAWS_C2S, ""));
    }

    public static void sendSaveLaw(ShenLaw law) {
        CHANNEL.sendToServer(new ShenMessage(SAVE_LAW_C2S, GSON.toJson(law)));
    }

    public static void sendDeleteLaw(String lawName) {
        CHANNEL.sendToServer(new ShenMessage(DELETE_LAW_C2S, lawName));
    }

    // 兼容旧入口：注册动作在静态初始化中完成，无需显式调用
    public static void registerServer() {
    }

    public static void registerClient() {
    }
}
