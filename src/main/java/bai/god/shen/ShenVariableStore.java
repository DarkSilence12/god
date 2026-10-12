package bai.god.shen;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShenVariableStore extends PersistentState {
    private static final String KEY = "god_shen_variables";
    private final Map<String, String> globalVars = new HashMap<>();
    private final Map<String, Map<String, String>> playerVars = new HashMap<>();

    public static ShenVariableStore get(MinecraftServer server) {
        PersistentStateManager manager = server.getOverworld().getPersistentStateManager();
        return manager.getOrCreate(ShenVariableStore::fromNbt, ShenVariableStore::new, KEY);
    }

    public void setGlobal(String key, String value) {
        globalVars.put(key, value);
        markDirty();
    }

    public String getGlobal(String key) {
        return globalVars.getOrDefault(key, "0");
    }

    public void setPlayer(UUID playerId, String key, String value) {
        playerVars.computeIfAbsent(playerId.toString(), k -> new HashMap<>()).put(key, value);
        markDirty();
    }

    public String getPlayer(UUID playerId, String key) {
        Map<String, String> vars = playerVars.get(playerId.toString());
        return vars != null ? vars.getOrDefault(key, "0") : "0";
    }

    public void modifyGlobal(String key, double amount) {
        double current = Double.parseDouble(getGlobal(key));
        setGlobal(key, String.valueOf(current + amount));
    }

    public void modifyPlayer(UUID playerId, String key, double amount) {
        double current = Double.parseDouble(getPlayer(playerId, key));
        setPlayer(playerId, key, String.valueOf(current + amount));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound global = new NbtCompound();
        for (Map.Entry<String, String> e : globalVars.entrySet()) {
            global.putString(e.getKey(), e.getValue());
        }
        nbt.put("global", global);

        NbtCompound players = new NbtCompound();
        for (Map.Entry<String, Map<String, String>> pe : playerVars.entrySet()) {
            NbtCompound pvars = new NbtCompound();
            for (Map.Entry<String, String> e : pe.getValue().entrySet()) {
                pvars.putString(e.getKey(), e.getValue());
            }
            players.put(pe.getKey(), pvars);
        }
        nbt.put("players", players);
        return nbt;
    }

    public static ShenVariableStore fromNbt(NbtCompound nbt) {
        ShenVariableStore store = new ShenVariableStore();
        if (nbt.contains("global")) {
            NbtCompound global = nbt.getCompound("global");
            for (String key : global.getKeys()) {
                store.globalVars.put(key, global.getString(key));
            }
        }
        if (nbt.contains("players")) {
            NbtCompound players = nbt.getCompound("players");
            for (String playerId : players.getKeys()) {
                NbtCompound pvars = players.getCompound(playerId);
                Map<String, String> vars = new HashMap<>();
                for (String key : pvars.getKeys()) {
                    vars.put(key, pvars.getString(key));
                }
                store.playerVars.put(playerId, vars);
            }
        }
        return store;
    }
}