package com.dicemc.money.client;

import java.text.DecimalFormat;

import com.dicemc.money.MoneyMod;
import com.dicemc.money.item.MoneyBag;
import com.dicemc.money.network.PacketAccountToServer;
import com.dicemc.money.network.PacketAccountToServer.PkType;
import com.dicemc.money.setup.Networking;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class GuiAccountManager extends ContainerScreen<ContainerAccountManager>{
	public static final ResourceLocation BACKGROUND = new ResourceLocation(MoneyMod.MOD_ID, "textures/gui/gui_background.png");
	private DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
	//Objects
	private Button depositButton, withdrawButton, transferButton;
	private TextFieldWidget amountField, nameField, bagCountField;
	//given variables
	private double balP = 0;

	public static void sync(double balP) {
		GuiAccountManager screen = (GuiAccountManager) Minecraft.getInstance().currentScreen;
		screen.updateGui(balP);
	}
	
	protected void updateGui(double balP) {
		this.balP = balP;
	}
	
	public GuiAccountManager(ContainerAccountManager type, PlayerInventory inv, ITextComponent titleIn) {
		super(type, inv, new StringTextComponent(""));
		this.xSize = 235;
        this.ySize = 185;
        Networking.sendToServer(new PacketAccountToServer(PkType.DEPOSIT, 0));
	}
	
	@Override
	protected void init() {
		super.init();
		this.playerInventoryTitleX = 68;
		this.playerInventoryTitleY = this.ySize - 94;
		amountField = new TextFieldWidget(font, this.guiLeft+ 8, this.guiTop+30, 60, 20, new StringTextComponent(""));
		nameField = new TextFieldWidget(font, amountField.x+amountField.getWidth()+ 5, amountField.y, 60, 20, new StringTextComponent(""));
		bagCountField = new TextFieldWidget(font, amountField.x, amountField.y + 35, 60, 20, new StringTextComponent(""));
		transferButton = new Button(nameField.x + nameField.getWidth() + 5, nameField.y, 60, 20, new TranslationTextComponent("gui.transfer"), button -> actionTransfer());
		withdrawButton = new Button(nameField.x, bagCountField.y, 60, 20, new TranslationTextComponent("gui.withdraw"), button -> actionWithdraw());
		depositButton = new Button(this.guiLeft+ 8, this.guiTop+162, 50, 20, new TranslationTextComponent("gui.deposit"), button -> actionDeposit());
		addButton(transferButton);
		addButton(withdrawButton);
		addButton(depositButton);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		super.keyPressed(keyCode, scanCode, modifiers);
		amountField.keyPressed(keyCode, scanCode, modifiers);
		nameField.keyPressed(keyCode, scanCode, modifiers);
		bagCountField.keyPressed(keyCode, scanCode, modifiers);
		return true;
	}
	
	public boolean charTyped(char ch, int a) {
		super.charTyped(ch, a);
		if (amountField.isFocused()) amountField.charTyped(ch, a);
		if (nameField.isFocused()) nameField.charTyped(ch, a);
		if (bagCountField.isFocused()) bagCountField.charTyped(ch, a);
		return true;
	}		
	
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		amountField.mouseClicked(mouseX, mouseY, mouseButton);
		nameField.mouseClicked(mouseX, mouseY, mouseButton);
		bagCountField.mouseClicked(mouseX, mouseY, mouseButton);
		return false;
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		amountField.render(matrixStack, mouseX, mouseY, partialTicks);
		nameField.render(matrixStack, mouseX, mouseY, partialTicks);
		bagCountField.render(matrixStack, mouseX, mouseY, partialTicks);
		ITextComponent strBalance = new TranslationTextComponent("gui.balance").appendString(df.format(balP)).mergeStyle(TextFormatting.DARK_GREEN);
		this.font.func_243248_b(matrixStack, strBalance, this.guiLeft +8, this.guiTop+ 8, 16777215);
		this.font.func_243248_b(matrixStack, new TranslationTextComponent("gui.amount"), this.amountField.x, this.amountField.y-11, 4210752);
		this.font.func_243248_b(matrixStack, new TranslationTextComponent("gui.bagdump"), this.guiLeft+8, this.guiTop+94, 4210752);
		this.font.func_243248_b(matrixStack, new TranslationTextComponent("gui.namefield"), this.nameField.x, this.nameField.y-11, 4210752);
		this.font.func_243248_b(matrixStack, new TranslationTextComponent("gui.count"), this.bagCountField.x, this.bagCountField.y-11, 4210752);
		this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
	}
	
	private void actionDeposit() {
		double amount = 0d;
		for (int i = 0; i < 9; i++) {
			if (this.container.inv.getStackInSlot(i).getItem() instanceof MoneyBag) {
				amount += (this.container.inv.getStackInSlot(i).getTag().getDouble("value") * this.container.inv.getStackInSlot(i).getCount());
				this.container.inv.getStackInSlot(i).setCount(0);
			}
		}
		this.getContainer().detectAndSendChanges();
		Networking.sendToServer(new PacketAccountToServer(PkType.DEPOSIT, amount));
	}
	private void actionWithdraw() {
		if (amountField.getText().length() > 0) {
			int count = 1;
			if (bagCountField.getText().length() > 0) {
				try {count = Math.abs(Integer.valueOf(bagCountField.getText()));} catch (NumberFormatException e) {}
			}
			double amount = 0;
			try {amount = Math.abs(Double.valueOf(amountField.getText()));} catch (NumberFormatException e) {}
			if (amount > 0) Networking.sendToServer(new PacketAccountToServer(PkType.WITHDRAW, amount, count));
		}
	}
	private void actionTransfer() {
		if (amountField.getText().length() > 0 && nameField.getText().length() > 0) {
			double amount = 0;
			try {amount = Math.abs(Double.valueOf(amountField.getText()));} catch (NumberFormatException e) {}
			if (amount > 0) Networking.sendToServer(new PacketAccountToServer(PkType.TRANSFER, amount, nameField.getText()));
		}
	}
	
	@Override
	public boolean isPauseScreen() {return false;}
	
	@Override
	public void onClose() {
		if (this.minecraft.player != null) {
			this.container.onContainerClosed(this.minecraft.player);
		}
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bindTexture(BACKGROUND);
        this.blit(matrixStack, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}
}
