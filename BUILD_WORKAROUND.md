# Build Workaround for Network Restrictions

This document provides solutions for building the Estonic Autofishing mod when facing network restrictions that block access to maven.fabricmc.net.

## Problem

Some networks (corporate, educational, or certain ISPs) block access to:
- maven.fabricmc.net (Fabric's Maven repository)
- Cloudflare IPs hosting Fabric's Maven

This prevents Gradle from downloading required dependencies like Fabric Loom.

## Solutions

### Solution 1: Use a Different Network (Recommended)

Build the project from a network with unrestricted internet access:

1. **Home Network**: Most home networks don't block Fabric Maven
2. **Mobile Hotspot**: Use your phone's mobile data
3. **VPN**: Use a VPN to bypass network restrictions
4. **Cloud Build**: Use GitHub Actions or other CI/CD services

### Solution 2: Add IP to Hosts File

Try adding Fabric Maven IPs to your hosts file:

**Linux/Mac:**
```bash
sudo nano /etc/hosts
```

**Windows:**
```
Run Notepad as Administrator
Open: C:\Windows\System32\drivers\etc\hosts
```

Add these lines:
```
104.21.33.240 maven.fabricmc.net
172.67.151.177 maven.fabricmc.net
2606:4700:3033::ac43:97b1 maven.fabricmc.net
2606:4700:3037::6815:21f0 maven.fabricmc.net
```

Then retry:
```bash
./gradlew clean build
```

**Note**: This only works if DNS is blocked but the IPs themselves are accessible.

### Solution 3: Use a VPN

If the entire Cloudflare network is blocked:

1. Install a VPN (ProtonVPN, NordVPN, Mullvad, etc.)
2. Connect to a server in a different country
3. Run the build command
4. Disconnect VPN after build completes

### Solution 4: Use GitHub Actions (Free CI/CD)

Use GitHub's servers to build the mod automatically:

1. Create `.github/workflows/build.yml`:

```yaml
name: Build Fabric Mod

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build with Gradle
      run: ./gradlew build --no-daemon
    
    - name: Upload artifacts
      uses: actions/upload-artifact@v3
      with:
        name: Fabric Mod JAR
        path: build/libs/*.jar
```

2. Commit and push to GitHub
3. Go to "Actions" tab on GitHub
4. Click "Build Fabric Mod" workflow
5. Click "Run workflow"
6. Download the built JAR from the artifacts

### Solution 5: Use Docker with Different DNS

Create a `Dockerfile`:

```dockerfile
FROM eclipse-temurin:21-jdk

WORKDIR /build

# Use Google DNS
RUN echo "nameserver 8.8.8.8" > /etc/resolv.conf
RUN echo "nameserver 8.8.4.4" >> /etc/resolv.conf

# Copy project files
COPY . .

# Make gradlew executable
RUN chmod +x gradlew

# Build the project
RUN ./gradlew build --no-daemon

# The JAR will be in build/libs/
```

Build and extract:
```bash
docker build -t fabric-mod-builder .
docker create --name temp-container fabric-mod-builder
docker cp temp-container:/build/build/libs/ ./output/
docker rm temp-container
```

### Solution 6: Download Dependencies Manually (Advanced)

If you have access to another computer with internet:

1. On a computer with internet, run:
   ```bash
   ./gradlew build
   ```

2. Copy the entire `~/.gradle/caches/` directory

3. Transfer to restricted computer

4. Place in same location (`~/.gradle/caches/`)

5. Run build (should use cached dependencies)

**Note**: This is complex and may not work if Gradle wrapper version differs.

### Solution 7: Request Network Whitelist

If on a corporate/school network:

1. Contact your IT department
2. Request whitelisting of:
   - `maven.fabricmc.net`
   - `*.fabricmc.net`
   - Cloudflare IPs: `104.21.33.240`, `172.67.151.177`
3. Explain it's for Minecraft mod development (legitimate software development)

## Quick Test Commands

Test if you can reach Fabric Maven:

```bash
# Test DNS resolution
nslookup maven.fabricmc.net

# Test connection
curl -I https://maven.fabricmc.net/

# Test with timeout
timeout 5 curl https://maven.fabricmc.net/
```

If any of these fail, you'll need one of the workarounds above.

## Recommended Workflow

**For Most Users:**

1. Try VPN (easiest if you have one)
2. Try mobile hotspot (usually works)
3. Use GitHub Actions (free, reliable)
4. Ask IT to whitelist (if on corporate network)

**For Developers:**

1. GitHub Actions for automated builds
2. VPN for local development
3. Consider using a cloud development environment (GitHub Codespaces, GitPod)

## Verification After Build

After successfully building with any method:

```bash
# Check that JAR was created
ls -lh build/libs/

# You should see:
# estonic-autofishing-1.0.0.jar
# estonic-autofishing-1.0.0-sources.jar
```

The main JAR file (without `-sources`) is what you install in Minecraft.

## Alternative: Pre-built Releases

If you cannot build the mod yourself:

1. Check the GitHub Releases page
2. Download the pre-built JAR file
3. Install directly into your mods folder

**Note**: The repository owner should provide pre-built releases for users who cannot build themselves.

## Still Having Issues?

If none of these solutions work:

1. **Open an issue** on GitHub with details:
   - Your operating system
   - Network type (home, school, corporate)
   - Error messages
   - Results of test commands

2. **Ask for pre-built release**: Request that the maintainer upload a pre-built JAR to GitHub Releases

3. **Community help**: Ask on:
   - Fabric Discord: https://discord.gg/v6v4pMv
   - r/fabricmc subreddit
   - Minecraft Forums

## Security Note

When using VPNs or changing network settings:
- Only use reputable VPN providers
- Verify you're downloading from official sources
- Don't disable firewall/antivirus
- Restore network settings after building

## For Repository Maintainers

To help users avoid build issues:

1. **Set up GitHub Actions** for automated builds
2. **Upload to Releases**: Provide pre-built JARs
3. **Document network requirements** clearly
4. **Consider alternative hosting**: Mirror on Maven Central if possible

---

*If you successfully build the mod with one of these methods, please share your solution in a GitHub issue to help others!*
