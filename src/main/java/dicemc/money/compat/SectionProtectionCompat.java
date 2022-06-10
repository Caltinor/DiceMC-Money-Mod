package dicemc.money.compat;

/*import com.matyrobbrt.sectionprotection.api.ActionType;
import com.matyrobbrt.sectionprotection.api.SectionProtectionAPI;
import dicemc.money.MoneyMod;*/

public class SectionProtectionCompat {

    public static void init() {
        /*SectionProtectionAPI.INSTANCE.registerPredicate(ActionType.BLOCK_INTERACTION, (player, interactionType, hand, level, pos, state) -> {
            final var entity = level.getBlockEntity(pos);
            if (entity != null && entity.getTileData().contains("is-shop"))
                return ActionType.Result.ALLOW;
            return ActionType.Result.CONTINUE;
        });
        MoneyMod.LOGGER.debug("Enabled SectionProtection integration: shops can now be used by everyone in claimed chunks.");*/
    }
}
