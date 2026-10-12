package bai.god.shen;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;

public class ShenLaw {
    public String name = "";
    public String description = "";
    public List<ShenRule> rules = new ArrayList<>();
    public boolean enabled = true;

    public ShenLaw() {}

    public ShenLaw(String name) {
        this.name = name;
    }

    public void addRule(ShenRule rule) {
        rules.add(rule);
    }

    public void removeRule(int index) {
        if (index >= 0 && index < rules.size()) {
            rules.remove(index);
        }
    }

    public boolean contains(ShenRule rule) {
        return rules.contains(rule);
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("name", name);
        nbt.putString("description", description);
        nbt.putBoolean("enabled", enabled);

        NbtList ruleList = new NbtList();
        for (ShenRule rule : rules) {
            ruleList.add(rule.toNbt());
        }
        nbt.put("rules", ruleList);

        return nbt;
    }

    public static ShenLaw fromNbt(NbtCompound nbt) {
        ShenLaw law = new ShenLaw();
        law.name = nbt.getString("name");
        law.description = nbt.getString("description");
        law.enabled = nbt.getBoolean("enabled");

        NbtList ruleList = nbt.getList("rules", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < ruleList.size(); i++) {
            law.rules.add(ShenRule.fromNbt(ruleList.getCompound(i)));
        }

        return law;
    }
}