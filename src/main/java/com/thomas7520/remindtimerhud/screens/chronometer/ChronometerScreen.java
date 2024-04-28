package com.thomas7520.remindtimerhud.screens.chronometer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thomas7520.remindtimerhud.RemindTimerHUD;
import com.thomas7520.remindtimerhud.object.Chronometer;
import com.thomas7520.remindtimerhud.screens.clock.PositionScreen;
import com.thomas7520.remindtimerhud.util.ButtonDropDown;
import com.thomas7520.remindtimerhud.util.ChronometerFormat;
import com.thomas7520.remindtimerhud.util.HUDMode;
import com.thomas7520.remindtimerhud.util.RemindTimerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.widget.ForgeSlider;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChronometerScreen extends Screen {


    private final Chronometer chronometer;
    private int guiLeft;
    private int guiTop;

    private String chronometerFormatted;

    private final String[] stateValues = {"text.red", "text.green", "text.blue", "text.alpha"};
    private ForgeSlider sliderRedText, sliderGreenText, sliderBlueText, sliderAlphaText, sliderRGBText;
    private ForgeSlider sliderRedBackground, sliderGreenBackground, sliderBlueBackground, sliderAlphaBackground, sliderRGBBackground;
    private final Screen lastScreen;
    private double waveCounterText;
    private double waveCounterBackground;

    private Button buttonWaveDirection;
    private Button buttonWaveDirectionBackground;
    private Button buttonBackgroundState;
    private long test;
    private int tickElapsed;
    private ButtonDropDown predefineFormatsButton;

    public ChronometerScreen(Screen lastScreen, Chronometer Chronometer) {
        super(Component.translatable("chronometer.title"));
        this.lastScreen = lastScreen;
        this.chronometer = Chronometer;
    }



    @Override
    protected void init() {
        this.guiLeft = (this.width / 2);
        this.guiTop = (this.height / 2);

        chronometer.setStartTime(System.currentTimeMillis());


        List<ButtonDropDown.Entry> formatEntries = new ArrayList<>();

        for (ChronometerFormat value : ChronometerFormat.values()) {
            formatEntries.add(new ButtonDropDown.Entry(value.name(), pEntry -> {
                chronometer.setFormat(ChronometerFormat.valueOf(pEntry.getName()));
                predefineFormatsButton.setMessage(Component.literal(value.name()));
            }));
        }

        predefineFormatsButton = ButtonDropDown.builder(Component.literal(chronometer.getFormat().name()))
                .bounds(0,0, 154, 20)
                .addEntries(formatEntries)
                .build();

        Button displayMode = Button.builder(Component.translatable("text.display_position"), pButton -> {
                    minecraft.setScreen(new PositionScreen());
                }).bounds(0,0, 154, 20)
                .build();

        Button rgbTextMode = Button.builder(Component.translatable("text.text_mode", chronometer.getRgbModeText().name()), pButton -> {
                    chronometer.setRgbModeText(getNextMode(chronometer.getRgbModeText()));
                    sliderRGBText.visible = chronometer.getRgbModeText() == HUDMode.WAVE || chronometer.getRgbModeText() == HUDMode.CYCLE;
                    buttonWaveDirection.visible = chronometer.getRgbModeText() == HUDMode.WAVE;
                    pButton.setMessage(Component.translatable("text.text_mode", chronometer.getRgbModeText().name()));
                }).bounds(0,0, 154, 20)
                .build();

        Button rgbBackgroundMode = Button.builder(Component.translatable("text.background_mode", chronometer.getRgbModeBackground().name()), pButton -> {
                    chronometer.setRgbModeBackground(getNextMode(chronometer.getRgbModeBackground()));
                    sliderRGBBackground.visible = chronometer.getRgbModeBackground() == HUDMode.WAVE || chronometer.getRgbModeBackground() == HUDMode.CYCLE;
                    buttonWaveDirectionBackground.visible = chronometer.getRgbModeBackground() == HUDMode.WAVE;
                    pButton.setMessage(Component.translatable("text.background_mode", chronometer.getRgbModeBackground().name()));
                }).bounds(0,0, 154, 20)
                .build();

        buttonWaveDirection = Button.builder(Component.translatable(chronometer.isTextRightToLeftDirection() ? "text.direction_lr" : "text.direction_rl"), pButton -> {
                    chronometer.setTextRightToLeftDirection(!chronometer.isTextRightToLeftDirection());
                    pButton.setMessage(Component.translatable(chronometer.isTextRightToLeftDirection() ? "text.direction_lr" : "text.direction_rl"));
        }).bounds(0,0, 100, 20)
                .build();

        buttonWaveDirection.visible = chronometer.getRgbModeText() == HUDMode.WAVE;

        buttonWaveDirectionBackground = Button.builder(Component.translatable(chronometer.isTextRightToLeftDirection() ? "text.direction_lr" : "text.direction_rl"), pButton -> {
                    chronometer.setBackgroundRightToLeftDirection(!chronometer.isBackgroundRightToLeftDirection());
                    pButton.setMessage(Component.translatable(chronometer.isBackgroundRightToLeftDirection() ? "text.direction_lr" : "text.direction_rl"));
                }).bounds(0,0, 100, 20)
                .build();

        buttonWaveDirectionBackground.visible = chronometer.getRgbModeBackground() == HUDMode.WAVE;


        buttonBackgroundState = Button.builder(Component.translatable(chronometer.isDrawBackground() ? "text.disable_background" : "text.enable_background"), pButton -> {
                    chronometer.setDrawBackground(!chronometer.isDrawBackground());
                    pButton.setMessage(Component.translatable(chronometer.isDrawBackground() ? "text.disable_background" : "text.enable_background"));

                    chronometer.setStop(chronometer.isDrawBackground());
                }).bounds(0,0, 154, 20)
                .build();


        int i = 1;
        sliderRedText = new ForgeSlider(0,0, 100, 20, Component.literal(Component.translatable(stateValues[i-1]).getString() + " : "), Component.empty()
                , 0, 255, chronometer.getRedText(), 1, 1, true) {


            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                final Minecraft mc = Minecraft.getInstance();
                guiGraphics.blitWithBorder(SLIDER_LOCATION, this.getX(), this.getY(), 0, getTextureY(), this.width, this.height, 200, 20, 2, 3, 2, 2);

                int rgb = (sliderRedText.getValueInt() << 16 | sliderGreenText.getValueInt() << 8 | sliderBlueText.getValueInt());

                int col1 = rgb | 0xff000000;
                drawGradientRect(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, 0,col1 & 0xff00ffff, col1 | 0x00ff0000, col1 & 0xff00ffff,
                        col1 | 0x00ff0000);

                guiGraphics.blitWithBorder(SLIDER_LOCATION, this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 0, getHandleTextureY(), 8, this.height, 200, 20 , 2, 3, 2, 2);

                renderScrollingString(guiGraphics, mc.font, 2, getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24);
            }

            @Override
            protected void applyValue() {
                chronometer.setRedText(getValueInt());
                super.applyValue();
            }

        };

        i++;

        sliderGreenText = new ForgeSlider(0,0, 100, 20, Component.literal(Component.translatable(stateValues[i-1]).getString() + " : "), Component.empty()
                , 0, 255, chronometer.getGreenText(), 1, 1, true) {

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                final Minecraft mc = Minecraft.getInstance();
                guiGraphics.blitWithBorder(SLIDER_LOCATION, this.getX(), this.getY(), 0, getTextureY(), this.width, this.height, 200, 20, 2, 3, 2, 2);

                int rgb = (sliderRedText.getValueInt() << 16 | sliderGreenText.getValueInt() << 8 | sliderBlueText.getValueInt());

                int col1 = rgb | 0xff000000;
                drawGradientRect(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, 0,col1 & 0xffff00ff, col1 | 0x0000ff00, col1 & 0xffff00ff,
                        col1 | 0x0000ff00);

                guiGraphics.blitWithBorder(SLIDER_LOCATION, this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 0, getHandleTextureY(), 8, this.height, 200, 20 , 2, 3, 2, 2);

                renderScrollingString(guiGraphics, mc.font, 2, getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24);
            }

            @Override
            protected void applyValue() {
                chronometer.setGreenText(getValueInt());
                super.applyValue();
            }
        };

        i++;

        sliderBlueText = new ForgeSlider(0,0, 100, 20, Component.literal(Component.translatable(stateValues[i-1]).getString() + " : "), Component.empty()
                , 0, 255, chronometer.getBlueText(), 1, 1, true) {

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                final Minecraft mc = Minecraft.getInstance();
                guiGraphics.blitWithBorder(SLIDER_LOCATION, this.getX(), this.getY(), 0, getTextureY(), this.width, this.height, 200, 20, 2, 3, 2, 2);

                int rgb = (sliderRedText.getValueInt() << 16 | sliderGreenText.getValueInt() << 8 | sliderBlueText.getValueInt());

                int col1 = rgb | 0xff000000;
                drawGradientRect(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, 0,col1 & 0xffffff00, col1 | 0x000000ff, col1 & 0xffffff00,
                        col1 | 0x000000ff);


                guiGraphics.blitWithBorder(SLIDER_LOCATION, this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 0, getHandleTextureY(), 8, this.height, 200, 20 , 2, 3, 2, 2);

                renderScrollingString(guiGraphics, mc.font, 2, getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24);
            }

            @Override
            protected void applyValue() {
                chronometer.setBlueText(getValueInt());
                super.applyValue();
            }
        };

        i++;




        sliderAlphaText = new ForgeSlider(0,0, 100, 20, Component.literal(Component.translatable(stateValues[i-1]).getString() + " : "), Component.literal("%")
                , 0, 100, (100 * (chronometer.getAlphaText()-25)) / (255.0 - 25), 1, 1, true) {

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                final Minecraft mc = Minecraft.getInstance();
                guiGraphics.blitWithBorder(SLIDER_LOCATION, this.getX(), this.getY(), 0, getTextureY(), this.width, this.height, 200, 20, 2, 3, 2, 2);

                int rgb = (sliderRedText.getValueInt() << 16 | sliderGreenText.getValueInt() << 8 | sliderBlueText.getValueInt());

                RenderSystem.setShaderColor(1,1,1,1);
                guiGraphics.blit(new ResourceLocation(RemindTimerHUD.MODID, "textures/transparency.png"), getX() + 1, getY() + 1, 0, 0F, 0F, getWidth() - 2, getHeight() - 2,  10,10);

                drawGradientRect(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, 0,rgb |  (0x46 << 24), rgb | (0xFF << 24), rgb | (0x46 << 24), rgb | (0xFF << 24));
                RenderSystem.setShaderColor(1,1,1,1);

                guiGraphics.blitWithBorder(SLIDER_LOCATION, this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 0, getHandleTextureY(), 8, this.height, 200, 20 , 2, 3, 2, 2);

                renderScrollingString(guiGraphics, mc.font, 2, getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24);
            }

            @Override
            public int getValueInt() {
                return (int) (((float) super.getValueInt() / 100) * (255 - 25) + 25);
            }

            @Override
            protected void applyValue() {
                chronometer.setAlphaText(getValueInt());
                super.applyValue();
            }
        };


        i++;

        sliderRGBText = new ForgeSlider(0,0, 100, 20, Component.literal(Component.translatable("text.speed").getString() + " : "), Component.literal("%")
                , 1, 100, chronometer.getRgbSpeedText(), 1, 1, true) {

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            }


            @Override
            protected void applyValue() {
                chronometer.setRgbSpeedText(getValueInt());
                super.applyValue();
            }
        };

        sliderRGBText.visible = chronometer.getRgbModeText() == HUDMode.WAVE || chronometer.getRgbModeText() == HUDMode.CYCLE;

        i = 1;
        sliderRedBackground = new ForgeSlider(0,0, 100, 20, Component.literal(Component.translatable(stateValues[i-1]).getString() + " : "), Component.empty()
                , 0, 255, chronometer.getRedBackground(), 1, 1, true) {


            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                final Minecraft mc = Minecraft.getInstance();
                guiGraphics.blitWithBorder(SLIDER_LOCATION, this.getX(), this.getY(), 0, getTextureY(), this.width, this.height, 200, 20, 2, 3, 2, 2);

                int rgb = (sliderRedBackground.getValueInt() << 16 | sliderGreenBackground.getValueInt() << 8 | sliderBlueBackground.getValueInt());

                int col1 = rgb | 0xff000000;
                drawGradientRect(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, 0,col1 & 0xff00ffff, col1 | 0x00ff0000, col1 & 0xff00ffff,
                        col1 | 0x00ff0000);

                guiGraphics.blitWithBorder(SLIDER_LOCATION, this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 0, getHandleTextureY(), 8, this.height, 200, 20 , 2, 3, 2, 2);

                renderScrollingString(guiGraphics, mc.font, 2, getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24);
            }

            @Override
            protected void applyValue() {
                chronometer.setRedBackground(getValueInt());
                super.applyValue();
            }
        };

        i++;

        sliderGreenBackground = new ForgeSlider(0,0, 100, 20, Component.literal(Component.translatable(stateValues[i-1]).getString() + " : "), Component.empty()
                , 0, 255, chronometer.getGreenBackground(), 1, 1, true) {

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                final Minecraft mc = Minecraft.getInstance();
                guiGraphics.blitWithBorder(SLIDER_LOCATION, this.getX(), this.getY(), 0, getTextureY(), this.width, this.height, 200, 20, 2, 3, 2, 2);

                int rgb = (sliderRedBackground.getValueInt() << 16 | sliderGreenBackground.getValueInt() << 8 | sliderBlueBackground.getValueInt());

                int col1 = rgb | 0xff000000;
                drawGradientRect(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, 0,col1 & 0xffff00ff, col1 | 0x0000ff00, col1 & 0xffff00ff,
                        col1 | 0x0000ff00);

                guiGraphics.blitWithBorder(SLIDER_LOCATION, this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 0, getHandleTextureY(), 8, this.height, 200, 20 , 2, 3, 2, 2);

                renderScrollingString(guiGraphics, mc.font, 2, getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24);
            }

            @Override
            protected void applyValue() {
                chronometer.setGreenBackground(getValueInt());
                super.applyValue();
            }
        };

        i++;

        sliderBlueBackground = new ForgeSlider(0,0, 100, 20, Component.literal(Component.translatable(stateValues[i-1]).getString() + " : "), Component.empty()
                , 0, 255, chronometer.getBlueBackground(), 1, 1, true) {

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                final Minecraft mc = Minecraft.getInstance();
                guiGraphics.blitWithBorder(SLIDER_LOCATION, this.getX(), this.getY(), 0, getTextureY(), this.width, this.height, 200, 20, 2, 3, 2, 2);

                int rgb = (sliderRedBackground.getValueInt() << 16 | sliderGreenBackground.getValueInt() << 8 | sliderBlueBackground.getValueInt());

                int col1 = rgb | 0xff000000;
                drawGradientRect(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, 0,col1 & 0xffffff00, col1 | 0x000000ff, col1 & 0xffffff00,
                        col1 | 0x000000ff);


                guiGraphics.blitWithBorder(SLIDER_LOCATION, this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 0, getHandleTextureY(), 8, this.height, 200, 20 , 2, 3, 2, 2);

                renderScrollingString(guiGraphics, mc.font, 2, getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24);
            }

            @Override
            protected void applyValue() {
                chronometer.setBlueBackground(getValueInt());
                super.applyValue();
            }
        };

        i++;



        sliderAlphaBackground = new ForgeSlider(0,0, 100, 20, Component.literal(Component.translatable(stateValues[i-1]).getString() + " : "), Component.literal("%")
                , 0, 100, (100 * chronometer.getAlphaBackground()) / 255.0, 1, 1, true) {

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                final Minecraft mc = Minecraft.getInstance();
                guiGraphics.blitWithBorder(SLIDER_LOCATION, this.getX(), this.getY(), 0, getTextureY(), this.width, this.height, 200, 20, 2, 3, 2, 2);

                int rgb = (sliderRedBackground.getValueInt() << 16 | sliderGreenBackground.getValueInt() << 8 | sliderBlueBackground.getValueInt());

                RenderSystem.setShaderColor(1,1,1,1);
                guiGraphics.blit(new ResourceLocation(RemindTimerHUD.MODID, "textures/transparency.png"), getX() + 1, getY() + 1, 0, 0F, 0F, getWidth() - 2, getHeight() - 2,  10,10);

                drawGradientRect(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, 0,rgb |  (0x46 << 24), rgb | (0xFF << 24), rgb | (0x46 << 24), rgb | (0xFF << 24));
                RenderSystem.setShaderColor(1,1,1,1);

                guiGraphics.blitWithBorder(SLIDER_LOCATION, this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 0, getHandleTextureY(), 8, this.height, 200, 20 , 2, 3, 2, 2);

                renderScrollingString(guiGraphics, mc.font, 2, getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24);
            }

            @Override
            public int getValueInt() {
                return (int) (((float) super.getValueInt() / 100) * (255));
            }

            @Override
            protected void applyValue() {
                chronometer.setAlphaBackground(getValueInt());
                super.applyValue();
            }
        };

        sliderRGBBackground = new ForgeSlider(0,0, 100, 20, Component.literal(Component.translatable("text.speed").getString() + " : "), Component.literal("%")
                , 1, 100, chronometer.getRgbSpeedBackground(), 1, 1, true) {

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            }



            @Override
            protected void applyValue() {
                chronometer.setRgbSpeedBackground(getValueInt());
                super.applyValue();
            }
        };

        sliderRGBBackground.visible = chronometer.getRgbModeBackground() == HUDMode.WAVE || chronometer.getRgbModeBackground() == HUDMode.CYCLE;


        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_280842_) -> {
            saveConfig();
            this.minecraft.setScreen(this.lastScreen);
        }).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());


        net.minecraft.client.gui.layouts.GridLayout gridlayout = new net.minecraft.client.gui.layouts.GridLayout();
        gridlayout.defaultCellSetting().padding(4, 4, 4, 0);

        GridLayout.RowHelper gridlayout$rowhelper = gridlayout.createRowHelper(3);

        gridlayout$rowhelper.addChild(predefineFormatsButton);
        gridlayout$rowhelper.addChild(sliderRedText);
        gridlayout$rowhelper.addChild(sliderRedBackground);

        gridlayout.addChild(displayMode, 1, 0);
        gridlayout.addChild(sliderGreenText, 1, 1);
        gridlayout.addChild(sliderGreenBackground, 1, 2);

        gridlayout.addChild(rgbTextMode, 2, 0);
        gridlayout.addChild(sliderBlueText, 2, 1);
        gridlayout.addChild(sliderBlueBackground, 2, 2);


        gridlayout.addChild(rgbBackgroundMode,3,0);
        gridlayout.addChild(sliderAlphaText, 3 ,1);
        gridlayout.addChild(sliderAlphaBackground, 3,2);


        gridlayout.addChild(buttonBackgroundState, 4, 0);
        gridlayout.addChild(sliderRGBText,4,1);
        gridlayout.addChild(sliderRGBBackground,4,2);

        gridlayout.addChild(buttonWaveDirection, 5, 1);
        gridlayout.addChild(buttonWaveDirectionBackground, 5, 2);

        gridlayout.arrangeElements();



        FrameLayout.alignInRectangle(gridlayout, 0, 0, this.width, this.height, 0.5F, 0.5F);

        gridlayout.visitWidgets(this::addRenderableWidget);
        super.init();

    }


    @Override
    public void tick() {
        waveCounterText+=(sliderRGBText.getValue() - 1) / (100 - 1) * (10 - 1) + 1;
        waveCounterBackground+=(sliderRGBBackground.getValue() - 1) / (100 - 1) * (20 - 1) + 1;

        if(tickElapsed % 20 == 0) {
            test+=1;
        }

        tickElapsed++;
        super.tick();
    }


    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float p_282465_) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, p_282465_);

        if (chronometer.isStop()) {
            chronometerFormatted = chronometer.getPauseTimeCache();
        } else {
            chronometerFormatted = chronometer.getFormat().formatTime(System.currentTimeMillis() - chronometer.getStartTime());
        }

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 3, 16777215);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);


        int x = width / 2 - font.width(chronometerFormatted) / 2;
        int y = predefineFormatsButton.getY() - 20;

        double rectX = width / 2f - font.width(chronometerFormatted) / 2f - 2;
        double rectY = y - 2;
        double rectWidth = font.width(chronometerFormatted) + 3;
        double rectHeight = 12;


        if(chronometer.isDrawBackground()) {
            if (chronometer.getRgbModeBackground() == HUDMode.WAVE) {
                for (int i = 0; i < rectWidth; i++) {
                    double hueStart = 1.0F - ((i - waveCounterBackground) / 360f); // Inversion de la couleur

                    double hueEnd = 1.0F - ((i + 1 - waveCounterBackground) / 360f); // Inversion de la couleur

                    if (chronometer.isBackgroundRightToLeftDirection()) {
                        hueStart = (i + waveCounterBackground) / 360f; // Inversion de la couleur
                        hueEnd = (i + 4 + waveCounterBackground) / 360f; // Inversion de la couleur

                    }

                    int colorStart = Color.HSBtoRGB((float) hueStart, 1.0F, 1.0F);
                    int colorEnd = Color.HSBtoRGB((float) hueEnd, 1.0F, 1.0F);

                    colorStart = (colorStart & 0x00FFFFFF) | (sliderAlphaBackground.getValueInt() << 24);

                    colorEnd = (colorEnd & 0x00FFFFFF) | (sliderAlphaBackground.getValueInt() << 24);

                    // Dessiner une colonne du rectangle avec le dégradé de couleur
                    drawGradientRect((rectX + i), rectY, rectX + i + 1, rectY + rectHeight, 0, colorStart, colorEnd, colorStart, colorEnd);
                }
            } else if (chronometer.getRgbModeBackground() == HUDMode.CYCLE) {

                float hueStart = 1.0F - ((float) (waveCounterBackground) / 360f); // Inversion de la couleur

                if (chronometer.isBackgroundRightToLeftDirection()) {
                    hueStart = (float) (waveCounterBackground) / 360f; // Inversion de la couleur
                }

                float hueEnd = hueStart; // Utilisez la même couleur pour le coin opposé

                int colorStart = Color.HSBtoRGB(hueStart, 1.0F, 1.0F);
                int colorEnd = Color.HSBtoRGB(hueEnd, 1.0F, 1.0F);


                colorStart = (colorStart & 0x00FFFFFF) | (sliderAlphaBackground.getValueInt() << 24);

                colorEnd = (colorEnd & 0x00FFFFFF) | (sliderAlphaBackground.getValueInt() << 24);
                // Dessiner une colonne du rectangle avec le dégradé de couleur
                drawGradientRect(rectX, rectY, rectX + rectWidth, rectY + rectHeight, 0, colorStart, colorStart, colorEnd, colorEnd);
            } else {
                int colorBackground = (sliderAlphaBackground.getValueInt() << 24 | sliderRedBackground.getValueInt() << 16 | sliderGreenBackground.getValueInt() << 8 | sliderBlueBackground.getValueInt());
                graphics.fill(width / 2 - font.width(chronometerFormatted) / 2 - 2, y - 2, width / 2 + font.width(chronometerFormatted) / 2 + 2, y + 8 + 2, colorBackground);
            }
        }


        if(chronometer.getRgbModeText() == HUDMode.WAVE) {
            for (int i = 0; i < chronometerFormatted.length(); i++) {
                char c = chronometerFormatted.charAt(i);

                double hue = 1.0F - ((float) (chronometerFormatted.length() - i + waveCounterText) * 2 / 360f); // Inversion de la couleur

                if (chronometer.isTextRightToLeftDirection())
                    hue = (chronometerFormatted.length() + i + waveCounterText) * 2 / 360; // Inversion de la couleur

                float saturation = 1.0F;
                float brightness = 1.0F;

                int color = Color.HSBtoRGB((float) hue, saturation, brightness);

                color = (color & 0x00FFFFFF) | (sliderAlphaText.getValueInt() << 24);

                graphics.drawString(font, String.valueOf(c), x, y, color, false);
                x += minecraft.font.width(String.valueOf(c));

            }
        } else if (chronometer.getRgbModeText() == HUDMode.CYCLE) {

            float hueStart = 1.0F - ((float) (waveCounterText) / 255); // Inversion de la couleur

            if (chronometer.isTextRightToLeftDirection()) {
                hueStart = (float) (waveCounterText) / 255; // Inversion de la couleur
            }


            int color = Color.HSBtoRGB(hueStart, 1.0F, 1.0F);

            color = (color & 0x00FFFFFF) | (sliderAlphaText.getValueInt() << 24);

            graphics.drawString(font, chronometerFormatted, x, y, color, false);

        } else {
            int colorText = (sliderAlphaText.getValueInt() << 24 | sliderRedText.getValueInt() << 16 | sliderGreenText.getValueInt() << 8 | sliderBlueText.getValueInt());

            graphics.drawString(font, chronometerFormatted, x, y, colorText, false);
        }

        RenderSystem.disableBlend();

    }


    private ChronometerFormat getNextFormat(ChronometerFormat currentFormat) {
        ChronometerFormat[] values = ChronometerFormat.values();
        int currentIndex = currentFormat.ordinal();
        int nextIndex = (currentIndex + 1) % values.length;
        return values[nextIndex];
    }
    private HUDMode getNextMode(HUDMode currentOption) {
        HUDMode[] values = HUDMode.values();
        int currentIndex = currentOption.ordinal();
        int nextIndex = (currentIndex + 1) % values.length;
        return values[nextIndex];
    }

    @Override
    public void onClose() {
        saveConfig();
        super.onClose();
    }

    private void saveConfig() {
        RemindTimerConfig.Client.Chronometer configChronometer = RemindTimerConfig.CLIENT.chronometer;

        configChronometer.format.set(chronometer.getFormat());
        configChronometer.drawBackground.set(chronometer.isDrawBackground());

        configChronometer.rgbModeText.set(chronometer.getRgbModeText());
        configChronometer.rgbModeBackground.set(chronometer.getRgbModeBackground());

        configChronometer.redText.set(chronometer.getRedText());
        configChronometer.greenText.set(chronometer.getGreenText());
        configChronometer.blueText.set(chronometer.getBlueText());
        configChronometer.alphaText.set(chronometer.getAlphaText());
        configChronometer.rgbSpeedText.set(chronometer.getRgbSpeedText());

        configChronometer.redBackground.set(chronometer.getRedBackground());
        configChronometer.greenBackground.set(chronometer.getGreenBackground());
        configChronometer.blueBackground.set(chronometer.getBlueBackground());
        configChronometer.alphaBackground.set(chronometer.getAlphaBackground());
        configChronometer.rgbSpeedBackground.set(chronometer.getRgbSpeedBackground());

        configChronometer.textRightToLeftDirection.set(chronometer.isTextRightToLeftDirection());
        configChronometer.backgroundRightToLeftDirection.set(chronometer.isBackgroundRightToLeftDirection());
    }

    private void drawGradientRect(double left, double top, double right, double bottom, int z, int coltl, int coltr, int colbl,
                                  int colbr) {

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        Tesselator tesselator = Tesselator.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);


        buffer.vertex(right, top, z).color((coltr & 0x00ff0000) >> 16, (coltr & 0x0000ff00) >> 8,
                (coltr & 0x000000ff), (coltr & 0xff000000) >>> 24).endVertex();
        buffer.vertex(left, top, z).color((coltl & 0x00ff0000) >> 16, (coltl & 0x0000ff00) >> 8, (coltl & 0x000000ff),
                (coltl & 0xff000000) >>> 24).endVertex();
        buffer.vertex(left, bottom, z).color((colbl & 0x00ff0000) >> 16, (colbl & 0x0000ff00) >> 8,
                (colbl & 0x000000ff), (colbl & 0xff000000) >>> 24).endVertex();
        buffer.vertex(right, bottom, z).color((colbr & 0x00ff0000) >> 16, (colbr & 0x0000ff00) >> 8,
                (colbr & 0x000000ff), (colbr & 0xff000000) >>> 24).endVertex();
        tesselator.end();
        RenderSystem.disableBlend();
    }
}
