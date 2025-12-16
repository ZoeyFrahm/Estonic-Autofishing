# Estonic Autofishing

A Minecraft Fabric mod for automatic fishing with leather boots detection support.

## Download

To download this project:
1. Click the green "Code" button on GitHub
2. Select "Download ZIP" to get the entire project
3. Extract the ZIP file to your desired location
4. Follow the building instructions below

Alternatively, clone the repository:
```bash
git clone https://github.com/ZoeyFrahm/Estonic-Autofishing.git
```

## Features

1. **Auto Fishing with Pixel-Based Detection:**
   - Automatically detects when a fish bites using pixel-coordinate-based color detection
   - Monitors fixed pixel coordinates above the hotbar for the green exclamation mark and yellow "Reel it in!" text
   - Samples screen pixels looking for specific RGB color values:
     - Green exclamation mark: RGB ~(85, 255, 85)
     - Yellow text: RGB ~(255, 255, 85)
   - Triple detection system for reliability:
     - Primary: Pixel-based color detection at action bar coordinates
     - Secondary: Overlay message detection (fallback)
     - Tertiary: Bobber velocity detection (fallback)
   - Reels in the fishing rod and casts it back out automatically
   - Adds slight random crosshair movement to avoid AFK detection

2. **Leather Boots Detection:**
   - Automatically detects Leather Boots in the hotbar
   - Switches to the boots and performs a right-click action
   - Returns to fishing automatically after use

3. **Toggle:**
   - Press `F8` to enable/disable the mod
   - Visual feedback in chat when toggled

## Requirements

- Minecraft 1.21
- Fabric Loader 0.16.5 or higher
- Fabric API 0.105.0 or higher
- Java 21

## Building

### Prerequisites
- Java 21 or higher
- Internet connection to download dependencies

### Build Instructions

To build the mod JAR file:

1. Open the project in IntelliJ IDEA
2. Open Terminal in IntelliJ and run:
   - On Linux/Mac: `./gradlew build`
   - On Windows: `gradlew.bat build`
3. The built JAR file will be in `build/libs/`

**Note:** If you encounter DNS resolution errors with `maven.fabricmc.net`, this is due to network restrictions. To resolve this:
- Build the project from a network with unrestricted access to Fabric Maven repositories
- Or use a VPN/proxy that allows access to maven.fabricmc.net
- Or configure your hosts file to resolve maven.fabricmc.net properly

The mod is fully implemented and ready to build once the network issue is resolved.

## Installation

1. Download and install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) version 0.105.0+1.21 or higher
3. Place both the Fabric API JAR and this mod's JAR file in your `.minecraft/mods` folder
4. Launch Minecraft with the Fabric profile

## Usage

1. Start Minecraft and join a world
2. Press `F8` to enable the mod
3. Hold a fishing rod and the mod will automatically fish
4. If Leather Boots appear in your hotbar, the mod will automatically use them
5. Press `F8` again to disable the mod

## License

MIT License - See LICENSE file for details