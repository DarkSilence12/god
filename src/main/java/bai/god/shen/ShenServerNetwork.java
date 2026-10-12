package bai.god.shen;

import com.google.gson.Gson;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 言出法随服务端网络处理器
 */
public class ShenServerNetwork {
    private static final Logger LOGGER = LoggerFactory.getLogger("god-shen-server-network");
    private static final Gson GSON = new Gson();

    public static final Identifier OPEN_SCREEN = new Identifier("god", "open_shen_screen");
    public static final Identifier REQUEST_LAWS_C2S = new Identifier("god", "request_shen_laws");
    public static final Identifier SYNC_LAWS_S2C = new Identifier("god", "sync_shen_laws");
    public static final Identifier SAVE_LAW_C2S = new Identifier("god", "save_shen_law");
    public static final Identifier DELETE_LAW_C2S = new Identifier("god", "delete_shen_law");

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_LAWS_C2S, (server, player, handler, buf, responseSender) -> {
            List<ShenLaw> laws = ShenDataManager.loadLaws(player);
            sendSyncLaws(player, laws);
        });

        ServerPlayNetworking.registerGlobalReceiver(SAVE_LAW_C2S, (server, player, handler, buf, responseSender) -> {
            String json = buf.readString();
            ShenLaw law = GSON.fromJson(json, ShenLaw.class);
            server.execute(() -> {
                ShenDataManager.saveLaw(player, law);
                ShenEventBus.registerLaw(law);
                List<ShenLaw> allLaws = ShenDataManager.loadLaws(player);
                sendSyncLaws(player, allLaws);
                LOGGER.info("已保存法则 {} 给玩家 {}", law.name, player.getName().getString());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(DELETE_LAW_C2S, (server, player, handler, buf, responseSender) -> {
            String lawName = buf.readString();
            server.execute(() -> {
                List<ShenLaw> laws = ShenDataManager.loadLaws(player);
                laws.removeIf(l -> l.name.equals(lawName));
                ShenDataManager.saveLaws(player, laws);
                ShenEventBus.unregisterByName(lawName);
                List<ShenLaw> allLaws = ShenDataManager.loadLaws(player);
                sendSyncLaws(player, allLaws);
                LOGGER.info("已删除法则 {} 给玩家 {}", lawName, player.getName().getString());
            });
        });
    }

    public static void sendOpenScreen(ServerPlayerEntity player) {
        sendOpenScreen(player, false);
    }

    public static void sendOpenScreenLimited(ServerPlayerEntity player) {
        sendOpenScreen(player, true);
    }

    private static void sendOpenScreen(ServerPlayerEntity player, boolean limited) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(limited);
        ServerPlayNetworking.send(player, OPEN_SCREEN, buf);
    }

    private static void sendSyncLaws(ServerPlayerEntity player, List<ShenLaw> laws) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(GSON.toJson(laws));
        ServerPlayNetworking.send(player, SYNC_LAWS_S2C, buf);
    }
}