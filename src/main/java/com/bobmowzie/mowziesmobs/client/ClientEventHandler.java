package com.bobmowzie.mowziesmobs.client;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import com.bobmowzie.mowziesmobs.server.entity.effects.EntityAxeAttack;
import com.bobmowzie.mowziesmobs.server.entity.frostmaw.EntityFrozenController;
import com.bobmowzie.mowziesmobs.server.item.ItemBarakoMask;
import com.bobmowzie.mowziesmobs.server.item.ItemBarakoaMask;
import com.bobmowzie.mowziesmobs.server.item.ItemWroughtAxe;
import com.bobmowzie.mowziesmobs.server.item.ItemWroughtHelm;
import com.bobmowzie.mowziesmobs.server.potion.PotionHandler;
import com.bobmowzie.mowziesmobs.server.property.MowzieLivingProperties;
import com.bobmowzie.mowziesmobs.server.property.MowziePlayerProperties;
import com.mojang.blaze3d.platform.GlStateManager;
import com.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public enum ClientEventHandler {
    INSTANCE;

    private static final ResourceLocation MARIO = new ResourceLocation(MowziesMobs.MODID, "textures/gui/mario.png");
    private static final ResourceLocation FROZEN_BLUR = new ResourceLocation(MowziesMobs.MODID, "textures/gui/frozenblur.png");

    long startWroughtnautHitTime;

    long lastWroughtnautHitTime;

    @SubscribeEvent
    public void onFrameRender(RenderItemInFrameEvent event) {
        if (event.getItem().getItem() instanceof ItemWroughtAxe) {
            GlStateManager.translatef(0.325f, 0.4f, -0.05f);
            GlStateManager.scalef(-0.65f, -0.65f, 0.65f);
            GlStateManager.rotatef(45f, 0f, -1f, 0f);
            GlStateManager.rotatef(45f, -1f, 0f, -1f);
        } else if (event.getItem().getItem() instanceof ItemWroughtHelm) {
            GlStateManager.translatef(0.19f, -0.37f, -0.25f);
        } else if (event.getItem().getItem() instanceof ItemBarakoaMask) {
            GlStateManager.rotatef(180f, 0f, 1f, 0f);
            GlStateManager.translatef(0f, -0.2f, 0.1f);
        } else if (event.getItem().getItem() instanceof ItemBarakoMask) {
            GlStateManager.scalef(0.85f, 0.85f, 0.85f);
            GlStateManager.translatef(0.32f, -0.4f, -0.25f);
        }
    }

    @SubscribeEvent
    public void onHandRender(RenderSpecificHandEvent event) {
        PlayerEntity player = Minecraft.getInstance().player;
        MowziePlayerProperties propertyPlayer = EntityPropertiesHandler.INSTANCE.getProperties(player, MowziePlayerProperties.class);
        if (event.getHand() == Hand.MAIN_HAND && propertyPlayer != null && propertyPlayer.untilAxeSwing > 0) {
            event.setCanceled(true);
        }
    }

    /*@SubscribeEvent
    public void onRenderPlayerPre(PlayerModelEvent.SetRotationAngles event) {
        if (event.getEntityPlayer() == null) {
            return;
        }
        PlayerEntity player = event.getEntityPlayer();
        ModelBiped model = event.getModel();
        player.getHeldItem(Hand.MAIN_HAND);
        MowziePlayerProperties propertyPlayer = EntityPropertiesHandler.INSTANCE.getProperties(player, MowziePlayerProperties.class);
        float delta = LLibrary.PROXY.getPartialTicks();
        if (propertyPlayer != null && propertyPlayer.geomancy.tunneling) {
            model.isSneak = false;
            Vec3d moveVec = new Vec3d(player.motionX, player.motionY, player.motionZ);
            moveVec = moveVec.normalize();
            GlStateManager.rotate(45 - 45 * (float)moveVec.y, 1.0F, 0.0F, 0.0F);
/* DEAD CODE
            toDefaultBiped(event.getModel());

            float spin = 1f * (player.ticksExisted + delta);
            event.getModel().bipedHead.rotateAngleX = 1.57f * Math.min(0f, (float)moveVec.y);
            event.getModel().bipedHead.rotateAngleY = spin;
            event.getModel().bipedHead.rotateAngleZ = 0;

            event.getModel().bipedHeadwear.rotateAngleX = 1.57f * Math.min(0f, (float)moveVec.y);
            event.getModel().bipedHeadwear.rotateAngleY = spin;
            event.getModel().bipedHeadwear.rotateAngleZ = 0;

            event.getModel().bipedBody.rotateAngleX = 0;
            event.getModel().bipedBody.rotateAngleY = spin;
            event.getModel().bipedBody.rotateAngleZ = 0;

            event.getModel().bipedLeftArm.rotateAngleX = -3.14f;
            event.getModel().bipedLeftArm.rotateAngleY = spin;
            event.getModel().bipedLeftArm.rotateAngleZ = 0;
            event.getModel().bipedLeftArm.setRotationPoint(5 * (float)Math.sin(spin + Math.PI/2), 2, 5 * (float)Math.cos(spin + Math.PI/2));

            event.getModel().bipedRightArm.rotateAngleX = -3.14f;
            event.getModel().bipedRightArm.rotateAngleY = spin;
            event.getModel().bipedRightArm.rotateAngleZ = 0;
            event.getModel().bipedRightArm.setRotationPoint(-5 * (float)Math.sin(spin + Math.PI/2), 2, -5 * (float)Math.cos(spin + Math.PI/2));

            event.getModel().bipedLeftLeg.rotateAngleX = 0;
            event.getModel().bipedLeftLeg.rotateAngleY = spin;
            event.getModel().bipedLeftLeg.rotateAngleZ = 0;
            event.getModel().bipedLeftLeg.setRotationPoint(1.9F * (float)Math.sin(spin + Math.PI/2), 12.0F, 1.9F * (float)Math.cos(spin + Math.PI/2));

            event.getModel().bipedRightLeg.rotateAngleX = 0;
            event.getModel().bipedRightLeg.rotateAngleY = spin;
            event.getModel().bipedRightLeg.rotateAngleZ = 0;
            event.getModel().bipedRightLeg.setRotationPoint(-1.9F * (float)Math.sin(spin + Math.PI/2), 12.0F, -1.9f * (float)Math.cos(spin + Math.PI/2));*/
        /*}

        // Axe of a thousand metals attack animations
        if (propertyPlayer != null && propertyPlayer.untilAxeSwing > 0) {
            float frame = (MowziePlayerProperties.SWING_COOLDOWN - propertyPlayer.untilAxeSwing) + delta;
            ModelRenderer arm = model.bipedRightArm;
            if (propertyPlayer.verticalSwing) {
                float swingArc = 3f;
                arm.rotateAngleX = -2.7f + (float) (swingArc * 1 / (1 + Math.exp(1.3f * (-frame + EntityAxeAttack.SWING_DURATION_HOR / 2f))));
                arm.rotateAngleX = Math.min(arm.rotateAngleX, -0.1f);
                if (!model.isSneak) {
                    GlStateManager.translate(0.0F, 0.3F, 0.0F);
                }
                model.isSneak = true;
            } else {
                float swingArc = 2.5f;
                arm.rotateAngleX = -1.75f + (float) (swingArc * 1 / (1 + Math.exp(1.3f * (-frame + EntityAxeAttack.SWING_DURATION_HOR / 2f))));
                arm.rotateAngleZ = 1.5f;
            }
        }
    }*/

    private void toDefaultBiped(BipedModel model) {
        model.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        model.bipedHead.rotateAngleX = 0;
        model.bipedHead.rotateAngleY = 0;
        model.bipedHead.rotateAngleZ = 0;
        model.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
        model.bipedHeadwear.rotateAngleX = 0;
        model.bipedHeadwear.rotateAngleY = 0;
        model.bipedHeadwear.rotateAngleZ = 0;
        model.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        model.bipedBody.rotateAngleX = 0;
        model.bipedBody.rotateAngleY = 0;
        model.bipedBody.rotateAngleZ = 0;
        model.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        model.bipedRightArm.rotateAngleX = 0;
        model.bipedRightArm.rotateAngleY = 0;
        model.bipedRightArm.rotateAngleZ = 0;
        model.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        model.bipedLeftArm.rotateAngleX = 0;
        model.bipedLeftArm.rotateAngleY = 0;
        model.bipedLeftArm.rotateAngleZ = 0;
        model.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
        model.bipedRightLeg.rotateAngleX = 0;
        model.bipedRightLeg.rotateAngleY = 0;
        model.bipedRightLeg.rotateAngleZ = 0;
        model.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        model.bipedLeftLeg.rotateAngleX = 0;
        model.bipedLeftLeg.rotateAngleY = 0;
        model.bipedLeftLeg.rotateAngleZ = 0;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            MowziePlayerProperties propertyPlayer = EntityPropertiesHandler.INSTANCE.getProperties(player, MowziePlayerProperties.class);
            if (propertyPlayer != null && propertyPlayer.geomancy.canUse(player) && propertyPlayer.geomancy.isSpawningBoulder() && propertyPlayer.geomancy.getSpawnBoulderCharge() > 2) {
                Vec3d lookPos = propertyPlayer.geomancy.getLookPos();
                Vec3d playerEyes = player.getEyePosition(MowziesMobs.PROXY.getPartialTicks());
                Vec3d vec = playerEyes.subtract(lookPos).normalize();
                float yaw = (float) Math.atan2(vec.z, vec.x);
                float pitch = (float) Math.asin(vec.y);
                float dYaw = ((float) (yaw * 180/Math.PI + 90) - player.rotationYaw)/2f;
                float dPitch = ((float)(pitch * 180/Math.PI) - player.rotationPitch)/2f;
                player.rotationYaw += dYaw;
                player.rotationPitch += dPitch;
                stopMouseMove();
            }
            MowzieLivingProperties propertyLiving = EntityPropertiesHandler.INSTANCE.getProperties(player, MowzieLivingProperties.class);
            if (player.isPotionActive(PotionHandler.FROZEN) && propertyLiving.prevFrozen) {
                stopMouseMove();
            }
        }
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        MowzieLivingProperties property = EntityPropertiesHandler.INSTANCE.getProperties(entity, MowzieLivingProperties.class);
        if (entity.isPotionActive(PotionHandler.FROZEN) && property.prevFrozen) {
            entity.rotationYaw = entity.prevRotationYaw = property.frozenYaw;
            entity.rotationPitch = entity.prevRotationPitch = property.frozenPitch;
            entity.rotationYawHead = entity.prevRotationYawHead = property.frozenYawHead;
            entity.renderYawOffset = entity.prevRenderYawOffset = property.frozenRenderYawOffset;
            entity.swingProgress = entity.prevSwingProgress = property.frozenSwingProgress;
            entity.limbSwingAmount = entity.prevLimbSwingAmount = property.frozenLimbSwingAmount;
            entity.setSneaking(false);
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post e) {
        final int startTime = 210;
        final int pointStart = 1200;
        final int timePerMillis = 22;
        if (e.getType() == ElementType.POTION_ICONS) {
            long now = System.currentTimeMillis();
            if (now - lastWroughtnautHitTime < 500) {
                int t = (int) (now - startWroughtnautHitTime);
                int progress = t / timePerMillis;
                int time = startTime - progress;
                if (time < 0) {
                    startWroughtnautHitTime = now;
                    progress = 0;
                    time = startTime;
                }
                int points = pointStart + progress * 50;
                Minecraft.getMinecraft().getTextureManager().bindTexture(MARIO);
                ScaledResolution res = e.getResolution();
                int offsetY = 16;
                int col = res.getScaledWidth() / 4;
                // MARIO
                int marioOffsetX = col / 2 - 18;
                AbstractGui.drawModalRectWithCustomSizedTexture(marioOffsetX, offsetY, 0, 16, 39, 7, 64, 64);
                // points
                drawMarioNumber(marioOffsetX, offsetY + 8, points, 6);
                // Coin
                int coinOffsetX = col + col / 2 - 15;
                int coinU = 40 + ((int) (Math.max(0, MathHelper.sin(t * 0.005F)) * 2 + 0.5F)) * 6;
                AbstractGui.drawModalRectWithCustomSizedTexture(coinOffsetX, offsetY + 8, coinU, 8, 5, 8, 64, 64);
                // x02
                AbstractGui.drawModalRectWithCustomSizedTexture(coinOffsetX + 9, offsetY + 8, 16, 8, 23, 7, 64, 64);
                // WORLD 1-1
                AbstractGui.drawModalRectWithCustomSizedTexture(col * 2 + col / 2 - 19, offsetY, 0, 24, 39, 15, 64, 64);
                // TIME
                int timeOffsetX = col * 3 + col / 2 - 15;
                AbstractGui.drawModalRectWithCustomSizedTexture(timeOffsetX, offsetY, 0, 40, 30, 7, 64, 64);
                // Time
                drawMarioNumber(timeOffsetX + 8, offsetY + 8, time, 3);
            }
            if (Minecraft.getMinecraft().player.isPotionActive(PotionHandler.FROZEN) && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(FROZEN_BLUR);
                ScaledResolution res = e.getResolution();
                AbstractGui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, res.getScaledWidth(), res.getScaledHeight(), res.getScaledWidth(), res.getScaledHeight());
            }
        }
    }

    // Remove frozen overlay
    @SubscribeEvent
    public void onRenderHUD(RenderGameOverlayEvent.Pre event) {
        ClientPlayerEntity player = Minecraft.getMinecraft().player;
        if (player != null && player.isRiding()) {
            if (player.getRidingEntity() instanceof EntityFrozenController) {
                if (event.getType().equals(RenderGameOverlayEvent.ElementType.HEALTHMOUNT)) {
                    event.setCanceled(true);
                }
                if (event.getType().equals(RenderGameOverlayEvent.ElementType.ALL)) {
                    Minecraft.getMinecraft().ingameGUI.setOverlayMessage("", false);
                }
            }
        }
    }

    private static void drawMarioNumber(int x, int y, int value, int length) {
        for (int n = 0; n < length; n++, value /= 10) {
            int digit = value % 10;
            AbstractGui.drawModalRectWithCustomSizedTexture(x + 8 * (length - n - 1), y, digit * 8 % 64, digit / 8 * 8, 8, 7, 64, 64);
        }
    }

    public static void stopMouseMove() {
        Mouse.getDX();
        Mouse.getDY();
        Minecraft.getMinecraft().mouseHelper.deltaX = Minecraft.getMinecraft().mouseHelper.deltaY = 0;
    }
}
