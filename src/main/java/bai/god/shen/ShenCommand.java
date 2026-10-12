package bai.god.shen;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

/**
 * 言出法随命令入口：/god shen
 * 无任何权限要求，普通玩家（无作弊权限）也可直接呼出编辑器。
 */
public class ShenCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("god")
            .then(CommandManager.literal("shen")
                .then(CommandManager.literal("open")
                    .executes(ShenCommand::executeOpen))
                .then(CommandManager.literal("list")
                    .executes(ShenCommand::executeList))
                .then(CommandManager.literal("reload")
                    .executes(ShenCommand::executeReload))
                .then(CommandManager.literal("delete")
                    .then(CommandManager.argument("name", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player != null) {
                                for (ShenLaw law : ShenDataManager.loadLaws(player)) {
                                    builder.suggest(law.name);
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(ShenCommand::executeDelete)))
                .executes(ShenCommand::executeOpen)));
    }

    private static int executeOpen(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("只能由玩家执行此命令"));
            return 0;
        }

        if (!ShenSystem.canUseShen(player)) {
            player.sendMessage(Text.literal("§c你没有权限使用言出法随系统"), false);
            return 0;
        }

        player.sendMessage(Text.literal("§6[言出法随] §e打开编辑器..."), false);
        ShenServerNetwork.sendOpenScreen(player);
        return 1;
    }

    private static int executeList(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;

        if (!ShenSystem.canUseShen(player)) {
            player.sendMessage(Text.literal("§c你没有权限使用言出法随系统"), false);
            return 0;
        }

        List<ShenLaw> laws = ShenDataManager.loadLaws(player);
        if (laws.isEmpty()) {
            player.sendMessage(Text.literal("§e你还没有保存任何法则"), false);
            return 0;
        }

        player.sendMessage(Text.literal("§6=== 已保存的法则 ==="), false);
        for (int i = 0; i < laws.size(); i++) {
            ShenLaw law = laws.get(i);
            String status = law.enabled ? "§a[启用]" : "§c[禁用]";
            player.sendMessage(Text.literal("§e" + (i + 1) + ". " + law.name + " " + status + " §7(" + law.rules.size() + "条规则)"), false);
        }
        return laws.size();
    }

    private static int executeReload(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;

        if (!ShenSystem.canUseShen(player)) {
            player.sendMessage(Text.literal("§c你没有权限使用言出法随系统"), false);
            return 0;
        }

        ShenSystem.unloadPlayerLaws(player);
        ShenSystem.loadPlayerLaws(player);
        player.sendMessage(Text.literal("§a法则已重新加载"), false);
        return 1;
    }

    private static int executeDelete(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;

        if (!ShenSystem.canUseShen(player)) {
            player.sendMessage(Text.literal("§c你没有权限使用言出法随系统"), false);
            return 0;
        }

        String name = StringArgumentType.getString(context, "name");
        ShenDataManager.deleteLaw(player, name);
        player.sendMessage(Text.literal("§c已删除法则: " + name), false);
        return 1;
    }
}
