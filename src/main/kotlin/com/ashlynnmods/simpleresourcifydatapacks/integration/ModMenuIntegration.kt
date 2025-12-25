package com.ashlynnmods.simpleresourcifydatapacks.integration

import com.ashlynnmods.simpleresourcifydatapacks.config.ModConfig
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import me.shedaniel.clothconfig2.api.ConfigBuilder
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import java.io.File
import java.nio.file.InvalidPathException
import java.nio.file.Paths
import java.util.Optional
import java.util.function.Supplier

class ModMenuIntegration : ModMenuApi {

    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent: Screen ->
            val builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("text.autoconfig.simple-resourcify-datapacks.title"))

            val entryBuilder = builder.entryBuilder()
            val general = builder.getOrCreateCategory(Component.translatable("text.autoconfig.simple-resourcify-datapacks.title"))
            
            val currentConfig = ModConfig.get()

            general.addEntry(
                entryBuilder.startBooleanToggle(
                    Component.translatable("text.autoconfig.simple-resourcify-datapacks.option.registerDatapackSource"),
                    currentConfig.registerDatapackSource
                )
                .setDefaultValue(true)
                .setTooltip(Component.translatable("text.autoconfig.simple-resourcify-datapacks.option.registerDatapackSource.@Tooltip"))
                .setSaveConsumer { newValue -> currentConfig.registerDatapackSource = newValue }
                .build()
            )

            var currentPathError: Component? = null
            val defaultTooltip = Component.translatable("text.autoconfig.simple-resourcify-datapacks.option.datapackPath.@Tooltip")

            val pathEntry = entryBuilder.startStrField(
                    Component.translatable("text.autoconfig.simple-resourcify-datapacks.option.datapackPath"),
                    currentConfig.datapackPath
                )
                .setDefaultValue("datapacks")
                .setSaveConsumer { newValue -> currentConfig.datapackPath = newValue }
                .setErrorSupplier { path -> 
                    val error = validatePath(path)
                    if (error.isPresent) {
                        currentPathError = error.get()
                        Optional.of(Component.literal("Invalid path"))
                    } else {
                        currentPathError = null
                        Optional.empty()
                    }
                }
                .build()

            pathEntry.setTooltipSupplier {
                if (currentPathError != null) {
                    Optional.of(arrayOf(currentPathError!!.copy().withStyle(ChatFormatting.RED)))
                } else {
                    Optional.of(arrayOf(defaultTooltip))
                }
            }

            general.addEntry(pathEntry)

            builder.setSavingRunnable {
                ModConfig.save()
            }

            builder.build()
        }
    }

    private fun validatePath(pathStr: String?): Optional<Component> {
        if (pathStr.isNullOrBlank()) {
            return Optional.of(Component.literal("Path cannot be empty"))
        }

        try {
            val path = Paths.get(expandPath(pathStr)).toAbsolutePath()
            val file = path.toFile()
            
            file.canonicalPath

            if (!file.exists()) {
                return Optional.of(Component.literal("Directory does not exist"))
            }
            if (!file.isDirectory) {
                return Optional.of(Component.literal("Path is not a directory"))
            }

        } catch (t: Throwable) {
            val rawMessage = t.message ?: t.toString()
            val friendlyMessage = when {
                t is java.io.IOError || rawMessage.contains("Unable to get working directory") -> 
                    "Error: Drive does not exist or is inaccessible"
                t is InvalidPathException || rawMessage.contains("InvalidPathException") -> 
                    "Error: Path contains invalid characters"
                rawMessage.contains("Access is denied") -> 
                    "Error: Access denied"
                else -> rawMessage.substringAfter(": ").trim()
            }
            return Optional.of(Component.literal("Error: $friendlyMessage"))
        }

        return Optional.empty()
    }

    // Potentially ensures Linux compatibility? Idk, just a shot in the dark here.
	// Might need to come back to this later. 
    private fun expandPath(pathStr: String): String {
    return if (pathStr.startsWith("~")) {
        System.getProperty("user.home") + pathStr.substring(1)
    } else {
        pathStr
    }
}
}