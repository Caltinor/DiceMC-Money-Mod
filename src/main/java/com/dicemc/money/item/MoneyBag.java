package com.dicemc.money.item;

import java.util.List;

import javax.annotation.Nullable;

import com.dicemc.money.setup.CommonSetup;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class MoneyBag extends Item{

	public MoneyBag() {
		super(new Item.Properties()
			.group(CommonSetup.ITEM_GROUP)
			.isImmuneToFire());
	}
	
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flags) {
		super.addInformation(stack, world, list, flags);
		double value = 0d;
		try {value = stack.getTag().getDouble("value");} catch (NullPointerException e) {}
        list.add(new TranslationTextComponent(TextFormatting.YELLOW+"$"+String.valueOf(value)));
    }

}
