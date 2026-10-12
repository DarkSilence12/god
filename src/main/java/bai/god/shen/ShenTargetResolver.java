package bai.god.shen;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.Structure;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShenTargetResolver {

    public static List<LivingEntity> resolve(ShenTargetType targetType, ShenEvent event, ShenRule rule) {
        List<LivingEntity> targets = new ArrayList<>();

        switch (targetType) {
            case EVENT_SOURCE:
                if (event.source != null) {
                    targets.add(event.source);
                }
                break;

            case ATTACKED_TARGET:
                if (event.target != null) {
                    targets.add(event.target);
                }
                break;

            case TYPE_FILTER:
                resolveTypeFilter(targets, event, rule);
                break;

            case NEARBY_RANGE:
                resolveNearbyRange(targets, event, rule);
                break;

            case LINE_OF_SIGHT:
                resolveLineOfSight(targets, event, rule);
                break;

            case SPECIFIC_ENTITY:
                resolveSpecificEntity(targets, event, rule);
                break;

            case ALL_PLAYERS:
                resolveAllPlayers(targets, event, rule);
                break;

            case RANDOM_ONE:
                resolveRandomOne(targets, event, rule);
                break;

            case STRUCTURE_BOUNDS:
                resolveStructureBounds(targets, event, rule);
                break;

            case REGION:
                resolveRegion(targets, event, rule);
                break;
        }

        return targets;
    }

    private static void resolveAllPlayers(List<LivingEntity> targets, ShenEvent event, ShenRule rule) {
        if (event.world != null && event.world.getServer() != null) {
            for (ServerPlayerEntity player : event.world.getServer().getPlayerManager().getPlayerList()) {
                targets.add(player);
            }
        }
    }

    private static void resolveRandomOne(List<LivingEntity> targets, ShenEvent event, ShenRule rule) {
        List<LivingEntity> pool = new ArrayList<>();
        if (event.world == null || event.pos == null) return;
        double range = rule.getParamDouble("range", 50.0);
        String entityType = rule.getParam("entityType", "all");

        Box box = new Box(event.pos).expand(range);
        for (Entity entity : event.world.getOtherEntities(null, box)) {
            if (entity instanceof LivingEntity living) {
                if (entity == event.source) continue;
                if (matchesEntityType(living, entityType)) {
                    pool.add(living);
                }
            }
        }

        if (!pool.isEmpty()) {
            java.util.Random rand = new java.util.Random();
            targets.add(pool.get(rand.nextInt(pool.size())));
        }
    }

    private static void resolveTypeFilter(List<LivingEntity> targets, ShenEvent event, ShenRule rule) {
    if (event.world == null) return;
    String entityType = rule.getParam("entityType", "all");
    double range = rule.getParamDouble("range", 50);
    boolean includeSelf = rule.getParamBool("includeSelf", false);

    Box box = new Box(event.pos).expand(range);
    for (Entity entity : event.world.getOtherEntities(null, box)) {
        if (entity instanceof LivingEntity living) {
            if (!includeSelf && living == event.source) continue;
            if (matchesEntityType(living, entityType)) {
                targets.add(living);
            }
        }
    }
}

    private static boolean matchesEntityType(LivingEntity entity, String typeFilter) {
        if (typeFilter.equals("all")) return true;
        if (typeFilter.equals("player")) return entity instanceof PlayerEntity;
        if (typeFilter.equals("monster")) return entity instanceof net.minecraft.entity.mob.Monster;
        if (typeFilter.equals("animal")) return entity instanceof net.minecraft.entity.passive.AnimalEntity;

        Identifier filterId = Identifier.tryParse(typeFilter);
        if (filterId != null) {
            Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
            return filterId.equals(entityId);
        }
        return false;
    }

    private static void resolveNearbyRange(List<LivingEntity> targets, ShenEvent event, ShenRule rule) {
        if (event.world == null || event.pos == null) return;
        double range = rule.getParamDouble("range", 10.0);
        String entityType = rule.getParam("entityType", "all");
        boolean includeSource = rule.getParamBool("includeSource", false);

        Box box = new Box(event.pos).expand(range);
        for (Entity entity : event.world.getOtherEntities(null, box)) {
            if (entity instanceof LivingEntity living) {
                if (!includeSource && entity == event.source) continue;
                if (matchesEntityType(living, entityType)) {
                    targets.add(living);
                }
            }
        }
    }

    private static void resolveLineOfSight(List<LivingEntity> targets, ShenEvent event, ShenRule rule) {
        if (event.source instanceof ServerPlayerEntity player) {
            double range = rule.getParamDouble("range", 50.0);
            net.minecraft.util.hit.HitResult hitResult = player.raycast(range, 0.0f, false);

            if (hitResult instanceof net.minecraft.util.hit.EntityHitResult entityHit) {
                if (entityHit.getEntity() instanceof LivingEntity target) {
                    targets.add(target);
                    return;
                }
            }

            if (event.pos != null && event.world != null) {
                Box box = new Box(event.pos).expand(2.0);
                for (Entity entity : event.world.getOtherEntities(player, box)) {
                    if (entity instanceof LivingEntity living) {
                        targets.add(living);
                        return;
                    }
                }
            }
        }
    }

    private static void resolveSpecificEntity(List<LivingEntity> targets, ShenEvent event, ShenRule rule) {
        if (event.world == null) return;
        String uuidStr = rule.getParam("uuid", "");
        String playerName = rule.getParam("playerName", "");
        String entityName = rule.getParam("entityName", "");

        if (!uuidStr.isEmpty()) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                if (event.world.getServer() != null) {
                    for (ServerPlayerEntity player : event.world.getServer().getPlayerManager().getPlayerList()) {
                        if (player.getUuid().equals(uuid)) {
                            targets.add(player);
                            break;
                        }
                    }
                }
            } catch (IllegalArgumentException ignored) {}
        }

        if (!playerName.isEmpty()) {
            if (event.world.getServer() != null) {
                ServerPlayerEntity namedPlayer = event.world.getServer().getPlayerManager().getPlayer(playerName);
                if (namedPlayer != null) {
                    targets.add(namedPlayer);
                }
            }
        }

        if (!entityName.isEmpty()) {
            Identifier targetId = Identifier.tryParse(entityName);
            if (targetId != null && event.world.getServer() != null) {
                for (net.minecraft.server.world.ServerWorld sw : event.world.getServer().getWorlds()) {
                    for (Entity entity : sw.iterateEntities()) {
                        if (entity instanceof LivingEntity living
                            && targetId.equals(Registries.ENTITY_TYPE.getId(living.getType()))) {
                            targets.add(living);
                        }
                    }
                }
            }
        }
    }

    private static void resolveStructureBounds(List<LivingEntity> targets, ShenEvent event, ShenRule rule) {
        if (event.world == null || !(event.world instanceof ServerWorld serverWorld)) return;
        String structureId = rule.getParam("structureId", "");
        double range = rule.getParamDouble("range", 200);
        String entityType = rule.getParam("entityType", "all");

        if (structureId.isEmpty()) return;
        Identifier expectedId = Identifier.tryParse(structureId);
        BlockPos center = event.pos != null ? event.pos : (event.source != null ? event.source.getBlockPos() : BlockPos.ORIGIN);

        int chunkRange = (int) Math.ceil(range / 16.0) + 1;
        for (int dx = -chunkRange; dx <= chunkRange; dx++) {
            for (int dz = -chunkRange; dz <= chunkRange; dz++) {
                Chunk chunk = serverWorld.getChunk(
                    (center.getX() + dx * 16) >> 4,
                    (center.getZ() + dz * 16) >> 4);
                var starts = chunk.getStructureStarts();

                for (var entry : starts.entrySet()) {
                    Structure structure = entry.getKey();
                    Identifier id = serverWorld.getRegistryManager()
                        .get(RegistryKeys.STRUCTURE).getId(structure);

                    boolean matches = expectedId != null && id != null && id.equals(expectedId);
                    if (!matches && id != null) {
                        matches = id.toString().contains(structureId);
                    }

                    if (matches) {
                        var start = entry.getValue();
                        net.minecraft.util.math.BlockBox bb = start.getBoundingBox();
                        Box boundingBox = new Box(
                            bb.getMinX(), bb.getMinY(), bb.getMinZ(),
                            bb.getMaxX() + 1, bb.getMaxY() + 1, bb.getMaxZ() + 1);
                        for (Entity entity : event.world.getOtherEntities(null, boundingBox)) {
                            if (entity instanceof LivingEntity living && living != event.source
                                    && matchesEntityType(living, entityType)) {
                                targets.add(living);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void resolveRegion(List<LivingEntity> targets, ShenEvent event, ShenRule rule) {
        String centerMode = rule.getParam("centerMode", "eventPos");
        if ("eventSource".equals(centerMode) && event.source != null) {
            targets.add(event.source);
        } else if (event.source != null) {
            targets.add(event.source);
        }
    }
}