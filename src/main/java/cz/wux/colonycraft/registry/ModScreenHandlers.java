package cz.wux.colonycraft.registry;

import cz.wux.colonycraft.screen.ColonyBannerScreenHandler;
import cz.wux.colonycraft.screen.ResearchScreenHandler;
import cz.wux.colonycraft.screen.StockpileScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {

    public static final ScreenHandlerType<StockpileScreenHandler> STOCKPILE =
            Registry.register(Registries.SCREEN_HANDLER,
                    Identifier.of("colonycraft", "stockpile"),
                    new ScreenHandlerType<>(StockpileScreenHandler::new, FeatureSet.empty()));

    public static final ScreenHandlerType<ColonyBannerScreenHandler> COLONY_BANNER =
            Registry.register(Registries.SCREEN_HANDLER,
                    Identifier.of("colonycraft", "colony_banner"),
                    new ScreenHandlerType<>(ColonyBannerScreenHandler::new, FeatureSet.empty()));

    public static final ScreenHandlerType<ResearchScreenHandler> RESEARCH =
            Registry.register(Registries.SCREEN_HANDLER,
                    Identifier.of("colonycraft", "research"),
                    new ScreenHandlerType<>(ResearchScreenHandler::new, FeatureSet.empty()));

    public static void initialize() {}
}
