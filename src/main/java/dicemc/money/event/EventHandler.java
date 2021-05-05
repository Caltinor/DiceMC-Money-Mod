package dicemc.money.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import dicemc.money.MoneyMod;
import dicemc.money.MoneyMod.AcctTypes;
import dicemc.money.setup.Config;
import dicemc.money.storage.MoneyWSD;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallSignBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@Mod.EventBusSubscriber( modid=MoneyMod.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {
	public static Map<UUID, Long> timeSinceClick = new HashMap<>();
	public static enum Shop {
		BUY, SELL, SERVER_BUY, SERVER_SELL
	}

	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void onPlayerLogin(PlayerLoggedInEvent event) {
		if (!event.getPlayer().getCommandSenderWorld().isClientSide && event.getPlayer() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
			String symbol = Config.CURRENCY_SYMBOL.get();
			double balP = MoneyWSD.get(player.getServer().overworld()).getBalance(AcctTypes.PLAYER.key, player.getUUID());
			player.sendMessage(new StringTextComponent(symbol+String.valueOf(balP)), player.getUUID());
		}
	}

	@SubscribeEvent
	public static void onShopBreak(BreakEvent event) {
		if (!event.getWorld().isClientSide() && event.getWorld().getBlockState(event.getPos()).getBlock() instanceof WallSignBlock) {
			SignTileEntity tile = (SignTileEntity) event.getWorld().getBlockEntity(event.getPos());
			CompoundNBT nbt = tile.serializeNBT();
			if (nbt.contains("ForgeData") && !nbt.getCompound("ForgeData").contains("shop-activated")) {
				PlayerEntity player = event.getPlayer();
				if (!nbt.getCompound("ForgeData").getUUID("owner").equals(player.getUUID())) {
					event.setCanceled(!player.hasPermissions(Config.ADMIN_LEVEL.get()));
				}
			}
		}
		else if (!event.getWorld().isClientSide() && event.getWorld().getBlockEntity(event.getPos()) != null) {
			if (event.getWorld().getBlockEntity(event.getPos()).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
				PlayerEntity player = event.getPlayer();
				BlockPos shop = event.getPos();
				if (event.getWorld().getBlockEntity(event.getPos().north()) != null 
						&&event.getWorld().getBlockEntity(event.getPos().north()).getBlockState().getBlock() instanceof WallSignBlock 
						&& event.getWorld().getBlockEntity(event.getPos().north()).serializeNBT().contains("ForgeData")
						&& event.getWorld().getBlockEntity(event.getPos().north()).serializeNBT().getCompound("ForgeData").getBoolean("shop-activated")) {
					shop = event.getPos().north();
				}
				else if (event.getWorld().getBlockEntity(event.getPos().south()) != null
						&& event.getWorld().getBlockEntity(event.getPos().south()).getBlockState().getBlock() instanceof WallSignBlock  
						&& event.getWorld().getBlockEntity(event.getPos().south()).serializeNBT().contains("ForgeData")
						&& event.getWorld().getBlockEntity(event.getPos().south()).serializeNBT().getCompound("ForgeData").getBoolean("shop-activated")) {
					shop = event.getPos().south();
				}
				else if (event.getWorld().getBlockEntity(event.getPos().east()) != null 
						&& event.getWorld().getBlockEntity(event.getPos().east()).getBlockState().getBlock() instanceof WallSignBlock  
						&& event.getWorld().getBlockEntity(event.getPos().east()).serializeNBT().contains("ForgeData")
						&& event.getWorld().getBlockEntity(event.getPos().east()).serializeNBT().getCompound("ForgeData").getBoolean("shop-activated")) {
					shop = event.getPos().east();
				}
				else if (event.getWorld().getBlockEntity(event.getPos().west()) != null 
						&& event.getWorld().getBlockEntity(event.getPos().west()).getBlockState().getBlock() instanceof WallSignBlock  
						&& event.getWorld().getBlockEntity(event.getPos().west()).serializeNBT().contains("ForgeData")
						&& event.getWorld().getBlockEntity(event.getPos().west()).serializeNBT().getCompound("ForgeData").getBoolean("shop-activated")) {
					shop = event.getPos().west();
				}
				if (!shop.equals(event.getPos()) && !event.getWorld().getBlockEntity(shop).serializeNBT().getCompound("ForgeData").getUUID("owner").equals(player.getUUID())) {
					event.setCanceled(!player.hasPermissions(Config.ADMIN_LEVEL.get()));
				}
			}
		}
	}
	
	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void onSignLeftClick(LeftClickBlock event) {
		if (!event.getWorld().isClientSide && event.getWorld().getBlockState(event.getPos()).getBlock() instanceof WallSignBlock) {
			SignTileEntity tile = (SignTileEntity) event.getWorld().getBlockEntity(event.getPos());
			CompoundNBT nbt = tile.getTileData();
			if (nbt.contains("shop-activated"))	
				getSaleInfo(nbt, event.getPlayer());
		}
	}
	
	@SuppressWarnings({ "resource", "static-access" })
	@SubscribeEvent
	public static void onSignRightClick(RightClickBlock event) {
		if (!event.getWorld().isClientSide && event.getWorld().getBlockState(event.getPos()).getBlock() instanceof WallSignBlock) {
			BlockState state = event.getWorld().getBlockState(event.getPos());
			WallSignBlock sign = (WallSignBlock) state.getBlock();
			BlockPos backBlock = BlockPos.of(BlockPos.offset(event.getPos().asLong(), state.getValue(sign.FACING).getOpposite()));
			if (event.getWorld().getBlockEntity(backBlock) != null) {
				TileEntity invTile = event.getWorld().getBlockEntity(backBlock);
				if (invTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
					SignTileEntity tile = (SignTileEntity) event.getWorld().getBlockEntity(event.getPos());
					CompoundNBT nbt = tile.serializeNBT();
					if (!nbt.contains("ForgeData") || !nbt.getCompound("ForgeData").contains("shop-activated")) {
						activateShop(invTile, tile, event.getWorld(), event.getPos(), nbt, event.getPlayer());				
					}
					else processTransaction(invTile, tile, event.getPlayer());
				}
			}
		}
	}
	
	private static void activateShop(TileEntity storage, SignTileEntity tile, World world, BlockPos pos, CompoundNBT nbt, PlayerEntity player) {
		ITextComponent actionEntry = ITextComponent.Serializer.fromJson(nbt.getString("Text1"));
		ITextComponent priceEntry  = ITextComponent.Serializer.fromJson(nbt.getString("Text4"));
		//check if the storage block has an item in the first slot
		LazyOptional<IItemHandler> inv = storage.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
		ItemStack srcStack = inv.map((c) -> c.getStackInSlot(0)).orElse(ItemStack.EMPTY);
		if (srcStack.equals(ItemStack.EMPTY, true)) return;
		//first confirm the action type is valid
		if (actionEntry.getContents().equalsIgnoreCase("[buy]")
				|| actionEntry.getContents().equalsIgnoreCase("[sell]")
				|| actionEntry.getContents().equalsIgnoreCase("[server-buy]")
				|| actionEntry.getContents().equalsIgnoreCase("[server-sell]")) {
			//second confirm the price value is valid
			if (actionEntry.getContents().equalsIgnoreCase("[server-buy]") || actionEntry.getContents().equalsIgnoreCase("[server-sell]")) {
				if (!player.hasPermissions(Config.ADMIN_LEVEL.get())) {
					player.sendMessage(new TranslationTextComponent("message.activate.failure.admin"), player.getUUID());
					return;
				}
			}
			try {
				double price = Math.abs(Double.valueOf(priceEntry.getString()));
				tile.getTileData().putDouble("price", price);
				StringTextComponent newAction = new StringTextComponent(actionEntry.getContents());
				newAction.withStyle(TextFormatting.BLUE);
				tile.setMessage(0, newAction);
				StringTextComponent newPrice = new StringTextComponent(Config.CURRENCY_SYMBOL.get()+String.valueOf(price));
				newPrice.withStyle(TextFormatting.GOLD);
				tile.setMessage(3, newPrice);
				switch (actionEntry.getContents()) {
				case "[buy]": {tile.getTileData().putInt("shop-type", Shop.BUY.ordinal()); break;}
				case "[sell]": {tile.getTileData().putInt("shop-type", Shop.SELL.ordinal());break;}
				case "[server-buy]": {tile.getTileData().putInt("shop-type", Shop.SERVER_BUY.ordinal());break;}
				case "[server-sell]": {tile.getTileData().putInt("shop-type", Shop.SERVER_SELL.ordinal());break;}
				default:}
				tile.getTileData().putBoolean("shop-activated", true);
				tile.getTileData().putUUID("owner", player.getUUID());
				tile.getTileData().put("item", srcStack.serializeNBT());
				tile.save(nbt);
				tile.setChanged();
				BlockState state = world.getBlockState(pos);
				world.sendBlockUpdated(pos, state, state, BlockFlags.DEFAULT_AND_RERENDER);
			}
			catch(NumberFormatException e) {
				world.destroyBlock(pos, true, player);
			}
		}
	}
	
	private static void getSaleInfo(CompoundNBT nbt, PlayerEntity player) {
		if (System.currentTimeMillis() - timeSinceClick.getOrDefault(player.getUUID(), 0l) < 1500) return;
		String type = nbt.getString("shop-type");
		boolean isBuy = type.equalsIgnoreCase("buy") || type.equalsIgnoreCase("server-buy");
		ItemStack transItem = ItemStack.of(nbt.getCompound("item"));
		double value = nbt.getDouble("price");
		StringTextComponent itemComponent = new StringTextComponent(transItem.getCount()+"x ");
		itemComponent.append(transItem.getDisplayName());
		if (isBuy)
			player.sendMessage(new TranslationTextComponent("message.shop.info", itemComponent, Config.CURRENCY_SYMBOL.get()+String.valueOf(value)), player.getUUID());
		else
			player.sendMessage(new TranslationTextComponent("message.shop.info", Config.CURRENCY_SYMBOL.get()+String.valueOf(value), itemComponent), player.getUUID());
		timeSinceClick.put(player.getUUID(), System.currentTimeMillis());
	}
	
	private static void processTransaction(TileEntity tile, SignTileEntity sign, PlayerEntity player) {
		MoneyWSD wsd = MoneyWSD.get(player.getServer().overworld());
		CompoundNBT nbt = sign.serializeNBT();
		LazyOptional<IItemHandler> inv = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		ItemStack transItem = ItemStack.of(nbt.getCompound("ForgeData").getCompound("item"));
		int action = nbt.getCompound("ForgeData").getInt("shop-type");
		double value = nbt.getCompound("ForgeData").getDouble("price");
		//================BUY=================================================================================
		if (action == Shop.BUY.ordinal()) { //BUY
			//First check the available funds and stock for trade
			double balP = wsd.getBalance(AcctTypes.PLAYER.key, player.getUUID());
			if (value > balP) {
				player.sendMessage(new TranslationTextComponent("message.shop.buy.failure.funds"), player.getUUID());
				return;
			}
			int stackSize = transItem.getCount();
			Map<Integer, Integer> slotMap = new HashMap<>();
			Optional<ItemStack> test = inv.map((p) -> {
				int found = 0;
				for (int i = 0; i < p.getSlots(); i++) {
					ItemStack inSlot = p.extractItem(i, stackSize, true);
					if (inSlot.getItem().equals(transItem.getItem())) {
						int count = inSlot.getCount() > (stackSize-found) ? (stackSize-found) : inSlot.getCount(); 
						slotMap.put(i, count);
						found += count;
					}
					if (found >= stackSize) break;
				}
				return found == stackSize ? transItem : ItemStack.EMPTY;
			});
			//Test if container has inventory to process.
			//If so, process transfer of items and funds.
			if (!test.get().equals(ItemStack.EMPTY, false)) {
				UUID shopOwner = nbt.getCompound("ForgeData").getUUID("owner");
				wsd.transferFunds(AcctTypes.PLAYER.key, player.getUUID(), AcctTypes.PLAYER.key, shopOwner, value);
				
				player.inventory.add(transItem.copy());				
				inv.ifPresent((p) -> {
					for (Map.Entry<Integer, Integer> map : slotMap.entrySet()) {
						p.extractItem(map.getKey(), map.getValue(), false);
					}
				});
				player.sendMessage(new TranslationTextComponent("message.shop.buy.success"
						, stackSize, transItem.getDisplayName(), Config.CURRENCY_SYMBOL.get()+String.valueOf(value)
						), player.getUUID());
			}
			else {
				player.sendMessage(new TranslationTextComponent("message.shop.buy.failure.stock"), player.getUUID());
			}
			return;
		}
		//================SELL=================================================================================
		else if (action == Shop.SELL.ordinal()) { //SELL
			//First check the available funds and stock for trade
			UUID shopOwner = nbt.getCompound("ForgeData").getUUID("owner");
			double balP = wsd.getBalance(AcctTypes.PLAYER.key, shopOwner);
			if (value > balP) {
				player.sendMessage(new TranslationTextComponent("message.shop.sell.failure.funds"), player.getUUID());
				return;
			}
			//test if player has item in inventory to sell
			//next test that the inventory has space
			Map<Integer, Integer> slotMap = new HashMap<>();
			int found = 0;
			for (int i = 0; i < player.inventory.getContainerSize(); i++) {
				ItemStack inSlot = player.inventory.getItem(i);
				if (inSlot.getItem().equals(transItem.getItem())) {
					int count = inSlot.getCount() > (transItem.getCount()-found) ? (transItem.getCount()-found) : inSlot.getCount();
					slotMap.put(i, count);
					found += count;
					if (found >= transItem.getCount()) break;
				}
			}
			if (found < transItem.getCount()) {
				player.sendMessage(new TranslationTextComponent("message.shop.sell.failure.stock"), player.getUUID());
				return;
			}
			Map<Integer, Integer> invSlotMap = new HashMap<>();
			boolean[] spaceInShop = {false};
			inv.ifPresent((p) -> {
				ItemStack sim = transItem.copy();
				for (int i = 0; i < p.getSlots(); i++) {
					ItemStack insertResult = p.insertItem(i, sim, true);
					if (insertResult.isEmpty()) {
						invSlotMap.put(i, sim.getCount());
						sim = ItemStack.EMPTY;
						break;
					}
					else if (insertResult.getCount() == sim.getCount()){
						continue;
					}
					else {
						int count = sim.getCount()-insertResult.getCount();
						sim.setCount(insertResult.getCount());
						invSlotMap.put(i, count);
					}
				}
				if (!sim.equals(ItemStack.EMPTY)) {
					player.sendMessage(new TranslationTextComponent("message.shop.sell.failure.space"), player.getUUID());
					return;
				}
				spaceInShop[0] = true;
			});
			if (!spaceInShop[0]) return;
			//Process Transfers now that reqs have been met
			wsd.transferFunds(AcctTypes.PLAYER.key, shopOwner, AcctTypes.PLAYER.key, player.getUUID(), value);
			for (Map.Entry<Integer, Integer> pSlots : slotMap.entrySet()) {
				player.inventory.removeItem(pSlots.getKey(), pSlots.getValue());
			}
			inv.ifPresent((p) -> {
				for (Map.Entry<Integer, Integer> map : invSlotMap.entrySet()) {
					ItemStack insert = transItem.copy();
					insert.setCount(map.getValue());
					p.insertItem(map.getKey(), insert, false);
				}
			});
			player.sendMessage(new TranslationTextComponent("message.shop.sell.success"
					, Config.CURRENCY_SYMBOL.get()+String.valueOf(value), transItem.getCount(), transItem.getDisplayName()
					), player.getUUID());
			return;
		}
		//================SERVER BUY=================================================================================
		else if (action == Shop.SERVER_BUY.ordinal()) { //SERVER BUY
			//First check the available funds and stock for trade
			double balP = wsd.getBalance(AcctTypes.PLAYER.key, player.getUUID());
			if (value > balP) {
				player.sendMessage(new TranslationTextComponent("message.shop.buy.failure.funds"), player.getUUID());
				return;
			}
			wsd.changeBalance(AcctTypes.PLAYER.key, player.getUUID(), -value);
			player.inventory.add(transItem);
			player.sendMessage(new TranslationTextComponent("message.shop.buy.success"
					, transItem.getCount(), transItem.getDisplayName(), Config.CURRENCY_SYMBOL.get()+String.valueOf(value)
					), player.getUUID());
			return;
		}
		//================SERVER SELL=================================================================================
		else if (action == Shop.SERVER_SELL.ordinal()) { //SERVER SELL
			Map<Integer, Integer> slotMap = new HashMap<>();
			int found = 0;
			for (int i = 0; i < player.inventory.getContainerSize(); i++) {
				ItemStack inSlot = player.inventory.getItem(i);
				if (inSlot.getItem().equals(transItem.getItem())) {
					int count = inSlot.getCount() > (transItem.getCount()-found) ? (transItem.getCount()-found) : inSlot.getCount();
					slotMap.put(i, count);
					found += count;
					if (found >= transItem.getCount()) break;
				}
			}
			if (found < transItem.getCount()) {
				player.sendMessage(new TranslationTextComponent("message.shop.sell.failure.stock"), player.getUUID());
				return;
			}
			wsd.changeBalance(AcctTypes.PLAYER.key, player.getUUID(), value);
			for (Map.Entry<Integer, Integer> pSlots : slotMap.entrySet()) {
				player.inventory.removeItem(pSlots.getKey(), pSlots.getValue());
			}
			player.sendMessage(new TranslationTextComponent("message.shop.sell.success"
					, Config.CURRENCY_SYMBOL.get()+String.valueOf(value), transItem.getCount(), transItem.getDisplayName()
					), player.getUUID());
			return;
		}
	}
}
