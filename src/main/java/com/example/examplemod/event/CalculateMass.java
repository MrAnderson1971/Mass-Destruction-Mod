package com.example.examplemod.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;

public class CalculateMass {
    private static final HashMap<Item, Double> itemMassMap = new HashMap<>();
    private static final double BODY_MASS = 80.0;

    static {
        // Base mass estimates in kilograms (kg) for different Minecraft items/blocks

        // Metals and Ores
        itemMassMap.put(Items.GOLD_BLOCK, 19320.0);       // Gold block: ~19320 kg/m³
        itemMassMap.put(Items.IRON_BLOCK, 7874.0);        // Iron block: ~7874 kg/m³
        itemMassMap.put(Items.DIAMOND_BLOCK, 3510.0);     // Diamond block: ~3510 kg/m³
        itemMassMap.put(Items.COPPER_BLOCK, 8960.0);      // Copper block: ~8960 kg/m³
        itemMassMap.put(Items.NETHERITE_BLOCK, 11000.0);  // Netherite block: estimate ~11000 kg/m³

        // Stone and Earth Materials
        itemMassMap.put(Items.STONE, 2600.0);             // Stone: ~2600 kg/m³
        itemMassMap.put(Items.COBBLESTONE, 2600.0);       // Cobblestone: same as stone
        itemMassMap.put(Items.DIRT, 1600.0);              // Dirt: ~1600 kg/m³
        itemMassMap.put(Items.SAND, 1602.0);              // Sand: ~1602 kg/m³
        itemMassMap.put(Items.GRAVEL, 1680.0);            // Gravel: ~1680 kg/m³
        itemMassMap.put(Items.OBSIDIAN, 2650.0);          // Obsidian: ~2650 kg/m³

        // Wood Materials
        itemMassMap.put(Items.OAK_LOG, 700.0);            // Oak log: ~700 kg/m³
        itemMassMap.put(Items.SPRUCE_LOG, 700.0);         // Spruce log: ~700 kg/m³
        itemMassMap.put(Items.BIRCH_LOG, 680.0);          // Birch log: ~680 kg/m³
        itemMassMap.put(Items.JUNGLE_LOG, 800.0);         // Jungle log: ~800 kg/m³
        itemMassMap.put(Items.ACACIA_LOG, 770.0);         // Acacia log: ~770 kg/m³
        itemMassMap.put(Items.DARK_OAK_LOG, 750.0);       // Dark oak log: ~750 kg/m³

        // Precious Materials
        itemMassMap.put(Items.EMERALD_BLOCK, 2710.0);     // Emerald block: ~2710 kg/m³
        itemMassMap.put(Items.LAPIS_BLOCK, 2800.0);       // Lapis block: estimate ~2800 kg/m³
        itemMassMap.put(Items.REDSTONE_BLOCK, 5000.0);    // Redstone block: estimate ~5000 kg/m³

        // Fluids (in bucket form, assume ~1m³ of fluid)
        itemMassMap.put(Items.WATER_BUCKET, 1000.0);      // Water: 1000 kg/m³
        itemMassMap.put(Items.LAVA_BUCKET, 3100.0);       // Lava: ~3100 kg/m³

        // Miscellaneous Items
        itemMassMap.put(Items.TNT, 1000.0);               // TNT block: ~1000 kg/m³
        itemMassMap.put(Items.BRICK, 2400.0);             // Brick block: ~2400 kg/m³
        itemMassMap.put(Items.GLASS, 2500.0);             // Glass: ~2500 kg/m³
        itemMassMap.put(Items.COAL_BLOCK, 1350.0);        // Coal block: ~1350 kg/m³
        itemMassMap.put(Items.HONEY_BLOCK, 1420.0);       // Honey block: ~1420 kg/m³
        itemMassMap.put(Items.ICE, 1000.0);
        itemMassMap.put(Items.PACKED_ICE, 9000.0);
        itemMassMap.put(Items.BLUE_ICE, 81_000.0);

        // Food Items (approximate)
        itemMassMap.put(Items.APPLE, 0.2);                // Apple: ~200 grams
        itemMassMap.put(Items.BREAD, 0.4);                // Bread: ~400 grams
        itemMassMap.put(Items.CARROT, 0.1);               // Carrot: ~100 grams
        itemMassMap.put(Items.POTATO, 0.15);              // Potato: ~150 grams
        itemMassMap.put(Items.BEEF, 0.25);                // Beef: ~250 grams

        // Tools and Weapons (approximate)
        itemMassMap.put(Items.DIAMOND_SWORD, 1.5);        // Diamond sword: ~1.5 kg
        itemMassMap.put(Items.IRON_PICKAXE, 2.0);         // Iron pickaxe: ~2 kg
        itemMassMap.put(Items.NETHERITE_AXE, 2.5);        // Netherite axe: ~2.5 kg

        // Add more items as needed...
    }

    public static double calculateInventoryMass(Player player) {
        double mass = BODY_MASS;
        for (ItemStack stack : player.getInventory().items) {
            mass += itemMassMap.getOrDefault(stack.getItem(), 1.0) * stack.getCount();
        }
        return mass;
    }
}
