package dicemc.money.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dicemc.money.MoneyMod;
import dicemc.money.MoneyMod.AcctTypes;
import dicemc.money.api.IMoneyManager;
import dicemc.money.setup.Config;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MoneyWSD extends SavedData implements IMoneyManager {
    public static final Codec<MoneyWSD> CODEC = RecordCodecBuilder.create((moneyWSDInstance ->
            moneyWSDInstance.group(
                    MoneyWSD.BalancesWithType.CODEC.listOf()
                            .optionalFieldOf("types", List.of())
                            .forGetter((wsd) -> wsd.accounts.entrySet().stream().map(resourceLocationMapEntry -> {
                                List<MoneyWSD.BalanceWithId> balances = resourceLocationMapEntry.getValue().entrySet().stream().map(entry -> new MoneyWSD.BalanceWithId(entry.getKey(), entry.getValue())).toList();
                                return new BalancesWithType(resourceLocationMapEntry.getKey(), balances);
                            }).toList())
            ).apply(moneyWSDInstance, MoneyWSD::new)));
    private static final String DATA_NAME = MoneyMod.MOD_ID + "_data";
    private static final SavedDataType<MoneyWSD> TYPE = new SavedDataType<>(DATA_NAME, MoneyWSD::new, CODEC);
    private final Map<ResourceLocation, Map<UUID, Double>> accounts = new HashMap<>();

    public MoneyWSD() {
        this.setDirty();
    }

    private MoneyWSD(List<MoneyWSD.BalancesWithType> types) {
        for (MoneyWSD.BalancesWithType balancesWithType : types) {
            this.accounts.put(balancesWithType.type, balancesWithType.balances.stream().collect(Collectors.toMap(MoneyWSD.BalanceWithId::id, MoneyWSD.BalanceWithId::balance)));
        }
    }

    public static MoneyWSD get() {
        if (ServerLifecycleHooks.getCurrentServer() != null)
            return ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage().computeIfAbsent(TYPE);
        else
            return new MoneyWSD();
    }
	
	public Map<UUID, Double> getAccountMap(ResourceLocation res) {return accounts.getOrDefault(res, new HashMap<>());}

	@Override
	public double getBalance(ResourceLocation type, UUID owner) {
		accountChecker(type, owner);
		return accounts.getOrDefault(type, new HashMap<>()).get(owner);
	}
	
	@Override
	public boolean setBalance(ResourceLocation type, UUID id, double value) {
		if (type != null && accounts.containsKey(type)) {
			if (id != null) {				
				accounts.get(type).put(id, value);
				this.setDirty();
				if (Config.ENABLE_HISTORY.get()) {
					
				}
				return true;
			}
		}
		return false;
	}

    @Override
    public boolean changeBalance(ResourceLocation type, UUID id, double value) {
        if (type == null || id == null) return false;
        double current = getBalance(type, id);
        double future = current + value;
        return setBalance(type, id, future);
    }

    @Override
    public boolean transferFunds(ResourceLocation fromType, UUID fromID, ResourceLocation toType, UUID toID, double value) {
        if (fromType == null || fromID == null || toType == null || toID == null) return false;
        double funds = Math.abs(value);
        double fromBal = getBalance(fromType, fromID);
        if (fromBal < funds) return false;
        if (changeBalance(fromType, fromID, -funds) && changeBalance(toType, toID, funds)) {
            this.setDirty();
            return true;
        } else
            return false;
    }

    public void accountChecker(ResourceLocation type, UUID owner) {
        if (type != null && !accounts.containsKey(type)) {
            accounts.put(type, new HashMap<>());
            this.setDirty();
        }
        if (owner != null && !accounts.get(type).containsKey(owner)) {
            accounts.get(type).put(owner, Config.STARTING_FUNDS.get());
            if (Config.ENABLE_HISTORY.get())
                MoneyMod.dbm.postEntry(System.currentTimeMillis(), DatabaseManager.NIL, AcctTypes.SERVER.key, "Server"
                        , owner, type, MoneyMod.dbm.server.getProfileCache().get(owner).get().getName()
                        , Config.STARTING_FUNDS.get(), "Starting Funds Deposit");
            this.setDirty();
        }
    }

    record BalancesWithType(ResourceLocation type, List<MoneyWSD.BalanceWithId> balances) {
        public static final Codec<MoneyWSD.BalancesWithType> CODEC = RecordCodecBuilder.create((balancesWithTypeInstance) -> balancesWithTypeInstance.group(
                Codec.STRING.fieldOf("type").forGetter(balancesWithType -> balancesWithType.type.toString()),
                MoneyWSD.BalanceWithId.CODEC.listOf()
                        .optionalFieldOf("data", List.of())
                        .forGetter((balancesWithType) -> balancesWithType.balances)
        ).apply(balancesWithTypeInstance, MoneyWSD.BalancesWithType::from));

        public static MoneyWSD.BalancesWithType from(String type, List<MoneyWSD.BalanceWithId> balances) {
            return new MoneyWSD.BalancesWithType(ResourceLocation.parse(type), balances);
        }
    }

    record BalanceWithId(UUID id, Double balance) {
        public static final Codec<MoneyWSD.BalanceWithId> CODEC = RecordCodecBuilder.create((balanceWithIdInstance) -> balanceWithIdInstance.group(
                Codec.INT_STREAM.fieldOf("id").forGetter(balanceWithId -> Arrays.stream(UUIDUtil.uuidToIntArray(balanceWithId.id))),
                Codec.DOUBLE.fieldOf("balance").forGetter(BalanceWithId::balance)
        ).apply(balanceWithIdInstance, MoneyWSD.BalanceWithId::from));

        private static BalanceWithId from(IntStream intStream, Double balance) {
            return new MoneyWSD.BalanceWithId(UUIDUtil.uuidFromIntArray(intStream.toArray()), balance);
        }
    }
}
