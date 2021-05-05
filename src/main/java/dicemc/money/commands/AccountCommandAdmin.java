package dicemc.money.commands;

import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dicemc.money.MoneyMod.AcctTypes;
import dicemc.money.setup.Config;
import dicemc.money.storage.MoneyWSD;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AccountCommandAdmin{
	
	public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
		return Commands.literal("admin")
				.requires((p) -> p.hasPermission(Config.ADMIN_LEVEL.get()))
				.then(Commands.literal("balance")
						.then(Commands.argument("player", StringArgumentType.word())
							.executes((p) -> balance(p))))
				.then(Commands.argument("action", StringArgumentType.word())
						.suggests((c, b) -> b
								.suggest("balance")
								.suggest("set")
								.suggest("give")
								.suggest("take")
								.buildFuture())
						.then(Commands.argument("player", StringArgumentType.word())
								.executes((p) -> process(p))
								.then(Commands.argument("amount", DoubleArgumentType.doubleArg(0d))
									.executes((p) -> process(p)))))
				.then(Commands.literal("transfer")
						.then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
								.then(Commands.argument("from", StringArgumentType.word())
										.then(Commands.argument("to", StringArgumentType.word())
											.executes((p) -> transfer(p))))))	;

	}
	
	public static int process(CommandContext<CommandSource> context) throws CommandSyntaxException {
		MoneyWSD wsd = MoneyWSD.get(context.getSource().getServer().overworld());
		MinecraftServer server = context.getSource().getServer();
		String option = StringArgumentType.getString(context, "action");
		String target = StringArgumentType.getString(context, "player");
		String symbol = Config.CURRENCY_SYMBOL.get();
		double value = DoubleArgumentType.getDouble(context, "amount");
		UUID player = server.getProfileCache().get(target).getId();
		if (player == null) {
			context.getSource().sendFailure(new TranslationTextComponent("message.command.playernotfound", target));
			return 1;
		}
		switch (option) {
		case "set": {
			boolean result = wsd.setBalance(AcctTypes.PLAYER.key, player, value);
			if (result) {
				context.getSource().sendSuccess(
					new TranslationTextComponent("message.command.set.success", target, symbol+String.valueOf(value)), true);
				return 0;
			}
			context.getSource().sendFailure(new TranslationTextComponent("message.command.set.failure"));
			return 1;
		}
		case "give": {
			boolean result = wsd.changeBalance(AcctTypes.PLAYER.key, player, value);
			if (result) {
				context.getSource().sendSuccess(
					new TranslationTextComponent("message.command.give.success", symbol+String.valueOf(value), target), true);
				return 0;
			}
			context.getSource().sendFailure(new TranslationTextComponent("message.command.change.failure"));
			return 1;
		}
		case "take": {
			boolean result = wsd.changeBalance(AcctTypes.PLAYER.key, player, -value);
			if (result) {
				context.getSource().sendSuccess(
					new TranslationTextComponent("message.command.take.success", symbol+String.valueOf(value), target), true);
				return 0;
			}
			context.getSource().sendFailure(new TranslationTextComponent("message.command.change.failure"));
			return 1;
		}
		default:}
		return 0;
	}
	
	public static int balance(CommandContext<CommandSource> context) throws CommandSyntaxException {
		MoneyWSD wsd = MoneyWSD.get(context.getSource().getServer().overworld());
		MinecraftServer server = context.getSource().getServer();
		String target = StringArgumentType.getString(context, "player");
		String symbol = Config.CURRENCY_SYMBOL.get();
		UUID player = server.getProfileCache().get(target).getId();
		if (player == null) {
			context.getSource().sendFailure(new TranslationTextComponent("message.command.playernotfound", target));
			return 1;
		}
		double balP = wsd.getBalance(AcctTypes.PLAYER.key, player); 
		context.getSource().sendSuccess(new StringTextComponent(symbol+String.valueOf(balP)), true);		
		return 0;
	}
	
	public static int transfer(CommandContext<CommandSource> context) throws CommandSyntaxException {
		MoneyWSD wsd = MoneyWSD.get(context.getSource().getServer().overworld());
		MinecraftServer server = context.getSource().getServer();
		String from = StringArgumentType.getString(context, "from");
		String to = StringArgumentType.getString(context, "to");
		String symbol = Config.CURRENCY_SYMBOL.get();
		double value = DoubleArgumentType.getDouble(context, "amount");
		UUID fromplayer = server.getProfileCache().get(from).getId();
		if (fromplayer == null) {
			context.getSource().sendFailure(new TranslationTextComponent("message.command.playernotfound", from));
			return 1;
		}
		UUID toplayer = server.getProfileCache().get(to).getId();
		if (toplayer == null) {
			context.getSource().sendFailure(new TranslationTextComponent("message.command.playernotfound", to));
			return 1;
		}
		boolean result = wsd.transferFunds(AcctTypes.PLAYER.key, fromplayer, AcctTypes.PLAYER.key, toplayer, value);
		if (result) {
			context.getSource().sendSuccess(
				new TranslationTextComponent("message.command.transfer.success", symbol+String.valueOf(value), to), true);
			return 0;
		}
		context.getSource().sendFailure(new TranslationTextComponent("message.command.transfer.failure"));
		return 1;
	}

}
