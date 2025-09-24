package com.awa.kissmod.client;

import com.awa.kissmod.KissMod;
import com.awa.kissmod.KissModConfig;
import com.awa.kissmod.packet.KissC2SPacket;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.Random;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = KissMod.MOD_ID, value = Dist.CLIENT)
public class KissModClient {
    public static boolean rightClickEnabled = KissModConfig.loadConfig();
    public static boolean chatMessageEnabled = KissModConfig.loadChatMessageConfig();
    public static String kissMessageFormat = KissModConfig.loadKissMessageConfig();
    public static String responseMessageFormat = KissModConfig.loadResponseMessageConfig();
    public static int kissCooldown = KissModConfig.loadKissCooldownConfig();
    public static boolean doNotDisturb = KissModConfig.loadDoNotDisturbConfig();
    public static boolean playerOnly = KissModConfig.loadPlayerOnlyConfig(); // 新增配置项
    private static KeyMapping kissKey;
    private static boolean wasKeyPressed = false;
    private static long lastTriggerTime = 0;
    private static long lastTriggerTime2 = 0; // 右键的冷却时间
    private static final long TRIGGER_INTERVAL = 175;
    
    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(KissModClient::clientSetup);
        modEventBus.addListener(KissModClient::registerKeyBindings);

        MinecraftForge.EVENT_BUS.register(KissModClient.class);
    }
    
    private static void clientSetup(final FMLClientSetupEvent event) {}
    
    private static void registerKeyBindings(final RegisterKeyMappingsEvent event) {
        kissKey = new KeyMapping(
                "key.kissmod.kiss", 
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F7,
                "category.kissmod.keybindings"
        );
        event.register(kissKey);
    }
    
    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        
        dispatcher.register(
            net.minecraft.commands.Commands.literal("kissmod-rightclick")
                .executes(context -> {
                    rightClickEnabled = !rightClickEnabled;
                    KissModConfig.saveConfig(rightClickEnabled);
                    String translationKey = rightClickEnabled ? "kissmod.toggle.enabled" : "kissmod.toggle.disabled";
                    context.getSource().sendSuccess(() -> Component.translatable(translationKey), false);
                    return 1;
                })
                .then(net.minecraft.commands.Commands.argument("state", BoolArgumentType.bool())
                    .executes(context -> {
                        boolean state = BoolArgumentType.getBool(context, "state");
                        rightClickEnabled = state;
                        KissModConfig.saveConfig(state);
                        String translationKey = state ? "kissmod.toggle.enabled" : "kissmod.toggle.disabled";
                        context.getSource().sendSuccess(() -> Component.translatable(translationKey), false);
                        return 1;
                    })
                )
        );
        
        // 添加控制聊天消息的命令
        dispatcher.register(
            net.minecraft.commands.Commands.literal("kissmod-chat")
                .executes(context -> {
                    chatMessageEnabled = !chatMessageEnabled;
                    KissModConfig.saveChatMessageConfig(chatMessageEnabled);
                    String translationKey = chatMessageEnabled ? "kissmod.chat.enabled" : "kissmod.chat.disabled";
                    context.getSource().sendSuccess(() -> Component.translatable(translationKey), false);
                    return 1;
                })
                .then(net.minecraft.commands.Commands.argument("state", BoolArgumentType.bool())
                    .executes(context -> {
                        boolean state = BoolArgumentType.getBool(context, "state");
                        chatMessageEnabled = state;
                        KissModConfig.saveChatMessageConfig(state);
                        String translationKey = state ? "kissmod.chat.enabled" : "kissmod.chat.disabled";
                        context.getSource().sendSuccess(() -> Component.translatable(translationKey), false);
                        return 1;
                    })
                )
        );
        
        // 添加设置亲吻消息格式的命令
        dispatcher.register(
            net.minecraft.commands.Commands.literal("kissmod-kissmsg")
                .then(net.minecraft.commands.Commands.argument("format", StringArgumentType.greedyString())
                    .executes(context -> {
                        String format = StringArgumentType.getString(context, "format");
                        kissMessageFormat = format;
                        KissModConfig.saveKissMessageConfig(format);
                        context.getSource().sendSuccess(() -> Component.literal("亲吻消息格式已设置为: " + format), false);
                        return 1;
                    })
                )
        );
        
        // 添加设置回应消息格式的命令
        dispatcher.register(
            net.minecraft.commands.Commands.literal("kissmod-responsemsg")
                .then(net.minecraft.commands.Commands.argument("format", StringArgumentType.greedyString())
                    .executes(context -> {
                        String format = StringArgumentType.getString(context, "format");
                        responseMessageFormat = format;
                        KissModConfig.saveResponseMessageConfig(format);
                        context.getSource().sendSuccess(() -> Component.literal("回应消息格式已设置为: " + format), false);
                        return 1;
                    })
                )
        );
        
        // 添加设置冷却时间的命令
        dispatcher.register(
            net.minecraft.commands.Commands.literal("kissmod-cooldown")
                .then(net.minecraft.commands.Commands.argument("seconds", IntegerArgumentType.integer(0))
                    .executes(context -> {
                        int cooldown = IntegerArgumentType.getInteger(context, "seconds");
                        kissCooldown = cooldown;
                        KissModConfig.saveKissCooldownConfig(cooldown);
                        context.getSource().sendSuccess(() -> Component.literal("亲吻冷却时间已设置为: " + cooldown + "秒"), false);
                        return 1;
                    })
                )
        );
        
        // 添加免打扰模式控制命令
        dispatcher.register(
            net.minecraft.commands.Commands.literal("kissmod-dnd")
                .executes(context -> {
                    doNotDisturb = !doNotDisturb;
                    KissModConfig.saveDoNotDisturbConfig(doNotDisturb);
                    String status = doNotDisturb ? "已开启" : "已关闭";
                    context.getSource().sendSuccess(() -> Component.literal("免打扰模式" + status), false);
                    return 1;
                })
                .then(net.minecraft.commands.Commands.argument("state", BoolArgumentType.bool())
                    .executes(context -> {
                        boolean state = BoolArgumentType.getBool(context, "state");
                        doNotDisturb = state;
                        KissModConfig.saveDoNotDisturbConfig(state);
                        String status = state ? "已开启" : "已关闭";
                        context.getSource().sendSuccess(() -> Component.literal("免打扰模式" + status), false);
                        return 1;
                    })
                )
        );
        
        // 添加玩家限定模式控制命令
        dispatcher.register(
            net.minecraft.commands.Commands.literal("kissmod-playeronly")
                .executes(context -> {
                    playerOnly = !playerOnly;
                    KissModConfig.savePlayerOnlyConfig(playerOnly);
                    String status = playerOnly ? "已开启" : "已关闭";
                    context.getSource().sendSuccess(() -> Component.literal("仅玩家之间亲吻模式" + status), false);
                    return 1;
                })
                .then(net.minecraft.commands.Commands.argument("state", BoolArgumentType.bool())
                    .executes(context -> {
                        boolean state = BoolArgumentType.getBool(context, "state");
                        playerOnly = state;
                        KissModConfig.savePlayerOnlyConfig(state);
                        String status = state ? "已开启" : "已关闭";
                        context.getSource().sendSuccess(() -> Component.literal("仅玩家之间亲吻模式" + status), false);
                        return 1;
                    })
                )
        );
    }
    
    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (!rightClickEnabled || !event.getLevel().isClientSide()) return;
        
        // 检查冷却时间
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTriggerTime2 < kissCooldown * 1000L) {
            return; // 仍在冷却中
        }
        
        if (event.getEntity().isShiftKeyDown()) {
            Entity target = event.getTarget();
            if (target != null) {
                // 检查是否只允许玩家之间亲吻
                if (playerOnly && !(target instanceof Player)) {
                    return; // 如果只允许玩家之间亲吻，且目标不是玩家，则返回
                }
                
                UUID senderUuid = null;
                String senderName = "";
                String targetName = "";
                
                if (Minecraft.getInstance().player != null) {
                    senderUuid = Minecraft.getInstance().player.getUUID();
                    senderName = Minecraft.getInstance().player.getName().getString();
                }
                
                if (target instanceof Player) {
                    targetName = ((Player) target).getName().getString();
                } else {
                    targetName = target.getName().getString();
                }
                
                KissC2SPacket packet = new KissC2SPacket(target.getUUID(), senderUuid, senderName, targetName, kissMessageFormat, responseMessageFormat, doNotDisturb);
                KissMod.NETWORK_CHANNEL.sendToServer(packet);
                triggerEffect(target, event.getLevel());
                lastTriggerTime2 = currentTime; // 更新最后触发时间
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        boolean isKeyPressed = kissKey != null && kissKey.isDown();
        long currentTime = System.currentTimeMillis();
        
        // 检查冷却时间
        if (currentTime - lastTriggerTime < kissCooldown * 1000L) {
            wasKeyPressed = isKeyPressed;
            return; // 仍在冷却中
        }
        
        if (isKeyPressed && (!wasKeyPressed || (currentTime - lastTriggerTime >= TRIGGER_INTERVAL))) {
            Minecraft client = Minecraft.getInstance();
            Entity target = client.crosshairPickEntity;
            if (target != null && client.player != null) {
                // 检查是否只允许玩家之间亲吻
                if (playerOnly && !(target instanceof Player)) {
                    wasKeyPressed = isKeyPressed;
                    return; // 如果只允许玩家之间亲吻，且目标不是玩家，则返回
                }
                
                UUID senderUuid = client.player.getUUID();
                String senderName = client.player.getName().getString();
                String targetName = "";
                
                if (target instanceof Player) {
                    targetName = ((Player) target).getName().getString();
                } else {
                    targetName = target.getName().getString();
                }
                
                KissC2SPacket packet = new KissC2SPacket(target.getUUID(), senderUuid, senderName, targetName, kissMessageFormat, responseMessageFormat, doNotDisturb);
                KissMod.NETWORK_CHANNEL.sendToServer(packet);
                if (client.level != null) {
                    triggerEffect(target, client.level);
                }
                lastTriggerTime = currentTime; // 更新最后触发时间
            }
        }
        wasKeyPressed = isKeyPressed;
    }
    
    public static void triggerEffect(Entity target, Level world) {
        if (world.isClientSide()) {
            spawnHeartParticles(world, target);
            
            net.minecraft.sounds.SoundEvent[] soundEvents = {
                    KissMod.CUSTOM_SOUND_EVENT.get(),
                    KissMod.CUSTOM_SOUND1_EVENT.get(),
                    KissMod.CUSTOM_SOUND2_EVENT.get()};
            net.minecraft.sounds.SoundEvent randomSound = soundEvents[new Random().nextInt(soundEvents.length)];
            
            world.playSound(
                    Minecraft.getInstance().player,
                    target.getX(), target.getY(), target.getZ(),
                    randomSound,
                    SoundSource.PLAYERS,
                    1.0F, 1.0F
            );
        }
    }
    
    public static void spawnHeartParticles(Level world, Entity entity) {
        if (world.isClientSide()) {
            double x = entity.getX();
            double y = entity.getY() + entity.getBbHeight();
            double z = entity.getZ();
            
            for (int i = 0; i < 20; i++) {
                double offsetX = world.getRandom().nextDouble() - 0.5;
                double offsetY = world.getRandom().nextDouble() - 0.5;
                double offsetZ = world.getRandom().nextDouble() - 0.5;
                world.addParticle(
                        ParticleTypes.HEART,
                        x + offsetX, y + offsetY, z + offsetZ,
                        0.0, 0.0, 0.0
                );
            }
        }
    }
}