package cz.wux.colonycraft.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique private static final Random RAND = new Random(42);
    @Unique private long openTime = -1;
    @Unique private final List<float[]> particles = new ArrayList<>();
    @Unique private boolean particlesInit = false;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void colonycraft$renderBranding(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (openTime < 0) openTime = System.currentTimeMillis();
        long elapsed = System.currentTimeMillis() - openTime;
        float t = elapsed / 1000.0f;

        // ── Initialize floating particles ──
        if (!particlesInit) {
            particlesInit = true;
            for (int i = 0; i < 35; i++) {
                // x, y, speedX, speedY, size, alpha, hue(0=gold,1=orange,2=red)
                particles.add(new float[]{
                    RAND.nextFloat() * this.width,
                    RAND.nextFloat() * this.height,
                    (RAND.nextFloat() - 0.5f) * 0.4f,
                    -0.2f - RAND.nextFloat() * 0.6f,
                    1.5f + RAND.nextFloat() * 2.5f,
                    0.15f + RAND.nextFloat() * 0.4f,
                    RAND.nextInt(3)
                });
            }
        }

        // ── Cover Minecraft logo completely, then add cinematic gradient ──
        context.fill(0, 0, this.width, 80, 0xFF0A0A0A); // fully opaque cover over MC logo
        context.fillGradient(0, 80, this.width, 120, 0xFF0A0A0A, 0x000A0A0A); // smooth fade
        context.fillGradient(0, this.height - 50, this.width, this.height, 0x000A0A0A, 0xCC0A0A0A);

        // ── Animated floating particles (embers/fireflies) ──
        for (float[] p : particles) {
            p[0] += p[2];
            p[1] += p[3];
            // Wrap around
            if (p[1] < -10) { p[1] = this.height + 10; p[0] = RAND.nextFloat() * this.width; }
            if (p[0] < -10) p[0] = this.width + 10;
            if (p[0] > this.width + 10) p[0] = -10;

            float flickerAlpha = p[5] * (0.7f + 0.3f * (float) Math.sin(t * 2.5f + p[0] * 0.01f));
            int alpha = Math.min(255, (int)(flickerAlpha * 255));
            int color;
            if (p[6] < 1) color = (alpha << 24) | 0xFFD700;       // gold
            else if (p[6] < 2) color = (alpha << 24) | 0xFF8C00;  // orange
            else color = (alpha << 24) | 0xCC3300;                 // ember red

            int px = (int) p[0], py = (int) p[1];
            int sz = (int) p[4];
            context.fill(px - sz, py - sz, px + sz, py + sz, color);
        }

        // ── Animated title scale (gentle breathe) ──
        float introScale = Math.min(1.0f, elapsed / 800.0f); // fade-in over 0.8s
        float breathe = 1.0f + 0.02f * (float) Math.sin(t * 1.5f);
        float titleScale = 3.5f * introScale * breathe;

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(this.width / 2.0f, 20.0f);
        context.getMatrices().scale(titleScale, titleScale);

        // Shadow layer (offset + darker)
        Text shadowText = Text.literal("\u00a78\u00a7lColonyCraft");
        int tw = this.textRenderer.getWidth(shadowText);
        context.drawText(this.textRenderer, shadowText, -tw / 2 + 1, 1, 0xFF222222, false);

        // Main title — golden glow
        Text titleText = Text.literal("\u00a76\u00a7lColonyCraft");
        context.drawText(this.textRenderer, titleText, -tw / 2, 0, 0xFFFFD700, true);
        context.getMatrices().popMatrix();

        // ── Subtitle with typewriter reveal ──
        String fullSub = "\u00a77\u00a7o\u2694 Colony Survival in Minecraft \u2694";
        int charsToShow = Math.min(fullSub.length(), (int)(elapsed / 40));
        if (charsToShow > 4) { // skip formatting codes
            Text subText = Text.literal(fullSub.substring(0, charsToShow));
            int fullW = this.textRenderer.getWidth(Text.literal(fullSub));
            int subW = this.textRenderer.getWidth(subText);
            // Center based on full width
            int subX = (this.width - fullW) / 2;
            context.drawText(this.textRenderer, subText, subX, 58, 0xFFBBBBBB, true);
        }

        // ── Decorative golden borders ──
        // Top thin line
        int goldAlpha = (int)(180 + 40 * Math.sin(t * 2.0));
        int gold = (goldAlpha << 24) | 0xDAA520;
        context.fill(this.width / 4, 75, this.width * 3 / 4, 76, gold);

        // Corner ornaments (small squares)
        int ornSize = 3;
        context.fill(this.width / 4 - ornSize, 75 - ornSize, this.width / 4 + ornSize, 75 + ornSize, gold);
        context.fill(this.width * 3 / 4 - ornSize, 75 - ornSize, this.width * 3 / 4 + ornSize, 75 + ornSize, gold);

        // ── Bottom bar — animated gradient accent ──
        float hueShift = (t * 0.3f) % 1.0f;
        int r1 = (int)(180 + 38 * Math.sin(hueShift * 6.28));
        int g1 = (int)(140 + 25 * Math.sin(hueShift * 6.28 + 2.0));
        int bottomColor = 0xBB000000 | (r1 << 16) | (g1 << 8) | 0x20;
        context.fill(0, this.height - 3, this.width, this.height, bottomColor);
        context.fill(0, this.height - 1, this.width, this.height, 0x88FFD700);

        // ── Version + credits bottom ──
        float verAlpha = Math.min(1.0f, Math.max(0.0f, (elapsed - 1500) / 1000.0f));
        int verColor = ((int)(verAlpha * 140) << 24) | 0x999999;
        context.drawText(this.textRenderer, Text.literal("ColonyCraft v1.0.0 \u00a78| \u00a75by Wux"),
                4, this.height - 14, verColor, true);

        // ── Tip text (cycles) ──
        if (elapsed > 2000) {
            String[] tips = {
                "\u00a7e\u00a7oPlace a Colony Banner to start your colony!",
                "\u00a7e\u00a7oPress ; to open Colony Management",
                "\u00a7e\u00a7oGuards defend your colony at night",
                "\u00a7e\u00a7o27+ jobs for your colonists",
                "\u00a7e\u00a7oSurvive the nightly monster waves!"
            };
            int tipIdx = (int)((elapsed / 4000) % tips.length);
            float tipAlpha = 0.5f + 0.5f * (float) Math.sin(t * 1.2f);
            Text tipText = Text.literal(tips[tipIdx]);
            int tipW = this.textRenderer.getWidth(tipText);
            int tipColor = ((int)(tipAlpha * 200) << 24) | 0xFFDD44;
            context.drawText(this.textRenderer, tipText, (this.width - tipW) / 2, this.height - 28, tipColor, true);
        }
    }
}
