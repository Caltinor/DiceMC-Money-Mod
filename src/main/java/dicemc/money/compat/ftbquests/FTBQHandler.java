package dicemc.money.compat.ftbquests;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dicemc.money.MoneyMod;
import net.minecraft.resources.ResourceLocation;

public class FTBQHandler {
	public static RewardType MONEY_REWARD = RewardTypes.register(ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "moneyreward"), MoneyReward::new, () -> Icon.getIcon(MoneyMod.MOD_ID + ":textures/moneybag.png"));

	public static void init() {
		MoneyMod.LOGGER.debug("Enabled FTBQuests integration!");
	}
}
