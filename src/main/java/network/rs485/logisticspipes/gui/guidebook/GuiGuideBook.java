/*
 * Copyright (c) 2020  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2020  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular
 * MIT license in your project, replace this copyright notice (this line and any
 * lines below and NOT the copyright line above) with the lines from the original
 * MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this file and associated documentation files (the "Source Code"), to deal in
 * the Source Code without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Source Code, and to permit persons to whom the Source Code is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Source Code, which also can be
 * distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.rs485.logisticspipes.gui.guidebook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import lombok.Getter;
import static network.rs485.logisticspipes.guidebook.BookContents.DEBUG_FILE;
import static network.rs485.logisticspipes.guidebook.BookContents.MAIN_MENU_FILE;
import org.lwjgl.opengl.GL11;

import logisticspipes.LPConstants;
import logisticspipes.items.ItemGuideBook;
import logisticspipes.utils.MinecraftColor;
import logisticspipes.utils.OpenGLDebugger;
import network.rs485.logisticspipes.gui.LPFontRenderer;
import network.rs485.logisticspipes.guidebook.BookContents;
import network.rs485.logisticspipes.util.ColorUtilKt;
import network.rs485.logisticspipes.util.math.Rectangle;
import network.rs485.markdown.TextFormat;

public class GuiGuideBook extends GuiScreen {

	/*
	 * Z Levels:
	 * Tooltip          : 20
	 * Title and Buttons: 15
	 * Background frame : 10
	 * Text             : 5
	 * Background       : 0
	 * Background tint  : -5
	 */

	private boolean loadedNBT = false;

	@Getter
	private final int zTooltip = 20;      // Tooltip z
	@Getter
	final int zTitleButtons = 15; // Title and Buttons Z
	@Getter
	private final int zFrame = 10;        // Frame Z
	@Getter
	final int zText = 5;          // Text/Information Z
	@Getter
	private final int zBackground = 0;    // Background Z

	public static final ResourceLocation GUI_BOOK_TEXTURE = new ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/guide_book.png");

	// Buttons
	private SliderButton slider;
	private GuiGuideBookTexturedButton home;
	private final int maxTabs = 10;
	private final ArrayList<TabButton> tabList;
	private GuiButton button;
	private int tabID = 50;

	public static SavedPage currentPage;
	private static String title;

	// Book Experimental variables
	private EnumHand hand;

	// Gui Drawing variables
	@Getter
	private final Rectangle outerGui, innerGui, guiSep;
	@Getter
	private int guiSliderX, guiSliderY0, guiSliderY1;
	private final int guiBorderThickness = 16, guiShadowThickness = 6, guiSeparatorThickness = 5;
	private final int guiBorderWithShadowThickness = guiBorderThickness + guiShadowThickness;
	private final int guiSliderWidth = 12, guiSliderHeight = 15, guiSepExtra = 1;
	private final int guiTabWidth = 24, guiTabHeight = 24, guiFullTabHeight = 32;
	// Usable area
	@Getter
	private static Rectangle usableArea;

	// Texture atlas constants
	private static final int atlasWidth = 256;
	private static final int atlasHeight = 256;
	private static final double atlasWidthScale = 1.0D / atlasWidth;
	private static final double atlasHeightScale = 1.0D / atlasHeight;
	// Gui Atlas constants
	private final int guiAtlasWidth = 64, guiAtlasHeight = 64;
	private final Rectangle guiAtlasBg = new Rectangle(64, 0, 32, 32);
	private final int guiAtlasU0 = 0, guiAtlasU1 = guiBorderWithShadowThickness, guiAtlasU2 = guiAtlasWidth - guiBorderWithShadowThickness, guiAtlasU3 = guiAtlasWidth;
	private final int guiAtlasV0 = 0, guiAtlasV1 = guiBorderWithShadowThickness, guiAtlasV2 = guiAtlasHeight - guiBorderWithShadowThickness, guiAtlasV3 = guiAtlasHeight;
	private static final Rectangle btnBackgroundUv = new Rectangle(64, 32, 32, 32);
	private static final Rectangle btnBorderUv = new Rectangle(0, 64, 16, 16);
	private static final int btnBorderWidth = 2;
	private final Rectangle guiAtlasSep = new Rectangle(96, 33, 16, 30);

	protected static Map<String, SavedPage> cachedPages;

	public static LPFontRenderer lpFontRenderer;

	public GuiGuideBook(EnumHand hand) {
		super();
		cachedPages = new HashMap<>();
		//		lpFontRenderer = new LPFontRenderer("minecraft-plain");
		lpFontRenderer = new LPFontRenderer("ter-u12n");
		innerGui = new Rectangle();
		outerGui = new Rectangle();
		guiSep = new Rectangle();
		usableArea = new Rectangle();
		this.hand = hand;
		//setPage(MAIN_MENU_FILE);
		setPage(DEBUG_FILE);
		this.tabList = new ArrayList<>();
	}

	public void setPage(String pagePath) {
		currentPage = cachedPages.computeIfAbsent(pagePath, it -> new SavedPage(it, 0, 0.0f));
		currentPage.initDrawables(usableArea.getX0(), usableArea.getY0(), usableArea.getWidth());
	}

	/*
	 * Calculates varius coordinates based on current width and height
	 */
	protected void calculateConstraints() {
		outerGui.setPos((int) (1.0D / 8.0D * this.width), (int) (1.0D / 8.0D * this.height)).setSize((int) (6.0D / 8.0D * this.width), (int) (6.0D / 8.0D * this.height));
		innerGui.setPos(outerGui.getX0() + guiBorderThickness, outerGui.getY0() + guiBorderThickness).setSize(outerGui.getWidth() - 2 * guiBorderThickness, outerGui.getHeight() - 2 * guiBorderThickness);
		guiSliderX = innerGui.getX1() - guiSliderWidth;
		guiSliderY0 = innerGui.getY0();
		guiSliderY1 = innerGui.getY1();
		guiSep.setPos(innerGui.getX1() - guiSliderWidth - guiSeparatorThickness - guiShadowThickness, innerGui.getY0()).setSize(2 * guiShadowThickness + guiSeparatorThickness, innerGui.getHeight());
		usableArea.setPos(innerGui.getX0() + guiShadowThickness, innerGui.getY0()).setSize(innerGui.getWidth() - 2 * guiShadowThickness - guiSliderWidth - guiSeparatorThickness, innerGui.getHeight());
		currentPage.initDrawables(usableArea.getX0(), usableArea.getY0(), usableArea.getWidth());
	}

	/**
	 * Gets information from the item's nbt
	 */
	protected boolean getDataFromNBT() {
		ItemStack bookItemStack = mc.player.getHeldItem(hand);
		if (bookItemStack.hasTagCompound()) {
			NBTTagCompound nbt = bookItemStack.getTagCompound();
			currentPage = new SavedPage().fromTag(nbt.getCompoundTag("page"));
			NBTTagList tagList = nbt.getTagList("bookmarks", 10);
			for (NBTBase tag : tagList) tabList.add(new TabButton(tabID++, outerGui.getX1() - 2 - 2 * guiTabWidth + (tabList.size() * guiTabWidth), outerGui.getY0(), new SavedPage().fromTag((NBTTagCompound) tag)));
		} else {
			SavedPage defaultPage = new SavedPage();
			currentPage = new SavedPage(defaultPage);
		}
		return true;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		currentPage.draw(mouseX, mouseY, partialTicks, usableArea);
		this.drawGui();
		for (TabButton tab : tabList) tab.drawButton(mc, mouseX, mouseY, partialTicks);
		this.drawTitle();
	}

	@Override
	public void initGui() {
		this.calculateConstraints();
		title = updateTitle();
		this.slider = this.addButton(new SliderButton(0, innerGui.getX1() - guiSliderWidth, innerGui.getY0(), innerGui.getHeight(), guiSliderWidth, guiSliderHeight, currentPage.getProgress()));
		this.slider.enabled = false;
		this.home = this.addButton(new GuiGuideBookTexturedButton(1, outerGui.getX1() - guiTabWidth, outerGui.getY0() - guiTabHeight, guiTabWidth, guiFullTabHeight, 16, 64, zTitleButtons, 128, 0, 16, 16, false, GuiGuideBookTexturedButton.EnumButtonType.TAB));
		this.button = this.addButton(new GuiGuideBookTexturedButton(4, outerGui.getX1() - 18 - guiTabWidth + 4, outerGui.getY0() - 18, 16, 16, 0, 0, zTitleButtons, 192, 0, 16, 16, true, GuiGuideBookTexturedButton.EnumButtonType.NORMAL));
		this.updateButtonVisibility();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void onGuiClosed() {
		// TODO THIS IS FOR TESTING ONLY
		BookContents.INSTANCE.clear();
		currentPage.setProgress(slider.getProgress());
		ArrayList<SavedPage> tabs = new ArrayList<>();
		for (TabButton tab : tabList) tabs.add(tab.getTab());
		//		final ItemStack stack = Minecraft.getMinecraft().player.getHeldItem(hand);
		//		ItemGuideBook.setCurrentPage(stack, currentPage, tabs, hand);
		super.onGuiClosed();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
			case 1:
				setPage(MAIN_MENU_FILE);
				slider.reset();
				break;
			case 2:
				if (!currentPage.getPage().equals(MAIN_MENU_FILE)) {
					if (tabNotFound(currentPage)) tryAddTab(currentPage);
				}
			default:
				break;
		}
		updateButtonVisibility();
	}

	private void tryAddTab(SavedPage currentPage) {
		tabList.add(new TabButton(tabID++, outerGui.getX1() - 2 - 2 * guiTabWidth + (tabList.size() * guiTabWidth), outerGui.getY0(), currentPage));
		updateButtonVisibility();
	}

	private void tryRemoveTab(TabButton tab) {
		//tabList.remove(tab);
		updateButtonVisibility();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (tabList != null && tabList.size() > 0) {
			for (TabButton tab : tabList) {
				if (tab.mousePressed(mc, mouseX, mouseY)) {
					if (mouseButton == 0) {
						//currentPage = new SavedPage(tab.getTab());
						tab.playPressSound(this.mc.getSoundHandler());
					} else if (mouseButton == 1) {
						tryRemoveTab(tab);
					} else if (mouseButton == 2) {
						tab.cycleColor();
					}
				}
			}
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
		updateButtonVisibility();
	}


	protected static String updateTitle() {
		return currentPage.getLoadedPage().getMetadata().getTitle();
	}

	protected void updateButtonVisibility() {
		this.home.visible = !currentPage.getPage().equals(MAIN_MENU_FILE);
		slider.enabled = currentPage.getHeight() > usableArea.getHeight();
		int offset = 0;
		for (TabButton tab : tabList) {
			tab.y = outerGui.getY0();
			tab.x = outerGui.getX1() - 2 - 2 * guiTabWidth - offset;
			offset += guiTabWidth;
			tab.setActive(tab.getTab().isEqual(currentPage));
		}
		this.button.visible = !currentPage.getPage().equals(MAIN_MENU_FILE) && tabList.size() < maxTabs;
		this.button.enabled = tabNotFound(currentPage);
		this.button.x = outerGui.getX1() - 20 - guiTabWidth - offset;
		title = updateTitle();
	}

	protected boolean tabNotFound(SavedPage checkTab) {
		for (TabButton tab : tabList) if (tab.getTab().isEqual(checkTab)) return false;
		return true;
	}

	/**
	 * Draws the main title on the centre of the top border of the GUI
	 */
	protected void drawTitle() {
		lpFontRenderer.drawCenteredString(title, this.width / 2, outerGui.getY0() + 4, MinecraftColor.WHITE.getColorCode(), EnumSet.of(TextFormat.Shadow), 1.0D);
	}

	/**
	 * Draws the main GUI border and background
	 */
	protected void drawGui() {
		Minecraft.getMinecraft().renderEngine.bindTexture(GUI_BOOK_TEXTURE);
		// Four vertices of square following order: TopLeft, TopRight, BottomLeft, BottomRight
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		// Background
		putRepeatingTexturedRectangle(bufferbuilder, innerGui.getX0(), innerGui.getY0(), innerGui.getX1(), innerGui.getY1(), zBackground, guiAtlasBg.getX0(), guiAtlasBg.getY0(), guiAtlasBg.getX1(), guiAtlasBg.getY1());
		// Corners: TopLeft, TopRight, BottomLeft & BottomRight
		putTexturedRectangle(bufferbuilder, outerGui.getX0(), outerGui.getY0(), innerGui.getX0() + guiShadowThickness, innerGui.getY0() + guiShadowThickness, zFrame, guiAtlasU0, guiAtlasV0, guiAtlasU1, guiAtlasV1);
		putTexturedRectangle(bufferbuilder, innerGui.getX1() - guiShadowThickness, outerGui.getY0(), outerGui.getX1(), innerGui.getY0() + guiShadowThickness, zFrame, guiAtlasU2, guiAtlasV0, guiAtlasU3, guiAtlasV1);
		putTexturedRectangle(bufferbuilder, outerGui.getX0(), innerGui.getY1() - guiShadowThickness, innerGui.getX0() + guiShadowThickness, outerGui.getY1(), zFrame, guiAtlasU0, guiAtlasV2, guiAtlasU1, guiAtlasV3);
		putTexturedRectangle(bufferbuilder, innerGui.getX1() - guiShadowThickness, innerGui.getY1() - guiShadowThickness, outerGui.getX1(), outerGui.getY1(), zFrame, guiAtlasU2, guiAtlasV2, guiAtlasU3, guiAtlasV3);
		// Edges: Top, Bottom, Left & Right
		putTexturedRectangle(bufferbuilder, innerGui.getX0() + guiShadowThickness, outerGui.getY0(), innerGui.getX1() - guiShadowThickness, innerGui.getY0() + guiShadowThickness, zFrame, guiAtlasU1, guiAtlasV0, guiAtlasU2, guiAtlasV1);
		putTexturedRectangle(bufferbuilder, innerGui.getX0() + guiShadowThickness, innerGui.getY1() - guiShadowThickness, innerGui.getX1() - guiShadowThickness, outerGui.getY1(), zFrame, guiAtlasU1, guiAtlasV2, guiAtlasU2, guiAtlasV3);
		putTexturedRectangle(bufferbuilder, outerGui.getX0(), innerGui.getY0() + guiShadowThickness, innerGui.getX0() + guiShadowThickness, innerGui.getY1() - guiShadowThickness, zFrame, guiAtlasU0, guiAtlasV1, guiAtlasU1, guiAtlasV2);
		putTexturedRectangle(bufferbuilder, innerGui.getX1() - guiShadowThickness, innerGui.getY0() + guiShadowThickness, outerGui.getX1(), innerGui.getY1() - guiShadowThickness, zFrame, guiAtlasU2, guiAtlasV1, guiAtlasU3, guiAtlasV2);
		// Slider Separator
		putTexturedRectangle(bufferbuilder, guiSep.getX0(), guiSep.getY0() - 1, guiSep.getX1(), guiSep.getY0(), zFrame, guiAtlasSep.getX0(), guiAtlasSep.getY0() - 1, guiAtlasSep.getX1(), guiAtlasSep.getY0());
		putTexturedRectangle(bufferbuilder, guiSep.getX0(), guiSep.getY0(), guiSep.getX1(), guiSep.getY1(), zFrame, guiAtlasSep.getX0(), guiAtlasSep.getY0(), guiAtlasSep.getX1(), guiAtlasSep.getY1());
		putTexturedRectangle(bufferbuilder, guiSep.getX0(), guiSep.getY1(), guiSep.getX1(), guiSep.getY1() + 1, zFrame, guiAtlasSep.getX0(), guiAtlasSep.getY1(), guiAtlasSep.getX1(), guiAtlasSep.getY1() + 1);
		tessellator.draw();
		GlStateManager.disableBlend();
	}

	/**
	 * Draws a square based on two vertices with (stretching) texture also determined by two vertices: TopLeft & BottomRight
	 * The vertex(xy) and vertex1(xy) translate to vertex(uv) and vertex1(uv) in the texture atlas.
	 * The Y increases from the top to the bottom. Blending turned off.
	 */
	public static void drawStretchingRectangle(int x0, int y0, int x1, int y1, double z, double u0, double v0, double u1, double v1) {
		drawStretchingRectangle(x0, y0, x1, y1, z, u0, v0, u1, v1, false);
	}

	/**
	 * Draws a square based on two vertices with (stretching) texture also determined by two vertices: TopLeft & BottomRight
	 * The vertex(xy) and vertex1(xy) translate to vertex(uv) and vertex1(uv) in the texture atlas.
	 * The Y increases from the top to the bottom. Blend optional
	 */
	public static void drawStretchingRectangle(int x0, int y0, int x1, int y1, double z, double u0, double v0, double u1, double v1, boolean blend) {
		drawStretchingRectangle(x0, y0, x1, y1, z, u0, v0, u1, v1, blend, 0xFFFFFF);
	}

	public static void drawStretchingRectangle(int x0, int y0, int x1, int y1, double z, double u0, double v0, double u1, double v1, boolean blend, int color) {

		Minecraft.getMinecraft().renderEngine.bindTexture(GUI_BOOK_TEXTURE);

		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;

		u0 *= atlasWidthScale;
		v0 *= atlasHeightScale;
		u1 *= atlasWidthScale;
		v1 *= atlasHeightScale;
		// Four vertices of square following order: TopLeft, TopRight, BottomLeft, BottomRight
		if (blend) GlStateManager.enableBlend();
		if (blend) GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(r, g, b, 1.0F);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(x0, y1, z).tex(u0, v1).endVertex();
		bufferbuilder.pos(x1, y1, z).tex(u1, v1).endVertex();
		bufferbuilder.pos(x1, y0, z).tex(u1, v0).endVertex();
		bufferbuilder.pos(x0, y0, z).tex(u0, v0).endVertex();
		tessellator.draw();
		if (blend) GlStateManager.disableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public static void putRepeatingTexturedRectangle(BufferBuilder bufferbuilder, int x0, int y0, int x1, int y1, double z, double u0, double v0, double u1, double v1) {
		int x = x1 - x0;
		int y = y1 - y0;
		int u = (int) (u1 - u0);
		int v = (int) (v1 - v0);
		int timesX = (x / u);
		int timesY = (y / v);
		int remainderX = x % u;
		int remainderY = y % v;
		for (int i = 0; i <= timesY; i++) {
			for (int j = 0; j <= timesX; j++) {
				if (j == timesX && i == timesY) {
					putTexturedRectangle(bufferbuilder, x0 + (j * u), y0 + (i * v), x0 + (j * u) + remainderX, y0 + (i * v) + remainderY, z, u0, v0, u0 + remainderX, v0 + remainderY);
				} else if (j == timesX) {
					putTexturedRectangle(bufferbuilder, x0 + (j * u), y0 + (i * v), x0 + (j * u) + remainderX, y0 + ((i + 1) * v), z, u0, v0, u0 + remainderX, v1);
				} else if (i == timesY) {
					putTexturedRectangle(bufferbuilder, x0 + (j * u), y0 + (i * v), x0 + ((j + 1) * u), y0 + (i * v) + remainderY, z, u0, v0, u1, v0 + remainderY);
				} else {
					putTexturedRectangle(bufferbuilder, x0 + (j * u), y0 + (i * v), x0 + ((j + 1) * u), y0 + ((i + 1) * v), z, u0, v0, u1, v1);
				}
			}
		}
	}

	public static void putTexturedRectangle(BufferBuilder bufferbuilder, int x0, int y0, int x1, int y1, double z, double u0, double v0, double u1, double v1) {
		u0 *= atlasWidthScale;
		v0 *= atlasHeightScale;
		u1 *= atlasWidthScale;
		v1 *= atlasHeightScale;
		// Four vertices of square following order: TopLeft, TopRight, BottomLeft, BottomRight
		bufferbuilder.pos(x0, y1, z).tex(u0, v1).endVertex();
		bufferbuilder.pos(x1, y1, z).tex(u1, v1).endVertex();
		bufferbuilder.pos(x1, y0, z).tex(u1, v0).endVertex();
		bufferbuilder.pos(x0, y0, z).tex(u0, v0).endVertex();
	}

	public static void drawBoxedCenteredString(Minecraft mc, String text, int x, int y, double z) {
		// TODO use LPFontRenderer
		// TODO clamp to the size of the screen
		int width = mc.fontRenderer.getStringWidth(text);
		int x1 = x - (width / 2 + 1);
		int x0 = x1 - 4;
		int x2 = x + (width / 2 + 1);
		int x3 = x2 + 4;
		int y0 = y;
		int y1 = y0 + 4;
		int y2 = y1 + 10;
		int y3 = y2 + 4;
		int u0 = 112;
		int v0 = 32;
		int u1 = 116;
		int v1 = 36;
		int u2 = 124;
		int v2 = 44;
		int u3 = 128;
		int v3 = 48;
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, z);
		GlStateManager.translate(0.0F, 0.0F, 100.0F);
		drawCenteredStringStatic(mc.fontRenderer, text, x, y + 5, 0xFFFFFF);
		GlStateManager.translate(0.0F, 0.0F, -100.0F);
		GlStateManager.enableAlpha();
		mc.renderEngine.bindTexture(GUI_BOOK_TEXTURE);
		// Background
		Minecraft.getMinecraft().renderEngine.bindTexture(GUI_BOOK_TEXTURE);
		//u0 *= atlasWidthScale;
		//v0 *= atlasHeightScale;
		//u1 *= atlasWidthScale;
		//v1 *= atlasHeightScale;
		// Four vertices of square following order: TopLeft, TopRight, BottomLeft, BottomRight
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		putTexturedRectangle(bufferbuilder, x1, y1, x2, y2, z, u1, v1, u2, v2);
		GlStateManager.enableBlend();
		// Corners: TopLeft, TopRight, BottomLeft & BottomRight
		putTexturedRectangle(bufferbuilder, x0, y0, x1, y1, z, u0, v0, u1, v1);
		putTexturedRectangle(bufferbuilder, x2, y0, x3, y1, z, u2, v0, u3, v1);
		putTexturedRectangle(bufferbuilder, x0, y2, x1, y3, z, u0, v2, u1, v3);
		putTexturedRectangle(bufferbuilder, x2, y2, x3, y3, z, u2, v2, u3, v3);
		// Edges: Top, Bottom, Left & Right
		putTexturedRectangle(bufferbuilder, x1, y0, x2, y1, z, u1, v0, u2, v1);
		putTexturedRectangle(bufferbuilder, x1, y2, x2, y3, z, u1, v2, u2, v3);
		putTexturedRectangle(bufferbuilder, x0, y1, x1, y2, z, u0, v1, u1, v2);
		putTexturedRectangle(bufferbuilder, x2, y1, x3, y2, z, u2, v1, u3, v2);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableBlend();
		tessellator.draw();
		GlStateManager.disableAlpha();
		GlStateManager.popMatrix();
	}

	public static void drawHorizontalLine(int x0, int x1, int y, double z, int thickness, int color) {
		int r = ColorUtilKt.red(color);
		int g = ColorUtilKt.green(color);
		int b = ColorUtilKt.blue(color);
		int a = ColorUtilKt.alpha(color);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.disableTexture2D();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
		bufferbuilder.pos(x0, y + thickness, z).color(r, g, b, a).endVertex();
		bufferbuilder.pos(x1, y + thickness, z).color(r, g, b, a).endVertex();
		bufferbuilder.pos(x1, y, z).color(r, g, b, a).endVertex();
		bufferbuilder.pos(x0, y, z).color(r, g, b, a).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableBlend();
	}

	public static void drawVerticalLine(int x, int y0, int y1, double z, int thickness, int color) {
		int r = ColorUtilKt.red(color);
		int g = ColorUtilKt.green(color);
		int b = ColorUtilKt.blue(color);
		int a = ColorUtilKt.alpha(color);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.disableTexture2D();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
		bufferbuilder.pos(x, y1, z).color(r, g, b, a).endVertex();
		bufferbuilder.pos(x + thickness, y1, z).color(r, g, b, a).endVertex();
		bufferbuilder.pos(x + thickness, y0, z).color(r, g, b, a).endVertex();
		bufferbuilder.pos(x, y0, z).color(r, g, b, a).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableBlend();
	}

	public static void drawCenteredStringStatic(FontRenderer fontRendererIn, String text, int x, int y, int color) {
		fontRendererIn.drawStringWithShadow(text, (float) (x - fontRendererIn.getStringWidth(text) / 2), (float) y, color);
	}

	public static void drawRectangleTile(Rectangle btn, double z, boolean isEnabled, boolean isHovered, int color) {
		// TODO make it cut the shape depending on broken borders
		Minecraft.getMinecraft().renderEngine.bindTexture(GUI_BOOK_TEXTURE);
		GlStateManager.color(ColorUtilKt.redF(color), ColorUtilKt.greenF(color), ColorUtilKt.blueF(color), 1.0F);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		int hovered = isHovered ? 1 : 0;
		int enabled = isEnabled ? 1 : 2;
		// Fill: Middle
		GuiGuideBook.putRepeatingTexturedRectangle(bufferbuilder, btn.getX0() + btnBorderWidth, btn.getY0() + btnBorderWidth, btn.getX1() - btnBorderWidth, btn.getY1() - btnBorderWidth, z, btnBackgroundUv.getX0(), btnBackgroundUv.getY0() + (hovered * enabled * btnBackgroundUv.getWidth()), btnBackgroundUv.getX1(),
				btnBackgroundUv.getY1() + (hovered * enabled * btnBackgroundUv.getWidth()));
		// Corners: TopLeft, TopRight, BottomLeft & BottomRight
		GuiGuideBook.putTexturedRectangle(bufferbuilder, btn.getX0(), btn.getY0(), btn.getX0() + btnBorderWidth, btn.getY0() + btnBorderWidth, z, btnBorderUv.getX0(), btnBorderUv.getY0() + (hovered * enabled * btnBorderUv.getHeight()), (btnBorderUv.getX0() + btnBorderWidth),
				(btnBorderUv.getY0() + btnBorderWidth) + (hovered * enabled * btnBorderUv.getHeight()));
		GuiGuideBook.putTexturedRectangle(bufferbuilder, btn.getX1() - btnBorderWidth, btn.getY0(), btn.getX1(), btn.getY0() + btnBorderWidth, z, (btnBorderUv.getX1() - btnBorderWidth), btnBorderUv.getY0() + (hovered * enabled * btnBorderUv.getHeight()), btnBorderUv.getX1(),
				(btnBorderUv.getY0() + btnBorderWidth) + (hovered * enabled * btnBorderUv.getHeight()));
		GuiGuideBook.putTexturedRectangle(bufferbuilder, btn.getX0(), btn.getY1() - btnBorderWidth, btn.getX0() + btnBorderWidth, btn.getY1(), z, btnBorderUv.getX0(), (btnBorderUv.getY1() - btnBorderWidth) + (hovered * enabled * btnBorderUv.getHeight()), (btnBorderUv.getX0() + btnBorderWidth),
				btnBorderUv.getY1() + (hovered * enabled * btnBorderUv.getHeight()));
		GuiGuideBook.putTexturedRectangle(bufferbuilder, btn.getX1() - btnBorderWidth, btn.getY1() - btnBorderWidth, btn.getX1(), btn.getY1(), z, (btnBorderUv.getX1() - btnBorderWidth), (btnBorderUv.getY1() - btnBorderWidth) + (hovered * enabled * btnBorderUv.getHeight()), btnBorderUv.getX1(),
				btnBorderUv.getY1() + (hovered * enabled * btnBorderUv.getHeight()));
		// Edges: Top, Bottom, Left & Right
		GuiGuideBook.putTexturedRectangle(bufferbuilder, btn.getX0() + btnBorderWidth, btn.getY0(), btn.getX1() - btnBorderWidth, btn.getY0() + btnBorderWidth, z, (btnBorderUv.getX0() + btnBorderWidth), btnBorderUv.getY0() + (hovered * enabled * btnBorderUv.getHeight()), (btnBorderUv.getX1() - btnBorderWidth),
				(btnBorderUv.getY0() + btnBorderWidth) + (hovered * enabled * btnBorderUv.getHeight()));
		GuiGuideBook.putTexturedRectangle(bufferbuilder, btn.getX0() + btnBorderWidth, btn.getY1() - btnBorderWidth, btn.getX1() - btnBorderWidth, btn.getY1(), z, (btnBorderUv.getX0() + btnBorderWidth), (btnBorderUv.getY1() - btnBorderWidth) + (hovered * enabled * btnBorderUv.getHeight()),
				(btnBorderUv.getX1() - btnBorderWidth), btnBorderUv.getY1() + (hovered * enabled * btnBorderUv.getHeight()));
		GuiGuideBook.putTexturedRectangle(bufferbuilder, btn.getX0(), btn.getY0() + btnBorderWidth, btn.getX0() + btnBorderWidth, btn.getY1() - btnBorderWidth, z, btnBorderUv.getX0(), (btnBorderUv.getY0() + btnBorderWidth) + (hovered * enabled * btnBorderUv.getHeight()), (btnBorderUv.getX0() + btnBorderWidth),
				(btnBorderUv.getY1() - btnBorderWidth) + (hovered * enabled * btnBorderUv.getHeight()));
		GuiGuideBook.putTexturedRectangle(bufferbuilder, btn.getX1() - btnBorderWidth, btn.getY0() + btnBorderWidth, btn.getX1(), btn.getY1() - btnBorderWidth, z, (btnBorderUv.getX1() - btnBorderWidth), (btnBorderUv.getY0() + btnBorderWidth) + (hovered * enabled * btnBorderUv.getHeight()), btnBorderUv.getX1(),
				(btnBorderUv.getY1() - btnBorderWidth) + (hovered * enabled * btnBorderUv.getHeight()));
		tessellator.draw();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public void drawHorizontalLine(int startX, int endX, int y, int color) {
		super.drawHorizontalLine(startX, endX, y, color);
	}
}
