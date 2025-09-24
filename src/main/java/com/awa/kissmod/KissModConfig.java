package com.awa.kissmod;

import java.io.*;
import java.util.Properties;

public class KissModConfig {
    private static final String CONFIG_FILE = "config/kissmod.properties";
    private static final String RIGHT_CLICK_KEY = "rightClickEnabled";
    private static final String CHAT_MESSAGE_KEY = "chatMessageEnabled";
    private static final String KISS_MESSAGE_KEY = "kissMessage";
    private static final String RESPONSE_MESSAGE_KEY = "responseMessage";
    private static final String KISS_COOLDOWN_KEY = "kissCooldown";
    private static final String DO_NOT_DISTURB_KEY = "doNotDisturb";

    public static boolean loadConfig() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            return true;
        }

        try (InputStream input = new FileInputStream(configFile)) {
            Properties prop = new Properties();
            prop.load(input);
            return Boolean.parseBoolean(prop.getProperty(RIGHT_CLICK_KEY, "true"));
        } catch (IOException e) {
            return true;
        }
    }

    public static boolean loadChatMessageConfig() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            return true; // 默认开启
        }

        try (InputStream input = new FileInputStream(configFile)) {
            Properties prop = new Properties();
            prop.load(input);
            return Boolean.parseBoolean(prop.getProperty(CHAT_MESSAGE_KEY, "true"));
        } catch (IOException e) {
            return true; // 默认开启
        }
    }
    
    public static String loadKissMessageConfig() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            return "%s对%s亲了亲并说爱你呦~"; // 默认消息格式
        }

        try (InputStream input = new FileInputStream(configFile)) {
            Properties prop = new Properties();
            prop.load(input);
            return prop.getProperty(KISS_MESSAGE_KEY, "%s对%s亲了亲并说爱你呦~");
        } catch (IOException e) {
            return "%s对%s亲了亲并说爱你呦~"; // 默认消息格式
        }
    }
    
    public static String loadResponseMessageConfig() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            return "%s窝也爱你喵~"; // 默认回应格式
        }

        try (InputStream input = new FileInputStream(configFile)) {
            Properties prop = new Properties();
            prop.load(input);
            return prop.getProperty(RESPONSE_MESSAGE_KEY, "%s窝也爱你喵~");
        } catch (IOException e) {
            return "%s窝也爱你喵~"; // 默认回应格式
        }
    }
    
    public static int loadKissCooldownConfig() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            return 10; // 默认10秒冷却时间
        }

        try (InputStream input = new FileInputStream(configFile)) {
            Properties prop = new Properties();
            prop.load(input);
            return Integer.parseInt(prop.getProperty(KISS_COOLDOWN_KEY, "10"));
        } catch (IOException | NumberFormatException e) {
            return 10; // 默认10秒冷却时间
        }
    }
    
    public static boolean loadDoNotDisturbConfig() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            return false; // 默认关闭免打扰
        }

        try (InputStream input = new FileInputStream(configFile)) {
            Properties prop = new Properties();
            prop.load(input);
            return Boolean.parseBoolean(prop.getProperty(DO_NOT_DISTURB_KEY, "false"));
        } catch (IOException e) {
            return false; // 默认关闭免打扰
        }
    }

    public static void saveConfig(boolean state) {
        File configDir = new File("config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        // 先读取现有配置
        Properties prop = new Properties();
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                prop.load(input);
            } catch (IOException e) {}
        }
        
        // 更新配置并保存
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            prop.setProperty(RIGHT_CLICK_KEY, String.valueOf(state));
            prop.store(output, "KissMod Configuration");
        } catch (IOException e) {}
    }
    
    public static void saveChatMessageConfig(boolean state) {
        File configDir = new File("config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        // 先读取现有配置
        Properties prop = new Properties();
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                prop.load(input);
            } catch (IOException e) {}
        }
        
        // 更新配置并保存
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            prop.setProperty(CHAT_MESSAGE_KEY, String.valueOf(state));
            prop.store(output, "KissMod Configuration");
        } catch (IOException e) {}
    }
    
    public static void saveKissMessageConfig(String message) {
        File configDir = new File("config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        // 先读取现有配置
        Properties prop = new Properties();
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                prop.load(input);
            } catch (IOException e) {}
        }
        
        // 更新配置并保存
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            prop.setProperty(KISS_MESSAGE_KEY, message);
            prop.store(output, "KissMod Configuration");
        } catch (IOException e) {}
    }
    
    public static void saveResponseMessageConfig(String message) {
        File configDir = new File("config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        // 先读取现有配置
        Properties prop = new Properties();
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                prop.load(input);
            } catch (IOException e) {}
        }
        
        // 更新配置并保存
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            prop.setProperty(RESPONSE_MESSAGE_KEY, message);
            prop.store(output, "KissMod Configuration");
        } catch (IOException e) {}
    }
    
    public static void saveKissCooldownConfig(int cooldown) {
        File configDir = new File("config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        // 先读取现有配置
        Properties prop = new Properties();
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                prop.load(input);
            } catch (IOException e) {}
        }
        
        // 更新配置并保存
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            prop.setProperty(KISS_COOLDOWN_KEY, String.valueOf(cooldown));
            prop.store(output, "KissMod Configuration");
        } catch (IOException e) {}
    }
    
    public static void saveDoNotDisturbConfig(boolean state) {
        File configDir = new File("config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        // 先读取现有配置
        Properties prop = new Properties();
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                prop.load(input);
            } catch (IOException e) {}
        }
        
        // 更新配置并保存
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            prop.setProperty(DO_NOT_DISTURB_KEY, String.valueOf(state));
            prop.store(output, "KissMod Configuration");
        } catch (IOException e) {}
    }
}