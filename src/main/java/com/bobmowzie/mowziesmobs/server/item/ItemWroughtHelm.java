package com.bobmowzie.mowziesmobs.server.item;

import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Items;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;

public class ItemWroughtHelm extends ArmorItem {
    private static final WroughtHelmMaterial ARMOR_WROUGHT_HELM = new WroughtHelmMaterial();

    public ItemWroughtHelm(Item.Properties properties) {
        super(ARMOR_WROUGHT_HELM, EquipmentSlotType.HEAD, properties);
    }

    // Dirty trick to get our item to render as the item model
    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return null;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        if (ConfigHandler.TOOLS_AND_ABILITIES.WROUGHT_HELM.breakable) return super.getIsRepairable(toRepair, repair);
        return false;
    }

    @Override
    public boolean isDamageable() {
        return ConfigHandler.TOOLS_AND_ABILITIES.WROUGHT_HELM.breakable;
    }

    @Override
    public int getDamage(ItemStack stack) {
        return ConfigHandler.TOOLS_AND_ABILITIES.WROUGHT_HELM.breakable ? super.getDamage(stack): 0;
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        if (ConfigHandler.TOOLS_AND_ABILITIES.WROUGHT_HELM.breakable) super.setDamage(stack, damage);
    }

    private static class WroughtHelmMaterial implements IArmorMaterial {

        @Override
        public int getDurability(EquipmentSlotType equipmentSlotType) {
            return ArmorMaterial.IRON.getDurability(equipmentSlotType);
        }

        @Override
        public int getDamageReductionAmount(EquipmentSlotType equipmentSlotType) {
            return ConfigHandler.TOOLS_AND_ABILITIES.WROUGHT_HELM.armorData.damageReduction;
        }

        @Override
        public int getEnchantability() {
            return ArmorMaterial.IRON.getEnchantability();
        }

        @Override
        public SoundEvent getSoundEvent() {
            return ArmorMaterial.IRON.getSoundEvent();
        }

        @Override
        public Ingredient getRepairMaterial() {
            return ArmorMaterial.IRON.getRepairMaterial();
        }

        @Override
        public String getName() {
            return "wrought_helm";
        }

        @Override
        public float getToughness() {
            return ConfigHandler.TOOLS_AND_ABILITIES.WROUGHT_HELM.armorData.toughness;
        }
    }
}
