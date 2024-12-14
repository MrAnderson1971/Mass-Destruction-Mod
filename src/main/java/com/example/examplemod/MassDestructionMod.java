package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import static com.example.examplemod.event.CalculateMass.calculateInventoryMass;

@Mod(MassDestructionMod.MODID)
public class MassDestructionMod {

    public static final String MODID = "mass_destruction";

    private final HashMap<Player, Vec3> prevPositions = new HashMap<>();
    private final Queue<BlockPos> explosionQueue = new LinkedList<>();

    private final double TNT_ENERGY_JOULES = 6.9036e9;
    private final int EXPLOSIONS_PER_TICK = 100;
    private final double G = 31.32;

    Logger logger = LogManager.getLogger(MODID);

    public MassDestructionMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            Level world = player.level();

            // Get player's fall velocity
            double fallDistance = event.getDistance();

            if (fallDistance > 3) { // Threshold to avoid small falls triggering explosions
                double mass = calculateInventoryMass(player);
                double kineticEnergy = mass * G * fallDistance; // Joules

                if (kineticEnergy > 0.1) {
                    BlockPos pos = player.blockPosition();

                    if (world instanceof ServerLevel sl) {
                        logger.info("Kinetic Energy Released From Falling: {} Joules", String.format("%,d", (long) kineticEnergy));
                        scheduleExplosions(sl, pos, (int) (kineticEnergy / TNT_ENERGY_JOULES));
                    }
                }
            }
        }
    }

    private void scheduleExplosions(ServerLevel world, BlockPos initialPos, int totalExplosions) {
        for (int i = 0; i < totalExplosions; i++) {
            // Calculate a dynamic offset to spread explosions farther out as more are generated
            int distanceOffset = (int) Math.sqrt(i) * 2;

            // Randomize explosion positions within the calculated distance offset
            BlockPos explosionPos = initialPos.offset(
                    world.random.nextInt(distanceOffset * 2 + 1) - distanceOffset,
                    world.random.nextInt(distanceOffset * 2 + 1) - distanceOffset,
                    world.random.nextInt(distanceOffset * 2 + 1) - distanceOffset
            );

            explosionQueue.add(explosionPos);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        Level world = player.level();
        if (world instanceof ServerLevel sl) {
            Vec3 current = player.position();
            Vec3 previous = prevPositions.getOrDefault(player, current);
            Vec3 displacement = current.subtract(previous);
            double speed = displacement.horizontalDistance() / 0.05;

            prevPositions.put(player, current);

            // Predict next position
            AABB predictedAABB = player.getBoundingBox();
            Optional<AABB> vehicleAABB = player.getVehicle() == null ? Optional.empty() :
                    Optional.of(player.getVehicle().getBoundingBox());

            // Check collision with predicted AABB
            boolean aboutToCollide = sl.getEntities(player, predictedAABB, entity ->
                            entity instanceof LivingEntity && entity != player && entity != player.getVehicle())
                    .stream()
                    .findAny()
                    .isPresent();
            boolean vehicleAboutToCollide = vehicleAABB.map(aabb -> sl.getEntities(player.getVehicle(), aabb,
                            entity -> entity instanceof LivingEntity && entity != player && entity != player.getVehicle())
                    .stream()
                    .findAny()
                    .isPresent()).orElse(false);

            if (aboutToCollide || vehicleAboutToCollide) {
                double mass = calculateInventoryMass(player);
                double kineticEnergy = 0.5 * mass * speed * speed; // Joules

                BlockPos pos = player.blockPosition();
                logger.info("Kinetic Energy Released During Collision: {} Joules", String.format("%,d", (long) kineticEnergy));
                scheduleExplosions(sl, pos, (int)(kineticEnergy / TNT_ENERGY_JOULES));
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            int explosionsToProcess = Math.min(EXPLOSIONS_PER_TICK, explosionQueue.size());

            for (int i = 0; i < explosionsToProcess; i++) {
                BlockPos pos = explosionQueue.poll();
                if (pos == null) {
                    continue;
                }
                for (ServerLevel world : event.getServer().getAllLevels()) {
                    if (world == null) {
                        continue;
                    }
                    world.explode(null, pos.getX(), pos.getY(), pos.getZ(), 4.0F,
                            Level.ExplosionInteraction.BLOCK);
                }
            }
        }
    }
}
