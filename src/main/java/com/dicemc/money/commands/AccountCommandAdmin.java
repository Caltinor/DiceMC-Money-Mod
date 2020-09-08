package com.dicemc.money.commands;

import com.dicemc.money.network.PacketOpenGui;
import com.dicemc.money.setup.Networking;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class AccountCommandAdmin implements Command<CommandSource>{
	private static final AccountCommandAdmin CMD = new AccountCommandAdmin();
	
	public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
		return Commands.literal("admin").requires(cs -> cs.hasPermissionLevel(1)).executes(CMD);
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		Networking.sendToClient(new PacketOpenGui(), context.getSource().asPlayer());
		return 0;
	}
}
