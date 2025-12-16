# Estonic Autofishing

A Minecraft Fabric mod for automatic fishing with leather boots detection support.

## Features

1. **Auto Fishing:**
   - Automatically detects when a fish bites
   - Reels in the fishing rod and casts it back out
   - Adds slight random crosshair movement to avoid AFK detection

2. **Leather Boots Detection:**
   - Automatically detects Leather Boots in the hotbar
   - Switches to the boots and performs a right-click action
   - Returns to fishing automatically after use

3. **Toggle:**
   - Press `K` to enable/disable the mod
   - Visual feedback in chat when toggled

## Requirements

- Minecraft 1.21
- Fabric Loader 0.16.5 or higher
- Fabric API 0.105.0 or higher
- Java 21

## Building

To build the mod JAR file:

1. Open the project in IntelliJ IDEA
2. Run `./gradlew build` (Linux/Mac) or `gradlew.bat build` (Windows)
3. The built JAR file will be in `build/libs/`

## Installation

1. Download and install Fabric Loader for Minecraft 1.21
2. Place the mod JAR file in your `.minecraft/mods` folder
3. Launch Minecraft with the Fabric profile

## Usage

1. Start Minecraft and join a world
2. Press `K` to enable the mod
3. Hold a fishing rod and the mod will automatically fish
4. If Leather Boots appear in your hotbar, the mod will automatically use them
5. Press `K` again to disable the mod

## License

MIT License - See LICENSE file for details