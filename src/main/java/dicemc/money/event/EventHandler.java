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
import dicemc.money.storage.DatabaseManager;
import dicemc.money.storage.MoneyWSD;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
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
		if (!event.getEntity().getLevel().isClientSide && event.getEntity() instanceof ServerPlayer) {
			ServerPlayer player = (ServerPlayer) event.getEntity();
			double balP = MoneyWSD.get(player.getServer().overworld()).getBalance(AcctTypes.PLAYER.key, player.getUUID());
			player.sendSystemMessage(Component.literal(Config.getFormattedCurrency(balP)));
		}
	}
	
	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event) {
		if (!event.getEntity().getLevel().isClientSide && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			double balp = MoneyWSD.get(player.getServer().overworld()).getBalance(AcctTypes.PLAYER.key, player.getUUID());
			double loss = balp * Config.LOSS_ON_DEATH.get();
			if (loss > 0) {
				MoneyWSD.get(player.getServer().overworld()).changeBalance(AcctTypes.PLAYER.key, player.getUUID(), -loss);
				if (Config.ENABLE_HISTORY.get()) {
					MoneyMod.dbm.postEntry(System.currentTimeMillis(), DatabaseManager.NIL, AcctTypes.SERVER.key, "Server"
							, player.getUUID(), AcctTypes.PLAYER.key, player.getName().getString()
							, -loss, "Loss on Death Event");
				}
				player.sendSystemMessage(Component.translatable("message.death", Config.getFormattedCurrency(loss)));
			}
		}
	}
	
	@SubscribeEvent
	public static void onBlockPlace(EntityPlaceEvent event) {
		if (event.getLevel().isClientSide()) return;
		boolean cancel = false;
		if (event.getLevel().getBlockEntity(event.getPos().north()) != null 
				&&event.getLevel().getBlockEntity(event.getPos().north()).getPersistentData().contains("is-shop")) {
			cancel = true;
		}
		if (event.getLevel().getBlockEntity(event.getPos().south()) != null
				&& event.getLevel().getBlockEntity(event.getPos().south()).getPersistentData().contains("is-shop")) {
			cancel = true;
		}
		if (event.getLevel().getBlockEntity(event.getPos().east()) != null 
				&& event.getLevel().getBlockEntity(event.getPos().east()).getPersistentData().contains("is-shop")) {
			cancel = true;
		}
		if (event.getLevel().getBlockEntity(event.getPos().west()) != null 
				&& event.getLevel().getBlockEntity(event.getPos().west()).getPersistentData().contains("is-shop")) {
			cancel = true;
		}
		if (event.getLevel().getBlockEntity(event.getPos().above()) != null 
				&& event.getLevel().getBlockEntity(event.getPos().above()).getPersistentData().contains("is-shop")) {
			cancel = true;
		}
		if (event.getLevel().getBlockEntity(event.getPos().below()) != null 
				&& event.getLevel().getBlockEntity(event.getPos().below()).getPersistentData().contains("is-shop")) {
			cancel = true;
		}
		event.setCanceled(cancel);
	}

	@SuppressWarnings("static-access")
	@SubscribeEvent
	public static void onShopBreak(BreakEvent event) {
		if (!event.getLevel().isClientSide() && event.getLevel().getBlockState(event.getPos()).getBlock() instanceof WallSignBlock) {
			SignBlockEntity tile = (SignBlockEntity) event.getLevel().getBlockEntity(event.getPos());
			CompoundTag nbt = tile.getPersistentData();
			if (!nbt.isEmpty() && nbt.contains("shop-activated")) {
				Player player = event.getPlayer();
				boolean hasPerms = player.hasPermissions(Config.ADMIN_LEVEL.get());
				if (!nbt.getUUID("owner").equals(player.getUUID())) {					
					event.setCanceled(!hasPerms);					
				}
				else if(nbt.getUUID("owner").equals(player.getUUID()) || hasPerms) {
					BlockPos backBlock = BlockPos.of(BlockPos.offset(event.getPos().asLong(), tile.getBlockState().getValue(((WallSignBlock)tile.getBlockState().getBlock()).FACING).getOpposite()));
					event.getLevel().getBlockEntity(backBlock).getPersistentData().remove("is-shop");
				}
			}
		}
		else if (!event.getLevel().isClientSide() && event.getLevel().getBlockEntity(event.getPos()) != null) {
			if (event.getLevel().getBlockEntity(event.getPos()).getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
				if (event.getLevel().getBlockEntity(event.getPos()).getPersistentData().contains("is-shop")) {
					Player player = event.getPlayer();
					event.setCanceled(!player.hasPermissions(Config.ADMIN_LEVEL.get()));
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onStorageOpen(RightClickBlock event) {
		BlockEntity invTile = event.getLevel().getBlockEntity(event.getPos());
		if (invTile != null && invTile.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
			if (invTile.getPersistentData().contains("is-shop")) {
				if (!invTile.getPersistentData().getUUID("owner").equals(event.getEntity().getUUID())) {					
					event.setCanceled(!event.getEntity().hasPermissions(Config.ADMIN_LEVEL.get()));					
				}
			}
		}
	}
	
	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void onSignLeftClick(LeftClickBlock event) {
		if (!event.getLevel().isClientSide && event.getLevel().getBlockState(event.getPos()).getBlock() instanceof WallSignBlock) {
			SignBlockEntity tile = (SignBlockEntity) event.getLevel().getBlockEntity(event.getPos());
			CompoundTag nbt = tile.getPersistentData();
			if (nbt.contains("shop-activated"))	
				getSaleInfo(nbt, event.getEntity());
		}
	}
	
	@SuppressWarnings({ "resource", "static-access" })
	@SubscribeEvent
	public static void onSignRightClick(RightClickBlock event) {
		if (!event.getLevel().isClientSide && event.getLevel().getBlockState(event.getPos()).getBlock() instanceof WallSignBlock) {
			BlockState state = event.getLevel().getBlockState(event.getPos());
			WallSignBlock sign = (WallSignBlock) state.getBlock();
			BlockPos backBlock = BlockPos.of(BlockPos.offset(event.getPos().asLong(), state.getValue(sign.FACING).getOpposite()));
			if (event.getLevel().getBlockEntity(backBlock) != null) {
				BlockEntity invTile = event.getLevel().getBlockEntity(backBlock);
				if (invTile.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
					SignBlockEntity tile = (SignBlockEntity) event.getLevel().getBlockEntity(event.getPos());
					CompoundTag nbt = tile.saveWithFullMetadata();
					if (!nbt.contains("ForgeData") || !nbt.getCompound("ForgeData").contains("shop-activated")) {
						if (activateShop(invTile, tile, event.getLevel(), event.getPos(), nbt, event.getEntity()))
							event.setUseBlock(Result.DENY);
					}
					else {
						processTransaction(invTile, tile, event.getEntity());
						event.setUseBlock(Result.DENY);
					}
				}
			}
		}
	}
	
	private static boolean activateShop(BlockEntity storage, SignBlockEntity tile, Level world, BlockPos pos, CompoundTag nbt, Player player) {
		Component actionEntry = Component.Serializer.fromJson(nbt.getString("Text1"));
		Component priceEntry  = Component.Serializer.fromJson(nbt.getString("Text4"));
		//check if the storage block has an item in the first slot
		LazyOptional<IItemHandler> inv = storage.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP);
		ItemStack srcStack = inv.map((c) -> {
			for (int i = 0; i < c.getSlots(); i++) {
				if (c.getStackInSlot(i).isEmpty()) continue;
				return c.getStackInSlot(i);
			}	
			return ItemStack.EMPTY;
		}).orElse(ItemStack.EMPTY);
		if (srcStack.equals(ItemStack.EMPTY, true)) return false;
		//first confirm the action type is valid
		if (actionEntry.getString().equalsIgnoreCase("[buy]")
				|| actionEntry.getString().equalsIgnoreCase("[sell]")
				|| actionEntry.getString().equalsIgnoreCase("[server-buy]")
				|| actionEntry.getString().equalsIgnoreCase("[server-sell]")) {
			//second confirm the price value is valid
			if (actionEntry.getString().equalsIgnoreCase("[server-buy]") || actionEntry.getString().equalsIgnoreCase("[server-sell]")) {
				if (!player.hasPermissions(Config.ADMIN_LEVEL.get())) {
					player.sendSystemMessage(Component.translatable("message.activate.failure.admin"));
					return false;
				}
			}
			else if (!player.hasPermissions(Config.SHOP_LEVEL.get())) {
				player.sendSystemMessage(Component.translatable("message.activate.failure.admin"));
				return false;
			}
			try {
				double price = Math.abs(Double.valueOf(priceEntry.getString()));
				tile.getPersistentData().putDouble("price", price);
				tile.setMessage(0, Component.literal(actionEntry.getString()).withStyle(ChatFormatting.BLUE));
				tile.setMessage(3, Component.literal(Config.getFormattedCurrency(price)).withStyle(ChatFormatting.GOLD));
				switch (actionEntry.getString().toLowerCase()) {
				case "[buy]": {tile.getPersistentData().putString("shop-type", "buy"); break;}
				case "[sell]": {tile.getPersistentData().putString("shop-type", "sell");break;}
				case "[server-buy]": {tile.getPersistentData().putString("shop-type", "server-buy");break;}
				case "[server-sell]": {tile.getPersistentData().putString("shop-type", "server-sell");break;}
				default:}
				tile.getPersistentData().putBoolean("shop-activated", true);
				tile.getPersistentData().putUUID("owner", player.getUUID());
				//Serialize all items in the TE and store them in a ListNBT
				ListTag lnbt = new ListTag();
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
				tile.getPersistentData().put("items", lnbt);
				tile.saveWithFullMetadata();
				tile.setChanged();
				storage.getPersistentData().putBoolean("is-shop", true);
				storage.getPersistentData().putUUID("owner", player.getUUID());
				storage.saveWithFullMetadata();
				BlockState state = world.getBlockState(pos);
				world.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
				return true;
			}
			catch(NumberFormatException e) {
				player.sendSystemMessage(Component.translatable("message.activate.failure.money"));
				world.destroyBlock(pos, true, player);
			}
		}
		return false;
	}
	
	private static CompoundTag getItemFromBook(ItemStack stack) {
		CompoundTag nbt = stack.getTag();
		if (nbt == null || nbt.isEmpty()) return stack.serializeNBT();
		String page = nbt.getList("pages", Tag.TAG_STRING).get(0).getAsString();
		if (page.substring(0, 7).equalsIgnoreCase("vending")) {
			String subStr = page.substring(8);
			try {
				stack = ItemStack.of(TagParser.parseTag(subStr));
				return stack.serializeNBT();
			}
			catch(CommandSyntaxException e) {e.printStackTrace();}
			
		}
		return stack.serializeNBT();
	}
	
	private static void getSaleInfo(CompoundTag nbt, Player player) {
		if (System.currentTimeMillis() - timeSinceClick.getOrDefault(player.getUUID(), 0l) < 1500) return;
		String type = nbt.getString("shop-type");
		boolean isBuy = type.equalsIgnoreCase("buy") || type.equalsIgnoreCase("server-buy");
		List<ItemStack> transItems = new ArrayList<>();
		ListTag itemsList = nbt.getList("items", Tag.TAG_COMPOUND);
		for (int i = 0; i < itemsList.size(); i++) {
			transItems.add(ItemStack.of(itemsList.getCompound(i)));
		}
		double value = nbt.getDouble("price");
		MutableComponent itemComponent = getTransItemsDisplayString(transItems);
		if (isBuy)
			player.sendSystemMessage(Component.translatable("message.shop.info", itemComponent, Config.getFormattedCurrency(value)));
		else
			player.sendSystemMessage(Component.translatable("message.shop.info", Config.getFormattedCurrency(value), itemComponent));
		timeSinceClick.put(player.getUUID(), System.currentTimeMillis());
	}
	
	private static MutableComponent getTransItemsDisplayString(List<ItemStack> list ) {
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
		MutableComponent itemComponent = Component.literal("");
		boolean isFirst = true;
		for (ItemStack item : items) {
			if (!isFirst) itemComponent.append(", ");
			itemComponent.append(item.getCount()+"x ");
			itemComponent.append(item.getDisplayName());
			isFirst = false;
		}
		return itemComponent;
	}
	
	private static void processTransaction(BlockEntity tile, SignBlockEntity sign, Player player) {
		MoneyWSD wsd = MoneyWSD.get(player.getServer().overworld());
		CompoundTag nbt = sign.getPersistentData();
		LazyOptional<IItemHandler> inv = tile.getCapability(ForgeCapabilities.ITEM_HANDLER);
		List<ItemStack> transItems = new ArrayList<>();
		Map<ItemStack, ItemStack> consolidatedItems = new HashMap<>();
		ListTag itemsList = nbt.getList("items", Tag.TAG_COMPOUND);
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
				player.sendSystemMessage(Component.translatable("message.shop.buy.failure.funds"));
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
					player.sendSystemMessage(Component.translatable("message.shop.buy.failure.stock"));
					return;
				}
			}
			//Test if container has inventory to process.
			//If so, process transfer of items and funds.			
			UUID shopOwner = nbt.getUUID("owner");
			wsd.transferFunds(AcctTypes.PLAYER.key, player.getUUID(), AcctTypes.PLAYER.key, shopOwner, value);
			if (Config.ENABLE_HISTORY.get()) {
				String itemPrint = "";
				itemsList.forEach((a) -> {itemPrint.concat(a.getAsString());});
				MoneyMod.dbm.postEntry(System.currentTimeMillis(), player.getUUID(), AcctTypes.PLAYER.key, player.getName().getString()
						, shopOwner, AcctTypes.PLAYER.key, player.getServer().getProfileCache().get(shopOwner).get().getName()
						, value, itemsList.getAsString());
			}
			inv.ifPresent((p) -> {
				for (Map.Entry<Integer, ItemStack> map : slotMap.entrySet()) {
					ItemStack pStack = p.extractItem(map.getKey(), map.getValue().getCount(), false);
					if (!player.addItem(pStack))
						player.drop(pStack, false);
				}
			});
			MutableComponent msg =  Component.translatable("message.shop.buy.success"
					, getTransItemsDisplayString(transItems), Config.getFormattedCurrency(value));
			player.displayClientMessage(msg, true);
			player.getServer().sendSystemMessage(msg);
			return;
		}
		//================SELL=================================================================================
		else if (action.equalsIgnoreCase("sell")) { //SELL
			//First check the available funds and stock for trade
			UUID shopOwner = nbt.getUUID("owner");
			double balP = wsd.getBalance(AcctTypes.PLAYER.key, shopOwner);
			if (value > balP) {
				player.sendSystemMessage(Component.translatable("message.shop.sell.failure.funds"));
				return;
			}
			//test if player has item in inventory to sell
			//next test that the inventory has space
			Map<Integer, ItemStack> slotMap = new HashMap<>();
			for (int t = 0; t < transItems.size(); t++) {
				int stackSize = transItems.get(t).getCount();
				for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
					ItemStack inSlot = player.getInventory().getItem(i).copy();
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
					player.sendSystemMessage(Component.translatable("message.shop.sell.failure.stock"));
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
						player.sendSystemMessage(Component.translatable("message.shop.sell.failure.space"));
						return false;
					}
					return true;
				});
				if (!test.get()) return;
			}
			//Process Transfers now that reqs have been met
			wsd.transferFunds(AcctTypes.PLAYER.key, shopOwner, AcctTypes.PLAYER.key, player.getUUID(), value);
			if (Config.ENABLE_HISTORY.get()) {
				String itemPrint = "";
				itemsList.forEach((a) -> {itemPrint.concat(a.getAsString());});
				MoneyMod.dbm.postEntry(System.currentTimeMillis(), shopOwner, AcctTypes.PLAYER.key, player.getServer().getProfileCache().get(shopOwner).get().getName()
						, player.getUUID(), AcctTypes.PLAYER.key, player.getName().getString()
						, value, itemsList.getAsString());
			}
			for (Map.Entry<Integer, ItemStack> pSlots : slotMap.entrySet()) {
				player.getInventory().removeItem(pSlots.getKey(), pSlots.getValue().getCount());
			}
			inv.ifPresent((p) -> {
				for (Map.Entry<Integer, ItemStack> map : invSlotMap.entrySet()) {
					p.insertItem(map.getKey(), map.getValue(), false);
				}
			});
			player.sendSystemMessage(Component.translatable("message.shop.sell.success"
					, Config.getFormattedCurrency(value), getTransItemsDisplayString(transItems)));
			return;
		}
		//================SERVER BUY=================================================================================
		else if (action.equalsIgnoreCase("server-buy")) { //SERVER BUY
			//First check the available funds and stock for trade
			double balP = wsd.getBalance(AcctTypes.PLAYER.key, player.getUUID());
			if (value > balP) {
				player.sendSystemMessage(Component.translatable("message.shop.buy.failure.funds"));
				return;
			}
			wsd.changeBalance(AcctTypes.PLAYER.key, player.getUUID(), -value);
			if (Config.ENABLE_HISTORY.get()) {
				String itemPrint = "";
				itemsList.forEach((a) -> {itemPrint.concat(a.getAsString());});
				MoneyMod.dbm.postEntry(System.currentTimeMillis(), DatabaseManager.NIL, AcctTypes.SERVER.key, "Server"
						, player.getUUID(), AcctTypes.PLAYER.key, player.getName().getString()
						, -value, itemsList.getAsString());
			}
			for (int i = 0; i < transItems.size(); i++) {
				ItemStack pStack = transItems.get(i).copy();
				if (!player.addItem(pStack))
					player.drop(pStack, false);
			}
			player.sendSystemMessage(Component.translatable("message.shop.buy.success"
					, getTransItemsDisplayString(transItems), Config.getFormattedCurrency(value)));
			return;
		}
		//================SERVER SELL=================================================================================
		else if (action.equalsIgnoreCase("server-sell")) { //SERVER SELL
			Map<Integer, ItemStack> slotMap = new HashMap<>();
			for (int t = 0; t < transItems.size(); t++) {
				int stackSize = transItems.get(t).getCount();
				for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
					ItemStack inSlot = player.getInventory().getItem(i).copy();
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
					player.sendSystemMessage(Component.translatable("message.shop.sell.failure.stock"));
					return;
				}
				
			}
			wsd.changeBalance(AcctTypes.PLAYER.key, player.getUUID(), value);
			if (Config.ENABLE_HISTORY.get()) {
				String itemPrint = "";
				itemsList.forEach((a) -> {itemPrint.concat(a.getAsString());});
				MoneyMod.dbm.postEntry(System.currentTimeMillis(), DatabaseManager.NIL, AcctTypes.SERVER.key, "Server"
						, player.getUUID(), AcctTypes.PLAYER.key, player.getName().getString()
						, value, itemsList.getAsString());
			}
			for (Map.Entry<Integer, ItemStack> pSlots : slotMap.entrySet()) {
				player.getInventory().getItem(pSlots.getKey()).shrink(pSlots.getValue().getCount());
			}
			player.sendSystemMessage(Component.translatable("message.shop.sell.success"
					, Config.getFormattedCurrency(value), getTransItemsDisplayString(transItems)));
			return;
		}
	}
}
