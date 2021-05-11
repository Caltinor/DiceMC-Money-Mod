package dicemc.money.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

public class ShopCommandBuilder implements Command<CommandSource>{
	public static final Map<UUID, ItemStack> buildRef = new HashMap<>();
	public static final ShopCommandBuilder CMD = new ShopCommandBuilder();
	
	//TODO add parameters to book builder
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("shop")
				.then(Commands.literal("builder")
						.executes(CMD)));
	}
	
	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		context.getSource().sendSuccess(new StringTextComponent("WIP"), true);
		return 0;
	}

}
