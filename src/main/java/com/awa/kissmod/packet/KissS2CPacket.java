package com.awa.kissmod.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import com.awa.kissmod.client.KissModClient;

import java.util.UUID;
import java.util.function.Supplier;

public class KissS2CPacket {
    private final UUID pattedEntityUuid;
    private final UUID whoPattedUuid;
    private final String senderName;
    private final String targetName;
    private final String kissMessageFormat;
    private final String responseMessageFormat;
    private final boolean senderDoNotDisturb;

    public KissS2CPacket(UUID pattedEntityUuid, UUID whoPattedUuid, String senderName, String targetName, String kissMessageFormat, String responseMessageFormat, boolean senderDoNotDisturb) {
        this.pattedEntityUuid = pattedEntityUuid;
        this.whoPattedUuid = whoPattedUuid;
        this.senderName = senderName;
        this.targetName = targetName;
        this.kissMessageFormat = kissMessageFormat;
        this.responseMessageFormat = responseMessageFormat;
        this.senderDoNotDisturb = senderDoNotDisturb;
    }

    public static void encode(KissS2CPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.pattedEntityUuid);
        buffer.writeUUID(packet.whoPattedUuid);
        buffer.writeUtf(packet.senderName);
        buffer.writeUtf(packet.targetName);
        buffer.writeUtf(packet.kissMessageFormat);
        buffer.writeUtf(packet.responseMessageFormat);
        buffer.writeBoolean(packet.senderDoNotDisturb);
    }

    public static KissS2CPacket decode(FriendlyByteBuf buffer) {
        UUID pattedEntityUuid = buffer.readUUID();
        UUID whoPattedUuid = buffer.readUUID();
        String senderName = buffer.readUtf(32767);
        String targetName = buffer.readUtf(32767);
        String kissMessageFormat = buffer.readUtf(32767);
        String responseMessageFormat = buffer.readUtf(32767);
        boolean senderDoNotDisturb = buffer.readBoolean();
        return new KissS2CPacket(pattedEntityUuid, whoPattedUuid, senderName, targetName, kissMessageFormat, responseMessageFormat, senderDoNotDisturb);
    }

    @OnlyIn(Dist.CLIENT)
    public static void handle(KissS2CPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft client = Minecraft.getInstance();
            ClientLevel world = client.level;

            if (world != null) {
                for (Entity entity : world.entitiesForRendering()) {
                    if (entity.getUUID().equals(packet.getPattedEntityUuid())) {
                        if (client.player != null && !client.player.getUUID().equals(packet.getWhoPattedUuid())) {
                            KissModClient.triggerEffect(entity, world);
                            break;
                        }
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }

    public UUID getPattedEntityUuid() {
        return this.pattedEntityUuid;
    }

    public UUID getWhoPattedUuid() {
        return this.whoPattedUuid;
    }
    
    public String getSenderName() {
        return this.senderName;
    }
    
    public String getTargetName() {
        return this.targetName;
    }
    
    public String getKissMessageFormat() {
        return this.kissMessageFormat;
    }
    
    public String getResponseMessageFormat() {
        return this.responseMessageFormat;
    }
    
    public boolean isSenderDoNotDisturb() {
        return this.senderDoNotDisturb;
    }
}