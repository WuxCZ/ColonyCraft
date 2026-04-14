package cz.wux.colonycraft.registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * Custom sound events for ColonyCraft.
 * We register our own IDs but all sounds point to vanilla .ogg entries
 * via sounds.json (or fall back to vanilla SoundEvents directly in code).
 */
public class ModSounds {

    public static final SoundEvent COLONIST_WORK  = register("colonist.work");
    public static final SoundEvent COLONIST_SPAWN = register("colonist.spawn");
    public static final SoundEvent WAVE_HORN      = register("wave.horn");

    private static SoundEvent register(String name) {
        Identifier id = Identifier.of("colonycraft", name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void initialize() {}
}
