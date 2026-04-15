package cz.wux.colonycraft.data;

import cz.wux.colonycraft.entity.ColonistEntity;
import cz.wux.colonycraft.entity.GuardEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

/**
 * Colony quests — tutorial-style objectives that guide players through colony building.
 * Each quest has prerequisites forming a progression tree.
 */
public enum Quest {

    // -- Tutorial chain --
    NEW_BEGINNINGS  ("New Beginnings",       "Place your colony banner",         QuestType.BANNER_PLACED,    1,   0,  null),
    FIRST_STEPS     ("First Steps",          "Build a stockpile near the banner",QuestType.STOCKPILE_PLACED, 1,   5,  "NEW_BEGINNINGS"),
    SHELTER         ("Shelter",              "Place 3 beds for your colonists",  QuestType.BED_COUNT,        3,   5,  "FIRST_STEPS"),
    WORKFORCE       ("Workforce",            "Recruit 3 colonists",             QuestType.COLONIST_COUNT,   3,  10,  "SHELTER"),
    SPECIALIZATION  ("Specialization",       "Assign jobs to 2 colonists",      QuestType.ASSIGNED_JOBS,    2,  10,  "WORKFORCE"),
    DAILY_BREAD     ("Daily Bread",          "Stockpile 20 food units",         QuestType.FOOD_UNITS,       20, 10,  "WORKFORCE"),

    // -- Survival chain --
    FIRST_NIGHT     ("First Night",          "Survive the first night wave",    QuestType.WAVES_SURVIVED,   1,  20,  "WORKFORCE"),
    ARMS_RACE       ("Arms Race",            "Recruit 2 guards",               QuestType.GUARD_COUNT,      2,  15,  "FIRST_NIGHT"),
    FORTIFIED       ("Fortified",            "Survive 5 night waves",           QuestType.WAVES_SURVIVED,   5,  50,  "FIRST_NIGHT"),
    IRON_WILL       ("Iron Will",            "Survive 10 night waves",          QuestType.WAVES_SURVIVED,  10, 100,  "FORTIFIED"),

    // -- Growth chain --
    GROWING_STRONG  ("Growing Strong",       "Expand to 10 colonists",          QuestType.COLONIST_COUNT,  10,  25,  "FIRST_NIGHT"),
    THRIVING        ("Thriving Colony",      "Grow to 25 colonists",            QuestType.COLONIST_COUNT,  25,  50,  "GROWING_STRONG"),
    MASTER          ("Master Builder",       "Reach 50 colonists",              QuestType.COLONIST_COUNT,  50, 200,  "THRIVING"),

    // -- Knowledge chain --
    KNOWLEDGE       ("Knowledge is Power",   "Earn 10 science points",          QuestType.SCIENCE_POINTS,  10,   0,  "SPECIALIZATION"),
    SCHOLAR         ("Scholar",              "Earn 50 science points",          QuestType.SCIENCE_POINTS,  50,  30,  "KNOWLEDGE"),
    ENLIGHTENED     ("Enlightened",          "Earn 200 science points",         QuestType.SCIENCE_POINTS, 200, 100,  "SCHOLAR");

    public final String displayName;
    public final String description;
    public final QuestType type;
    public final int targetValue;
    public final int scienceReward;
    public final String prerequisite;

    Quest(String displayName, String description, QuestType type,
          int targetValue, int scienceReward, String prerequisite) {
        this.displayName   = displayName;
        this.description   = description;
        this.type          = type;
        this.targetValue   = targetValue;
        this.scienceReward = scienceReward;
        this.prerequisite  = prerequisite;
    }

    /** Check if this quest's prerequisite is complete. */
    public boolean isUnlocked(ColonyData colony) {
        if (prerequisite == null) return true;
        return colony.isQuestCompleted(prerequisite);
    }

    /** Get current progress value for this quest. */
    public int getProgress(ColonyData colony, ServerWorld world) {
        return switch (type) {
            case BANNER_PLACED    -> colony.getBannerPos() != null ? 1 : 0;
            case STOCKPILE_PLACED -> colony.getStockpilePos() != null ? 1 : 0;
            case BED_COUNT        -> colony.getPopulationCap();
            case COLONIST_COUNT   -> colony.getColonistCount();
            case FOOD_UNITS       -> colony.getFoodUnits();
            case WAVES_SURVIVED   -> colony.getDaysSurvived();
            case SCIENCE_POINTS   -> colony.getSciencePoints();
            case GUARD_COUNT -> {
                int count = 0;
                for (UUID uuid : colony.getColonistUuids()) {
                    if (world.getEntity(uuid) instanceof GuardEntity) count++;
                }
                yield count;
            }
            case ASSIGNED_JOBS -> {
                int count = 0;
                for (UUID uuid : colony.getColonistUuids()) {
                    var entity = world.getEntity(uuid);
                    if (entity instanceof ColonistEntity ce && ce.getColonistJob() != ColonistJob.UNEMPLOYED) count++;
                    else if (entity instanceof GuardEntity) count++;
                }
                yield count;
            }
        };
    }

    /** Check completion without marking. */
    public boolean isComplete(ColonyData colony, ServerWorld world) {
        return getProgress(colony, world) >= targetValue;
    }

    public enum QuestType {
        BANNER_PLACED, STOCKPILE_PLACED, BED_COUNT, COLONIST_COUNT,
        ASSIGNED_JOBS, FOOD_UNITS, WAVES_SURVIVED, GUARD_COUNT, SCIENCE_POINTS
    }
}
