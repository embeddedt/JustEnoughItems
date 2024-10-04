package mezz.jei.recipes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.ingredients.IngredientInformation;
import mezz.jei.ingredients.IngredientsForType;
import net.minecraft.util.ResourceLocation;

import java.util.*;

/**
 * A RecipeMap efficiently links recipes, IRecipeCategory, and Ingredients.
 */
public class RecipeMap {
	private final Object2ObjectOpenHashMap<IRecipeCategory<?>, Object2ObjectOpenHashMap<String, Object>> recipeCategoryToRecipesByUid = new Object2ObjectOpenHashMap<>();
	private final Multimap<String, ResourceLocation> categoryUidMap = Multimaps.newListMultimap(new Object2ObjectOpenHashMap<>(), () -> new ObjectArrayList<>(2));
	private final Comparator<ResourceLocation> recipeCategoryUidComparator;
	private final IIngredientManager ingredientManager;

	public RecipeMap(Comparator<ResourceLocation> recipeCategoryUidComparator, IIngredientManager ingredientManager) {
		this.recipeCategoryUidComparator = recipeCategoryUidComparator;
		this.ingredientManager = ingredientManager;
	}

	public <V> ImmutableList<ResourceLocation> getRecipeCategories(V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);

		Set<ResourceLocation> recipeCategories = new HashSet<>();

		for (String key : IngredientInformation.getUniqueIdsWithWildcard(ingredientHelper, ingredient, UidContext.Recipe)) {
			recipeCategories.addAll(categoryUidMap.get(key));
		}

		return ImmutableList.sortedCopyOf(recipeCategoryUidComparator, recipeCategories);
	}

	public <V> void addRecipeCategory(IRecipeCategory<?> recipeCategory, V ingredient, IIngredientHelper<V> ingredientHelper) {
		String key = ingredientHelper.getUniqueId(ingredient, UidContext.Recipe);
		Collection<ResourceLocation> recipeCategories = categoryUidMap.get(key);
		ResourceLocation recipeCategoryUid = recipeCategory.getUid();
		if (!recipeCategories.contains(recipeCategoryUid)) {
			recipeCategories.add(recipeCategoryUid);
		}
	}

	public <T, V> ImmutableList<T> getRecipes(IRecipeCategory<T> recipeCategory, V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);

		Map<String, Object> recipesForType = recipeCategoryToRecipesByUid.get(recipeCategory);

		if (recipesForType == null) {
			return ImmutableList.of();
		}

		ImmutableList.Builder<T> listBuilder = ImmutableList.builder();
		for (String key : IngredientInformation.getUniqueIdsWithWildcard(ingredientHelper, ingredient, UidContext.Recipe)) {
			@SuppressWarnings("unchecked")
			Object recipes = recipesForType.get(key);
			if (recipes instanceof List) {
				listBuilder.addAll((List<T>)recipes);
			} else if (recipes != null) {
				listBuilder.add((T)recipes);
			}
		}
		return listBuilder.build();
	}

	public <T> void addRecipe(T recipe, IRecipeCategory<T> recipeCategory, List<IngredientsForType<?>> ingredientsByType) {
		for (IngredientsForType<?> ingredientsForType : ingredientsByType) {
			addRecipe(recipe, recipeCategory, ingredientsForType);
		}
	}

	private <T, V> void addRecipe(T recipe, IRecipeCategory<T> recipeCategory, IngredientsForType<V> ingredientsForType) {
		IIngredientType<V> ingredientType = ingredientsForType.getIngredientType();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);

		Map<String, Object> recipesForType = recipeCategoryToRecipesByUid.computeIfAbsent(recipeCategory, c -> new Object2ObjectOpenHashMap<>());

		Set<String> uniqueIds = new HashSet<>();

		List<List<V>> ingredients = ingredientsForType.getIngredients();
		for (List<V> slot : ingredients) {
			for (V ingredient : slot) {
				if (ingredient == null) {
					continue;
				}

				String key = ingredientHelper.getUniqueId(ingredient, UidContext.Recipe);
				if (uniqueIds.contains(key)) {
					continue;
				} else {
					uniqueIds.add(key);
				}

				recipesForType.compute(key, (k, previousValue) -> {
					if (previousValue == null) {
						return recipe;
					} else if (previousValue instanceof ArrayList) {
						((ArrayList<T>)previousValue).add(recipe);
						return previousValue;
					} else {
						ArrayList<T> recipes = new ArrayList<>(2);
						recipes.add((T)previousValue);
						recipes.add(recipe);
						return recipes;
					}
				});

				addRecipeCategory(recipeCategory, ingredient, ingredientHelper);
			}
		}
	}
}
