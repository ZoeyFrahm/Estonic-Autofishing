# Build Instructions for Estonic Autofishing Mod

This guide will help you build the Estonic Autofishing mod from source code into a usable JAR file for Minecraft 1.21.

## Prerequisites

Before building, ensure you have:

1. **Java Development Kit (JDK) 21 or higher**
   - Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [Adoptium](https://adoptium.net/)
   - Verify installation: `java -version` should show version 21 or higher

2. **Internet Connection**
   - Required to download Gradle dependencies
   - Must have access to maven.fabricmc.net (Fabric Maven repository)

3. **Git** (optional, for cloning)
   - Download from [git-scm.com](https://git-scm.com/)

## Getting the Source Code

### Option 1: Download ZIP
1. Go to https://github.com/ZoeyFrahm/Estonic-Autofishing
2. Click the green "Code" button
3. Select "Download ZIP"
4. Extract the ZIP file to a folder of your choice

### Option 2: Clone with Git
```bash
git clone https://github.com/ZoeyFrahm/Estonic-Autofishing.git
cd Estonic-Autofishing
```

## Building with Command Line

### On Linux/Mac:
```bash
./gradlew build
```

### On Windows:
```bash
gradlew.bat build
```

### First-Time Build
The first build will take several minutes as Gradle downloads:
- Gradle wrapper (if not already cached)
- Minecraft source code
- Fabric Loader
- Fabric API
- Yarn mappings
- Other dependencies

### Subsequent Builds
Subsequent builds will be much faster (typically 10-30 seconds) as dependencies are cached.

## Building with IntelliJ IDEA

### Opening the Project
1. Open IntelliJ IDEA
2. Select "Open" from the welcome screen
3. Navigate to the project folder
4. Select the folder containing `build.gradle`
5. Click "OK"
6. IntelliJ will automatically import the Gradle project

### Building the JAR
1. Open the Gradle tool window (View → Tool Windows → Gradle)
2. Navigate to: `estonic-autofishing → Tasks → build → build`
3. Double-click "build" to start the build
4. Wait for the build to complete (progress shown in the Build tool window)

### Alternative: Using Terminal in IntelliJ
1. Open Terminal in IntelliJ (View → Tool Windows → Terminal)
2. Run the appropriate Gradle command:
   - Linux/Mac: `./gradlew build`
   - Windows: `gradlew.bat build`

## Build Output

After a successful build, you'll find the JAR files in:
```
build/libs/
```

The mod JAR will be named:
```
estonic-autofishing-1.0.0.jar
```

There may also be a `-sources.jar` file, which contains the source code for reference.

## Build Artifacts

### Main JAR File
- **Name**: `estonic-autofishing-1.0.0.jar`
- **Purpose**: The mod file to place in your Minecraft mods folder
- **Size**: Approximately 20-50 KB

### Sources JAR
- **Name**: `estonic-autofishing-1.0.0-sources.jar`
- **Purpose**: Contains source code for debugging/reference
- **Usage**: Not needed for playing, only for development

## Cleaning Build Artifacts

To clean previous build artifacts:

### Linux/Mac:
```bash
./gradlew clean
```

### Windows:
```bash
gradlew.bat clean
```

This removes the `build/` directory and allows for a fresh build.

## Troubleshooting

### Error: "Could not resolve net.fabricmc:fabric-loom"

**Cause**: Cannot access maven.fabricmc.net

**Solutions**:
1. Check your internet connection
2. Verify you can access https://maven.fabricmc.net in a browser
3. If behind a firewall/proxy, configure Gradle proxy settings
4. Try using a VPN if the domain is blocked

**Gradle Proxy Configuration** (if needed):
Create/edit `gradle.properties` in the project root:
```properties
systemProp.http.proxyHost=your.proxy.host
systemProp.http.proxyPort=8080
systemProp.https.proxyHost=your.proxy.host
systemProp.https.proxyPort=8080
```

### Error: "Java version X is required"

**Cause**: Wrong Java version

**Solution**:
1. Install JDK 21 or higher
2. Set JAVA_HOME environment variable to point to JDK 21
3. Verify with: `java -version`

### Error: "Permission denied" (Linux/Mac)

**Cause**: Gradle wrapper script is not executable

**Solution**:
```bash
chmod +x gradlew
./gradlew build
```

### Build Hangs or Takes Too Long

**Solutions**:
1. Be patient on first build (can take 5-10 minutes)
2. Check your internet connection speed
3. Try building with `--no-daemon` flag:
   ```bash
   ./gradlew build --no-daemon
   ```
4. Clear Gradle cache and retry:
   ```bash
   rm -rf ~/.gradle/caches
   ./gradlew build
   ```

### Out of Memory Error

**Solution**: Increase Gradle memory in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4G
```

### Compilation Errors

**Solutions**:
1. Ensure you have the latest code from the repository
2. Clean and rebuild:
   ```bash
   ./gradlew clean build
   ```
3. Check that no files were modified incorrectly
4. Verify Java version is 21 or higher

## Installing the Mod

After building successfully:

1. Locate the JAR file in `build/libs/estonic-autofishing-1.0.0.jar`
2. Install Minecraft 1.21 with Fabric Loader
   - Download from [fabricmc.net](https://fabricmc.net/use/)
3. Download Fabric API 0.105.0+1.21 or higher
   - Download from [Modrinth](https://modrinth.com/mod/fabric-api) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
4. Copy both JARs to your `.minecraft/mods/` folder:
   - `fabric-api-x.x.x.jar`
   - `estonic-autofishing-1.0.0.jar`
5. Launch Minecraft with the Fabric profile
6. Join a world and press `F` to toggle the mod

## Development Build

If you want to test the mod in a development environment:

```bash
./gradlew runClient
```

This will:
1. Set up a test Minecraft instance
2. Launch Minecraft with the mod loaded
3. Allow for testing without building a JAR

## Additional Gradle Tasks

### List All Tasks
```bash
./gradlew tasks
```

### Generate Sources
```bash
./gradlew genSources
```

### Generate IntelliJ Run Configurations
```bash
./gradlew idea
```

### Build Without Running Tests
```bash
./gradlew build -x test
```

## Getting Help

If you encounter issues not covered here:

1. Check the [Fabric Wiki](https://fabricmc.net/wiki/)
2. Open an issue on [GitHub](https://github.com/ZoeyFrahm/Estonic-Autofishing/issues)
3. Consult the [Fabric Discord](https://discord.gg/v6v4pMv)

## Summary

**Quick Start for Most Users:**
```bash
# Download the source code
# Open terminal in the project folder
# Run the build command:

./gradlew build    # Linux/Mac
gradlew.bat build  # Windows

# Find your JAR in: build/libs/estonic-autofishing-1.0.0.jar
```

That's it! The JAR file is ready to use in Minecraft.
