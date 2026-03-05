package com.leclowndu93150.thick_air;

import com.leclowndu93150.thick_air.block.SafetyLanternBlock;
import com.leclowndu93150.thick_air.block.SignalTorchBlock;
import com.leclowndu93150.thick_air.block.WallSignalTorchBlock;
import com.leclowndu93150.thick_air.item.AirBladderItem;
import com.leclowndu93150.thick_air.item.RespiratorItem;
import com.leclowndu93150.thick_air.item.SoulfireBottleItem;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = ThickAir.MODID)
public class ModRegistry {

    public static final ItemArmor.ArmorMaterial RESPIRATOR_MATERIAL = EnumHelper.addArmorMaterial(
            "thick_air:respirator", ThickAir.MODID + ":respirator",
            5, new int[]{0, 0, 0, 1}, 15, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.0f);

    public static final CreativeTabs THICK_AIR_TAB = new CreativeTabs(ThickAir.MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(AIR_BLADDER);
        }
    };

    public static SignalTorchBlock SIGNAL_TORCH_BLOCK;
    public static WallSignalTorchBlock WALL_SIGNAL_TORCH_BLOCK;
    public static SafetyLanternBlock SAFETY_LANTERN_BLOCK;

    public static ItemArmor RESPIRATOR;
    public static AirBladderItem AIR_BLADDER;
    public static AirBladderItem REINFORCED_AIR_BLADDER;
    public static SoulfireBottleItem SOULFIRE_BOTTLE;
    public static Item SAFETY_LANTERN_ITEM;
    public static ItemBlock SIGNAL_TORCH_ITEM;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        SIGNAL_TORCH_BLOCK = new SignalTorchBlock();
        WALL_SIGNAL_TORCH_BLOCK = new WallSignalTorchBlock();
        SAFETY_LANTERN_BLOCK = new SafetyLanternBlock();

        SIGNAL_TORCH_BLOCK.setRegistryName(ThickAir.MODID, "signal_torch");
        SIGNAL_TORCH_BLOCK.setTranslationKey(ThickAir.MODID + ".signal_torch");
        SIGNAL_TORCH_BLOCK.setCreativeTab(THICK_AIR_TAB);
        WALL_SIGNAL_TORCH_BLOCK.setRegistryName(ThickAir.MODID, "wall_signal_torch");
        WALL_SIGNAL_TORCH_BLOCK.setTranslationKey(ThickAir.MODID + ".wall_signal_torch");
        SAFETY_LANTERN_BLOCK.setRegistryName(ThickAir.MODID, "safety_lantern");
        SAFETY_LANTERN_BLOCK.setTranslationKey(ThickAir.MODID + ".safety_lantern");

        event.getRegistry().registerAll(SIGNAL_TORCH_BLOCK, WALL_SIGNAL_TORCH_BLOCK);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        RESPIRATOR = (ItemArmor) new RespiratorItem(RESPIRATOR_MATERIAL, 0, EntityEquipmentSlot.HEAD)
                .setMaxDamage(77)
                .setRegistryName(ThickAir.MODID, "respirator")
                .setTranslationKey(ThickAir.MODID + ".respirator")
                .setCreativeTab(THICK_AIR_TAB);

        AIR_BLADDER = (AirBladderItem) new AirBladderItem()
                .setMaxDamage(327)
                .setRegistryName(ThickAir.MODID, "air_bladder")
                .setTranslationKey(ThickAir.MODID + ".air_bladder")
                .setCreativeTab(THICK_AIR_TAB);

        REINFORCED_AIR_BLADDER = (AirBladderItem) new AirBladderItem()
                .setMaxDamage(1962)
                .setRegistryName(ThickAir.MODID, "reinforced_air_bladder")
                .setTranslationKey(ThickAir.MODID + ".reinforced_air_bladder")
                .setCreativeTab(THICK_AIR_TAB);

        SOULFIRE_BOTTLE = (SoulfireBottleItem) new SoulfireBottleItem()
                .setMaxStackSize(16)
                .setRegistryName(ThickAir.MODID, "soulfire_bottle")
                .setTranslationKey(ThickAir.MODID + ".soulfire_bottle")
                .setCreativeTab(THICK_AIR_TAB);

        SAFETY_LANTERN_ITEM = new Item()
                .setMaxStackSize(1)
                .setRegistryName(ThickAir.MODID, "safety_lantern")
                .setTranslationKey(ThickAir.MODID + ".safety_lantern")
                .setCreativeTab(THICK_AIR_TAB);

        SIGNAL_TORCH_ITEM = (ItemBlock) new ItemBlock(SIGNAL_TORCH_BLOCK)
                .setRegistryName(ThickAir.MODID, "signal_torch")
                .setTranslationKey(ThickAir.MODID + ".signal_torch")
                .setCreativeTab(THICK_AIR_TAB);

        event.getRegistry().registerAll(
                RESPIRATOR, AIR_BLADDER, REINFORCED_AIR_BLADDER,
                SOULFIRE_BOTTLE, SAFETY_LANTERN_ITEM, SIGNAL_TORCH_ITEM
        );
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        registerItemModel(RESPIRATOR);
        registerItemModel(AIR_BLADDER);
        registerItemModel(REINFORCED_AIR_BLADDER);
        registerItemModel(SOULFIRE_BOTTLE);
        registerItemModel(SAFETY_LANTERN_ITEM);
        registerItemModel(SIGNAL_TORCH_ITEM);
    }

    private static void registerItemModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
