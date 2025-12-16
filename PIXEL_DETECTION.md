# Pixel-Based Detection Implementation

## Overview

This document describes the pixel-based detection system implemented in the Estonic Autofishing mod. The system monitors specific screen coordinates to detect the "Reel it in!" fishing indicator by analyzing pixel colors.

## Detection Strategy

### Primary Detection Method: Pixel Color Sampling

The mod now uses **pixel-coordinate-based color detection** as its primary method for detecting when a fish bites. This approach directly reads pixel colors from the screen framebuffer and checks for the characteristic colors of the fishing indicator.

### Detection Area

- **Location**: Action bar area above the hotbar
- **Y Coordinate**: `screenHeight - 68` (scaled coordinates)
- **X Coordinate**: Centered horizontally at `screenWidth / 2`
- **Sampling Pattern**: 5 horizontal samples spread across ±40 pixels from center

### Color Thresholds

#### Green Exclamation Mark
The green "!" that appears in the action bar:
- **Red**: 60-120 (darker than pure green)
- **Green**: 220-255 (bright green)
- **Blue**: 60-120 (darker than pure green)
- **Alpha**: >200 (must be fairly opaque)

#### Yellow Text ("Reel it in!")
The yellow text of the message:
- **Red**: 220-255 (bright yellow)
- **Green**: 220-255 (bright yellow)
- **Blue**: 50-120 (less blue for yellow)
- **Alpha**: >200 (must be fairly opaque)

### Detection Algorithm

1. **Calculate Screen Position**
   - Get current scaled screen dimensions
   - Calculate action bar Y position (screenHeight - 68)
   - Calculate center X position (screenWidth / 2)

2. **Sample Multiple Pixels**
   - Take 5 samples in a horizontal line
   - Each sample is 20 pixels apart
   - Spread: -40, -20, 0, +20, +40 from center

3. **Color Matching**
   - Check each sample against green exclamation color ranges
   - Check each sample against yellow text color ranges
   - Count matches for each color type

4. **Confirmation**
   - Require 2+ matches out of 5 samples
   - Can match either green OR yellow (both indicate the same message)
   - Reduces false positives from random colored pixels

### Performance Optimization

- **Check Frequency**: Every 2 ticks (0.1 seconds, 10 checks per second)
- **Render Thread Only**: Pixel reading only occurs on render thread
- **Early Exit**: Returns immediately if detection succeeds
- **Minimal Impact**: Very low CPU usage due to sparse sampling

## Implementation Details

### PixelDetector Class

Located at: `src/main/java/com/zoey/estonicautofishing/PixelDetector.java`

**Key Methods:**

1. **`detectFishingIndicator()`**
   - Main detection method called from tick loop
   - Returns true if fishing indicator detected
   - Handles all coordinate calculations and sampling

2. **`getPixelColor(x, y)`**
   - Reads a single pixel from the framebuffer
   - Converts scaled coordinates to framebuffer coordinates
   - Uses OpenGL glReadPixels for direct pixel access
   - Returns Color object with RGBA values

3. **`isGreenExclamation(Color)`**
   - Checks if a color matches the green exclamation mark
   - Uses configurable min/max thresholds for each channel

4. **`isYellowText(Color)`**
   - Checks if a color matches the yellow text
   - Uses configurable min/max thresholds for each channel

### Integration with Main Mod

The PixelDetector is integrated into the main EstonicAutofishing class:

```java
// Initialization
pixelDetector = new PixelDetector(client);

// Detection in tick loop
if (pixelCheckCooldown == 0 && !justCast) {
    pixelCheckCooldown = 2; // Check every 2 ticks
    if (pixelDetector.detectFishingIndicator()) {
        LOGGER.info("Detected fish bite via pixel detection");
        reelAndRecast(client);
        return;
    }
}
```

## Multi-Layered Detection System

The mod uses a **three-tier detection system** for maximum reliability:

### Tier 1: Pixel-Based Detection (Primary)
- Most responsive and reliable
- Directly reads screen pixels
- Checks every 2 ticks when bobber is in water
- No dependency on game internals (text messages)

### Tier 2: Text-Based Detection (Secondary)
- Monitors overlay messages via mixin
- Catches "Reel it in!" text through game's HUD system
- Fallback for cases where pixel detection might miss
- Lower priority than pixel detection

