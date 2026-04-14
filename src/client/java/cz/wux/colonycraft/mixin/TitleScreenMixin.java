package cz.wux.colonycraft.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void colonycraft$renderBranding(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Version + mod name in bottom-left, below the MC copyright
        String ver = "\u00a76ColonyCraft \u00a78v1.0.0 \u00a77| Colony Survival in Minecraft";
        context.drawText(this.textRenderer, Text.literal(ver),
                4, this.height - 10, 0xFFAAAAAA, false);
    }
}
