# Contributing to ColonyCraft

Thanks for wanting to help! This document explains how the project is structured, how to get it running, and what specifically still needs to be done.

---

## Quick Setup

**Requirements:** Java 21, Git

```bash
git clone https://github.com/WuxCZ/ColonyCraft.git
cd ColonyCraft
./gradlew runClient   # opens Minecraft dev environment
```

On Windows use `gradlew.bat` instead of `./gradlew`.

The dev client launches with the mod pre-loaded â€” no `.jar` install needed.

---

## Project Layout

```
src/
â”œâ”€â”€ main/java/cz/wux/colonycraft/
â”‚   â”œâ”€â”€ block/           â€“ All 26 job blocks + ColonyBanner + Stockpile
â”‚   â”œâ”€â”€ blockentity/     â€“ BlockEntity + screen handlers for interactive blocks
â”‚   â”œâ”€â”€ data/
â”‚   â”‚   â”œâ”€â”€ ColonistJob.java    â€“ Enum of all 27 jobs
â”‚   â”‚   â”œâ”€â”€ ColonyData.java     â€“ Per-colony data: food, pop, science, days
â”‚   â”‚   â””â”€â”€ ColonyManager.java  â€“ Server-side singleton, PersistentState
â”‚   â”œâ”€â”€ entity/
â”‚   â”‚   â”œâ”€â”€ ColonistEntity.java       â€“ The main worker colonist
â”‚   â”‚   â”œâ”€â”€ GuardEntity.java          â€“ Combat colonist, patrols banner radius
â”‚   â”‚   â”œâ”€â”€ ColonyMonsterEntity.java  â€“ Nightly wave attacker
â”‚   â”‚   â””â”€â”€ goal/                     â€“ All AI goals
â”‚   â”œâ”€â”€ item/            â€“ JobAssignmentBook, GuidebookItem
â”‚   â”œâ”€â”€ registry/        â€“ ModBlocks, ModItems, ModEntities (all registrations)
â”‚   â””â”€â”€ screen/          â€“ Server-side screen handlers
â””â”€â”€ client/java/cz/wux/colonycraft/client/
    â”œâ”€â”€ render/
    â”‚   â”œâ”€â”€ ColonistEntityRenderer.java  â€“ Per-job vanilla texture mapping
    â”‚   â””â”€â”€ ColonistRenderState.java     â€“ Custom render state with jobTexture field
    â””â”€â”€ screen/          â€“ Client GUI screens (Stockpile, ColonyBanner)
```

---

## Current Status & TODO

### ðŸ”´ High priority (bugs / missing core features)

- [x] **Build & compile check** - BUILD SUCCESSFUL
- [x] **Colony border visualizer** - HUD overlay shows live colony radius; border particle ring added (orange dust, 64-block radius, 40-tick interval)
- [ ] **Multiplayer testing** â€” multiple players each founding their own colony; confirm no UUID collisions or shared state bugs
- [x] **Wave scaling balance** - S-curve cap: max 40 monsters (reached ~day 18); HP soft-capped at 180

### ðŸŸ¡ Medium priority (polish / gameplay)

- [x] **More production recipes** - All 27 jobs fully mapped in WorkAtJobGoal.RECIPES
- [x] **Research tree GUI** - ResearchScreen + ResearchScreenHandler + ResearchTableBlock: click job row to spend science and unlock
- [x] **Food consumption balance** - Colonists eat every 100 colony-ticks (~5 min real time), configurable via ColonyCraftConfig
- [x] **Guard improvements** - GUARD_BOW=20t/2dmg, GUARD_CROSSBOW=14t/3dmg, GUARD_MUSKET=40t/7dmg

### ðŸŸ¢ Low priority (nice to have)

- [ ] **Custom colonist models** â€” currently reuses vanilla entity skins (zombie/pillager/etc.); proper humanoid model with job-specific outfit would be ideal
- [x] **Sound effects** - ModSounds registered: colonist.work, colonist.spawn, wave.horn (via vanilla MC sounds.json)
- [x] **Colonist name tags** - Job name shown above colonist head after auto-assign and job change
- [x] **Colony statistics screen** - ColonyBannerScreen shows live colonists/food/science/day data
- [x] **Config file** - ColonyCraftConfig.java, saved to config/colonycraft.json on first run

---

## Key Files to Know

| File | What it does |
|---|---|
| `ColonyData.java` | All colony state: food units, population cap, science, days. Pop cap = `2 + daysSurvived`. |
| `ColonyManager.java` | Server-side singleton. `get(server).getAllColonies()` returns all active colonies. |
| `ColonistEntity.java` | Main colonist AI entity. `getColonistJob()` returns current job. |
| `ColonistEntityRenderer.java` | Maps each `ColonistJob` enum value to a vanilla texture path. |
| `ModBlocks.java` | All block registrations. Add new blocks here. |
| `ModItems.java` | All item registrations including `BlockItem` wrappers. |
| `ColonistJob.java` | The master enum of all 27 jobs â€” add new jobs here first. |

---

## How to Add a New Job

1. Add an entry to `ColonistJob.java` enum
2. Add a new block class extending `AbstractJobBlock` in `block/`
3. Register the block in `ModBlocks.java` and `ModItems.java`
4. Add a texture PNG in `assets/colonycraft/textures/block/`
5. Add block model JSON in `assets/colonycraft/models/block/` and `blockstates/`
6. Add item model JSON in `assets/colonycraft/models/item/`
7. Add the loot table in `data/colonycraft/loot_table/blocks/`
8. Add a crafting recipe in `data/colonycraft/recipe/`
9. Add translation key in `assets/colonycraft/lang/en_us.json`
10. Map a texture in `ColonistEntityRenderer.JOB_TEXTURES`

---

## Branch Strategy

| Branch | Purpose |
|---|---|
| `master` | Stable, working builds only |
| `dev` | Active development â€” push your work here |

Open a Pull Request from `dev` â†’ `master` when a feature is complete and builds successfully.

---

## Code Style

- Standard Java formatting, 4-space indentation
- Server-side code in `src/main/`, client-only code in `src/client/`
- Never access `MinecraftClient` from server-side code
- `@Environment(EnvType.CLIENT)` on any class that touches client APIs
