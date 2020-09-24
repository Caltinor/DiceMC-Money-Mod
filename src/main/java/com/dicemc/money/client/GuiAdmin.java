package com.dicemc.money.client;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.money.network.PacketAccountToServer;
import com.dicemc.money.network.PacketAdminToServer;
import com.dicemc.money.network.PacketAdminToServer.PkType;
import com.dicemc.money.setup.Networking;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.gui.ScrollPanel;
import net.minecraftforge.common.ForgeHooks;

public class GuiAdmin extends Screen{
	private DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
	//objects
	private Button setButton, addButton, subButton;
	private PlayerListPanel playerList;
	private TextFieldWidget valueField;
	//variables
	private Map<String, Double> masterList = new HashMap<String, Double>();
	
	public static void open() {Minecraft.getInstance().displayGuiScreen(new GuiAdmin());}
	
	public static void sync(Map<String, Double> masterList) {
		GuiAdmin screen = (GuiAdmin) Minecraft.getInstance().currentScreen;
		screen.updateGui(masterList);
	}
	
	protected void updateGui(Map<String, Double> masterList) {
		this.masterList = masterList;
		playerList.setInfo(masterList);
	}
	
	protected GuiAdmin() { 
		super(new StringTextComponent("admin"));
		Networking.sendToServer(new PacketAdminToServer(PkType.SYNC, 0, ""));
	}
	
	@Override
	protected void init() {
		playerList = new PlayerListPanel(minecraft, this.width/4, this.height/2, this.height/4, this.width/4);
		valueField = new TextFieldWidget(font, this.width/2 + 5, playerList.y, 75, 20, new StringTextComponent(""));
		addButton = new Button(valueField.x, valueField.y+25, valueField.getWidth()/2, 20, new TranslationTextComponent("admin.add"), button -> actionAdd());
		subButton = new Button(valueField.x + addButton.getWidth(), addButton.y, valueField.getWidth()/2, 20, new TranslationTextComponent("admin.sub"), button -> actionSub());
		setButton = new Button(valueField.x, addButton.y+25, valueField.getWidth(), 20, new TranslationTextComponent("admin.set"), button -> actionSet());
		addButton(addButton);
		addButton(subButton);
		addButton(setButton);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		super.keyPressed(keyCode, scanCode, modifiers);
		valueField.keyPressed(keyCode, scanCode, modifiers);
		return true;
	}
	
