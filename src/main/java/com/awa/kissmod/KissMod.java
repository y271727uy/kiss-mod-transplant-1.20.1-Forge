package com.awa.kissmod;

import com.awa.kissmod.packet.KissC2SPacket;
import com.awa.kissmod.packet.KissS2CPacket;
import com.awa.kissmod.client.KissModClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@Mod(KissMod.MOD_ID)
public class KissMod {
    public static final String MOD_ID = "kissmod";

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK_CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister
            .create(ForgeRegistries.SOUND_EVENTS, MOD_ID);

    public static final RegistryObject<SoundEvent> CUSTOM_SOUND_EVENT = SOUND_EVENTS.register("custom_sound",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "custom_sound")));

    public static final RegistryObject<SoundEvent> CUSTOM_SOUND1_EVENT = SOUND_EVENTS.register("custom_sound1",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "custom_sound1")));

    public static final RegistryObject<SoundEvent> CUSTOM_SOUND2_EVENT = SOUND_EVENTS.register("custom_sound2",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "custom_sound2")));

    // 存储玩家的免打扰状态
    public static final Map<UUID, Boolean> PLAYER_DO_NOT_DISTURB_STATUS = new HashMap<>();

    public KissMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        SOUND_EVENTS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> KissModClient.init(modEventBus));
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            registerNetworkPackets();
        });
    }

    private void registerNetworkPackets() {
        NETWORK_CHANNEL.registerMessage(0, KissC2SPacket.class,
                KissC2SPacket::encode,
                KissC2SPacket::decode,
                KissC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NETWORK_CHANNEL.registerMessage(1, KissS2CPacket.class,
                KissS2CPacket::encode,
                KissS2CPacket::decode,
                KissS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
    
    // 设置玩家免打扰状态
    public static void setPlayerDoNotDisturb(UUID playerUUID, boolean doNotDisturb) {
        PLAYER_DO_NOT_DISTURB_STATUS.put(playerUUID, doNotDisturb);
    }
    
    // 获取玩家免打扰状态
    public static boolean getPlayerDoNotDisturb(UUID playerUUID) {
        return PLAYER_DO_NOT_DISTURB_STATUS.getOrDefault(playerUUID, false);
    }
}