package com.thomas7520.remindtimerhud.screens.clock;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thomas7520.remindtimerhud.RemindTimerHUD;
import com.thomas7520.remindtimerhud.object.Clock;
import com.thomas7520.remindtimerhud.util.HUDMode;
import com.thomas7520.remindtimerhud.util.RemindTimerConfig;
import com.thomas7520.remindtimerhud.util.RemindTimerUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ClockPositionScreen extends Screen {

    private Screen lastScreen;
    public Clock clock;
    private boolean clicked;
    private double percentageX;
    private double percentageY;
    private double x;
    private double y;

    public ClockPositionScreen(Screen lastScreen) {
        super(Component.empty());

        this.lastScreen = lastScreen;
        this.clock = RemindTimerHUD.getClock();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if(pMouseX >= x && pMouseX < x + font.width(clock.getFormatText()) + 3
                && pMouseY >= y && pMouseY < y + 12) {
            System.out.println("tt");
            if (!clicked) {
                clicked = true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if(clicked) {
            clicked = false;
            RemindTimerConfig.Client.Clock configClock = RemindTimerConfig.CLIENT.clock;
            configClock.posX.set(clock.getPosX());
            configClock.posY.set(clock.getPosY());
        }

        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float p_282465_) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, p_282465_);

        if(clicked) {
            percentageX = (double) mouseX / minecraft.getWindow().getGuiScaledWidth() * 100;
            percentageY = (double) mouseY / minecraft.getWindow().getGuiScaledHeight() * 100;

            percentageX = Math.min(100, Math.max(0, percentageX));
            percentageY = Math.min(100, Math.max(0, percentageY));

            clock.setPosX(percentageX);
            clock.setPosY(percentageY);
        }

        for(int i = 1; i < 4; i++) {
            graphics.fill((width / 4) * (i), 0, (width / 4) * (i) + 1, height, Color.RED.getRGB());

            graphics.fill(0, (height / 4) * (i), width, (height / 4) * (i) + 1, Color.RED.getRGB());
        }

        x = (float) (clock.getPosX() / 100.0 * minecraft.getWindow().getGuiScaledWidth());
        y = (float) (clock.getPosY() / 100.0 * minecraft.getWindow().getGuiScaledHeight());

        RemindTimerUtil.renderClock(clock, graphics, font, x, y, width, height);

    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {

        float xOffset = 0;
        float yOffset = 0;


        switch (pKeyCode) {
            case GLFW.GLFW_KEY_LEFT -> xOffset-=0.5f;
            case GLFW.GLFW_KEY_RIGHT -> xOffset+=0.5f;
            case GLFW.GLFW_KEY_UP -> yOffset-=0.5f;
            case GLFW.GLFW_KEY_DOWN -> yOffset+=0.5f;
        }

        x = (float) ((clock.getPosX()+xOffset) / 100.0 * minecraft.getWindow().getGuiScaledWidth());
        y = (float) ((clock.getPosY()+yOffset) / 100.0 * minecraft.getWindow().getGuiScaledHeight());

        int rectWidth = font.width(clock.getDateFormatted()) + 3;
        int rectHeight = 12;

        x = Math.max(0, x);
        x = Math.min(width - rectWidth, x);

        y = Math.max(y, 0);
        y = Math.min(y, height - rectHeight);

        percentageX = x / minecraft.getWindow().getGuiScaledWidth() * 100;
        percentageY = y / minecraft.getWindow().getGuiScaledHeight() * 100;

        percentageX = Math.min(100, Math.max(0, percentageX));
        percentageY = Math.min(100, Math.max(0, percentageY));

        clock.setPosX(percentageX);
        clock.setPosY(percentageY);

        RemindTimerConfig.Client.Clock configClock = RemindTimerConfig.CLIENT.clock;
        configClock.posX.set(clock.getPosX());
        configClock.posY.set(clock.getPosY());

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
}
