# ColonyCraft

> **Colony Survival mechanics recreated inside Minecraft as a Fabric mod.**

ColonyCraft ports the core gameplay loop of [Colony Survival](https://store.steampowered.com/app/366090/Colony_Survival/) into Minecraft 1.21.11 using the [Fabric](https://fabricmc.net/) modloader.

---

## Features

### Colony Management
- Place a **Colony Banner** to found your colony
- Colonies persist across sessions (saved with world data via `PersistentState`)
- Track food reserves, science points, population cap, and days survived

### 27 Colonist Jobs
All major jobs from Colony Survival are implemented as craftable job blocks:

| Category | Jobs |
|---|---|
| **Resource gathering** | Woodcutter, Forester, Miner, Farmer, Berry Farmer, Fisherman, Water Gatherer |
| **Processing** | Cook, Smelter (Bloomery), Blacksmith (Blast Furnace), Tanner, Tailor, Fletcher, Stonemason, Composter, Grinder, Potter, Glassblower |
| **Science** | Researcher, Alchemist |
| **Beekeeping / Farming** | Beekeeper, Chicken Farmer |
| **Defense** | Guard (Bow), Guard (Crossbow), Guard (Musket) |

### AI Colonists
- Colonists automatically claim nearby unclaimed job blocks
- **WorkAtJobGoal** — colonists walk to their job block, withdraw inputs from the Stockpile, and deposit outputs
- **ColonistEatGoal** — colonists consume food from the Stockpile when hungry; they die after ~20 minutes without food
- **ReturnToColonyGoal** — colonists return home when idle or lost
- 18+ mapped production recipes mirroring Colony Survival output rates

### Guards
- Guard colonists shoot arrows at hostile mobs near the banner radius
- **GuardPatrolGoal** — patrols 24-block radius around the colony

### Nightly Waves
- At dusk (game time 13 000) a wave of **ColonyMonster** entities spawns
- Wave size: `4 + daysSurvived × 2`
- Monster HP scales with days: `20 + (days / 5) × 10`

### Stockpile
- 54-slot inventory block (double-chest layout)
- Automatically tracks 19 food items and syncs food count to the colony
- Full GUI accessible by right-clicking

### Research
- **Research Table** block accumulates science points produced by Researchers
- Science points unlock new job types (extensible)

### Job Assignment Book
- Right-click any job block with the Job Assignment Book to see its job type and assigned colonist

---

## Requirements

| Dependency | Version |
|---|---|
| Minecraft | 1.21.11 |
| Fabric Loader | ≥ 0.19.1 |
| Fabric API | 0.141.3+1.21.11 |
| Java | 21 |

---

## Building from source

```bash
git clone https://github.com/WuxCZ/colonycraft.git
cd colonycraft
./gradlew build
# Output: build/libs/colonycraft-1.0.0.jar
```

---

## Installation

1. Install [Fabric Loader 0.19.1+](https://fabricmc.net/use/installer/) for Minecraft 1.21.11
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.11
3. Drop both JARs into your `.minecraft/mods/` folder
4. Launch and enjoy!

---

## Getting started in-game

1. Craft a **Colony Banner** (gold block recipe) and place it
2. Craft a **Stockpile** and place it within 64 blocks of the banner
3. Fill the Stockpile with food (bread, fish, etc.)
4. Craft job blocks and place them near the banner
5. Colonists will spawn automatically up to your population cap
6. Survive the nightly waves!

---

## Project structure

```
src/
├── main/java/cz/wux/colonycraft/
│   ├── block/          # ColonyBanner, Stockpile, ResearchTable + 23 job blocks
│   ├── blockentity/    # BlockEntity implementations
│   ├── data/           # ColonistJob enum, ColonyData, ColonyManager
│   ├── entity/         # ColonistEntity, GuardEntity, ColonyMonsterEntity
│   │   └── goal/       # AI goals (Work, Eat, Return, Patrol)
│   ├── item/           # JobAssignmentBook
│   ├── registry/       # ModBlocks, ModItems, ModEntities, …
│   └── screen/         # GUI screen handlers
└── client/java/cz/wux/colonycraft/client/
    ├── render/         # Entity renderers
    └── screen/         # GUI screens
```

---

## License

MIT — do whatever you want with this.

---

*Made by [WuxCZ](https://github.com/WuxCZ)*
