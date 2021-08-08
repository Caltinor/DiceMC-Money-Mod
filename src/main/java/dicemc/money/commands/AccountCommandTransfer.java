package dicemc.money.commands;

import java.util.UUID;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dicemc.money.MoneyMod;
import dicemc.money.MoneyMod.AcctTypes;
import dicemc.money.setup.Config;
import dicemc.money.storage.MoneyWSD;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class AccountCommandTransfer implements Command<CommandSource>{
private static final AccountCommandTransfer CMD = new AccountCommandTransfer();
	
	public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
		return Commands.literal("transfer")
				.then(Commands.argument("value", DoubleArgumentType.doubleArg(0d))
						.then(Commands.argument("recipient", StringArgumentType.word())
								.executes(CMD)));

	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrException();
		ServerWorld world = context.getSource().getServer().overworld();
		double value = DoubleArgumentType.getDouble(context, "value");
		UUID recipient = context.getSource().getServer().getProfileCache().get(StringArgumentType.getString(context, "recipient")).getId();
		if (MoneyWSD.get(world).transferFunds(AcctTypes.PLAYER.key, player.getUUID(), AcctTypes.PLAYER.key, recipient, value)) {
			if (Config.ENABLE_HISTORY.get()) {
				MoneyMod.dbm.postEntry(System.currentTimeMillis(), player.getUUID(), AcctTypes.PLAYER.key, player.getName().getContents()
						, recipient, AcctTypes.PLAYER.key, context.getSource().getServer().getProfileCache().get(recipient).getName()
						, value, "Player Transfer Command. From is who executed");
			}
			context.getSource().sendSuccess(new TranslationTextComponent("message.command.transfer.success", Math.abs(value), StringArgumentType.getString(context, "recipient")), true);
		}
		else 
			context.getSource().sendSuccess(new TranslationTextComponent("message.command.transfer.failure"), false);
		return 0;
	}
}
