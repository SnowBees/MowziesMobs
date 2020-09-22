package com.bobmowzie.mowziesmobs.server.entity.barakoa;

import com.bobmowzie.mowziesmobs.client.model.tools.ControlledAnimation;
import com.bobmowzie.mowziesmobs.server.ai.animation.EntityAIAvoidEntity;
import com.bobmowzie.mowziesmobs.server.ai.animation.*;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.entity.EntityDart;
import com.bobmowzie.mowziesmobs.server.entity.MowzieEntity;
import com.bobmowzie.mowziesmobs.server.entity.SmartBodyHelper;
import com.bobmowzie.mowziesmobs.server.entity.effects.EntitySunstrike;
import com.bobmowzie.mowziesmobs.server.item.ItemBarakoaMask;
import com.bobmowzie.mowziesmobs.server.item.ItemHandler;
import com.bobmowzie.mowziesmobs.server.loot.LootTableHandler;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import com.ilexiconn.llibrary.server.animation.Animation;
import com.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.controller.BodyController;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class EntityBarakoa extends MowzieEntity implements IRangedAttackMob, IMob {
    public static final Animation DIE_ANIMATION = Animation.create(70);
    public static final Animation HURT_ANIMATION = Animation.create(10);
    public static final Animation ATTACK_ANIMATION = Animation.create(19);
    public static final Animation PROJECTILE_ATTACK_ANIMATION = Animation.create(20);
    public static final Animation IDLE_ANIMATION = Animation.create(35);
    public static final Animation ACTIVATE_ANIMATION = Animation.create(25);
    public static final Animation DEACTIVATE_ANIMATION = Animation.create(26);
    public static final Animation BLOCK_ANIMATION = Animation.create(10);
    private static final DataParameter<Boolean> DANCING = EntityDataManager.createKey(EntityBarakoa.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> MASK = EntityDataManager.createKey(EntityBarakoa.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WEAPON = EntityDataManager.createKey(EntityBarakoa.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> ACTIVE = EntityDataManager.createKey(EntityBarakoa.class, DataSerializers.BOOLEAN);
    public ControlledAnimation doWalk = new ControlledAnimation(3);
    public ControlledAnimation dancing = new ControlledAnimation(7);
    private boolean circleDirection = true;
    protected int circleTick = 0;
    protected boolean attacking = false;
    private int timeSinceAttack = 0;
    private int cryDelay = -1;
    private int danceTimer = 0;
    private int ticksWithoutTarget;
    public int timeUntilDeath = -1;

    public EntityBarakoa(EntityType<? extends EntityBarakoa> type, World world) {
        super(type, world);
        setMask(MaskType.from(MathHelper.nextInt(rand, 1, 4)));
        stepHeight = 1;
        circleTick += rand.nextInt(200);
        frame += rand.nextInt(50);
        experienceValue = 8;
        active = false;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        setPathPriority(PathNodeType.WATER, 0);
        goalSelector.addGoal(0, new SwimGoal(this));
        goalSelector.addGoal(0, new AnimationActivateAI<>(this, ACTIVATE_ANIMATION));
        goalSelector.addGoal(0, new AnimationDeactivateAI<>(this, DEACTIVATE_ANIMATION));
        goalSelector.addGoal(1, new AnimationDieAI<>(this));
        goalSelector.addGoal(1, new EntityAIAvoidEntity<>(this, EntitySunstrike.class, EntitySunstrike::isStriking, 3, 0.7F));
        goalSelector.addGoal(2, new AnimationBlockAI<>(this, BLOCK_ANIMATION));
        goalSelector.addGoal(2, new AnimationAttackAI<>(this, ATTACK_ANIMATION, MMSounds.ENTITY_BARAKOA_SWING, null, 1, 2.5f, ConfigHandler.MOBS.BARAKOA.combatData.attackMultiplier, 9, true));
        goalSelector.addGoal(2, new AnimationProjectileAttackAI<EntityBarakoa>(this, PROJECTILE_ATTACK_ANIMATION, 9, MMSounds.ENTITY_BARAKOA_BLOWDART, true) {
            @Override
            public void startExecuting() {
                super.startExecuting();
                playSound(MMSounds.ENTITY_BARAKOA_INHALE, 0.7f, 1.2f);
            }
        });
        goalSelector.addGoal(3, new AnimationTakeDamage<>(this));
        goalSelector.addGoal(4, new SimpleAnimationAI<EntityBarakoa>(this, IDLE_ANIMATION, false, true) {
            private LivingEntity talkTarget;
            private final EntityPredicate pred = new EntityPredicate().allowFriendlyFire().allowInvulnerable().setLineOfSiteRequired().setDistance(8).setUseInvisibilityCheck();

            @Override
            public void startExecuting() {
                super.startExecuting();
                LivingEntity player = this.entity.world.func_225318_b(PlayerEntity.class, pred, entity, entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ, this.entity.getBoundingBox().grow(8.0D, 3.0D, 8.0D));
                LivingEntity barakoa = this.entity.world.func_225318_b(EntityBarakoa.class, pred, this.entity, entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ, this.entity.getBoundingBox().grow(8.0D, 3.0D, 8.0D));
                if (player == null) talkTarget = barakoa;
                else if (barakoa == null) talkTarget = player;
                else if (rand.nextBoolean()) talkTarget = player;
                else talkTarget = barakoa;
            }

            @Override
            public void tick() {
                super.tick();
                if (talkTarget != null) this.entity.lookController.setLookPosition(this.talkTarget.posX, this.talkTarget.posY + (double)this.talkTarget.getEyeHeight(), this.talkTarget.posZ, (float)this.entity.getHorizontalFaceSpeed(), (float)this.entity.getVerticalFaceSpeed());
            }
        });
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 0.5D, false));
        goalSelector.addGoal(5, new RandomWalkingGoal(this, 0.4));
        goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        goalSelector.addGoal(8, new LookAtGoal(this, EntityBarakoa.class, 8.0F));
        goalSelector.addGoal(8, new LookAtGoal(this, EntityBarako.class, 8.0F));
        goalSelector.addGoal(8, new LookRandomlyGoal(this));
    }

    @Override
    protected BodyController createBodyController() {
        return new SmartBodyHelper(this);
    }

    @Override
    public int getAttack() {
        return getMask() == MaskType.FURY ? 6 : 4;
    }

    @Override
    public Animation getDeathAnimation() {
        return DIE_ANIMATION;
    }

    @Override
    public Animation getHurtAnimation() {
        return HURT_ANIMATION;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (getAnimation() == DEACTIVATE_ANIMATION) {
            return null;
        }
        if (!active || danceTimer != 0 || (getEntitiesNearby(EntityBarakoa.class, 8, 3, 8, 8).isEmpty() && getEntitiesNearby(EntityBarako.class, 8, 3, 8, 8).isEmpty() && getEntitiesNearby(PlayerEntity.class, 8, 3, 8, 8).isEmpty())) {
            return null;
        }
        if (getAttackTarget() == null) {
            int i = MathHelper.nextInt(rand, 0, 11);
            if (i < MMSounds.ENTITY_BARAKOA_TALK.size()) {
                playSound(MMSounds.ENTITY_BARAKOA_TALK.get(i).get(), 1, 1.5f);
                AnimationHandler.INSTANCE.sendAnimationMessage(this, IDLE_ANIMATION);
            }
        } else {
            int i = MathHelper.nextInt(rand, 0, 7);
            if (i < MMSounds.ENTITY_BARAKOA_ANGRY.size()) {
                playSound(MMSounds.ENTITY_BARAKOA_ANGRY.get(i).get(), 1, 1.6f);
            }
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return active ? MMSounds.ENTITY_BARAKOA_HURT : null;
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10  * ConfigHandler.MOBS.BARAKOA.combatData.healthMultiplier);
    }

    protected void updateAttackAI() {
        if (!world.isRemote && getAttackTarget() != null && !getAttackTarget().isAlive()) setAttackTarget(null);

        if (timeSinceAttack < 80) {
            timeSinceAttack++;
        }
        if (getAttackTarget() != null) {
            if (targetDistance > 6.5) {
                getNavigator().tryMoveToXYZ(getAttackTarget().posX, getAttackTarget().posY, getAttackTarget().posZ, 0.6);
            } else {
                if (!attacking) {
                    updateCircling();
                }
            }
            if (rand.nextInt(80) == 0 && timeSinceAttack == 80 && getEntitySenses().canSee(getAttackTarget())) {
                attacking = true;
                if (getAnimation() == NO_ANIMATION && getWeapon() == 0) {
                    getNavigator().tryMoveToEntityLiving(getAttackTarget(), 0.5);
                }
            }
            if (attacking && getAnimation() == NO_ANIMATION && getEntitySenses().canSee(getAttackTarget())) {
                if (targetDistance <= 2.5 && getWeapon() == 0) {
                    attacking = false;
                    timeSinceAttack = 0;
                    AnimationHandler.INSTANCE.sendAnimationMessage(this, ATTACK_ANIMATION);
                }
                if (getWeapon() == 1) {
                    AnimationHandler.INSTANCE.sendAnimationMessage(this, PROJECTILE_ATTACK_ANIMATION);
                }
            }
        } else {
            attacking = false;
        }
    }

    @Nullable
    @Override
    public ILivingEntityData onInitialSpawn(IWorld world, DifficultyInstance difficulty, SpawnReason reason, @Nullable ILivingEntityData livingData, @Nullable CompoundNBT compound) {
        if (canHoldVaryingWeapons()) {
            setWeapon(rand.nextInt(3) == 0 ? 1 : 0);
        }
        return super.onInitialSpawn(world, difficulty, reason, livingData, compound);
    }

    protected boolean canHoldVaryingWeapons() {
        return true;
    }

    protected void updateCircling() {
        LivingEntity target = getAttackTarget();
        if (target != null) {
            if (rand.nextInt(200) == 0) {
                circleDirection = !circleDirection;
            }
            if (circleDirection) {
                circleTick++;
            } else {
                circleTick--;
            }
            if (!attacking && targetDistance < 4.5) {
                circleEntity(target, 7, 0.3f, true, circleTick, 0, 1.75f);
            } else {
                circleEntity(target, 7, 0.3f, true, circleTick, 0, 1);
            }
            attacking = false;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!world.isRemote && active && !getActive()) {
            setActive(true);
        }
        active = getActive();
        if (!active) {
            getNavigator().clearPath();
            rotationYaw = prevRotationYaw;
            renderYawOffset = rotationYaw;
            if (onGround && getAnimation() == NO_ANIMATION) {
                AnimationHandler.INSTANCE.sendAnimationMessage(this, ACTIVATE_ANIMATION);
                playSound(MMSounds.ENTITY_BARAKOA_EMERGE, 1, 1);
            }
            return;
        }
        updateAttackAI();
        if (getAnimation() != NO_ANIMATION) {
            getNavigator().clearPath();
        }

        if (getDancing()) {
            setDancing(false);
            danceTimer++;
        }

        if (getAnimation() == NO_ANIMATION || getAnimation() == IDLE_ANIMATION) {
            doWalk.increaseTimer();
        } else {
            doWalk.decreaseTimer();
        }

        if (danceTimer != 0 && danceTimer != 30) {
            danceTimer++;
            dancing.increaseTimer();
        } else {
            danceTimer = 0;
            dancing.decreaseTimer();
        }
        if (!world.isRemote && getAnimation() == NO_ANIMATION && danceTimer == 0 && rand.nextInt(800) == 0) {
            setDancing(true);
            playSound(MMSounds.ENTITY_BARAKOA_BATTLECRY_2, 1.2f, 1.3f);
        }
        if (getAnimation() != NO_ANIMATION) {
            danceTimer = 0;
        }

        if (cryDelay > -1) {
            cryDelay--;
        }
        if (cryDelay == 0) {
            playSound(MMSounds.ENTITY_BARAKOA_BATTLECRY, 1.5f, 1.5f);
        }
        if (getAttackTarget() != null && ticksWithoutTarget > 3) {
            cryDelay = MathHelper.nextInt(rand, -15, 30);
        }

        if (getAnimation() == ATTACK_ANIMATION && getAnimationTick() == 5) {
            playSound(MMSounds.ENTITY_BARAKOA_SHOUT, 1, 1.1f);
        }
//        if (getAnimation() == PROJECTILE_ATTACK_ANIMATION && getAnimationTick() == 1) {
//            playSound(MMSounds.ENTITY_BARAKOA_INHALE, 0.7f, 1.2f);
//        }

        if (getAttackTarget() == null) {
            ticksWithoutTarget++;
        } else {
            ticksWithoutTarget = 0;
        }

        if (timeUntilDeath > 0) timeUntilDeath--;
        else if (timeUntilDeath == 0) {
            attackEntityFrom(DamageSource.causeMobDamage(this), getHealth());
        }

//        if (ticksExisted > 50) setDead();
//        if (getAnimation() == NO_ANIMATION) AnimationAPI.sendAnimPacket(this, 4);
    }

    @Override
    protected void onAnimationFinish(Animation animation) {
        if (animation == ACTIVATE_ANIMATION) {
            setActive(true);
            active = true;
        }
        if (animation == DEACTIVATE_ANIMATION) {
            remove();
            ItemBarakoaMask mask = ItemHandler.BARAKOA_MASK_FURY;
            switch (getMask()) {
                case BLISS:
                    mask = ItemHandler.BARAKOA_MASK_BLISS;
                    break;
                case FEAR:
                    mask = ItemHandler.BARAKOA_MASK_FEAR;
                    break;
                case FURY:
                    mask = ItemHandler.BARAKOA_MASK_FURY;
                    break;
                case MISERY:
                    mask = ItemHandler.BARAKOA_MASK_MISERY;
                    break;
                case RAGE:
                    mask = ItemHandler.BARAKOA_MASK_RAGE;
                    break;
            }
            if (!world.isRemote) {
                ItemStack item = entityDropItem(new ItemStack(mask), 1.5f).getItem();
                item.setDamage((int) Math.ceil((1.0f - getHealthRatio()) * item.getMaxDamage()));
            }
        }
    }

    @Override
    protected SoundEvent getDeathSound() {
        return MMSounds.ENTITY_BARAKOA_DIE;
    }

    @Override
    protected void registerData() {
        super.registerData();
        getDataManager().register(DANCING, false);
        getDataManager().register(MASK, 0);
        getDataManager().register(WEAPON, 0);
        getDataManager().register(ACTIVE, true);
    }

    public boolean getDancing() {
        return getDataManager().get(DANCING);
    }

    public void setDancing(boolean dancing) {
        getDataManager().set(DANCING, dancing);
    }

    public MaskType getMask() {
        return MaskType.from(getDataManager().get(MASK));
    }

    public void setMask(MaskType type) {
        getDataManager().set(MASK, type.ordinal());
    }

    public int getWeapon() {
        return getDataManager().get(WEAPON);
    }

    public void setWeapon(int type) {
        getDataManager().set(WEAPON, type);
    }

    public boolean getActive() {
        return getDataManager().get(ACTIVE);
    }

    public void setActive(boolean active) {
        getDataManager().set(ACTIVE, active);
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("mask", getMask().ordinal());
        compound.putInt("weapon", getWeapon());
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        setMask(MaskType.from(compound.getInt("mask")));
        setWeapon(compound.getInt("weapon"));
    }

    @Override
    public void attackEntityWithRangedAttack(LivingEntity target, float p_82196_2_) {
        AbstractArrowEntity dart = new EntityDart(this.world, this);
        double dx = target.posX - this.posX;
        double dy = target.getBoundingBox().minY + (double)(target.getHeight() / 3.0F) - dart.posY;
        double dz = target.posZ - this.posZ;
        double dist = (double)MathHelper.sqrt(dx * dx + dz * dz);
        dart.shoot(dx, dy + dist * 0.2D, dz, 1.6F, 1);
        int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, this.getHeldItem(Hand.MAIN_HAND));
        int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, this.getHeldItem(Hand.MAIN_HAND));
        dart.setDamage((double) (p_82196_2_ * 2.0F) + this.rand.nextGaussian() * 0.25D + (double) ((float) this.world.getDifficulty().getId() * 0.11F));

        if (i > 0) {
            dart.setDamage(dart.getDamage() + (double) i * 0.5D + 0.5D);
        }

        if (j > 0) {
            dart.setKnockbackStrength(j);
        }

        dart.setDamage(dart.getDamage() * ConfigHandler.MOBS.BARAKOA.combatData.attackMultiplier);

        this.world.addEntity(dart);
        attacking = false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        if (getAnimation() == DEACTIVATE_ANIMATION) {
            return false;
        }
        Entity entity = source.getTrueSource();
        boolean angleFlag = true;
        if (entity != null) {
            int arc = 220;
            float entityHitAngle = (float) ((Math.atan2(entity.posZ - posZ, entity.posX - posX) * (180 / Math.PI) - 90) % 360);
            float entityAttackingAngle = renderYawOffset % 360;
            if (entityHitAngle < 0) {
                entityHitAngle += 360;
            }
            if (entityAttackingAngle < 0) {
                entityAttackingAngle += 360;
            }
            float entityRelativeAngle = entityHitAngle - entityAttackingAngle;
            angleFlag = (entityRelativeAngle <= arc / 2.0 && entityRelativeAngle >= -arc / 2.0) || (entityRelativeAngle >= 360 - arc / 2.0 || entityRelativeAngle <= -arc + 90 / 2.0);
        }
        if (angleFlag && getMask().canBlock && entity instanceof LivingEntity && (getAnimation() == NO_ANIMATION || getAnimation() == HURT_ANIMATION || getAnimation() == BLOCK_ANIMATION)) {
            blockingEntity = (LivingEntity) entity;
            playSound(SoundEvents.ITEM_SHIELD_BLOCK, 0.3F, 1.5F);
            AnimationHandler.INSTANCE.sendAnimationMessage(this, BLOCK_ANIMATION);
            return false;
        }
        return super.attackEntityFrom(source, damage);
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        switch (getMask()) {
            case BLISS:
                return LootTableHandler.BARAKOA_BLISS;
            case FEAR:
                return LootTableHandler.BARAKOA_FEAR;
            case FURY:
                return LootTableHandler.BARAKOA_FURY;
            case MISERY:
                return LootTableHandler.BARAKOA_MISERY;
            case RAGE:
                return LootTableHandler.BARAKOA_RAGE;
        }
        return LootTableHandler.BARAKOA_FURY;
    }

    @Override
    public boolean canBeCollidedWith() {
        return active;
    }

    @Override
    public void fall(float distance, float damageMultipler) {
        if (active) {
            super.fall(distance, damageMultipler);
        }
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{DIE_ANIMATION, HURT_ANIMATION, ATTACK_ANIMATION, PROJECTILE_ATTACK_ANIMATION, BLOCK_ANIMATION, IDLE_ANIMATION, ACTIVATE_ANIMATION, DEACTIVATE_ANIMATION};
    }

    public boolean isBarakoDevoted() {
        return true;
    }

    public int randomizeWeapon() {
        return rand.nextInt(3) == 0 ? 1 : 0;
    }
}
