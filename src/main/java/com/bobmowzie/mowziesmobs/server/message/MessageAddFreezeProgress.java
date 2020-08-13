package com.bobmowzie.mowziesmobs.server.message;

import com.bobmowzie.mowziesmobs.server.property.MowzieLivingProperties;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by Josh on 5/31/2017.
 */
public class MessageAddFreezeProgress extends AbstractMessage<MessageAddFreezeProgress> {
    private int entityID;
    private float amount;

    public MessageAddFreezeProgress() {

    }

    public MessageAddFreezeProgress(EntityLivingBase entity, float amount) {
        entityID = entity.getEntityId();
        this.amount = amount;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityID);
        buf.writeFloat(amount);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityID = buf.readInt();
        amount = buf.readFloat();
    }

    @Override
    public void onClientReceived(Minecraft client, MessageAddFreezeProgress message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = player.world.getEntityByID(message.entityID);
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) entity;
            MowzieLivingProperties property = EntityPropertiesHandler.INSTANCE.getProperties(living, MowzieLivingProperties.class);
            if (property != null) {
                property.freezeProgress += amount;
                property.freezeDecayDelay = MowzieLivingProperties.MAX_FREEZE_DECAY_DELAY;
            }
        }
    }

    @Override
    public void onServerReceived(MinecraftServer server, MessageAddFreezeProgress message, EntityPlayer player, MessageContext messageContext) {

    }
}
