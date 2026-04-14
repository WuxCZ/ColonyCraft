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
- Track food, science, population and days survived
- Colony data persists across sessions via `PersistentState`
- **Colony HUD** shows real-time status top-left of your screen

</td>
<td width="50%">

**👷 27 Colonist Jobs**
- Every major job from Colony Survival
- Colonists auto-claim job blocks near the banner
- Each job has custom visual appearance (vanilla entity skin reuse)
- Jobs produce items into and take inputs from the Stockpile

</td>
</tr>
<tr>
<td width="50%">

**⚔️ Nightly Waves**
- Waves spawn at dusk every in-game day
- Wave size = `4 + days × 2`, HP scales with time
- **Guard colonists** patrol and shoot hostile mobs
- Survive long enough and your colony grows unstoppable

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

## 🚀 Getting Started (In-Game)

```
1. Craft a Colony Banner and place it in the ground
2. Craft a Stockpile and place it within 64 blocks of the banner
3. Fill the Stockpile with food (bread, cooked fish, etc.)
4. Craft any job block and place it near the banner
5. Colonists spawn automatically — up to your population cap
6. Survive the nightly wave at dusk!
```

> Population cap starts at **2** and grows by +1 per in-game day survived.

---

## 🔨 Key Crafting Recipes

| Item | Recipe |
|---|---|
| **Colony Banner** | White wool (top row) + Red wool center + Stick |
| **Stockpile** | Oak planks ring (like a chest) |
| **Job Assignment Book** | Book + Paper + Oak planks |
| **Guidebook** | Feather + Book + Paper + Ink Sac |
| **Woodcutter Bench** | Oak log + Iron axe + Planks |
| **Guard Tower** | Stone + Bow + Iron ingot |

*All 26 job blocks have unique shaped recipes. Open the recipe book in-game (press **E → Recipe Book**).*

---

## 🖥️ HUD

When you have an active colony, a status panel appears top-left:

```
[Colony] PlayerName
Food:  42
Pop:   2/3
Sci:   15
Day:   4
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
│   │   └── goal/        – AI goals: Work, Eat, Return, Patrol, Wave
│   ├── item/            – JobAssignmentBook, GuidebookItem
│   ├── registry/        – ModBlocks, ModItems, ModEntities, ModScreenHandlers
│   └── screen/          – Stockpile + ColonyBanner screen handlers
└── client/java/cz/wux/colonycraft/client/
    ├── render/          – ColonistEntityRenderer (per-job textures), Guards
    └── screen/          – Stockpile GUI, ColonyBanner GUI
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

## 📦 Dependencies

| Dependency | Version |
|---|---|
| Minecraft | 1.21.11 |
| Fabric Loader | ≥ 0.19.1 |
| Fabric API | 0.141.3+1.21.11 |
| Java | 21 |

---

## 🧩 What Still Needs Work

See [CONTRIBUTING.md](CONTRIBUTING.md) for the full task list and how to help.

Quick overview of what's missing or rough:
- [ ] Custom colonist models (currently reuses vanilla entity skins)
- [ ] Colony border visual (particle or block outline)
- [ ] Sound effects for jobs and waves
- [ ] More production recipes (currently ~18 mapped)
- [ ] Research tree GUI
- [ ] Multiplayer — each player owns their own colony, no conflicts yet tested
- [ ] Balance pass on wave difficulty and food consumption rates

---

## 🤝 Contributing

Contributions are very welcome! Read [CONTRIBUTING.md](CONTRIBUTING.md) to get up to speed quickly.

---

## 📜 License

MIT — do whatever you want with this code.

---

<div align="center">

Made with ☕ by **[WuxCZ](https://github.com/WuxCZ)**

*Inspired by [Colony Survival](https://store.steampowered.com/app/366090/Colony_Survival/) by Pipliz*

</div>