### Tier 3: Velocity Detection (Tertiary)
- Monitors fishing bobber velocity
- Triggers when velocity exceeds 0.15
- Last-resort fallback
- Can occasionally have false positives

## Coordinate System

### Screen Coordinates (Scaled)
- Origin: Top-left corner (0, 0)
- X increases to the right
- Y increases downward
- Values range from 0 to scaled width/height

### Framebuffer Coordinates
- Origin: Bottom-left corner (0, 0) - OpenGL convention
- X increases to the right
- Y increases upward
- Values range from 0 to texture width/height
- Y coordinate must be flipped: `fbY = fbHeight - scaledY - 1`

### Conversion Formula
```java
int fbX = (int) ((float) scaledX / scaledWidth * fbWidth);
int fbY = (int) ((float) scaledY / scaledHeight * fbHeight);
fbY = fbHeight - fbY - 1; // Flip Y coordinate
```

## Configuration

All detection parameters are defined as constants in `PixelDetector.java`:

```java
// Color thresholds
GREEN_EXCLAMATION_R_MIN = 60;
GREEN_EXCLAMATION_R_MAX = 120;
// ... etc

// Sampling configuration
SAMPLES_PER_CHECK = 5;        // Number of pixels to sample
REQUIRED_MATCHES = 2;         // Minimum matches needed
```

To adjust for different resolutions, GUI scales, or texture packs:
1. Modify the color threshold constants
2. Adjust the action bar Y offset (currently 68)
3. Change the sample spacing (currently 20 pixels)
4. Rebuild the mod

## Advantages Over Text Detection

1. **Faster Response**: No need to wait for text rendering
2. **Direct Detection**: Reads actual visual output
3. **Resolution Independent**: Works at any resolution
4. **Texture Pack Compatible**: Detects colors regardless of font
5. **No Mixin Dependency**: Pixel method doesn't rely on game internals
6. **Visual Confirmation**: Detects what the player actually sees

## Limitations

1. **GUI Scale Dependency**: Coordinates may need adjustment for different GUI scales
2. **Render Thread Requirement**: Can only read pixels on render thread (not an issue in practice)
3. **Texture Pack Colors**: May need color adjustment if texture pack changes text colors
4. **Anti-Aliasing**: Color ranges account for anti-aliasing but extreme changes might need adjustment

## Testing Recommendations

When testing the pixel detection:

1. **Enable Debug Logging**: Check console for "Detected fish bite via pixel detection" messages
2. **Verify Detection Priority**: Pixel detection should trigger before text detection
3. **Test Different GUI Scales**: Verify detection works at 1x, 2x, 3x GUI scales
4. **Test Different Resolutions**: Ensure coordinate calculations work at various resolutions
5. **Monitor Performance**: Check that tick rate remains stable (should be negligible impact)

## Future Enhancements

Possible improvements for the pixel detection system:

1. **Auto-Calibration**: Detect action bar position automatically
2. **Color Learning**: Let users sample correct colors in-game
3. **Multi-Scale Support**: Auto-adjust coordinates based on GUI scale
4. **Performance Profiling**: Add detailed timing metrics
5. **Configurable Thresholds**: GUI or config file for color adjustments
6. **Particle Detection**: Combine with particle effect detection for even more reliability

## Troubleshooting

### Detection Not Working
- Check console logs for pixel sampling errors
- Verify GUI scale is standard (Auto or 2x recommended)
- Ensure texture pack doesn't drastically change text colors
- Try adjusting color thresholds in PixelDetector.java

### False Positives
- Increase REQUIRED_MATCHES constant
- Narrow color threshold ranges
- Increase sample count for better accuracy

### Performance Issues
- Increase pixelCheckCooldown value (currently 2 ticks)
- Reduce SAMPLES_PER_CHECK constant
- Check for other mods conflicting with rendering

## Conclusion

The pixel-based detection system provides a robust, efficient, and reliable method for detecting fishing indicators in Minecraft. By directly analyzing screen pixels, the mod achieves faster response times and greater compatibility compared to text-based approaches alone. The multi-layered detection strategy ensures maximum reliability across different game configurations and edge cases.
