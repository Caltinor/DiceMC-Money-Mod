package dicemc.money.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dicemc.money.MoneyMod.AcctTypes;
import dicemc.money.setup.Config;
import dicemc.money.storage.MoneyWSD;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class AccountCommandRoot implements Command<CommandSource>{
	private static final AccountCommandRoot CMD = new AccountCommandRoot();
	
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("money")
				.then(AccountCommandAdmin.register(dispatcher))
				.then(AccountCommandTransfer.register(dispatcher))
				.executes(CMD));
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		ServerWorld world = context.getSource().getServer().overworld();		
		Double balP = MoneyWSD.get(world).getBalance(AcctTypes.PLAYER.key, context.getSource().getEntityOrException().getUUID());
		context.getSource().sendSuccess(new StringTextComponent(Config.CURRENCY_SYMBOL.get()+String.valueOf(balP)), false);
		return 0;
	}
	
	
}
