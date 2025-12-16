# Estonic Autofishing - Technical Documentation

## Project Overview

This is a Minecraft 1.21 Fabric mod that provides automated fishing functionality with leather boots detection and anti-AFK measures.

## Features

### 1. Auto Fishing
The mod automatically detects when a fish bites and reels it in, then recasts the fishing rod.

**Detection Methods (in priority order):**
1. **Primary - Pixel-Based Detection**: Monitors specific screen coordinates above the hotbar (action bar area)
   - Samples pixels in a horizontal line around screen center at Y = screenHeight - 68
   - Detects green exclamation mark color: RGB(60-120, 220-255, 60-120)
   - Detects yellow "Reel it in!" text color: RGB(220-255, 220-255, 50-120)
   - Takes 5 samples spread across the area, requires 2+ matches for detection
   - Checks every 2 ticks for optimal performance
2. **Secondary - Overlay Message Detection**: Mixin-based detection of "Reel it in!" text (fallback)
3. **Tertiary - Bobber Velocity Detection**: Monitors bobber movement velocity > 0.15 (fallback)

**Anti-AFK Measures:**
- Random crosshair movement (±1 degree in yaw and pitch) when reeling in
- Ensures the player appears active to anti-cheat systems

### 2. Leather Boots Detection
The mod automatically detects Leather Boots in the player's hotbar and uses them.

**Behavior:**
1. Scans hotbar every 3 seconds (60 ticks)
2. When detected, switches to the boots slot
3. Performs a right-click action after 5 ticks
4. Returns to the previous slot after 15 ticks
5. Resumes fishing automatically

### 3. Toggle Mechanism
Press the `F` key to enable/disable the mod.

**Feedback:**
- Green message when enabled: "[Estonic Autofishing] Enabled"
- Red message when disabled: "[Estonic Autofishing] Disabled"

## Project Structure

```
Estonic-Autofishing/
├── build.gradle                    # Gradle build configuration
├── settings.gradle                 # Gradle settings
├── gradle.properties               # Project properties
├── gradlew / gradlew.bat          # Gradle wrapper scripts
├── gradle/wrapper/                 # Gradle wrapper files
├── LICENSE                         # MIT License
├── README.md                       # User documentation
├── TECHNICAL.md                    # This file
└── src/main/
    ├── java/com/zoey/estonicautofishing/
    │   ├── EstonicAutofishing.java           # Main mod class
    │   ├── PixelDetector.java                # Pixel-based color detection
    │   └── mixin/
    │       └── InGameHudMixin.java           # Mixin for detecting overlay messages
    └── resources/
        ├── fabric.mod.json                    # Mod metadata
        ├── estonicautofishing.mixins.json    # Mixin configuration
        └── assets/estonicautofishing/
            ├── icon.png                       # Mod icon
            └── lang/
                └── en_us.json                 # English translations
```

## Technical Implementation

### Main Mod Class (EstonicAutofishing.java)

**Key Components:**
- `ClientModInitializer`: Fabric API entry point for client-side mods
- `ClientTickEvents`: Handles per-tick game logic
- `KeyBindingHelper`: Registers the toggle keybind
- `PixelDetector`: Handles pixel-based color detection at specific screen coordinates

**State Variables:**
- `enabled`: Whether the mod is active
- `pixelDetector`: Instance of PixelDetector for screen color sampling
- `castCooldown`: Prevents immediate recasting
- `pixelCheckCooldown`: Throttles pixel detection checks (every 2 ticks)
- `leatherBootsCheckCooldown`: Throttles boots detection
- `processingLeatherBoots`: Flag for boots handling sequence
- `reelItInDetected`: Flag set by mixin when message appears (fallback)

**Core Methods:**
- `onOverlayMessage()`: Called by mixin when overlay text appears (fallback detection)
- `checkAndReelFish()`: Main fishing logic with pixel detection
- `reelAndRecast()`: Reels in and schedules recast
- `randomCrosshairMovement()`: Anti-AFK movement
- `findLeatherBootsInHotbar()`: Scans for leather boots
- `processLeatherBootsSequence()`: State machine for boots handling

### Pixel Detector (PixelDetector.java)

**Purpose**: Monitors specific screen coordinates to detect fishing indicators via color matching

**Key Features:**
- Reads pixel colors directly from the framebuffer using OpenGL
- Converts scaled screen coordinates to framebuffer coordinates
- Samples multiple pixels in a horizontal line for reliability
- Uses color range matching to handle anti-aliasing and shadows

