package cz.wux.colonycraft.entity.goal;

import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.entity.ColonistEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;

/**
 * Makes SHEPHERD and COW_HERDER colonists tend their animals:
 * - Find animals in work area
 * - Shear unsheared sheep (SHEPHERD)
 * - Breed animals when below threshold
 * - Collect resources and deposit in stockpile
 */
public class TendAnimalsGoal extends Goal {

    private static final int WORK_TICKS = 30;
    private static final int COOLDOWN = 60;
    private static final int BREED_THRESHOLD = 6;

    private final ColonistEntity colonist;
    private AnimalEntity targetAnimal;
    private int workTick;
    private boolean breeding;

    public TendAnimalsGoal(ColonistEntity colonist) {
        this.colonist = colonist;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        ColonistJob job = colonist.getColonistJob();
        if (job != ColonistJob.SHEPHERD && job != ColonistJob.COW_HERDER) return false;
        if (colonist.isWorkCoolingDown() || colonist.isHungry() || colonist.isNight()) return false;
        return colonist.getStockpile().isPresent();
    }

    @Override
    public void start() {
        targetAnimal = findTarget();
        workTick = 0;
        breeding = false;
        if (targetAnimal != null) {
            colonist.getNavigation().startMovingTo(targetAnimal, 0.9);
        }
    }

    @Override
    public void tick() {
        if (targetAnimal == null || !targetAnimal.isAlive()) {
            targetAnimal = findTarget();
            if (targetAnimal != null) {
                colonist.getNavigation().startMovingTo(targetAnimal, 0.9);
                workTick = 0;
            }
            return;
        }

        double distSq = colonist.squaredDistanceTo(targetAnimal);
        if (distSq > 6.25) {
            if (!colonist.getNavigation().isFollowingPath()) {
                colonist.getNavigation().startMovingTo(targetAnimal, 0.9);
            }
            return;
        }

        // Set descriptive status
        ColonistJob job = colonist.getColonistJob();
        if (job == ColonistJob.SHEPHERD) {
            colonist.setCurrentStatus(breeding ? "\u2764 Breeding sheep" : "\u2702 Shearing sheep");
        } else {
            colonist.setCurrentStatus(breeding ? "\u2764 Breeding cows" : "\uD83D\uDC04 Tending cows");
        }

        workTick++;
        if (workTick % 5 == 0) {
            colonist.swingHand(Hand.MAIN_HAND);
            colonist.getLookControl().lookAt(targetAnimal);
        }

        if (workTick >= WORK_TICKS) {
            performAction();
            workTick = 0;
            targetAnimal = null;
            colonist.startWorkCooldown(COOLDOWN);
        }
    }

    private void performAction() {
        ColonistJob job = colonist.getColonistJob();

        if (job == ColonistJob.SHEPHERD && targetAnimal instanceof SheepEntity sheep) {
            if (!sheep.isSheared()) {
                sheep.setSheared(true);
                int woolCount = 1 + colonist.getEntityWorld().getRandom().nextInt(3);
                colonist.getStockpile().ifPresent(s ->
                    s.insertItem(new ItemStack(Items.WHITE_WOOL, woolCount)));
            } else if (breeding) {
                colonist.getStockpile().ifPresent(s -> s.withdrawItem(Items.WHEAT, 2));
                // Trigger love mode on 2 nearby sheep
                World world = colonist.getEntityWorld();
                Box box = getSearchBox();
                if (box != null) {
                    List<SheepEntity> sheep2 = world.getEntitiesByClass(SheepEntity.class, box,
                            s -> !s.isBaby() && s.getBreedingAge() == 0);
                    int bred = 0;
                    for (SheepEntity s : sheep2) {
                        if (bred >= 2) break;
                        s.setBreedingAge(-6000); // prevent immediate re-breeding
                        bred++;
                    }
                }
            }
        } else if (job == ColonistJob.COW_HERDER && targetAnimal instanceof CowEntity) {
            if (breeding) {
                colonist.getStockpile().ifPresent(s -> s.withdrawItem(Items.WHEAT, 2));
                World world = colonist.getEntityWorld();
                Box box = getSearchBox();
                if (box != null) {
                    List<CowEntity> cows = world.getEntitiesByClass(CowEntity.class, box,
                            c -> !c.isBaby() && c.getBreedingAge() == 0);
                    int bred = 0;
                    for (CowEntity c : cows) {
                        if (bred >= 2) break;
                        c.setBreedingAge(-6000);
                        bred++;
                    }
                }
            } else {
                // Produce leather from tending
                colonist.getStockpile().ifPresent(s ->
                    s.insertItem(new ItemStack(Items.LEATHER, 1)));
            }
        }
    }

    private AnimalEntity findTarget() {
        Box searchBox = getSearchBox();
        if (searchBox == null) return null;
        ColonistJob job = colonist.getColonistJob();
        World world = colonist.getEntityWorld();

        if (job == ColonistJob.SHEPHERD) {
            List<SheepEntity> sheep = world.getEntitiesByClass(SheepEntity.class, searchBox, e -> !e.isBaby());
            // Priority: unsheared sheep
            SheepEntity unsheared = sheep.stream().filter(s -> !s.isSheared()).findFirst().orElse(null);
            if (unsheared != null) {
                breeding = false;
                return unsheared;
            }
            // If all sheared and few sheep, breed
            if (sheep.size() < BREED_THRESHOLD && sheep.size() >= 2) {
                boolean hasWheat = colonist.getStockpile().map(s -> s.hasItem(Items.WHEAT, 2)).orElse(false);
                if (hasWheat) {
                    breeding = true;
                    return sheep.get(0);
                }
            }
            // Just tend any sheep
            if (!sheep.isEmpty()) {
                breeding = false;
                return sheep.get(world.getRandom().nextInt(sheep.size()));
            }
        } else if (job == ColonistJob.COW_HERDER) {
            List<CowEntity> cows = world.getEntitiesByClass(CowEntity.class, searchBox, e -> !e.isBaby());
            if (cows.size() < BREED_THRESHOLD && cows.size() >= 2) {
                boolean hasWheat = colonist.getStockpile().map(s -> s.hasItem(Items.WHEAT, 2)).orElse(false);
                if (hasWheat) {
                    breeding = true;
                    return cows.get(0);
                }
            }
            if (!cows.isEmpty()) {
                breeding = false;
                return cows.get(world.getRandom().nextInt(cows.size()));
            }
        }
        return null;
    }

    private Box getSearchBox() {
        var jobBlock = colonist.getJobBlock();
        if (jobBlock.isPresent() && jobBlock.get().hasArea()) {
            BlockPos min = jobBlock.get().getAreaMin();
            BlockPos max = jobBlock.get().getAreaMax();
            return new Box(min.getX(), min.getY() - 2, min.getZ(),
                           max.getX() + 1, max.getY() + 4, max.getZ() + 1);
        }
        BlockPos center = colonist.getJobBlockPos();
        if (center == null) center = colonist.getHomePos();
        if (center == null) return null;
        return new Box(center.getX() - 16, center.getY() - 4, center.getZ() - 16,
                       center.getX() + 17, center.getY() + 5, center.getZ() + 17);
    }

    @Override
    public boolean shouldContinue() {
        return !colonist.isWorkCoolingDown() && !colonist.isHungry() && !colonist.isNight();
    }

    @Override
    public void stop() {
        targetAnimal = null;
        workTick = 0;
        colonist.getNavigation().stop();
    }
}
