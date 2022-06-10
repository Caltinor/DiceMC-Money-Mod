package dicemc.money.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public class ShopCommandBuilder implements Command<CommandSourceStack>{
	public static final Map<UUID, ItemStack> buildRef = new HashMap<>();
	public static final ShopCommandBuilder CMD = new ShopCommandBuilder();
	
	//TODO add parameters to book builder
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("shop")
				.then(Commands.literal("builder")
						.executes(CMD)));
	}
	
	@Override
	public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		context.getSource().sendSuccess(Component.literal("WIP"), true);
		return 0;
	}

}
