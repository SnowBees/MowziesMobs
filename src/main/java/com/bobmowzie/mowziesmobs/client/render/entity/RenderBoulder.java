package com.bobmowzie.mowziesmobs.client.render.entity;

import com.bobmowzie.mowziesmobs.client.model.entity.ModelBoulder;
import com.bobmowzie.mowziesmobs.server.entity.effects.EntityBoulder;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;
import java.util.TreeMap;

@OnlyIn(Dist.CLIENT)
public class RenderBoulder extends EntityRenderer<EntityBoulder> {
    private static final ResourceLocation TEXTURE_DIRT = new ResourceLocation("textures/blocks/dirt.png");
    private static final ResourceLocation TEXTURE_STONE = new ResourceLocation("textures/blocks/stone.png");
    private static final ResourceLocation TEXTURE_SANDSTONE = new ResourceLocation("textures/blocks/sandstone.png");
    private static final ResourceLocation TEXTURE_CLAY = new ResourceLocation("textures/blocks/clay.png");
    Map<String, ResourceLocation> texMap;

    ModelBoulder model;

    public RenderBoulder(EntityRendererManager mgr) {
        super(mgr);
        model = new ModelBoulder();
        texMap = new TreeMap<String, ResourceLocation>();
        texMap.put(Blocks.STONE.getTranslationKey(), TEXTURE_STONE);
        texMap.put(Blocks.DIRT.getTranslationKey(), TEXTURE_DIRT);
        texMap.put(Blocks.CLAY.getTranslationKey(), TEXTURE_CLAY);
        texMap.put(Blocks.SANDSTONE.getTranslationKey(), TEXTURE_SANDSTONE);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBoulder entity) {
//        if (entity.storedBlock != null) {
//            return Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(entity.storedBlock).;
//        }
//        else return TEXTURE_DIRT;
        if (entity.storedBlock != null) {
            ResourceLocation tex = texMap.get(entity.storedBlock.getBlock().getTranslationKey());
            if (tex != null) return tex;
        }
        return TEXTURE_DIRT;
    }

    @Override
    public void doRender(EntityBoulder entity, double x, double y, double z, float yaw, float delta) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        model.render(entity, 0.0625F, delta);
        GlStateManager.popMatrix();
    }
}
