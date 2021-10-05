package dicemc.money.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dicemc.money.MoneyMod;
import dicemc.money.MoneyMod.AcctTypes;
import dicemc.money.setup.Config;
import dicemc.money.storage.MoneyWSD;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallSignBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WritableBookItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
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
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
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
	
	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event) {
		if (!event.getEntityLiving().getCommandSenderWorld().isClientSide && event.getEntityLiving() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) event.getEntityLiving();
			double balp = MoneyWSD.get(player.getServer().overworld()).getBalance(AcctTypes.PLAYER.key, player.getUUID());
			double loss = balp * Config.LOSS_ON_DEATH.get();
			if (loss > 0) {
				MoneyWSD.get(player.getServer().overworld()).changeBalance(AcctTypes.PLAYER.key, player.getUUID(), -loss);
				/*if (Config.ENABLE_HISTORY.get()) {
					MoneyMod.dbm.postEntry(System.currentTimeMillis(), DatabaseManager.NIL, AcctTypes.SERVER.key, "Server"
							, player.getUUID(), AcctTypes.PLAYER.key, player.getName().getContents()
							, -loss, "Loss on Death Event");
				}*/
				player.sendMessage(new TranslationTextComponent("message.death", loss), player.getUUID());
			}
		}
	}
	
	@SubscribeEvent
	public static void onBlockPlace(EntityPlaceEvent event) {
		if (event.getWorld().isClientSide()) return;
		boolean cancel = false;
		if (event.getWorld().getBlockEntity(event.getPos().north()) != null 
				&&event.getWorld().getBlockEntity(event.getPos().north()).getTileData().contains("is-shop")) {
			cancel = true;
		}
		if (event.getWorld().getBlockEntity(event.getPos().south()) != null
				&& event.getWorld().getBlockEntity(event.getPos().south()).getTileData().contains("is-shop")) {
			cancel = true;
		}
		if (event.getWorld().getBlockEntity(event.getPos().east()) != null 
				&& event.getWorld().getBlockEntity(event.getPos().east()).getTileData().contains("is-shop")) {
			cancel = true;
		}
		if (event.getWorld().getBlockEntity(event.getPos().west()) != null 
				&& event.getWorld().getBlockEntity(event.getPos().west()).getTileData().contains("is-shop")) {
			cancel = true;
		}
		if (event.getWorld().getBlockEntity(event.getPos().above()) != null 
				&& event.getWorld().getBlockEntity(event.getPos().above()).getTileData().contains("is-shop")) {
			cancel = true;
		}
		if (event.getWorld().getBlockEntity(event.getPos().below()) != null 
				&& event.getWorld().getBlockEntity(event.getPos().below()).getTileData().contains("is-shop")) {
			cancel = true;
		}
		event.setCanceled(cancel);
	}

	@SuppressWarnings("static-access")
	@SubscribeEvent
	public static void onShopBreak(BreakEvent event) {
		if (!event.getWorld().isClientSide() && event.getWorld().getBlockState(event.getPos()).getBlock() instanceof WallSignBlock) {
			SignTileEntity tile = (SignTileEntity) event.getWorld().getBlockEntity(event.getPos());
			CompoundNBT nbt = tile.getTileData();
			if (!nbt.isEmpty() && nbt.contains("shop-activated")) {
				PlayerEntity player = event.getPlayer();
				boolean hasPerms = player.hasPermissions(Config.ADMIN_LEVEL.get());
				if (!nbt.getUUID("owner").equals(player.getUUID())) {					
					event.setCanceled(!hasPerms);					
				}
				else if(nbt.getUUID("owner").equals(player.getUUID()) || hasPerms) {
					BlockPos backBlock = BlockPos.of(BlockPos.offset(event.getPos().asLong(), tile.getBlockState().getValue(((WallSignBlock)tile.getBlockState().getBlock()).FACING).getOpposite()));
					event.getWorld().getBlockEntity(backBlock).getTileData().remove("is-shop");
				}
			}
		}
		else if (!event.getWorld().isClientSide() && event.getWorld().getBlockEntity(event.getPos()) != null) {
			if (event.getWorld().getBlockEntity(event.getPos()).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
				if (event.getWorld().getBlockEntity(event.getPos()).getTileData().contains("is-shop")) {
					PlayerEntity player = event.getPlayer();
					event.setCanceled(!player.hasPermissions(Config.ADMIN_LEVEL.get()));
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onStorageOpen(RightClickBlock event) {
		TileEntity invTile = event.getWorld().getBlockEntity(event.getPos());
		if (invTile != null && invTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
			if (invTile.getTileData().contains("is-shop")) {
				if (!invTile.getTileData().getUUID("owner").equals(event.getPlayer().getUUID())) {					
					event.setCanceled(!event.getPlayer().hasPermissions(Config.ADMIN_LEVEL.get()));					
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
	@SubscribeEvent(priority=EventPriority.HIGHEST)
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
						if (activateShop(invTile, tile, event.getWorld(), event.getPos(), nbt, event.getPlayer()))
							event.setUseBlock(Result.DENY);
					}
					else {
						processTransaction(invTile, tile, event.getPlayer());
						event.setUseBlock(Result.DENY);
					}
				}
			}
		}
	}
	
	private static boolean activateShop(TileEntity storage, SignTileEntity tile, World world, BlockPos pos, CompoundNBT nbt, PlayerEntity player) {
		ITextComponent actionEntry = ITextComponent.Serializer.fromJson(nbt.getString("Text1"));
		ITextComponent priceEntry  = ITextComponent.Serializer.fromJson(nbt.getString("Text4"));
		//check if the storage block has an item in the first slot
		LazyOptional<IItemHandler> inv = storage.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
		ItemStack srcStack = inv.map((c) -> {
			for (int i = 0; i < c.getSlots(); i++) {
				if (c.getStackInSlot(i).isEmpty()) continue;
				return c.getStackInSlot(i);
			}	
			return ItemStack.EMPTY;
		}).orElse(ItemStack.EMPTY);
		if (srcStack.equals(ItemStack.EMPTY, true)) return false;
		//first confirm the action type is valid
		if (actionEntry.getContents().equalsIgnoreCase("[buy]")
				|| actionEntry.getContents().equalsIgnoreCase("[sell]")
				|| actionEntry.getContents().equalsIgnoreCase("[server-buy]")
				|| actionEntry.getContents().equalsIgnoreCase("[server-sell]")
				|| actionEntry.getContents().equalsIgnoreCase("[buy-from-server]")
				|| actionEntry.getContents().equalsIgnoreCase("[sell-to-server]")) {
			//second confirm the price value is valid
			if (actionEntry.getContents().equalsIgnoreCase("[server-buy]") || actionEntry.getContents().equalsIgnoreCase("[server-sell]")) {
				if (!player.hasPermissions(Config.ADMIN_LEVEL.get())) {
					player.sendMessage(new TranslationTextComponent("message.activate.failure.admin"), player.getUUID());
					return false;
				}
			}
			else if (!player.hasPermissions(Config.SHOP_LEVEL.get())) {
				player.sendMessage(new TranslationTextComponent("message.activate.failure.admin"), player.getUUID());
				return false;
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
				case "[buy]": {tile.getTileData().putString("shop-type", "buy"); break;}
				case "[sell]": {tile.getTileData().putString("shop-type", "sell");break;}
				case "[server-buy]": {tile.getTileData().putString("shop-type", "server-buy");break;}
				case "[server-sell]": {tile.getTileData().putString("shop-type", "server-sell");break;}
				default:}
				tile.getTileData().putBoolean("shop-activated", true);
				tile.getTileData().putUUID("owner", player.getUUID());
				//Serialize all items in the TE and store them in a ListNBT
				ListNBT lnbt = new ListNBT();
				inv.ifPresent((p) -> {
					for (int i = 0; i < p.getSlots(); i++) {
						ItemStack inSlot = p.getStackInSlot(i);
						if (inSlot.isEmpty()) continue;
						if (inSlot.getItem() instanceof WritableBookItem)
							lnbt.add(getItemFromBook(inSlot));
						else
							lnbt.add(inSlot.serializeNBT());
					}
				});
				tile.getTileData().put("items", lnbt);
				tile.save(nbt);
				tile.setChanged();
				storage.getTileData().putBoolean("is-shop", true);
				storage.getTileData().putUUID("owner", player.getUUID());
				storage.save(new CompoundNBT());
				BlockState state = world.getBlockState(pos);
				world.sendBlockUpdated(pos, state, state, BlockFlags.DEFAULT_AND_RERENDER);
				return true;
			}
			catch(NumberFormatException e) {
				player.sendMessage(new TranslationTextComponent("message.activate.failure.money"), player.getUUID());
				world.destroyBlock(pos, true, player);
			}
		}
		return false;
	}
	
	private static CompoundNBT getItemFromBook(ItemStack stack) {
		CompoundNBT nbt = stack.getTag();
		if (nbt.isEmpty()) return stack.serializeNBT();
		String page = nbt.getList("pages", NBT.TAG_STRING).get(0).getAsString();
		if (page.substring(0, 7).equalsIgnoreCase("vending")) {
			String subStr = page.substring(8);
			try {
				stack = ItemStack.of(JsonToNBT.parseTag(subStr));
				return stack.serializeNBT();
			}
			catch(CommandSyntaxException e) {e.printStackTrace();}
			
		}
		return stack.serializeNBT();
	}
	
	private static void getSaleInfo(CompoundNBT nbt, PlayerEntity player) {
		if (System.currentTimeMillis() - timeSinceClick.getOrDefault(player.getUUID(), 0l) < 1500) return;
		String type = nbt.getString("shop-type");
		boolean isBuy = type.equalsIgnoreCase("buy") || type.equalsIgnoreCase("server-buy");
		List<ItemStack> transItems = new ArrayList<>();
		ListNBT itemsList = nbt.getList("items", NBT.TAG_COMPOUND);
		for (int i = 0; i < itemsList.size(); i++) {
			transItems.add(ItemStack.of(itemsList.getCompound(i)));
		}
		double value = nbt.getDouble("price");
		StringTextComponent itemComponent = getTransItemsDisplayString(transItems);
		if (isBuy)
			player.sendMessage(new TranslationTextComponent("message.shop.info", itemComponent, Config.CURRENCY_SYMBOL.get()+String.valueOf(value)), player.getUUID());
		else
			player.sendMessage(new TranslationTextComponent("message.shop.info", Config.CURRENCY_SYMBOL.get()+String.valueOf(value), itemComponent), player.getUUID());
		timeSinceClick.put(player.getUUID(), System.currentTimeMillis());
	}
	
	private static StringTextComponent getTransItemsDisplayString(List<ItemStack> list ) {
		List<ItemStack> items = new ArrayList<>();
		for (int l = 0; l < list.size(); l++) {
			boolean hadMatch = false;
			for (int i = 0; i < items.size(); i++) {
				if (list.get(l).sameItem(items.get(i)) && ItemStack.tagMatches(list.get(l), items.get(i))) {
					items.get(i).grow(list.get(l).getCount());
					hadMatch = true;
					break;
				}
			}
			if (!hadMatch) items.add(list.get(l));
		}
		StringTextComponent itemComponent = new StringTextComponent("");
		boolean isFirst = true;
		for (ItemStack item : items) {
			if (!isFirst) itemComponent.append(", ");
			itemComponent.append(item.getCount()+"x ");
			itemComponent.append(item.getDisplayName());
			isFirst = false;
		}
		return itemComponent;
	}
	
	private static void processTransaction(TileEntity tile, SignTileEntity sign, PlayerEntity player) {
		MoneyWSD wsd = MoneyWSD.get(player.getServer().overworld());
		CompoundNBT nbt = sign.getTileData();
		LazyOptional<IItemHandler> inv = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		List<ItemStack> transItems = new ArrayList<>();
		Map<ItemStack, ItemStack> consolidatedItems = new HashMap<>();
		ListNBT itemsList = nbt.getList("items", NBT.TAG_COMPOUND);
		for (int i = 0; i < itemsList.size(); i++) {
			ItemStack srcStack = ItemStack.of(itemsList.getCompound(i));
			ItemStack keyStack = srcStack.copy();
			keyStack.setCount(1);
			boolean hasEntry = false;
			for (Map.Entry<ItemStack, ItemStack> map : consolidatedItems.entrySet()) {
				if (map.getKey().sameItem(srcStack) && ItemStack.tagMatches(map.getKey(), srcStack)) {
					map.getValue().grow(srcStack.getCount());
					hasEntry = true;
				}
			}
			if (!hasEntry) consolidatedItems.put(keyStack, srcStack);
		}
		for (Map.Entry<ItemStack, ItemStack> map : consolidatedItems.entrySet()) {
			transItems.add(map.getValue());
		}
		//ItemStack transItem = ItemStack.of(nbt.getCompound("item"));
		String action = nbt.getString("shop-type");
		double value = nbt.getDouble("price");
		//================BUY=================================================================================
		if (action.equalsIgnoreCase("buy")) { //BUY
			//First check the available funds and stock for trade
			double balP = wsd.getBalance(AcctTypes.PLAYER.key, player.getUUID());
			if (value > balP) {
				player.sendMessage(new TranslationTextComponent("message.shop.buy.failure.funds"), player.getUUID());
				return;
			}
			Map<Integer, ItemStack> slotMap = new HashMap<>();
			for (int tf = 0; tf < transItems.size(); tf++) {
				int[] stackSize = {transItems.get(tf).getCount()};
				final Integer t = Integer.valueOf(tf);
				Optional<Boolean> test = inv.map((p) -> {
					for (int i = 0; i < p.getSlots(); i++) {
						ItemStack inSlot = ItemStack.EMPTY;
						if (slotMap.containsKey(i) && transItems.get(t).getItem().equals(slotMap.get(i).getItem()) && ItemStack.tagMatches(transItems.get(t), slotMap.get(i))) {
							inSlot = p.extractItem(i, stackSize[0]+slotMap.get(i).getCount(), true);
							inSlot.shrink(slotMap.get(i).getCount());
						}
						else inSlot = p.extractItem(i, stackSize[0], true);
						if (inSlot.getItem().equals(transItems.get(t).getItem()) && ItemStack.tagMatches(inSlot, transItems.get(t))) {
							slotMap.merge(i, inSlot, (s, o) -> {s.grow(o.getCount()); return s;});
							stackSize[0] -= inSlot.getCount();
						}						
						if (stackSize[0] <= 0) break;
					}
					return stackSize[0] <= 0;
				});
				if (!test.get()) {
					player.sendMessage(new TranslationTextComponent("message.shop.buy.failure.stock"), player.getUUID());
					return;
				}
			}
			//Test if container has inventory to process.
			//If so, process transfer of items and funds.			
			UUID shopOwner = nbt.getUUID("owner");
			wsd.transferFunds(AcctTypes.PLAYER.key, player.getUUID(), AcctTypes.PLAYER.key, shopOwner, value);
			/*if (Config.ENABLE_HISTORY.get()) {
				String itemPrint = "";
				itemsList.forEach((a) -> {itemPrint.concat(a.getAsString());});
				MoneyMod.dbm.postEntry(System.currentTimeMillis(), player.getUUID(), AcctTypes.PLAYER.key, player.getName().getContents()
						, shopOwner, AcctTypes.PLAYER.key, player.getServer().getProfileCache().get(shopOwner).getName()
						, value, itemsList.getAsString());
			}*/
			inv.ifPresent((p) -> {
				for (Map.Entry<Integer, ItemStack> map : slotMap.entrySet()) {
					player.inventory.add(p.extractItem(map.getKey(), map.getValue().getCount(), false));
				}
			});
			TranslationTextComponent msg =  new TranslationTextComponent("message.shop.buy.success"
					, getTransItemsDisplayString(transItems), Config.CURRENCY_SYMBOL.get()+String.valueOf(value));
			player.sendMessage(msg, player.getUUID());
			player.getServer().sendMessage(msg, player.getUUID());
			return;
		}
		//================SELL=================================================================================
		else if (action.equalsIgnoreCase("sell")) { //SELL
			//First check the available funds and stock for trade
			UUID shopOwner = nbt.getUUID("owner");
			double balP = wsd.getBalance(AcctTypes.PLAYER.key, shopOwner);
			if (value > balP) {
				player.sendMessage(new TranslationTextComponent("message.shop.sell.failure.funds"), player.getUUID());
				return;
			}
			//test if player has item in inventory to sell
			//next test that the inventory has space
			Map<Integer, ItemStack> slotMap = new HashMap<>();
			for (int t = 0; t < transItems.size(); t++) {
				int stackSize = transItems.get(t).getCount();
				for (int i = 0; i < player.inventory.getContainerSize(); i++) {
					ItemStack inSlot = player.inventory.getItem(i).copy();
					int count = stackSize > inSlot.getCount() ? inSlot.getCount() : stackSize;
					inSlot.setCount(count);
					if (slotMap.containsKey(i) && transItems.get(t).getItem().equals(slotMap.get(i).getItem()) && ItemStack.tagMatches(transItems.get(t), slotMap.get(i))) {
						count = stackSize+slotMap.get(i).getCount() > inSlot.getCount() ? inSlot.getCount() : stackSize+slotMap.get(i).getCount();
						inSlot.setCount(count);
					}
					if (inSlot.getItem().equals(transItems.get(t).getItem()) && ItemStack.tagMatches(inSlot, transItems.get(t))) {
						slotMap.merge(i, inSlot, (s, o) -> {s.grow(o.getCount()); return s;});
						stackSize -= inSlot.getCount();
					}						
					if (stackSize <= 0) break;
				}
				if (stackSize > 0) {
					player.sendMessage(new TranslationTextComponent("message.shop.sell.failure.stock"), player.getUUID());
					return;
				}
				
			}
			Map<Integer, ItemStack> invSlotMap = new HashMap<>();
			for (int t = 0; t < transItems.size(); t++) {
				ItemStack sim = transItems.get(t).copy();
				Optional<Boolean> test = inv.map((p) -> {
					for (int i = 0; i < p.getSlots(); i++) {
						ItemStack insertResult = p.insertItem(i, sim, true);
						if (insertResult.isEmpty()) {
							invSlotMap.merge(i, sim.copy(), (s, o) -> {s.grow(o.getCount()); return s;});
							sim.setCount(0);
							break;
						}
						else if (insertResult.getCount() == sim.getCount()){
							continue;
						}
						else {
							ItemStack insertSuccess = sim.copy();
							insertSuccess.shrink(insertResult.getCount());
							sim.setCount(insertResult.getCount());
							invSlotMap.merge(i, insertSuccess, (s, o) -> {s.grow(insertSuccess.getCount()); return s;});
						}
					}
					if (!sim.isEmpty()) {
						player.sendMessage(new TranslationTextComponent("message.shop.sell.failure.space"), player.getUUID());
						return false;
					}
					return true;
				});
				if (!test.get()) return;
			}
			//Process Transfers now that reqs have been met
			wsd.transferFunds(AcctTypes.PLAYER.key, shopOwner, AcctTypes.PLAYER.key, player.getUUID(), value);
			/*if (Config.ENABLE_HISTORY.get()) {
				String itemPrint = "";
				itemsList.forEach((a) -> {itemPrint.concat(a.getAsString());});
				MoneyMod.dbm.postEntry(System.currentTimeMillis(), shopOwner, AcctTypes.PLAYER.key, player.getServer().getProfileCache().get(shopOwner).getName()
						, player.getUUID(), AcctTypes.PLAYER.key, player.getName().getContents()
						, value, itemsList.getAsString());
			}*/
			for (Map.Entry<Integer, ItemStack> pSlots : slotMap.entrySet()) {
				player.inventory.removeItem(pSlots.getKey(), pSlots.getValue().getCount());
			}
			inv.ifPresent((p) -> {
				for (Map.Entry<Integer, ItemStack> map : invSlotMap.entrySet()) {
					p.insertItem(map.getKey(), map.getValue(), false);
				}
			});
			player.sendMessage(new TranslationTextComponent("message.shop.sell.success"
					, Config.CURRENCY_SYMBOL.get()+String.valueOf(value), getTransItemsDisplayString(transItems)
					), player.getUUID());
			return;
		}
		//================SERVER BUY=================================================================================
		else if (action.equalsIgnoreCase("server-buy")) { //SERVER BUY
			//First check the available funds and stock for trade
			double balP = wsd.getBalance(AcctTypes.PLAYER.key, player.getUUID());
			if (value > balP) {
				player.sendMessage(new TranslationTextComponent("message.shop.buy.failure.funds"), player.getUUID());
				return;
			}
			wsd.changeBalance(AcctTypes.PLAYER.key, player.getUUID(), -value);
			/*if (Config.ENABLE_HISTORY.get()) {
				String itemPrint = "";
				itemsList.forEach((a) -> {itemPrint.concat(a.getAsString());});
				MoneyMod.dbm.postEntry(System.currentTimeMillis(), DatabaseManager.NIL, AcctTypes.SERVER.key, "Server"
						, player.getUUID(), AcctTypes.PLAYER.key, player.getName().getContents()
						, -value, itemsList.getAsString());
			}*/
			for (int i = 0; i < transItems.size(); i++) {
				player.inventory.add(transItems.get(i).copy());
			}
			player.sendMessage(new TranslationTextComponent("message.shop.buy.success"
					, getTransItemsDisplayString(transItems), Config.CURRENCY_SYMBOL.get()+String.valueOf(value)
					), player.getUUID());
			return;
		}
		//================SERVER SELL=================================================================================
		else if (action.equalsIgnoreCase("server-sell")) { //SERVER SELL
			Map<Integer, ItemStack> slotMap = new HashMap<>();
			for (int t = 0; t < transItems.size(); t++) {
				int stackSize = transItems.get(t).getCount();
				for (int i = 0; i < player.inventory.getContainerSize(); i++) {
					ItemStack inSlot = player.inventory.getItem(i).copy();
					int count = stackSize > inSlot.getCount() ? inSlot.getCount() : stackSize;
					inSlot.setCount(count);
					if (slotMap.containsKey(i) && transItems.get(t).getItem().equals(slotMap.get(i).getItem()) && ItemStack.tagMatches(transItems.get(t), slotMap.get(i))) {
						count = stackSize+slotMap.get(i).getCount() > inSlot.getCount() ? inSlot.getCount() : stackSize+slotMap.get(i).getCount();
						inSlot.setCount(count);
					}
					if (inSlot.getItem().equals(transItems.get(t).getItem()) && ItemStack.tagMatches(inSlot, transItems.get(t))) {
						slotMap.merge(i, inSlot, (s, o) -> {s.grow(o.getCount()); return s;});
						stackSize -= inSlot.getCount();
					}						
					if (stackSize <= 0) break;
				}
				if (stackSize > 0) {
					player.sendMessage(new TranslationTextComponent("message.shop.sell.failure.stock"), player.getUUID());
					return;
				}
				
			}
			wsd.changeBalance(AcctTypes.PLAYER.key, player.getUUID(), value);
			/*if (Config.ENABLE_HISTORY.get()) {
				String itemPrint = "";
				itemsList.forEach((a) -> {itemPrint.concat(a.getAsString());});
				MoneyMod.dbm.postEntry(System.currentTimeMillis(), DatabaseManager.NIL, AcctTypes.SERVER.key, "Server"
						, player.getUUID(), AcctTypes.PLAYER.key, player.getName().getContents()
						, value, itemsList.getAsString());
			}*/
			for (Map.Entry<Integer, ItemStack> pSlots : slotMap.entrySet()) {
				player.inventory.getItem(pSlots.getKey()).shrink(pSlots.getValue().getCount());
			}
			player.sendMessage(new TranslationTextComponent("message.shop.sell.success"
					, Config.CURRENCY_SYMBOL.get()+String.valueOf(value), getTransItemsDisplayString(transItems)
					), player.getUUID());
			return;
		}
	}
}
