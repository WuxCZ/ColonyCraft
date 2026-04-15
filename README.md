<div align="center">

```
 ██████╗ ██████╗ ██╗      ██████╗ ███╗   ██╗██╗   ██╗ ██████╗██████╗  █████╗ ███████╗████████╗
██╔════╝██╔═══██╗██║     ██╔═══██╗████╗  ██║╚██╗ ██╔╝██╔════╝██╔══██╗██╔══██╗██╔════╝╚══██╔══╝
██║     ██║   ██║██║     ██║   ██║██╔██╗ ██║ ╚████╔╝ ██║     ██████╔╝███████║█████╗     ██║
██║     ██║   ██║██║     ██║   ██║██║╚██╗██║  ╚██╔╝  ██║     ██╔══██╗██╔══██║██╔══╝     ██║
╚██████╗╚██████╔╝███████╗╚██████╔╝██║ ╚████║   ██║   ╚██████╗██║  ██║██║  ██║██║        ██║
 ╚═════╝ ╚═════╝ ╚══════╝ ╚═════╝ ╚═╝  ╚═══╝   ╚═╝    ╚═════╝╚═╝  ╚═╝╚═╝  ╚═╝╚═╝        ╚═╝
```

### Colony Survival mechanics — rebuilt from scratch inside Minecraft

