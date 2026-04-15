# Publishing ColonyCraft to CurseForge

## One-Time Setup

### 1. Create a CurseForge Account
- Go to **https://www.curseforge.com** and sign up / log in

### 2. Get your API Token
- Go to **https://www.curseforge.com/account/api-tokens**
- Click **Generate Token**
- Copy the token — you will NOT see it again
- Save it as environment variable:
  ```powershell
  # PowerShell (permanent)
  [Environment]::SetEnvironmentVariable("CURSEFORGE_TOKEN", "your-token-here", "User")
  ```

### 3. Create the Project
- Go to **https://www.curseforge.com/project/create**
- Fill in:
  - **Name:** ColonyCraft
  - **Project Type:** Mods
  - **Game:** Minecraft
  - **Category:** Adventure / Gameplay
  - **Summary:** Colony Survival mechanics in Minecraft — colonists, jobs, nightly waves, economy, and colony management
  - **Description:** *(copy from `curseforge_description.md` in this repo)*
  - **Icon:** Upload `curseforge_icon.png` from this repo (400×400)
  - **License:** MIT
  - **Source URL:** https://github.com/WuxCZ/colonycraft
  - **Issues URL:** https://github.com/WuxCZ/colonycraft/issues
- After creating, note the **Project ID** from the project URL (a number like `123456`)
- Save it:
  ```powershell
  [Environment]::SetEnvironmentVariable("CURSEFORGE_PROJECT_ID", "123456", "User")
  ```

---

## Publishing a Release

### Option A: Gradle (Automated)

```powershell
cd C:\ColonyCraft

# Build first
.\gradlew.bat build

# Publish to CurseForge
.\gradlew.bat publishToCurseForge
```

Or pass credentials inline:
```powershell
.\gradlew.bat publishToCurseForge -Pcurseforge_token=YOUR_TOKEN -Pcurseforge_project_id=YOUR_ID
```

### Option B: Manual Upload

1. Build the mod: `.\gradlew.bat build`
2. Go to your CurseForge project → **Upload File**
3. Upload `build/libs/colonycraft-1.0.0.jar`
4. Set:
   - **Release Type:** Release
   - **Game Version:** 1.21.11
   - **Mod Loader:** Fabric
   - **Java Version:** Java 21
   - **Dependencies:** Fabric API (Required)
5. Paste changelog from `CHANGELOG.md`
6. Submit

---

## Recommended Tags & Categories

| Field | Value |
|---|---|
| Primary Category | Adventure |
| Secondary Category | Gameplay |
| Tags | Colony, Survival, Colonists, NPCs, Jobs, Waves, Management |
| Environment | Client & Server |
| Mod Loader | Fabric |
| Game Version | 1.21.11 |

---

## Included Extras

### ColonyCraft Shaders
The `shaderpack/` folder contains "ColonyCraft Shaders" — a lightweight Iris-compatible shader pack with:
- Soft shadow mapping (1024×1024, 5-tap PCF)
- Warm Colony Survival-style color grading
- Day/night lighting transitions
- Subtle vignette effect

To distribute:
1. Zip the `shaderpack/shaders/` folder → `ColonyCraft_Shaders.zip`
2. Upload as an additional file on CurseForge (File Type: Resource Pack)
3. Or host separately and link in the description

---

## Version Bumping

Before a new release, update the version in `gradle.properties`:
```properties
mod_version=1.1.0
```

Then rebuild and publish.
