package com.zoey.estonicautofishing;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class EstonicAutofishing implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("estonicautofishing");
    private static final Random RANDOM = new Random();
    
    private static boolean enabled = false;
    private static KeyBinding toggleKey;
    private static PixelDetector pixelDetector;
    
    private static boolean justCast = false;
    private static int castCooldown = 0;
    private static int leatherBootsCheckCooldown = 0;
    private static boolean processingLeatherBoots = false;
    private static int processingStep = 0;
    private static int previousSlot = -1;
    
    // For detecting "Reel it in!" message via pixels
    private static boolean reelItInDetected = false;
    private static int reelItInCooldown = 0;
    private static int pixelCheckCooldown = 0;
    
    // For scheduled recast
    private static boolean needsRecast = false;
    private static int recastTimer = 0;
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("Estonic Autofishing mod initializing...");
        
        // Initialize pixel detector
        MinecraftClient client = MinecraftClient.getInstance();
        pixelDetector = new PixelDetector(client);
        
        // Register toggle keybind (F key)
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.estonicautofishing.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            "category.estonicautofishing"
        ));
        
        // Register tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                return;
            }
            
            // Handle toggle key
            if (toggleKey.wasPressed()) {
                enabled = !enabled;
                client.player.sendMessage(
                    Text.literal("§a[Estonic Autofishing] " + (enabled ? "§aEnabled" : "§cDisabled")),
                    false
                );
            }
            
            if (!enabled) {
                return;
            }
            
            // Decrease cooldowns
            if (castCooldown > 0) {
                castCooldown--;
            }
            if (leatherBootsCheckCooldown > 0) {
                leatherBootsCheckCooldown--;
            }
            if (reelItInCooldown > 0) {
                reelItInCooldown--;
            }
            if (pixelCheckCooldown > 0) {
                pixelCheckCooldown--;
            }
            
            // Handle scheduled recast
            if (needsRecast) {
                if (recastTimer > 0) {
                    recastTimer--;
                } else {
                    needsRecast = false;
                    castFishingRod(client);
                }
            }
            
            // Handle leather boots processing
            if (processingLeatherBoots) {
                processLeatherBootsSequence(client);
                return;
            }
            
            // Check for leather boots every 60 ticks (3 seconds)
            if (leatherBootsCheckCooldown == 0) {
                leatherBootsCheckCooldown = 60;
                int leatherBootsSlot = findLeatherBootsInHotbar(client.player.getInventory());
                if (leatherBootsSlot != -1) {
                    startLeatherBootsProcess(client, leatherBootsSlot);
                    return;
                }
            }
            
            // Auto fishing logic
            if (castCooldown == 0) {
                checkAndReelFish(client);
            }
        });
        
        LOGGER.info("Estonic Autofishing mod initialized!");
    }
    
    // Called by mixin when overlay message is displayed
    public static void onOverlayMessage(String message) {
        if (message != null && message.toLowerCase().contains("reel it in")) {
            reelItInDetected = true;
            reelItInCooldown = 20; // Valid for 1 second
            LOGGER.info("Detected 'Reel it in!' message");
        }
    }
    
    private void checkAndReelFish(MinecraftClient client) {
        // Check if player has fishing rod in hand
        ItemStack mainHandItem = client.player.getMainHandStack();
        if (mainHandItem.getItem() != Items.FISHING_ROD) {
            return;
        }
        
        // Check if there's a fishing bobber
        if (client.player.fishHook != null) {
            // Method 1: Pixel-based detection for "Reel it in!" indicator
            if (pixelCheckCooldown == 0 && !justCast) {
                pixelCheckCooldown = 2; // Check every 2 ticks for performance
                if (pixelDetector.detectFishingIndicator()) {
                    LOGGER.info("Detected fish bite via pixel detection");
                    reelAndRecast(client);
                    return;
                }
            }
            
            // Method 2: Check for text-based "Reel it in!" message (from mixin)
            if (reelItInDetected && reelItInCooldown > 0) {
                reelItInDetected = false;
                LOGGER.info("Detected fish bite via text message");
                reelAndRecast(client);
                return;
            }
            
            // Method 3: Check bobber velocity (fallback detection)
            if (!justCast) {
                double velocity = client.player.fishHook.getVelocity().lengthSquared();
                // When a fish bites, the bobber moves with significant velocity
                if (velocity > 0.15) {
                    LOGGER.info("Detected fish bite via bobber velocity");
                    reelAndRecast(client);
                }
            }
        } else if (!justCast) {
            // No bobber means we need to cast
            castFishingRod(client);
        }
        
        if (justCast) {
            justCast = false;
        }
    }
    
    private void reelAndRecast(MinecraftClient client) {
        LOGGER.info("Reeling in fish and recasting...");
        
        // Move crosshair slightly to avoid AFK detection
        randomCrosshairMovement(client);
        
        // Right-click to reel in
        rightClick(client);
        
        // Wait a bit before recasting
        castCooldown = 10; // 0.5 seconds
        justCast = true;
        
        // Schedule recast using tick-based timer
        needsRecast = true;
        recastTimer = 10; // 10 ticks = 0.5 seconds
    }
    
    private void castFishingRod(MinecraftClient client) {
        // Right-click to cast
        rightClick(client);
        justCast = true;
        castCooldown = 20; // 1 second cooldown
        LOGGER.info("Cast fishing rod");
    }
    
    private void randomCrosshairMovement(MinecraftClient client) {
        // Small random mouse movement
        float deltaYaw = (RANDOM.nextFloat() - 0.5f) * 2.0f; // -1 to 1 degree
        float deltaPitch = (RANDOM.nextFloat() - 0.5f) * 2.0f; // -1 to 1 degree
        
        if (client.player != null) {
            client.player.setYaw(client.player.getYaw() + deltaYaw);
            client.player.setPitch(client.player.getPitch() + deltaPitch);
        }
    }
    
    private int findLeatherBootsInHotbar(PlayerInventory inventory) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.LEATHER_BOOTS) {
                return i;
            }
        }
        return -1;
    }
    
    private void startLeatherBootsProcess(MinecraftClient client, int slot) {
        LOGGER.info("Leather boots detected in slot " + slot);
        processingLeatherBoots = true;
        processingStep = 0;
        previousSlot = client.player.getInventory().selectedSlot;
        // Switch to leather boots slot
        client.player.getInventory().selectedSlot = slot;
    }
    
    private void processLeatherBootsSequence(MinecraftClient client) {
        processingStep++;
        
        if (processingStep == 5) {
            // Right-click the leather boots
            rightClick(client);
            LOGGER.info("Right-clicked leather boots");
        } else if (processingStep == 15) {
            // Switch back to previous slot
            if (previousSlot != -1) {
                client.player.getInventory().selectedSlot = previousSlot;
            }
            // Reset state
            processingLeatherBoots = false;
            processingStep = 0;
            previousSlot = -1;
            castCooldown = 20; // Give some time before fishing again
            LOGGER.info("Returned to fishing");
        }
    }
    
    private void rightClick(MinecraftClient client) {
        // Simulate right-click with main hand explicitly
        if (client.interactionManager != null && client.player != null) {
            client.interactionManager.interactItem(
                client.player,
                Hand.MAIN_HAND
            );
        }
    }
}
