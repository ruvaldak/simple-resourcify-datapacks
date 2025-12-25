package com.ashlynnmods.simpleresourcifydatapacks.mixin

import com.ashlynnmods.simpleresourcifydatapacks.config.ModConfig
import dev.dediamondpro.resourcify.services.ProjectType
import net.minecraft.client.gui.screens.Screen
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.io.File

@Mixin(ProjectType::class)
abstract class ProjectTypeMixin {

    @Inject(
        method = ["getDirectory"], 
        at = [At("HEAD")], 
        cancellable = true, 
        remap = false
    )
    private fun onGetDirectory(screen: Screen, callback: CallbackInfoReturnable<File>) {
        val self = (this as Any) as ProjectType

        if (self == ProjectType.DATA_PACK) {
            try {
                val path = ModConfig.get().datapackPath
                callback.returnValue = File(path).absoluteFile
            } catch (e: Exception) {
                callback.returnValue = File("datapacks").absoluteFile
            }
        }
    }
}