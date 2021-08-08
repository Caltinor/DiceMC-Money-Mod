package dicemc.money.FTBQuests;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dicemc.money.MoneyMod;
import dicemc.money.MoneyMod.AcctTypes;
import dicemc.money.setup.Config;
import dicemc.money.storage.DatabaseManager;
import dicemc.money.storage.MoneyWSD;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MoneyReward extends Reward{
	public static RewardType MONEY_REWARD = FTBQHandler.MONEY_REWARD;
	public double amount = 1;

	public MoneyReward(Quest q) {
		super(q);
	}
	
	@Override
	public RewardType getType() {return MONEY_REWARD;}

	@Override
	public void claim(ServerPlayerEntity player, boolean bool) {
		MoneyWSD.get(player.getServer().overworld()).changeBalance(AcctTypes.PLAYER.key, player.getUUID(), amount);
		if (Config.ENABLE_HISTORY.get()) {
			MoneyMod.dbm.postEntry(System.currentTimeMillis(), DatabaseManager.NIL, AcctTypes.SERVER.key, "Server"
					, player.getUUID(), AcctTypes.PLAYER.key, player.getName().getContents()
					, amount, "Quest Claim Reward");
		}
	}
	
	@Override
    public void writeData( CompoundNBT nbt )
    {
        super.writeData( nbt );
        nbt.putDouble("amount", amount);
    }

    @Override
    public void readData( CompoundNBT nbt )
    {
        super.readData( nbt );
        amount = nbt.getDouble("amount");
    }

    @Override
    public void writeNetData( PacketBuffer buffer )
    {
        super.writeNetData(buffer );
        buffer.writeDouble( amount );
    }

    @Override
    public void readNetData( PacketBuffer buffer )
    {
        super.readNetData(buffer );
        amount = buffer.readDouble();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getConfig( ConfigGroup config )
    {
        super.getConfig( config );
        config.addDouble( "amount", amount, input -> amount = input, 1d, 0d, Double.MAX_VALUE );
    }
    
    @Override
    public IFormattableTextComponent getAltTitle()
    {
        return new TranslationTextComponent("ftbquests.reward.dicemcmm.moneyreward").append(" "+Config.CURRENCY_SYMBOL.get()+String.valueOf(amount));
    }

}
