package com.ashlynnmods.simpleresourcifydatapacks

import com.ashlynnmods.simpleresourcifydatapacks.config.ModConfig
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Paths

object SimpleResourcifyDatapacks : ModInitializer {
    private val logger = LoggerFactory.getLogger("simple-resourcify-datapacks")

    override fun onInitialize() {
        val config = ModConfig.get()

        try {
            val pathStr = config.datapackPath
            if (!pathStr.isNullOrBlank()) {
                val path = Paths.get(pathStr).toAbsolutePath()
                
                if (!Files.exists(path)) {
                    Files.createDirectories(path)
                    logger.info("Created datapack directory: $path")
                }
            }
        } catch (e: Exception) {
            // This should literally never happen
            throw RuntimeException(
                "Simple Resourcify Datapacks: Something has gone terribly wrong...", 
                e
            )
        }

        logger.info("Simple Resourcify Datapacks initialized. Target: ${config.datapackPath}")
    }
}