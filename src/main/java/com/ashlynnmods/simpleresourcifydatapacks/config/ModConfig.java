package com.ashlynnmods.simpleresourcifydatapacks.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(name = "simple-resourcify-datapacks")
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Excluded
    private static ConfigHolder<ModConfig> holder;

    // Used by TitleScreenMixin to show startup errors
    @ConfigEntry.Gui.Excluded
    public static boolean pendingRevertToast = false;

    public boolean registerDatapackSource = true;
    public String datapackPath = "datapacks";

    public static ModConfig get() {
        if (holder == null) {
            try {
                holder = AutoConfig.getConfigHolder(ModConfig.class);
            } catch (RuntimeException e) {
                holder = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
            }
        }
        return holder.getConfig();
    }

    public static void save() {
        if (holder != null) {
            holder.save();
        }
    }
    
    public static void showToast() {
        try {
            net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
            if (client != null && client.getToastManager() != null) {

                net.minecraft.client.gui.components.toasts.SystemToast toast = net.minecraft.client.gui.components.toasts.SystemToast.multiline(
                    client, 
                    net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                    net.minecraft.network.chat.Component.translatable("simple-resourcify-datapacks.toast.title"),
                    net.minecraft.network.chat.Component.translatable("simple-resourcify-datapacks.toast.description")
                );
                
                client.getToastManager().addToast(toast);
            }
        } catch (Exception e) {
            // Ignore
        }
    }
}