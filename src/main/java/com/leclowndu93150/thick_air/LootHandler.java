package com.leclowndu93150.thick_air;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryTable;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LootHandler {

    public static final ResourceLocation SAFETY_LANTERN_DUNGEON = ThickAir.id("chest/inject/safety_lantern_dungeon");
    public static final ResourceLocation SAFETY_LANTERN_MINESHAFT = ThickAir.id("chest/inject/safety_lantern_mineshaft");
    public static final ResourceLocation SAFETY_LANTERN_STRONGHOLD = ThickAir.id("chest/inject/safety_lantern_stronghold");

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation id = event.getName();

        if (id.equals(LootTableList.CHESTS_SIMPLE_DUNGEON)) {
            event.getTable().addPool(createInjectionPool("thick_air_dungeon", SAFETY_LANTERN_DUNGEON));
        } else if (id.equals(LootTableList.CHESTS_ABANDONED_MINESHAFT)) {
            event.getTable().addPool(createInjectionPool("thick_air_mineshaft", SAFETY_LANTERN_MINESHAFT));
        } else if (id.equals(LootTableList.CHESTS_STRONGHOLD_CORRIDOR)) {
            event.getTable().addPool(createInjectionPool("thick_air_stronghold", SAFETY_LANTERN_STRONGHOLD));
        }
    }

    private static LootPool createInjectionPool(String name, ResourceLocation lootTable) {
        return new LootPool(
                new LootEntry[]{
                        new LootEntryTable(lootTable, 1, 0, new LootCondition[0], "thick_air_inject")
                },
                new LootCondition[0],
                new RandomValueRange(1),
                new RandomValueRange(0),
                name
        );
    }
}