[![Build](https://github.com/WuxCZ/ColonyCraft/actions/workflows/build.yml/badge.svg)](https://github.com/WuxCZ/ColonyCraft/actions/workflows/build.yml)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-62b47a?logo=minecraft&logoColor=white)
![Fabric](https://img.shields.io/badge/Fabric-0.19.1-dbb468?logo=curseforge&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue)
![Status](https://img.shields.io/badge/Status-Active%20Dev-brightgreen)

<br/>

> **What if Colony Survival and Minecraft had a baby?**
> ColonyCraft is a Fabric mod that recreates the full Colony Survival gameplay loop — colonists with jobs, nightly monster waves, food economy, research, and colony management — all inside vanilla Minecraft.

</div>

---

## ✨ Feature Overview

<table>
<tr>
<td width="50%">

**🏰 Colony Management**
- Place a **Colony Banner** to found your colony
- Press **;** to open Colony Management screen
- Track food, science, population and days survived
- **Colony HUD** shows real-time status top-left of your screen
- Recruit colonists directly from the management screen

</td>
<td width="50%">

**👷 27 Colonist Jobs**
- Every major job from Colony Survival
- Colonists auto-claim job blocks near the banner
- Each job has a dedicated workstation block with unique texture
- Jobs produce items into and take inputs from the Stockpile

</td>
</tr>
<tr>
<td width="50%">

**🔮 Colony Survey Wand**
- Select work areas with 2-corner selection (like Colony Survival!)
- **Particle visualization** when holding the wand
- Gold particles show your current selection
- Green particles show all assigned job areas nearby
- Job selection popup after marking an area

</td>
<td width="50%">

**⚔️ Nightly Waves**
- Waves spawn at dusk every in-game day
- Wave size = `4 + days × 2`, HP scales with time
- **Guard colonists** patrol and shoot hostile mobs
- Survive long enough and your colony grows unstoppable

</td>
</tr>
<tr>
<td width="50%">

**📖 In-Game Guidebook**
- 10-page illustrated guide with book-style GUI
- Covers all gameplay mechanics
- Accessible from inventory right-click or colony management

</td>
<td width="50%">

**🔬 Research & Economy**
- Researchers produce science points
- Science points unlock new job types
- Food economy — colonists eat and die without supply
- 54-slot Stockpile GUI is the colony's heart

</td>
</tr>
</table>

---

## 🗂️ All Jobs

| Category | Jobs |
|---|---|
| 🌲 **Gathering** | Woodcutter, Forester, Miner, Farmer, Berry Farmer, Fisherman, Water Gatherer |
| 🔥 **Processing** | Cook, Smelter, Blacksmith, Tanner, Tailor, Fletcher, Stonemason, Composter, Grinder, Potter, Glassblower |
| 🔭 **Science** | Researcher, Alchemist |
| 🐝 **Animals** | Beekeeper, Chicken Farmer |
| 🏹 **Defense** | Guard (Bow), Guard (Crossbow), Guard (Musket) |

---

## 🚀 Getting Started

### In-Game Tutorial
```
1. Craft a Colony Banner and place it on flat ground
2. A Stockpile and food are given as starter kit
3. Press ; to open Colony Management
4. Place job blocks within 32 blocks of the banner
5. Use the Colony Survey Wand to mark work areas
6. Colonists spawn automatically and start working
7. Survive the nightly wave at dusk!
```

> Population cap starts at **2** and grows by +1 per in-game day survived (max 50).

### Installation (Official Minecraft Launcher)
```
1. Download and install Fabric Loader from https://fabricmc.net/use/installer/
2. Select Minecraft 1.21.11 and install
3. Download Fabric API from https://modrinth.com/mod/fabric-api
4. Download ColonyCraft from the Releases page
5. Put both .jar files in .minecraft/mods/
6. Launch Minecraft with the "fabric-loader-1.21.11" profile
```

---

## 🖥️ HUD & Controls

| Control | Action |
|---|---|
| **;** | Open Colony Management screen |
| **Right-click** Guide Book | Open 10-page in-game guide |
| **Right-click** with Wand | Set work area corners → auto job selection |
| **Right-click** Job Block | View job info / assign area |

When you have an active colony, a status panel appears top-left:
```
🏰 Colony
Food:  42
Pop:   2/3
Sci:   15
Day:   4
```

---

## ⚙️ Building from Source

**Requirements:** Java 21, Git

```bash
git clone https://github.com/WuxCZ/ColonyCraft.git
cd ColonyCraft
./gradlew build
# → build/libs/colonycraft-1.0.0.jar
```

**Run the dev client:**
```bash
./gradlew runClient
```

---

## 📁 Project Structure

```
src/
├── main/java/cz/wux/colonycraft/
│   ├── block/           – ColonyBanner, Stockpile + 24 job blocks
│   ├── blockentity/     – BlockEntity implementations + screen handlers
│   ├── data/            – ColonistJob enum, ColonyData, ColonyManager
│   ├── entity/          – ColonistEntity, GuardEntity, ColonyMonsterEntity
│   │   └── goal/        – AI goals: Chop, Harvest, Mine, Plant, Eat, Patrol
│   ├── item/            – GuidebookItem, JobAssignmentBook, AreaWandItem
│   ├── registry/        – ModBlocks, ModItems, ModEntities, ModScreenHandlers
│   └── screen/          – Stockpile + ColonyBanner screen handlers
└── client/java/cz/wux/colonycraft/client/
    ├── render/          – Entity renderers, AreaWandRenderer, ColonyBorderRenderer
    └── screen/          – Management, Guidebook, Research, JobSelection GUIs
```

---

## 📦 Dependencies

| Dependency | Version |
|---|---|
| Minecraft | 1.21.11 |
| Fabric Loader | ≥ 0.19.1 |
| Fabric API | 0.141.3+1.21.11 |
| Java | 21 |

---

<!-- AUTO-GENERATED STATS - DO NOT EDIT BELOW -->
## Stats

| Metric | Count |
|---|---|
| Job Types | 3 |
| Block Types | 26 |
| Item Types | 29 |
| Recipe Files | 28 |
| Source Files | ~77 |
| Textures | 32 |

*Auto-updated on 2026-04-15 by GitHub Actions.*
<!-- END AUTO-GENERATED STATS -->

---

## 📜 License

MIT — do whatever you want with this code.

---

<div align="center">

Made with ☕ by **[WuxCZ](https://github.com/WuxCZ)**

*Inspired by [Colony Survival](https://store.steampowered.com/app/366090/Colony_Survival/) by Pipliz*

</div>