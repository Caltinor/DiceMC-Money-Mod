package com.dicemc.money.client;

import com.dicemc.money.MoneyMod;
import com.dicemc.money.item.MoneyBag;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ObjectHolder;

public class ContainerAccountManager extends Container{
	@ObjectHolder(MoneyMod.MOD_ID+":gui_container")
	public static ContainerType<ContainerAccountManager> TYPE;
	
	public final Inventory inv;
	
	public ContainerAccountManager(int id, PlayerInventory playerInventory, PacketBuffer buf) {
		this(id, playerInventory);
	}

    public ContainerAccountManager(int id, PlayerInventory playerInventory) {
        super(TYPE, id);
        inv = new Inventory(9);
        int xOff = 8;
        int yOff = 104;
        for (int x = 0; x < 3; x++ ) {
	        for (int y = 0; y < 3; y++) {
	            this.addSlot(new Slot(inv, (x*3)+y, xOff + x*18, yOff + y*18) {
	            	@Override
	            	public boolean isItemValid(ItemStack stack) {
	            	      return stack.getItem() instanceof MoneyBag;
	            	   }
	            });
	        }
        }
        bindPlayerInventory(playerInventory);
    }

    private void bindPlayerInventory(IInventory playerInventory) {
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                int index = j1 + l * 9 + 9;
                this.addSlot(new Slot(playerInventory, index, 68 + j1 * 18, l * 18 + 104));
            }
        }
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 68 + i1 * 18, 162));
        }
    }

    @Override
    public void detectAndSendChanges(){ super.detectAndSendChanges();}

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) { return true; }
    
    @Override
    public void onContainerClosed(PlayerEntity playerIn) { 
    	super.onContainerClosed(playerIn);
    	for (int i = 0; i < this.inv.getSizeInventory(); i ++) {
    		if (!this.inv.getStackInSlot(i).isEmpty()) {
    			playerIn.inventory.addItemStackToInventory(this.inv.getStackInSlot(i));
    			this.inv.removeStackFromSlot(i);
    		}
    	}
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < 9){
                if (!this.mergeItemStack(itemstack1, 9, this.inventorySlots.size(), true)){ return ItemStack.EMPTY; }
            }
            else if (!this.mergeItemStack(itemstack1, 0, 9, false)){ return ItemStack.EMPTY; }
            if (itemstack1.getCount() == 0) { slot.putStack(ItemStack.EMPTY); }
            else{ slot.onSlotChanged();}
        }
        return itemstack;
    }
}
