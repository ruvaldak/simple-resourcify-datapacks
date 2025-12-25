package com.ashlynnmods.simpleresourcifydatapacks.mixin;

import com.ashlynnmods.simpleresourcifydatapacks.config.ModConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.lang.reflect.Constructor;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(PackRepository.class)
public class PackRepositoryMixin {

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static RepositorySource[] addExternalSource(RepositorySource[] sources) {
        boolean isDatapackRepo = false;
        for (RepositorySource source : sources) {
            if (source instanceof ServerPacksSource) {
                isDatapackRepo = true;
                break;
            }
        }

        if (!isDatapackRepo) {
            return sources;
        }

        ModConfig config = ModConfig.get();

        if (!config.registerDatapackSource) {
            return sources;
        }

        Path expandedPath;
        Path targetPath;

        try {
            String rawPath = config.datapackPath;
            if (rawPath == null || rawPath.isBlank()) {
                throw new IllegalArgumentException("Path is empty");
            }

            expandedPath = Paths.get(expandPath(rawPath));
            targetPath = expandedPath.toAbsolutePath().normalize();
            
            // Check existence
            if (!Files.exists(targetPath)) {
                boolean isDefault = expandedPath.normalize().equals("datapacks") || expandedPath.normalize().equals("./datapacks") || expandedPath.normalize().equals(".\\datapacks");

                if (isDefault) {
                    // Creating Default folder is expected and not an error.
                    Files.createDirectories(targetPath);
                } else {
                    // Encountered a custom path that doesn't exist. This is a user error.
                    // Throw exception to trigger the revert toast.
                    throw new IllegalArgumentException("Directory does not exist: " + targetPath);
                }
            } else if (!Files.isDirectory(targetPath)) {
                // It exists but is a file, not a folder. Always invalid.
                throw new IllegalArgumentException("Target is not a directory");
            }

        } catch (Throwable t) {
            System.err.println("[Simple Resourcify Datapacks] Invalid config path detected: " + t.getMessage());
            
            // Revert config
            config.datapackPath = "datapacks";
            ModConfig.save();
            ModConfig.pendingRevertToast = true;

            // Fallback to default
            try {
                targetPath = Paths.get("datapacks").toAbsolutePath();
                if (!Files.exists(targetPath)) {
                    Files.createDirectories(targetPath);
                }
            } catch (Exception ignored) {
                return sources;
            }
        }

        RepositorySource mySource = createSource(targetPath);

        if (mySource != null) {
            List<RepositorySource> list = new ArrayList<>(Arrays.asList(sources));
            list.add(mySource);
            return list.toArray(new RepositorySource[0]);
        }
        return sources;
    }

    private static RepositorySource createSource(Path path) {
        try {
            Constructor<?> sourceCtor = null;
            for (Constructor<?> ctor : FolderRepositorySource.class.getDeclaredConstructors()) {
                if (ctor.getParameterCount() == 4) {
                    sourceCtor = ctor;
                    break;
                }
            }

            if (sourceCtor == null) return null;

            Class<?> validatorClass = sourceCtor.getParameterTypes()[3]; 
            Constructor<?> validatorCtor = validatorClass.getDeclaredConstructor(PathMatcher.class);
            validatorCtor.setAccessible(true);

            PackSource customSource = PackSource.create(
                (name) -> Component.translatable("pack.nameAndSource", name, Component.literal("external")), 
                false
            );

            PathMatcher allowAll = FileSystems.getDefault().getPathMatcher("regex:.*");
            Object validatorInstance = validatorCtor.newInstance(allowAll);

            sourceCtor.setAccessible(true);
            return (RepositorySource) sourceCtor.newInstance(path, PackType.SERVER_DATA, customSource, validatorInstance);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Potentially ensures Linux compatibility? Idk, just a shot in the dark here.
	// Might need to come back to this later. 
	private static String expandPath(String pathStr) {
		if (pathStr != null && pathStr.startsWith("~")) {
			return System.getProperty("user.home") + pathStr.substring(1);
		}
		return pathStr;
	}
}