package mezz.jei.plugins.vanilla.brewing;

import java.util.List;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;

public class PotionSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
	public static final PotionSubtypeInterpreter INSTANCE = new PotionSubtypeInterpreter();

	private PotionSubtypeInterpreter() {

	}

	@Override
	public String apply(ItemStack itemStack, UidContext context) {
		if (!itemStack.hasTag()) {
			return IIngredientSubtypeInterpreter.NONE;
		}
		Potion potionType = PotionUtils.getPotion(itemStack);
		StringBuilder stringBuilder = new StringBuilder(potionType.getName(""));
		List<EffectInstance> effects = PotionUtils.getMobEffects(itemStack);
		for (EffectInstance effect : effects) {
			stringBuilder.append(";");
			stringBuilder.append(Registry.MOB_EFFECT.getId(effect.getEffect()));
			if(effect.getAmplifier() > 0) {
				stringBuilder.append("x");
				stringBuilder.append(effect.getAmplifier());
			}
			stringBuilder.append("d");
			stringBuilder.append(effect.getDuration());
			if(effect.splash) {
				stringBuilder.append("s");
			}
			if(!effect.isVisible()) {
				stringBuilder.append("h");
			}
			if(!effect.showIcon()) {
				stringBuilder.append("i");
			}
		}

		return stringBuilder.toString();
	}
}
