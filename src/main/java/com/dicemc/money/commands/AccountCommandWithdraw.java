package com.dicemc.money.commands;

import com.dicemc.money.MoneyMod;
import com.dicemc.money.setup.Registration;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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

public class AccountCommandWithdraw implements Command<CommandSource>{
	private static final AccountCommandWithdraw CMD = new AccountCommandWithdraw();
	
	public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
		return Commands.literal("withdraw")
				.then(Commands.argument("value", DoubleArgumentType.doubleArg(0d))
						.then(Commands.argument("count", IntegerArgumentType.integer(1))
								.executes(CMD)
						.executes(CMD)));

	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		int count = 1;
		double value = DoubleArgumentType.getDouble(context, "value");
		try {count = IntegerArgumentType.getInteger(context, "count");} catch (IllegalArgumentException e) {}
		if ((value * count) <= MoneyMod.AcctProvider.getBalance(player.getUniqueID(), MoneyMod.playerAccounts)) {
			MoneyMod.AcctProvider.changeBalance(player.getUniqueID(), MoneyMod.playerAccounts, (-1 * value));
			ItemStack bag = new ItemStack(ForgeRegistries.ITEMS.getValue(Registration.MONEYBAG.getId()));
			bag.setCount(count);
			CompoundNBT nbt = new CompoundNBT();
			nbt.putDouble("value", value);
			bag.setTag(nbt);
			player.addItemStackToInventory(bag);
			TextComponent text = new TranslationTextComponent("message.commandwithdrawsuccess");
			text.append(new StringTextComponent(String.valueOf(value)));
			context.getSource().sendFeedback(text, false);
		}
		else context.getSource().sendFeedback(new TranslationTextComponent("message.commandwithdrawfail"), false);
		return 0;
	}
}
