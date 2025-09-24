package com.awa.kissmod.packet;

import com.awa.kissmod.KissMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public class KissC2SPacket {
    private final UUID kissedEntityUuid;
    private final UUID senderUuid;
    private final String senderName;
    private final String targetName;
    private final String kissMessageFormat;
    private final String responseMessageFormat;
    private final boolean senderDoNotDisturb;

    public KissC2SPacket(UUID kissedEntityUuid, UUID senderUuid, String senderName, String targetName, String kissMessageFormat, String responseMessageFormat, boolean senderDoNotDisturb) {
        this.kissedEntityUuid = kissedEntityUuid;
        this.senderUuid = senderUuid;
        this.senderName = senderName;
        this.targetName = targetName;
        this.kissMessageFormat = kissMessageFormat;
        this.responseMessageFormat = responseMessageFormat;
        this.senderDoNotDisturb = senderDoNotDisturb;
    }

    public static void encode(KissC2SPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.kissedEntityUuid);
        buffer.writeUUID(packet.senderUuid);
        buffer.writeUtf(packet.senderName);
        buffer.writeUtf(packet.targetName);
        buffer.writeUtf(packet.kissMessageFormat);
        buffer.writeUtf(packet.responseMessageFormat);
        buffer.writeBoolean(packet.senderDoNotDisturb);
    }

    public static KissC2SPacket decode(FriendlyByteBuf buffer) {
        UUID kissedEntityUuid = buffer.readUUID();
        UUID senderUuid = buffer.readUUID();
        String senderName = buffer.readUtf(32767);
        String targetName = buffer.readUtf(32767);
        String kissMessageFormat = buffer.readUtf(32767);
        String responseMessageFormat = buffer.readUtf(32767);
        boolean senderDoNotDisturb = buffer.readBoolean();
        return new KissC2SPacket(kissedEntityUuid, senderUuid, senderName, targetName, kissMessageFormat, responseMessageFormat, senderDoNotDisturb);
    }

    public static void handle(KissC2SPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                UUID targetUuid = packet.getKissedEntityUuid();
                UUID senderUuid = packet.getSenderUuid();
                ServerLevel world = player.serverLevel();
                Entity target = world.getEntity(targetUuid);

                if (target != null) {
                    KissS2CPacket broadcastPacket = new KissS2CPacket(target.getUUID(), senderUuid, packet.senderName, packet.targetName, packet.kissMessageFormat, packet.responseMessageFormat, packet.senderDoNotDisturb);
                    for (ServerPlayer nearbyPlayer : world.players()) {
                        if (!nearbyPlayer.getUUID().equals(senderUuid)) {
                            KissMod.NETWORK_CHANNEL.send(PacketDistributor.PLAYER.with(() -> nearbyPlayer), broadcastPacket);
                        }
                    }
                    
                    // 更新发送者的免打扰状态
                    KissMod.setPlayerDoNotDisturb(senderUuid, packet.senderDoNotDisturb);
                    
                    // 检查目标玩家是否开启了免打扰模式
                    boolean targetDoNotDisturb = false;
                    if (target instanceof ServerPlayer) {
                        targetDoNotDisturb = KissMod.getPlayerDoNotDisturb(target.getUUID());
                    }
                    
                    // 如果发送者开启了免打扰，则不发送聊天消息
                    if (!packet.senderDoNotDisturb) {
                        // 发送聊天消息，使用自定义格式
                        String message = String.format(packet.kissMessageFormat, packet.senderName, packet.targetName);
                        world.getServer().getPlayerList().broadcastSystemMessage(
                            net.minecraft.network.chat.Component.literal(message), 
                            false
                        );
                        
                        // 如果目标是玩家且未开启免打扰，自动发送回应消息
                        if (target instanceof Player && !targetDoNotDisturb) {
                            String responseMessage = String.format(packet.responseMessageFormat, packet.targetName);
                            world.getServer().getPlayerList().broadcastSystemMessage(
                                net.minecraft.network.chat.Component.literal(responseMessage),
                                false
                            );
                        }
                        // 如果目标玩家开启了免打扰，发送提示消息给发送者
                        else if (target instanceof ServerPlayer && targetDoNotDisturb) {
                            String dndMessage = String.format("%s开启了免打扰", packet.targetName);
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(dndMessage));
                        }
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }

    public UUID getKissedEntityUuid() {
        return this.kissedEntityUuid;
    }

    public UUID getSenderUuid() {
        return this.senderUuid;
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