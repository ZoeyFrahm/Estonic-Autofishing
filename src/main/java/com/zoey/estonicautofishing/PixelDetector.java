package com.zoey.estonicautofishing;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Pixel-based detection for fishing indicators.
 * Monitors specific screen coordinates for color matching.
 */
public class PixelDetector {
    private static final Logger LOGGER = LoggerFactory.getLogger("estonicautofishing");
    
    // Color thresholds for detection
    private static final int GREEN_EXCLAMATION_R_MIN = 60;
    private static final int GREEN_EXCLAMATION_R_MAX = 120;
    private static final int GREEN_EXCLAMATION_G_MIN = 220;
    private static final int GREEN_EXCLAMATION_G_MAX = 255;
    private static final int GREEN_EXCLAMATION_B_MIN = 60;
    private static final int GREEN_EXCLAMATION_B_MAX = 120;
    
    private static final int YELLOW_TEXT_R_MIN = 220;
    private static final int YELLOW_TEXT_R_MAX = 255;
    private static final int YELLOW_TEXT_G_MIN = 220;
    private static final int YELLOW_TEXT_G_MAX = 255;
    private static final int YELLOW_TEXT_B_MIN = 50;
    private static final int YELLOW_TEXT_B_MAX = 120;
    
    // Sampling configuration
    private static final int SAMPLES_PER_CHECK = 5;
    private static final int REQUIRED_MATCHES = 2;
    
    private final MinecraftClient client;
    private ByteBuffer pixelBuffer;
    
    public PixelDetector(MinecraftClient client) {
        this.client = client;
    }
    
    /**
     * Check if the "Reel it in!" indicator is present on screen.
     * Samples pixels in the action bar area above the hotbar.
     * 
     * @return true if fishing indicator is detected
     */
    public boolean detectFishingIndicator() {
        if (client.getFramebuffer() == null || client.getWindow() == null) {
            return false;
        }
        
        try {
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();
            
            // Calculate detection area (above hotbar, centered)
            // Action bar is typically at Y = screenHeight - 68 (scaled)
            int centerX = screenWidth / 2;
            int actionBarY = screenHeight - 68;
            
            // Sample area around where text appears
            // Check for both green (exclamation) and yellow (text) colors
            int greenMatches = 0;
            int yellowMatches = 0;
            
            // Sample pixels in a horizontal line around center
            for (int i = 0; i < SAMPLES_PER_CHECK; i++) {
                int offsetX = (i - SAMPLES_PER_CHECK / 2) * 20; // Spread samples
                int sampleX = centerX + offsetX;
                
                if (sampleX >= 0 && sampleX < screenWidth) {
                    Color color = getPixelColor(sampleX, actionBarY);
                    
                    if (color != null) {
                        if (isGreenExclamation(color)) {
                            greenMatches++;
                        }
                        if (isYellowText(color)) {
                            yellowMatches++;
                        }
                    }
                }
            }
            
            // Require detecting either green exclamation or yellow text
            boolean detected = (greenMatches >= REQUIRED_MATCHES) || (yellowMatches >= REQUIRED_MATCHES);
            
            if (detected) {
                LOGGER.info("Fishing indicator detected! Green matches: {}, Yellow matches: {}", 
                    greenMatches, yellowMatches);
            }
            
            return detected;
            
        } catch (Exception e) {
            LOGGER.debug("Error detecting fishing indicator: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get the color of a pixel at the specified screen coordinates.
     * 
     * @param x Screen X coordinate (scaled)
     * @param y Screen Y coordinate (scaled)
     * @return Color object or null if unable to read
     */
    private Color getPixelColor(int x, int y) {
        try {
            Framebuffer framebuffer = client.getFramebuffer();
            if (framebuffer == null) {
                return null;
            }
            
            int fbWidth = framebuffer.textureWidth;
            int fbHeight = framebuffer.textureHeight;
            
            // Convert scaled coordinates to framebuffer coordinates
            int scaledWidth = client.getWindow().getScaledWidth();
            int scaledHeight = client.getWindow().getScaledHeight();
            
            int fbX = (int) ((float) x / scaledWidth * fbWidth);
            int fbY = (int) ((float) y / scaledHeight * fbHeight);
            
            // Flip Y coordinate (OpenGL uses bottom-left origin)
            fbY = fbHeight - fbY - 1;
            
            if (fbX < 0 || fbX >= fbWidth || fbY < 0 || fbY >= fbHeight) {
                return null;
            }
            
            // Ensure buffer is allocated (only needs 4 bytes for one RGBA pixel)
            if (pixelBuffer == null) {
                pixelBuffer = BufferUtils.createByteBuffer(4);
            }
            
            // Bind framebuffer and read pixel
            framebuffer.beginRead();
            pixelBuffer.clear();
            
            // glReadPixels to get pixel data
            org.lwjgl.opengl.GL11.glReadPixels(fbX, fbY, 1, 1, 
                org.lwjgl.opengl.GL11.GL_RGBA, 
                org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE, 
                pixelBuffer);
            
            framebuffer.endRead();
            
            // Extract RGBA values (0-255)
            int r = pixelBuffer.get(0) & 0xFF;
            int g = pixelBuffer.get(1) & 0xFF;
            int b = pixelBuffer.get(2) & 0xFF;
            int a = pixelBuffer.get(3) & 0xFF;
            
            return new Color(r, g, b, a);
            
        } catch (Exception e) {
            // Silently fail - will retry next check
            return null;
        }
    }
    
    /**
     * Check if a color matches the green exclamation mark.
     */
    private boolean isGreenExclamation(Color color) {
        return color.r >= GREEN_EXCLAMATION_R_MIN && color.r <= GREEN_EXCLAMATION_R_MAX &&
               color.g >= GREEN_EXCLAMATION_G_MIN && color.g <= GREEN_EXCLAMATION_G_MAX &&
               color.b >= GREEN_EXCLAMATION_B_MIN && color.b <= GREEN_EXCLAMATION_B_MAX &&
               color.a > 200; // Must be fairly opaque
    }
    
    /**
     * Check if a color matches the yellow "Reel it in!" text.
     */
    private boolean isYellowText(Color color) {
        return color.r >= YELLOW_TEXT_R_MIN && color.r <= YELLOW_TEXT_R_MAX &&
               color.g >= YELLOW_TEXT_G_MIN && color.g <= YELLOW_TEXT_G_MAX &&
               color.b >= YELLOW_TEXT_B_MIN && color.b <= YELLOW_TEXT_B_MAX &&
               color.a > 200; // Must be fairly opaque
    }
    
    /**
     * Simple color holder class.
     */
    private static class Color {
        final int r, g, b, a;
        
        Color(int r, int g, int b, int a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }
        
        @Override
        public String toString() {
            return String.format("Color(r=%d, g=%d, b=%d, a=%d)", r, g, b, a);
        }
    }
}
