package com.ashlynnmods.simpleresourcifydatapacks.mixin;

import com.ashlynnmods.simpleresourcifydatapacks.config.ModConfig;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (ModConfig.pendingRevertToast) {
            ModConfig.showToast();
            ModConfig.pendingRevertToast = false;
        }
    }
}