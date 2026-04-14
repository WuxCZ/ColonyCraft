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

The dev client launches with the mod pre-loaded — no `.jar` install needed.

---

## Project Layout

```
src/
├── main/java/cz/wux/colonycraft/
│   ├── block/           – All 26 job blocks + ColonyBanner + Stockpile
│   ├── blockentity/     – BlockEntity + screen handlers for interactive blocks
│   ├── data/
│   │   ├── ColonistJob.java    – Enum of all 27 jobs
│   │   ├── ColonyData.java     – Per-colony data: food, pop, science, days
│   │   └── ColonyManager.java  – Server-side singleton, PersistentState
│   ├── entity/
│   │   ├── ColonistEntity.java       – The main worker colonist
│   │   ├── GuardEntity.java          – Combat colonist, patrols banner radius
│   │   ├── ColonyMonsterEntity.java  – Nightly wave attacker
│   │   └── goal/                     – All AI goals
│   ├── item/            – JobAssignmentBook, GuidebookItem
│   ├── registry/        – ModBlocks, ModItems, ModEntities (all registrations)
│   └── screen/          – Server-side screen handlers
└── client/java/cz/wux/colonycraft/client/
    ├── render/
    │   ├── ColonistEntityRenderer.java  – Per-job vanilla texture mapping
    │   └── ColonistRenderState.java     – Custom render state with jobTexture field
    └── screen/          – Client GUI screens (Stockpile, ColonyBanner)
```

---

## Current Status & TODO

### 🔴 High priority (bugs / missing core features)

- [ ] **Build & compile check** — run `./gradlew build` and fix any compile errors after recent changes
- [ ] **Colony border visualizer** — show particles or an outline at the colony's 64-block radius so the player can see the border
- [ ] **Multiplayer testing** — multiple players each founding their own colony; confirm no UUID collisions or shared state bugs
- [ ] **Wave scaling balance** — `4 + days×2` monsters gets brutal fast; needs a cap or curve

### 🟡 Medium priority (polish / gameplay)

- [ ] **More production recipes** — `ColonistEntity.PRODUCTION_RECIPES` currently has ~18 entries; fill in the remaining jobs (Beekeeper, Chicken Farmer, Potter, Glassblower, Alchemist, Tanner, Tailor, etc.)
- [ ] **Research tree GUI** — currently science points accumulate but there's no GUI to spend them; needs a screen + unlock tree
- [ ] **Food consumption balance** — colonists eat every `200` ticks; tune based on play-testing
- [ ] **Guard improvements** — Guards currently only use bow; GUARD_CROSSBOW and GUARD_MUSKET use the same logic

### 🟢 Low priority (nice to have)

- [ ] **Custom colonist models** — currently reuses vanilla entity skins (zombie/pillager/etc.); proper humanoid model with job-specific outfit would be ideal
- [ ] **Sound effects** — ambient colony sounds, job sounds (chopping, mining), wave horn
- [ ] **Colonist name tags** — show colonist name + job above head
- [ ] **Colony statistics screen** — detailed screen showing all colonists, their jobs, and production rates
- [ ] **Config file** — allow tuning wave size, food rates, pop cap growth via a config

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
| `ColonistJob.java` | The master enum of all 27 jobs — add new jobs here first. |

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
| `dev` | Active development — push your work here |

Open a Pull Request from `dev` → `master` when a feature is complete and builds successfully.

---

## Code Style

- Standard Java formatting, 4-space indentation
- Server-side code in `src/main/`, client-only code in `src/client/`
- Never access `MinecraftClient` from server-side code
- `@Environment(EnvType.CLIENT)` on any class that touches client APIs
