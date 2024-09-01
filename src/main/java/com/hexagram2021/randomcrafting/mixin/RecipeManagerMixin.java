package com.hexagram2021.randomcrafting.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hexagram2021.randomcrafting.config.RCCommonConfig;
import com.hexagram2021.randomcrafting.config.RCServerConfig;
import com.hexagram2021.randomcrafting.util.IMessUpRecipes;
import com.hexagram2021.randomcrafting.util.IMutableItemStack;
import com.hexagram2021.randomcrafting.util.ListShuffler;
import com.hexagram2021.randomcrafting.util.RCLogger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin implements IMessUpRecipes {

	@Shadow
	private Map<ResourceLocation, RecipeHolder<?>> byName;

	@Unique
	private Map<RecipeType<?>, Map<ResourceLocation, RecipeHolder<?>>> backup_recipes;

	@Shadow
	protected static RecipeHolder<?> fromJson(ResourceLocation resourceLocation, JsonObject jsonObject, HolderLookup.Provider provider) {
		throw new AssertionError();
	}

	@Shadow
	private HolderLookup.Provider registries;

	@Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At(value = "TAIL"))
	public void init_backups(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profilerFiller, CallbackInfo ci) {
		Map<RecipeType<?>, ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>>> backup_map = Maps.newHashMap();

		for(Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
			ResourceLocation id = entry.getKey();

			try {
				RecipeHolder<?> recipeHolder = fromJson(id, GsonHelper.convertToJsonObject(entry.getValue(), "top element"), this.registries);
				Recipe<?> recipe = recipeHolder.value();
				if(!RCCommonConfig.WHITELIST_RECIPE_TYPES.get().contains(recipe.getType().toString()) &&
						!RCCommonConfig.WHITELIST_RECIPES.get().contains(recipeHolder.id().toString())) {
					backup_map.computeIfAbsent(recipe.getType(), recipeType -> ImmutableMap.builder()).put(id, recipeHolder);
				}
			} catch (IllegalArgumentException | JsonParseException ignored) { }
		}

		this.backup_recipes = backup_map.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> entry.getValue().build()));
	}

	@Override
	public void revoke(RegistryAccess registryAccess) {
		this.backup_recipes.forEach((recipeType, recipeMap) -> {
			Map<ResourceLocation, RecipeHolder<?>> originalMap = this.byName;
			recipeMap.forEach((id, recipe) -> {
				if(originalMap.get(id) == null) {
					RCLogger.error("Find a null recipe: " + recipeType + " - <" + id + ">");
				} else {
					ItemStack itemStack = originalMap.get(id).value().getResultItem(registryAccess);
					ItemStack target = recipe.value().getResultItem(registryAccess);
					((IMutableItemStack) (Object) itemStack).setItemAndCount(target.getItem(), target.getCount());
				}
			});
		});
	}

	@Override
	public void messup(RandomSource random, RegistryAccess registryAccess) {
		if (!RCCommonConfig.ENABLE_RECIPE_SHUFFLING.get()) {
			RCLogger.info("Recipe shuffling is disabled. Skipping recipe modifications.");
			return;
		}
		List<Triple<RecipeType<?>, ResourceLocation, Integer>> list = Lists.newArrayList();
		List<ItemStack> results = Lists.newArrayList();
		List<RecipeType<?>> recipeTypes = Lists.newArrayList();

		this.backup_recipes.forEach((recipeType, recipeMap) ->
				recipeTypes.add(recipeType)
		);
		recipeTypes.sort(Comparator.comparing(Object::toString));

		if(RCServerConfig.TYPE_SEPARATED.get()) {
			recipeTypes.forEach(recipeType -> {
				RCLogger.debug("Shuffling " + recipeType);
				List<ItemStack> temp_results = Lists.newArrayList();
				this.backup_recipes.get(recipeType).forEach((id, recipe) -> {
					ItemStack resultItem = recipe.value().getResultItem(registryAccess);
					if(!resultItem.isEmpty()) {
						list.add(Triple.of(recipeType, id, results.size() + temp_results.size()));
						temp_results.add(resultItem);
					} else {
						RCLogger.error("Skipping empty recipe: " + recipeType + " - <" + id + ">");
					}
				});
				ListShuffler.shuffle(temp_results, random);
				results.addAll(temp_results);
			});
		} else {
			recipeTypes.forEach(recipeType -> this.backup_recipes.get(recipeType).forEach((id, recipe) -> {
				ItemStack resultItem = recipe.value().getResultItem(registryAccess);
				if (!resultItem.isEmpty()) {
					list.add(Triple.of(recipeType, id, results.size()));
					results.add(resultItem);
				} else {
					RCLogger.warn("Skipping empty recipe: " + recipeType + " - <" + id + ">");
				}
			}));
			ListShuffler.shuffle(results, random);
		}

		list.forEach(tp -> {
			ItemStack itemStack = this.byName.get(tp.getMiddle()).value().getResultItem(registryAccess);
			ItemStack target = results.get(tp.getRight());
			((IMutableItemStack)(Object) itemStack).setItemAndCount(target.getItem(), target.getCount());
		});
	}
}
