package com.dicemc.money.item;

import com.dicemc.money.MoneyMod;
import com.dicemc.money.setup.Registration;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

public class MoneybagCombineRecipe extends SpecialRecipe {

	public MoneybagCombineRecipe(ResourceLocation p_i48169_1_) {super(p_i48169_1_);}

	@Override
	public boolean matches(CraftingInventory inv, World worldIn) {
		int bagcount = 0;
		for (int i = 0; i < inv.getSizeInventory(); i++) {			
			ItemStack current = inv.getStackInSlot(i);
	        if (current.isEmpty()) { continue;}
	        Item item = current.getItem();
			if (item instanceof MoneyBag) {bagcount++; continue;}	
			else { return false;}
		}
		return bagcount > 0;
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {
		double valueSum = 0;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			if (inv.getStackInSlot(i).getItem() instanceof MoneyBag) valueSum += (inv.getStackInSlot(i).getTag().getDouble("value"));
		}
		ItemStack result = new ItemStack(ForgeRegistries.ITEMS.getValue(Registration.MONEYBAG.getId()));
		result.setTagInfo("value", DoubleNBT.valueOf(valueSum));
		return result;
	}

	@Override
	public boolean canFit(int width, int height) {return (width * height) >= 2;}
	
	@Override
	public boolean isDynamic() {return true;}
	
	@Override
	public ItemStack getRecipeOutput() {return ItemStack.EMPTY;}

	@Override
	public IRecipeSerializer<?> getSerializer() {return SERIALIZER; }
	
	@ObjectHolder(MoneyMod.MOD_ID+":moneybag_combine")
	public static SpecialRecipeSerializer<MoneybagCombineRecipe> SERIALIZER;
	
}
