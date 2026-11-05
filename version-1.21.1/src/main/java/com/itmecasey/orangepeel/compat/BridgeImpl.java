package com.itmecasey.orangepeel.compat;

import com.itmecasey.orangepeel.EffectInfo;
import com.itmecasey.orangepeel.FoodInfo;
import com.itmecasey.orangepeel.PlayerState;
import com.itmecasey.orangepeel.client.Bridge;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;

/** Minecraft 1.21.1: food data is the FOOD component, with saturation stored as a modifier. */
public final class BridgeImpl implements Bridge {
    @Override
    public FoodInfo food(ItemStack stack) {
        FoodProperties props = stack.get(DataComponents.FOOD);
        if (props == null) {
            return null;
        }
        List<EffectInfo> effects = new ArrayList<>();
        for (FoodProperties.PossibleEffect possible : props.effects()) {
            var inst = possible.effect();
            effects.add(new EffectInfo(inst.getEffect().value().getDisplayName().getString(), inst.getAmplifier(),
                    inst.getDuration(), possible.probability(),
                    inst.getEffect().value().getCategory() == MobEffectCategory.BENEFICIAL));
        }
        // 1.21.1 stores saturation as a modifier; the game adds nutrition * modifier * 2 when the food is eaten.
        float saturation = props.nutrition() * props.saturation() * 2f;
        return new FoodInfo(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.getHoverName().getString(),
                props.nutrition(), saturation, props.eatDurationTicks(), effects);
    }

    @Override
    public PlayerState player() {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) {
            return null;
        }
        FoodData fd = p.getFoodData();
        boolean visible = Minecraft.getInstance().gameMode != null && Minecraft.getInstance().gameMode.canHurtPlayer()
                && !(p.getVehicle() instanceof LivingEntity);
        return new PlayerState(fd.getFoodLevel(), fd.getSaturationLevel(), com.itmecasey.orangepeel.ExhaustionSync.get(), p.getHealth(),
                p.getMaxHealth(), p.getAbsorptionAmount(), visible);
    }

    @Override
    public List<ItemStack> inventory() {
        LocalPlayer p = Minecraft.getInstance().player;
        List<ItemStack> out = new ArrayList<>();
        if (p != null) {
            for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
                out.add(p.getInventory().getItem(i));
            }
        }
        return out;
    }

    @Override
    public ItemStack heldFood() {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) {
            return ItemStack.EMPTY;
        }
        if (p.getMainHandItem().has(DataComponents.FOOD)) {
            return p.getMainHandItem();
        }
        if (p.getOffhandItem().has(DataComponents.FOOD)) {
            return p.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }
}
