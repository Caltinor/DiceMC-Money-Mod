package com.dicemc.money.commands;

import java.text.DecimalFormat;

import com.dicemc.money.MoneyMod;
import com.dicemc.money.setup.Registration;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

public class AccountCommandDeposit implements Command<CommandSource>{
	private static final AccountCommandDeposit CMD = new AccountCommandDeposit();
	
	public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
		return Commands.literal("deposit")
				.then(Commands.argument("value", DoubleArgumentType.doubleArg(0))
						.executes(CMD))
				.then(Commands.literal("all")
						.executes(CMD));
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		Double value = 0d;
		Double toAccount = 0d;
		try {value = DoubleArgumentType.getDouble(context, "value");} catch (IllegalArgumentException e) {}
		if (value == 0d) {
			for (int i = 0; i < 37; i++) {
				if (player.inventory.getStackInSlot(i).isItemEqual(new ItemStack(ForgeRegistries.ITEMS.getValue(Registration.MONEYBAG.getId())))) {
					toAccount += player.inventory.getStackInSlot(i).getTag().getDouble("value");
					player.inventory.removeStackFromSlot(i);
				}
			}
			player.inventory.markDirty();
		}
		else if (invTotalsValue(player, value)) {
			for (int i = 0; i < 37; i++) {
				if (value <= 0) break;
				if (player.inventory.getStackInSlot(i).isItemEqual(new ItemStack(ForgeRegistries.ITEMS.getValue(Registration.MONEYBAG.getId())))) {
					double stackVal = player.inventory.getStackInSlot(i).getTag().getDouble("value") * (double)player.inventory.getStackInSlot(i).getCount();
					if (stackVal <= value) {
						toAccount += stackVal;
						value -= stackVal;
						player.inventory.removeStackFromSlot(i);
					}
					else if (stackVal > value) {
						toAccount += value;
						CompoundNBT nbt = player.inventory.getStackInSlot(i).getTag();
						double writeVal = (stackVal-value)/player.inventory.getStackInSlot(i).getCount();
						nbt.putDouble("value", writeVal);
						player.inventory.getStackInSlot(i).setTag(nbt);
						value -= stackVal;
					}
				}
			}
			player.inventory.markDirty();
		}
		else context.getSource().sendFeedback(new TranslationTextComponent("message.commanddepositfail"), false);
		MoneyMod.AcctProvider.changeBalance(context.getSource().asPlayer().getUniqueID(), MoneyMod.playerAccounts, toAccount);
		DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
		TextComponent text = new StringTextComponent("$"+df.format(value));
		text.append(new TranslationTextComponent("message.commandwithdrawsuccess"));
		context.getSource().sendFeedback(text, false);
		return 0;
	}
	
	public boolean invTotalsValue(ServerPlayerEntity player, double value) {
		double inInv = 0d;
		for (int i = 0; i < 37; i++) {
			if (player.inventory.getStackInSlot(i).isItemEqual(new ItemStack(ForgeRegistries.ITEMS.getValue(Registration.MONEYBAG.getId())))) {
				inInv += player.inventory.getStackInSlot(i).getTag().getDouble("value") * (double)player.inventory.getStackInSlot(i).getCount();
			}
		}
		return inInv >= value;
	}
}
