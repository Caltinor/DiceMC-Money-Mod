package com.dicemc.money.commands;

import com.dicemc.money.client.ContainerAccountManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

public class AccountCommandGui implements Command<CommandSource>{
	private static final AccountCommandGui CMD = new AccountCommandGui();
	
	public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
		return Commands.literal("gui").executes(CMD);
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		NetworkHooks.openGui(player, new SimpleNamedContainerProvider (
				(i, playerInventory, playerEntity) -> 
				new ContainerAccountManager(i, playerInventory)
				, new StringTextComponent("account_gui")
				));
		return 0;
	}
}
