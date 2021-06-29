package dicemc.money.commands;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;

import dicemc.money.MoneyMod.AcctTypes;
import dicemc.money.setup.Config;
import dicemc.money.storage.MoneyWSD;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AccountCommandTop implements Command<CommandSource>{
	private static final AccountCommandTop CMD = new AccountCommandTop();
	
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("top").executes(CMD));
	}
	
	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		Map<UUID, Double> unsorted = MoneyWSD.get(context.getSource().getServer().overworld()).getAccountMap(AcctTypes.PLAYER.key);
		List<Pair<UUID, Double>> sorted = new ArrayList<>();
		DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
		
		unsorted.entrySet().stream()
			.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			.forEachOrdered(x -> sorted.add(Pair.of(x.getKey(), x.getValue())));
		
		int limit = sorted.size() > Config.TOP_SIZE.get() ? Config.TOP_SIZE.get() : sorted.size();
		
		String tkey = limit == 1 ? "message.command.top1" : "message.command.top";
		context.getSource().sendSuccess(new TranslationTextComponent(tkey, limit), false);
		for (int i = 0; i < limit; i++) {
			Pair<UUID, Double> p = sorted.get(i);
			String name = context.getSource().getServer().getProfileCache().get(p.getFirst()).getName();
			context.getSource().sendSuccess(new StringTextComponent("#"+(i+1)+" "+name+": "+Config.CURRENCY_SYMBOL.get()+df.format(p.getSecond())), false);
		}
		return 0;
	}
}
