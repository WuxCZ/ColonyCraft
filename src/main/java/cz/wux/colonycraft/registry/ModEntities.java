package cz.wux.colonycraft.registry;

import cz.wux.colonycraft.entity.ColonistEntity;
import cz.wux.colonycraft.entity.ColonyMonsterEntity;
import cz.wux.colonycraft.entity.GuardEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEntities {

    public static final EntityType<ColonistEntity> COLONIST =
            Registry.register(Registries.ENTITY_TYPE,
                    Identifier.of("colonycraft", "colonist"),
                    FabricEntityTypeBuilder.<ColonistEntity>create(SpawnGroup.CREATURE, ColonistEntity::new)
                            .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
                            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("colonycraft", "colonist"))));

    public static final EntityType<GuardEntity> GUARD =
            Registry.register(Registries.ENTITY_TYPE,
                    Identifier.of("colonycraft", "guard"),
                    FabricEntityTypeBuilder.<GuardEntity>create(SpawnGroup.CREATURE, GuardEntity::new)
                            .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
                            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("colonycraft", "guard"))));

    public static final EntityType<ColonyMonsterEntity> COLONY_MONSTER =
            Registry.register(Registries.ENTITY_TYPE,
                    Identifier.of("colonycraft", "colony_monster"),
                    FabricEntityTypeBuilder.<ColonyMonsterEntity>create(SpawnGroup.MONSTER, ColonyMonsterEntity::new)
                            .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
                            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("colonycraft", "colony_monster"))));

    public static void initialize() {}
}
