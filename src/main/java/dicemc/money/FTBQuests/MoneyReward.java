package dicemc.money.FTBQuests;

/*import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dicemc.money.MoneyMod.AcctTypes;
import dicemc.money.setup.Config;
import dicemc.money.storage.MoneyWSD;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
*/
public class MoneyReward /*extends Reward*/{
	/*public static RewardType MONEY_REWARD = FTBQHandler.MONEY_REWARD;
	public double amount = 0;

	public MoneyReward(Quest q) {
		super(q);
	}
	
	@Override
	public RewardType getType() {return MONEY_REWARD;}

	@Override
	public void claim(ServerPlayer player, boolean bool) {
		MoneyWSD.get(player.getServer().overworld()).changeBalance(AcctTypes.PLAYER.key, player.getUUID(), amount);		
	}
	
	@Override
    public void writeData( CompoundTag nbt )
    {
        super.writeData( nbt );
        nbt.putDouble("amount", amount);
    }

    @Override
    public void readData( CompoundTag nbt )
    {
        super.readData( nbt );
        amount = nbt.getDouble("amount");
    }

    @Override
    public void writeNetData( FriendlyByteBuf buffer )
    {
        super.writeNetData(buffer );
        buffer.writeDouble( amount );
    }

    @Override
    public void readNetData( FriendlyByteBuf buffer )
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
    public MutableComponent getAltTitle()
    {
        return new TranslatableComponent("ftbquests.reward.dicemcmm.moneyreward").append(" "+Config.CURRENCY_SYMBOL.get()+String.valueOf(amount));
    }
*/
}
