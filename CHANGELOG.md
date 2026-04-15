# Changelog

## [Unreleased] — v0.5.0

### Added
- **Quest System** — 16 progression quests (New Beginnings → Master Builder) with science point rewards
  - Tutorial chain: Banner → Stockpile → Beds → Workforce → Daily Bread
  - Survival chain: First Night → Arms Race → Fortified → Iron Will
  - Growth chain: Growing Strong → Thriving Colony → Master Builder
  - Knowledge chain: Knowledge → Scholar → Enlightened
- **Chisel Tool** — right-click blocks to cycle through decorative variants (20+ block families: Stone, Deepslate, Sandstone, Quartz, Blackstone, Copper, Tuff, Prismarine, etc.)
- **Banner Raiders** — ColonyMonsterEntity enemies with `DestroyBannerGoal` that specifically pathfind to and destroy your Colony Banner
- **Wave Warning System**:
  - Dusk warning message at day tick 11800 ("Prepare your defenses!")
  - Imminent warning + horn sound at day tick 12700 ("WAVE INCOMING!")
  - Wave countdown timer in HUD (shows from dusk until wave spawn)
  - Blinking red countdown when wave is less than 50 seconds away
- **Work Particle Effects** — job-specific particles while colonists work (flames for smelter, enchant sparks for researcher, splash for fisherman, etc.)
- **25 Custom Job Textures** — unique tinted colonist skins per job (farmer=green, miner=gray, smelter=orange, researcher=blue, etc.)
- **Guard Custom Textures** — guard_sword.png and guard_bow.png with baked armor appearance

### Changed
- Colonist arms now visible (using NEUTRAL illager state instead of CROSSED)
- Colonist status no longer shows "Idle" immediately after world load — initializes correctly from saved job
- Guard textures updated from vanilla vindicator/pillager to custom skins with armor
- Wave enemy composition rebalanced: 35% skeletons, 30% zombies, 20% banner raiders, 15% spiders
- Wave scaling now uses `max(6, min(100, colonists*2.5 + days*2))` for aggressive Colony Survival-like growth
- fabric.mod.json updated with proper homepage/issues URLs

### Fixed
- Colonists and guards no longer trample farmland (`FarmlandBlockMixin`)
- Status text correctly initialized after NBT deserialization
- HeldItemFeatureRenderer added to colonists so held items are visible

---

## [0.4.0] — Previous Session

### Added
- Random colonist names from a pool of 64+ names
- Death message system (nearby players notified when colonists die)
- Population growth system (random chance, requires food > 20)
- Auto-spawn guards for unclaimed guard towers
- Area wand with job selection popup
- Colony party system (multiple players per colony)
- Wave scaling by colonist count and days survived

### Fixed
- Guard job assignment saved/loaded correctly ("GUARD" → "GUARD_SWORD" migration)
- Stockpile food sync every tick
- Job block area assignment persistence

---

## [0.3.0]

### Added
- GuardEntity with sword/bow variants and PatrolGoal
- 25+ job blocks (all jobs registered)
- ColonyManagementScreen with scrollable colonist list
- Research system with science point economy
- GuidebookScreen (10-page in-game tutorial)
- ResearchScreen for job unlocking

---

## [0.2.0]

### Added
- ColonistEntity with hunger/starvation system
- AI goals: ChopTree, HarvestCrops, MineBlocks, PlantSaplings, WorkAtJob, ColonistSleep, ColonistEat
- StockpileBlock with 54-slot GUI
- ColonyBannerBlock that founds a colony on placement
- ColonyData and ColonyManager (persistent world data)

---

## [0.1.0] — Initial Release

- Basic colony infrastructure
- Colony Banner placement
- Starter kit on first join
- Server tick event for colony processing
