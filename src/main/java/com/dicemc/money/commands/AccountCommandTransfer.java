package com.dicemc.money.commands;

import java.util.UUID;

import com.dicemc.money.MoneyMod;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AccountCommandTransfer implements Command<CommandSource>{
private static final AccountCommandTransfer CMD = new AccountCommandTransfer();
	
	public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
		return Commands.literal("transfer")
				.then(Commands.argument("value", DoubleArgumentType.doubleArg(0d))
						.then(Commands.argument("recipient", StringArgumentType.string())
								.executes(CMD)));

	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		double value = DoubleArgumentType.getDouble(context, "value");
		UUID recipient = context.getSource().getServer().getPlayerProfileCache().getGameProfileForUsername(StringArgumentType.getString(context, "recipient")).getId();
		boolean exists = recipient != null ? MoneyMod.AcctProvider.getBalance(recipient, MoneyMod.playerAccounts) >= 0 : false;
		if (value <= MoneyMod.AcctProvider.getBalance(player.getUniqueID(), MoneyMod.playerAccounts) && exists) {
			MoneyMod.AcctProvider.changeBalance(player.getUniqueID(), MoneyMod.playerAccounts, (-1 * value));
			MoneyMod.AcctProvider.changeBalance(recipient, MoneyMod.playerAccounts, (value));
			context.getSource().sendFeedback(new TranslationTextComponent("message.commandtransfersuccess", String.valueOf(value), StringArgumentType.getString(context, "recipient")), false);
		}
		else context.getSource().sendFeedback(new TranslationTextComponent("message.commandfailed"), false);
		return 0;
	}
}
