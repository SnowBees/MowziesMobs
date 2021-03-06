package com.bobmowzie.mowziesmobs.server.entity.effects;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import com.bobmowzie.mowziesmobs.client.model.tools.ControlledAnimation;
import com.bobmowzie.mowziesmobs.client.particle.MMParticle;
import com.bobmowzie.mowziesmobs.client.particle.ParticleFactory.ParticleArgs;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.damage.DamageUtil;
import com.bobmowzie.mowziesmobs.server.entity.LeaderSunstrikeImmune;
import com.bobmowzie.mowziesmobs.server.entity.barakoa.EntityBarako;
import com.bobmowzie.mowziesmobs.server.entity.wroughtnaut.EntityWroughtnaut;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntitySolarBeam extends Entity {
    private final double RADIUS = 20;
    public LivingEntity caster;
    public double endPosX, endPosY, endPosZ;
    public double collidePosX, collidePosY, collidePosZ;
    public ControlledAnimation appear = new ControlledAnimation(3);

    public boolean on = true;

    public Direction blockSide = null;

    private static final DataParameter<Float> YAW = EntityDataManager.createKey(EntitySolarBeam.class, DataSerializers.FLOAT);

    private static final DataParameter<Float> PITCH = EntityDataManager.createKey(EntitySolarBeam.class, DataSerializers.FLOAT);

    private static final DataParameter<Integer> DURATION = EntityDataManager.createKey(EntitySolarBeam.class, DataSerializers.VARINT);

    private static final DataParameter<Boolean> HAS_PLAYER = EntityDataManager.createKey(EntitySolarBeam.class, DataSerializers.BOOLEAN);

    private static final DataParameter<Integer> CASTER = EntityDataManager.createKey(EntitySolarBeam.class, DataSerializers.VARINT);

    public EntitySolarBeam(EntityType<? extends EntitySolarBeam> type, World world) {
        super(type, world);
        ignoreFrustumCheck = true;
    }

    public EntitySolarBeam(EntityType<? extends EntitySolarBeam> type, World world, LivingEntity caster, double x, double y, double z, float yaw, float pitch, int duration) {
        this(type, world);
        this.caster = caster;
        this.setYaw(yaw);
        this.setPitch(pitch);
        this.setDuration(duration);
        this.setPosition(x, y, z);
        this.calculateEndPos();
        this.playSound(MMSounds.LASER, 2f, 1);
        if (!world.isRemote) {
            this.setCasterID(caster.getEntityId());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (ticksExisted == 1 && world.isRemote) {
            caster = (LivingEntity) world.getEntityByID(getCasterID());
        }
        if (!world.isRemote && getHasPlayer()) {
            this.updateWithPlayer();
        }

        if (!on && appear.getTimer() == 0) {
            this.remove();
        }
        if (on && ticksExisted > 20) {
            appear.increaseTimer();
        } else {
            appear.decreaseTimer();
        }

        if (caster != null && !caster.isAlive()) remove();

        if (world.isRemote && ticksExisted <= 10) {
            int particleCount = 8;
            while (--particleCount != 0) {
                double radius = 2f;
                double yaw = rand.nextFloat() * 2 * Math.PI;
                double pitch = rand.nextFloat() * 2 * Math.PI;
                double ox = radius * Math.sin(yaw) * Math.sin(pitch);
                double oy = radius * Math.cos(pitch);
                double oz = radius * Math.cos(yaw) * Math.sin(pitch);
                double offsetX = -2 * Math.cos(getYaw());
                double offsetZ = -2 * Math.sin(getYaw());
                if (getHasPlayer()) {
                    offsetX = offsetZ = 0;
                }
                MMParticle.ORB.spawn(world, posX + ox + offsetX, posY + oy + 0.3, posZ + oz + offsetZ, ParticleArgs.get().withData(posX + offsetX, posY + 0.3, posZ + offsetZ, 10));
            }
        }
        if (ticksExisted > 20) {
            this.calculateEndPos();
            List<LivingEntity> hit = raytraceEntities(world, new Vec3d(posX, posY, posZ), new Vec3d(endPosX, endPosY, endPosZ), false, true, true).entities;
            if (blockSide != null) {
                spawnExplosionParticles(2);
            }
            if (!world.isRemote) {
                for (LivingEntity target : hit) {
                    if (caster instanceof EntityBarako && target instanceof LeaderSunstrikeImmune) {
                        continue;
                    }
                    float damageFire = 1.5f;
                    float damageMob = 3f;
                    if (caster instanceof EntityBarako) {
                        damageFire *= ConfigHandler.MOBS.BARAKO.combatData.attackMultiplier;
                        damageMob *= ConfigHandler.MOBS.BARAKO.combatData.attackMultiplier;
                    }
                    if (caster instanceof PlayerEntity) {
                        damageFire *= ConfigHandler.TOOLS_AND_ABILITIES.sunsBlessingAttackMultiplier;
                        damageMob *= ConfigHandler.TOOLS_AND_ABILITIES.sunsBlessingAttackMultiplier;
                    }
                    DamageUtil.dealMixedDamage(target, DamageSource.causeMobDamage(caster), damageMob, DamageSource.ON_FIRE, damageFire);
                }
            } else {
                for (LivingEntity e : hit) {
                    if (e instanceof EntityWroughtnaut) {
                        MowziesMobs.PROXY.solarBeamHitWroughtnaught(caster);
                        break;
                    }
                }
                if (ticksExisted - 15 < getDuration()) {
                    int particleCount = 4;
                    while (particleCount --> 0) {
                        double radius = 1f;
                        double yaw = rand.nextFloat() * 2 * Math.PI;
                        double pitch = rand.nextFloat() * 2 * Math.PI;
                        double ox = radius * Math.sin(yaw) * Math.sin(pitch);
                        double oy = radius * Math.cos(pitch);
                        double oz = radius * Math.cos(yaw) * Math.sin(pitch);
                        double o2x = -1 * Math.cos(getYaw()) * Math.cos(getPitch());
                        double o2y = -1 * Math.sin(getPitch());
                        double o2z = -1 * Math.sin(getYaw()) * Math.cos(getPitch());
                        MMParticle.ORB.spawn(world, posX + o2x + ox, posY + o2y + oy, posZ + o2z + oz, ParticleArgs.get().withData(collidePosX + o2x + ox, collidePosY + o2y + oy, collidePosZ + o2z + oz, 15));
                    }
                    particleCount = 4;
                    while (particleCount --> 0) {
                        double radius = 2f;
                        double yaw = rand.nextFloat() * 2 * Math.PI;
                        double pitch = rand.nextFloat() * 2 * Math.PI;
                        double ox = radius * Math.sin(yaw) * Math.sin(pitch);
                        double oy = radius * Math.cos(pitch);
                        double oz = radius * Math.cos(yaw) * Math.sin(pitch);
                        double o2x = -1 * Math.cos(getYaw()) * Math.cos(getPitch());
                        double o2y = -1 * Math.sin(getPitch());
                        double o2z = -1 * Math.sin(getYaw()) * Math.cos(getPitch());
                        MMParticle.ORB.spawn(world, collidePosX + o2x, collidePosY + o2y, collidePosZ + o2z, ParticleArgs.get().withData(collidePosX + o2x + ox, collidePosY + o2y + oy, collidePosZ + o2z + oz, 20));
                    }
                }
            }
        }
        if (ticksExisted - 20 > getDuration()) {
            on = false;
        }
    }

    private void spawnExplosionParticles(int amount) {
        for (int i = 0; i < amount; i++) {
            final float velocity = 0.1F;
            float yaw = (float) (rand.nextFloat() * 2 * Math.PI);
            float motionY = rand.nextFloat() * 0.08F;
            float motionX = velocity * MathHelper.cos(yaw);
            float motionZ = velocity * MathHelper.sin(yaw);
//            world.spawnParticle(EnumParticleTypes.FLAME, collidePosX, collidePosY + 0.1, collidePosZ, motionX, motionY, motionZ);
        }
        for (int i = 0; i < amount / 2; i++) {
//            world.spawnParticle(EnumParticleTypes.LAVA, collidePosX, collidePosY + 0.1, collidePosZ, 0, 0, 0);
        }
    }

    @Override
    protected void registerData() {
        getDataManager().register(YAW, 0F);
        getDataManager().register(PITCH, 0F);
        getDataManager().register(DURATION, 0);
        getDataManager().register(HAS_PLAYER, false);
        getDataManager().register(CASTER, -1);
    }

    public float getYaw() {
        return getDataManager().get(YAW);
    }

    public void setYaw(float yaw) {
        getDataManager().set(YAW, yaw);
    }

    public float getPitch() {
        return getDataManager().get(PITCH);
    }

    public void setPitch(float pitch) {
        getDataManager().set(PITCH, pitch);
    }

    public int getDuration() {
        return getDataManager().get(DURATION);
    }

    public void setDuration(int duration) {
        getDataManager().set(DURATION, duration);
    }

    public boolean getHasPlayer() {
        return getDataManager().get(HAS_PLAYER);
    }

    public void setHasPlayer(boolean player) {
        getDataManager().set(HAS_PLAYER, player);
    }

    public int getCasterID() {
        return getDataManager().get(CASTER);
    }

    public void setCasterID(int id) {
        getDataManager().set(CASTER, id);
    }

    @Override
    protected void readAdditional(CompoundNBT nbt) {}

    @Override
    protected void writeAdditional(CompoundNBT nbt) {}

    @Override
    public IPacket<?> createSpawnPacket() {
        return new SSpawnObjectPacket();
    }

    private void calculateEndPos() {
        endPosX = posX + RADIUS * Math.cos(getYaw()) * Math.cos(getPitch());
        endPosZ = posZ + RADIUS * Math.sin(getYaw()) * Math.cos(getPitch());
        endPosY = posY + RADIUS * Math.sin(getPitch());
    }

    public HitResult raytraceEntities(World world, Vec3d from, Vec3d to, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        HitResult result = new HitResult();
        result.setBlockHit(world.rayTraceBlocks(new RayTraceContext(from, to, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this)));
        if (result.blockHit != null) {
            Vec3d hitVec = result.blockHit.getHitVec();
            collidePosX = hitVec.x;
            collidePosY = hitVec.y;
            collidePosZ = hitVec.z;
            blockSide = result.blockHit.getFace();
        } else {
            collidePosX = endPosX;
            collidePosY = endPosY;
            collidePosZ = endPosZ;
            blockSide = null;
        }
        List<LivingEntity> entities = world.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(Math.min(posX, collidePosX), Math.min(posY, collidePosY), Math.min(posZ, collidePosZ), Math.max(posX, collidePosX), Math.max(posY, collidePosY), Math.max(posZ, collidePosZ)).grow(1, 1, 1));
        for (LivingEntity entity : entities) {
            if (entity == caster) {
                continue;
            }
            float pad = entity.getCollisionBorderSize() + 0.5f;
            AxisAlignedBB aabb = entity.getBoundingBox().grow(pad, pad, pad);
            boolean hit = aabb.intersects(from, to);
            if (aabb.contains(from)) {
                result.addEntityHit(entity);
            } else if (hit) {
                result.addEntityHit(entity);
            }
        }
        return result;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        return distance < 1024;
    }

    private void updateWithPlayer() {
        this.setYaw((float) ((caster.rotationYawHead + 90) * Math.PI / 180));
        this.setPitch((float) (-caster.rotationPitch * Math.PI / 180));
        this.setPosition(caster.posX, caster.posY + 1.2f, caster.posZ);
    }

    public static class HitResult {
        private BlockRayTraceResult blockHit;

        private List<LivingEntity> entities = new ArrayList<>();

        public BlockRayTraceResult getBlockHit() {
            return blockHit;
        }

        public void setBlockHit(RayTraceResult rayTraceResult) {
            if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK)
                this.blockHit = (BlockRayTraceResult) rayTraceResult.hitInfo;
        }

        public void addEntityHit(LivingEntity entity) {
            entities.add(entity);
        }
    }
}
