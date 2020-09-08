package com.dicemc.money.commands;

import com.dicemc.money.MoneyMod;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AccountCommandRoot implements Command<CommandSource>{
	private static final AccountCommandRoot CMD = new AccountCommandRoot();
	
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralCommandNode<CommandSource> cmdSRC = dispatcher.register(Commands.literal("account")
				.then(AccountCommandDeposit.register(dispatcher))
				.then(AccountCommandWithdraw.register(dispatcher))
				.then(AccountCommandGui.register(dispatcher))
				.then(AccountCommandAdmin.register(dispatcher))
				.then(AccountCommandTransfer.register(dispatcher))
				.executes(CMD));
		
		dispatcher.register(Commands.literal("acct").redirect(cmdSRC));
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		Double balP = MoneyMod.AcctProvider.getBalance(context.getSource().asPlayer().getUniqueID(), MoneyMod.playerAccounts);
		TextComponent text = new TranslationTextComponent("message.commandrootbalance");
		text.append(new StringTextComponent(String.valueOf(balP)));
		context.getSource().sendFeedback(text, false);
		return 0;
	}
	
	
}
