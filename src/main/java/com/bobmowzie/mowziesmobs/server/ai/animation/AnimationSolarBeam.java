package com.bobmowzie.mowziesmobs.server.ai.animation;

import com.bobmowzie.mowziesmobs.server.entity.EntityHandler;
import com.bobmowzie.mowziesmobs.server.entity.MowzieEntity;
import com.bobmowzie.mowziesmobs.server.entity.effects.EntitySolarBeam;
import com.ilexiconn.llibrary.server.animation.Animation;
import com.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.entity.LivingEntity;

import java.util.EnumSet;

public class AnimationSolarBeam<T extends MowzieEntity & IAnimatedEntity> extends SimpleAnimationAI<T> {
    protected LivingEntity entityTarget;

    private EntitySolarBeam solarBeam;

    public AnimationSolarBeam(T entity, Animation animation) {
        super(entity, animation);
        this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
        entityTarget = entity.getAttackTarget();
    }

    @Override
    public void tick() {
        super.tick();
        float radius1 = 1.7f;
        if (entity.getAnimationTick() == 4 && !entity.world.isRemote) {
            solarBeam = new EntitySolarBeam(EntityHandler.SOLAR_BEAM, entity.world, entity, entity.posX + radius1 * Math.sin(-entity.rotationYaw * Math.PI / 180), entity.posY + 1.4, entity.posZ + radius1 * Math.cos(-entity.rotationYaw * Math.PI / 180), (float) ((entity.rotationYawHead + 90) * Math.PI / 180), (float) (-entity.rotationPitch * Math.PI / 180), 55);
            entity.world.addEntity(solarBeam);
        }
        if (entity.getAnimationTick() >= 4) {
            if (solarBeam != null) {
                float radius2 = 1.2f;
                double x = entity.posX + radius1 * Math.sin(-entity.rotationYaw * Math.PI / 180) + radius2 * Math.sin(-entity.rotationYawHead * Math.PI / 180) * Math.cos(-entity.rotationPitch * Math.PI / 180);
                double y = entity.posY + 1.4 + radius2 * Math.sin(-entity.rotationPitch * Math.PI / 180);
                double z = entity.posZ + radius1 * Math.cos(-entity.rotationYaw * Math.PI / 180) + radius2 * Math.cos(-entity.rotationYawHead * Math.PI / 180) * Math.cos(-entity.rotationPitch * Math.PI / 180);
                solarBeam.setPosition(x, y, z);

                float yaw = entity.rotationYawHead + 90;
                float pitch = -entity.rotationPitch;
                solarBeam.setYaw((float) (yaw * Math.PI / 180));
                solarBeam.setPitch((float) (pitch * Math.PI / 180));
            }
        }
        if (entity.getAnimationTick() >= 22) {
            if (entityTarget != null) {
                entity.getLookController().setLookPosition(entityTarget.posX, entityTarget.posY + entityTarget.getHeight() / 2, entityTarget.posZ, 2, 90);
            }
        }
    }
}
