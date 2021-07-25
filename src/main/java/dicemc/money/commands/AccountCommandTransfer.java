package dicemc.money.commands;

import java.util.UUID;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dicemc.money.MoneyMod.AcctTypes;
import dicemc.money.storage.MoneyWSD;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;

public class AccountCommandTransfer implements Command<CommandSourceStack>{
private static final AccountCommandTransfer CMD = new AccountCommandTransfer();
	
	public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
		return Commands.literal("transfer")
				.then(Commands.argument("value", DoubleArgumentType.doubleArg(0d))
						.then(Commands.argument("recipient", StringArgumentType.word())
								.executes(CMD)));

	}

	@Override
	public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		ServerLevel world = context.getSource().getServer().overworld();
		double value = DoubleArgumentType.getDouble(context, "value");
		UUID recipient = context.getSource().getServer().getProfileCache().get(StringArgumentType.getString(context, "recipient")).get().getId();
		if (MoneyWSD.get(world).transferFunds(AcctTypes.PLAYER.key, player.getUUID(), AcctTypes.PLAYER.key, recipient, value))
			context.getSource().sendSuccess(new TranslatableComponent("message.command.transfer.success", Math.abs(value), StringArgumentType.getString(context, "recipient")), true);
		else 
			context.getSource().sendSuccess(new TranslatableComponent("message.command.transfer.failure"), false);
		return 0;
	}
}