**Detection Strategy:**
1. Calculate action bar position (Y = screenHeight - 68)
2. Sample 5 pixels in a horizontal line around screen center
3. Check each pixel against green exclamation mark color ranges
4. Check each pixel against yellow text color ranges
5. Require 2+ matches to confirm detection

**Color Thresholds:**
- Green exclamation: R(60-120), G(220-255), B(60-120), A(>200)
- Yellow text: R(220-255), G(220-255), B(50-120), A(>200)

**Performance:**
- Only runs on render thread for safe framebuffer access
- Checks every 2 ticks (10 times per second)
- Minimal performance impact

### Mixin (InGameHudMixin.java)

**Purpose**: Intercepts overlay messages to detect "Reel it in!"

**Target**: `net.minecraft.client.gui.hud.InGameHud`

**Injection Point**: `setOverlayMessage` method

**How it works:**
1. Intercepts all overlay messages (action bar text)
2. Checks if message contains "reel it in" (case-insensitive)
3. Calls `EstonicAutofishing.onOverlayMessage()` to notify main class
4. Sets a cooldown flag to prevent duplicate detections

## Dependencies

### Required
- Minecraft 1.21
- Fabric Loader 0.16.5+
- Fabric API 0.105.0+1.21
- Java 21

### Build Dependencies
- Gradle 8.8
- Fabric Loom 1.7.4
- Yarn Mappings 1.21+build.9

## Building the Mod

### Prerequisites
1. Java 21 JDK installed
2. Internet connection to download dependencies
3. Access to maven.fabricmc.net (no firewall/DNS blocking)

### Build Steps
```bash
# Linux/Mac
./gradlew build

# Windows
gradlew.bat build
```

### Output
The compiled JAR file will be located at:
```
build/libs/estonic-autofishing-1.0.0.jar
```

## Installation

1. Install Fabric Loader for Minecraft 1.21
2. Download Fabric API from CurseForge/Modrinth
3. Place both Fabric API and this mod's JAR in `.minecraft/mods/`
4. Launch Minecraft with the Fabric profile

## Usage in Game

1. Join a world or server
2. Hold a fishing rod in your main hand
3. Press `K` to enable the mod
4. Stand near water and the mod will automatically fish
5. If Leather Boots appear in your hotbar, they'll be used automatically
6. Press `K` again to disable

## Configuration

Currently, the mod has no configuration file. All settings are hardcoded:
- Toggle key: `F`
- Leather boots check interval: 3 seconds
- Crosshair movement range: ±1 degree
- Velocity threshold for bite detection: 0.15
- Pixel check interval: 2 ticks (0.1 seconds)
- Pixel sample count: 5 horizontal samples
- Required color matches: 2 out of 5 samples

To change these, edit `EstonicAutofishing.java` or `PixelDetector.java` and rebuild.

## Compatibility

**Compatible with:**
- Vanilla Minecraft 1.21
- Most Fabric mods
- Server-side anti-cheat (due to anti-AFK measures)

**May conflict with:**
- Other auto-fishing mods
- Mods that modify the HUD overlay system
- Mods that override player interaction handling

## Known Limitations

1. **Server Detection**: Some servers may still detect this as automation
2. **Lure Enchantment**: Works with lure but may need timing adjustments
3. **Moving Water**: Works best in still water
4. **Leather Boots**: Only detects in hotbar (slots 0-8), not full inventory
5. **Pixel Detection**: May need coordinate adjustment for different GUI scales or screen resolutions
6. **Render Thread**: Pixel detection only works when on render thread (not an issue in normal gameplay)

## Future Enhancements (Not Implemented)

- Configuration file for customizable settings
- Support for other items beyond leather boots
- Configurable pixel detection coordinates and color ranges
- GUI for adjusting detection settings in-game
- Multiple GUI scale support with auto-adjustment
- Configurable toggle key
- Sound-based detection as additional fallback

## Troubleshooting

### Mod doesn't load
- Check that Fabric API is installed
- Verify Java 21 is being used
- Check logs for errors

### Fishing doesn't work
- Ensure you're holding a fishing rod
- Press `F` to verify mod is enabled
- Check that you're near water
- If pixel detection isn't working, try different GUI scale settings

### Leather boots not detected
- Ensure boots are in hotbar (not full inventory)
- Check that they're exactly "Leather Boots" item

### Build fails
- Verify internet connection
- Check that maven.fabricmc.net is accessible
- Try clearing Gradle cache: `./gradlew clean`

## License

MIT License - See LICENSE file for full text

## Credits

- Developed by ZoeyFrahm
- Uses Fabric API and Fabric Loader
- Built with Gradle and Fabric Loom
