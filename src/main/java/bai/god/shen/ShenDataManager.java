package bai.god.shen;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShenDataManager {
    private static final String STATE_KEY = "god_shen_state";

    public static List<ShenLaw> loadLaws(ServerPlayerEntity player) {
        ShenState state = ShenState.get(player.server);
        return state.getLaws(player.getUuid());
    }

    public static void saveLaws(ServerPlayerEntity player, List<ShenLaw> laws) {
        ShenState state = ShenState.get(player.server);
        state.setLaws(player.getUuid(), laws);
        state.markDirty();
    }

    public static void saveLaw(ServerPlayerEntity player, ShenLaw law) {
        List<ShenLaw> laws = loadLaws(player);

        boolean found = false;
        for (int i = 0; i < laws.size(); i++) {
            if (laws.get(i).name.equals(law.name)) {
                laws.set(i, law);
                found = true;
                break;
            }
        }
        if (!found) {
            laws.add(law);
        }

        saveLaws(player, laws);
    }

    public static void deleteLaw(ServerPlayerEntity player, String lawName) {
        List<ShenLaw> laws = loadLaws(player);
        laws.removeIf(law -> law.name.equals(lawName));
        saveLaws(player, laws);
    }

    public static class ShenState extends PersistentState {
        private final Map<UUID, List<ShenLaw>> playerLaws = new HashMap<>();

        public static ShenState get(MinecraftServer server) {
            World world = server.getOverworld();
            return ((net.minecraft.server.world.ServerWorld) world).getPersistentStateManager()
                .getOrCreate(ShenState::fromNbt, ShenState::new, STATE_KEY);
        }

        public List<ShenLaw> getLaws(UUID playerUuid) {
            return playerLaws.getOrDefault(playerUuid, new ArrayList<>());
        }

        public void setLaws(UUID playerUuid, List<ShenLaw> laws) {
            playerLaws.put(playerUuid, new ArrayList<>(laws));
        }

        public void removeLaws(UUID playerUuid) {
            playerLaws.remove(playerUuid);
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt) {
            NbtCompound playersNbt = new NbtCompound();
            for (Map.Entry<UUID, List<ShenLaw>> entry : playerLaws.entrySet()) {
                NbtList lawList = new NbtList();
                for (ShenLaw law : entry.getValue()) {
                    lawList.add(law.toNbt());
                }
                playersNbt.put(entry.getKey().toString(), lawList);
            }
            nbt.put("playerLaws", playersNbt);
            return nbt;
        }

        public static ShenState fromNbt(NbtCompound nbt) {
            ShenState state = new ShenState();
            if (nbt.contains("playerLaws")) {
                NbtCompound playersNbt = nbt.getCompound("playerLaws");
                for (String key : playersNbt.getKeys()) {
                    try {
                        UUID uuid = UUID.fromString(key);
                        NbtList lawList = playersNbt.getList(key, NbtElement.COMPOUND_TYPE);
                        List<ShenLaw> laws = new ArrayList<>();
                        for (int i = 0; i < lawList.size(); i++) {
                            try {
                                laws.add(ShenLaw.fromNbt(lawList.getCompound(i)));
                            } catch (Exception ignored) {
                            }
                        }
                        state.playerLaws.put(uuid, laws);
                    } catch (Exception ignored) {
                    }
                }
            }
            return state;
        }
    }
}