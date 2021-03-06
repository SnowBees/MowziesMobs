package com.bobmowzie.mowziesmobs.server.property.power;

import com.bobmowzie.mowziesmobs.server.property.MowziePlayerProperties;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

public abstract class Power {

    private MowziePlayerProperties properties;

    public Power(MowziePlayerProperties properties) {
        this.properties = properties;
    }

    public void onUpdate(TickEvent.PlayerTickEvent event) {

    }

    public void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {

    }

    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {

    }

    public void onRightClickWithItem(PlayerInteractEvent.RightClickItem event) {

    }

    public void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {

    }

    public void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {

    }

    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {

    }

    public void onLeftClickEntity(AttackEntityEvent event) {

    }

    public void onTakeDamage(LivingHurtEvent event) {

    }

    public void onJump(LivingEvent.LivingJumpEvent event) {

    }

    public void onRightMouseDown(PlayerEntity player) {

    }

    public void onLeftMouseDown(PlayerEntity player) {

    }

    public void onRightMouseUp(PlayerEntity player) {

    }

    public void onLeftMouseUp(PlayerEntity player) {

    }

    public void onSneakDown(PlayerEntity player) {

    }

    public void onSneakUp(PlayerEntity player) {

    }

    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public MowziePlayerProperties getProperties() {
        return properties;
    }

    public List<LivingEntity> getEntityLivingBaseNearby(double distanceX, double distanceY, double distanceZ, double radius) {
        return getEntitiesNearby(LivingEntity.class, distanceX, distanceY, distanceZ, radius);
    }

    public <T extends Entity> List<T> getEntitiesNearby(Class<T> entityClass, double r) {
        PlayerEntity player = properties.getEntity();
        if (player == null) return Lists.<T>newArrayList();
        return player.world.getEntitiesWithinAABB(entityClass, player.getBoundingBox().grow(r, r, r), e -> e != player && player.getDistance(e) <= r);
    }

    public <T extends Entity> List<T> getEntitiesNearby(Class<T> entityClass, double dX, double dY, double dZ, double r) {
        PlayerEntity player = properties.getEntity();
        if (player == null) return Lists.<T>newArrayList();
        return player.world.getEntitiesWithinAABB(entityClass, player.getBoundingBox().grow(dX, dY, dZ), e -> e != player && player.getDistance(e) <= r);
    }

}