	public boolean charTyped(char ch, int a) {
		super.charTyped(ch, a);
		if (valueField.isFocused()) valueField.charTyped(ch, a);
		return true;
	}		
	
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		valueField.mouseClicked(mouseX, mouseY, mouseButton);
		playerList.mouseClicked(mouseX, mouseY, mouseButton);
		return false;
	}
	
	public boolean mouseScrolled(double mouseX, double mouseY, double amountScrolled) {
		super.mouseScrolled(mouseX, mouseY, amountScrolled);
		if (mouseX > playerList.x && mouseX < playerList.x+playerList.width && mouseY > playerList.y && mouseY < playerList.y+playerList.height)
			playerList.mouseScrolled(mouseX, mouseY, amountScrolled);
		return true;
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		playerList.render(matrixStack, mouseX, mouseY, partialTicks);
		valueField.render(matrixStack, mouseX, mouseY, partialTicks);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
	
	private void actionAdd() {
		if (valueField.getText().length() > 0 && playerList.selectedItem >= 0) {
			double amount = 0;
			try {amount = Math.abs(Double.valueOf(valueField.getText()));} catch (NumberFormatException e) {}
			if (amount > 0) Networking.sendToServer(new PacketAdminToServer(PkType.ADD, amount, playerList.getSelected()));
		}
	}
	private void actionSub() {
		if (valueField.getText().length() > 0 && playerList.selectedItem >= 0) {
			double amount = 0;
			try {amount = Math.abs(Double.valueOf(valueField.getText()));} catch (NumberFormatException e) {}
			if (amount > 0) Networking.sendToServer(new PacketAdminToServer(PkType.SUB, amount, playerList.getSelected()));
		}
	}
	private void actionSet() {
		if (valueField.getText().length() > 0 && playerList.selectedItem >= 0) {
			double amount = 0;
			try {amount = Math.abs(Double.valueOf(valueField.getText()));} catch (NumberFormatException e) {}
			if (amount > 0) Networking.sendToServer(new PacketAdminToServer(PkType.SET, amount, playerList.getSelected()));
		}
	}
	
	class PlayerListPanel extends ScrollPanel {
        private List<ITextProperties> lines = Collections.emptyList();
        public int x, y, width, height, selectedItem;

        public PlayerListPanel(Minecraft mcIn, int width, int height, int y, int x)
        {
            super(mcIn, width, height, y, x);
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            selectedItem = -1;
        }

        public String getSelected() {return lines.get(selectedItem).getString();}
        
        void setInfo(Map<String, Double> lines) { 
        	List<String> list = new ArrayList<String>();
        	for (Map.Entry<String, Double> entries : lines.entrySet()) {
        		list.add(entries.getKey());
        	}
        	this.lines = resizeContent(list); 
        }

        void clearInfo() { this.lines = Collections.emptyList(); }

        private List<ITextProperties> resizeContent(List<String> lines) {
            List<ITextProperties> ret = new ArrayList<>();
            for (String line : lines) {
                if (line == null) {
                    ret.add(null);
                    continue;
                }
                ITextComponent chat = ForgeHooks.newChatWithLinks(line, false);
                int maxTextLength = this.width - 12;
                if (maxTextLength >= 0) {
                    ret.addAll(font.getCharacterManager().func_238362_b_(chat, maxTextLength, Style.EMPTY));
                }
            }
            return ret;
        }

        @Override
        public int getContentHeight() {
            int height = 50;
            height += (lines.size() * font.FONT_HEIGHT);
            if (height < this.bottom - this.top - 8)
                height = this.bottom - this.top - 8;
            return height;
        }

        @Override
        protected int getScrollAmount() { return font.FONT_HEIGHT * 3; }

        @Override
        protected void drawPanel(MatrixStack mStack, int entryRight, int relativeY, Tessellator tess, int mouseX, int mouseY)
        {
            for (int i = 0; i < lines.size(); i++){
                if (lines.get(i) != null)
                {
                	if (i == selectedItem) {
                    	hLine(mStack, left, left+width, relativeY-1, Color.YELLOW.getRGB());
                    	hLine(mStack, left, left+width, relativeY-1+font.FONT_HEIGHT, Color.YELLOW.getRGB());
                    	vLine(mStack, left, relativeY-1, relativeY-1+font.FONT_HEIGHT, Color.YELLOW.getRGB());
                    	vLine(mStack, left+width-1, relativeY-1, relativeY-1+font.FONT_HEIGHT, Color.YELLOW.getRGB());
                    }
                    RenderSystem.enableBlend();
                    StringTextComponent stc = new StringTextComponent(lines.get(i).getString());
                    stc.appendString(" $"+df.format(masterList.getOrDefault(lines.get(i).getString(), 0d)));
                    GuiAdmin.this.font.func_243248_b(mStack, stc, left+1, relativeY, 0xFFFFFF);
                    RenderSystem.disableAlphaTest();
                    RenderSystem.disableBlend();
                }
                relativeY += font.FONT_HEIGHT;
            }
        }

        private Style findTextLine(final int mouseX, final int mouseY) {
            double offset = (mouseY - top) + border + scrollDistance + 1;

            int lineIdx = (int) (offset / font.FONT_HEIGHT);
            if (lineIdx > lines.size() || lineIdx < 1)
                return null;

            ITextProperties line = lines.get(lineIdx-1);
            selectedItem = lineIdx-1;
            if (line != null)
            {
                return font.getCharacterManager().func_238357_a_(line, mouseX);
            }
            return null;
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            final Style component = findTextLine((int) mouseX, (int) mouseY);
            if (component != null) {
                GuiAdmin.this.handleComponentClicked(component);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        protected void drawBackground() {}
	}
}
