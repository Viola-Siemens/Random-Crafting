package com.hexagram2021.randomcrafting.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class RCServerConfig {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.LongValue SALT;
	public static final ForgeConfigSpec.BooleanValue DISABLE;
	public static final ForgeConfigSpec.IntValue PERMISSION_LEVEL_RESHUFFLE;
	public static final ForgeConfigSpec.IntValue PERMISSION_LEVEL_REVOKE;
	public static final ForgeConfigSpec.BooleanValue TYPE_SEPARATED;

	static {
		BUILDER.push("randomcrafting-server-config");
			SALT = BUILDER.comment("The salt for random messing up the recipes. You can change it dynamically by using command `/rc reshuffle`.")
					.defineInRange("SALT", 0L, -9223372036854775808L, 9223372036854775807L);
			DISABLE = BUILDER.comment("Disable this mod in your world.")
					.define("DISABLE", false);
			PERMISSION_LEVEL_RESHUFFLE = BUILDER.comment("The permission level for command `/rc reshuffle`.")
					.defineInRange("PERMISSION_LEVEL_RESHUFFLE", 2, 0, 4);
			PERMISSION_LEVEL_REVOKE = BUILDER.comment("The permission level for command `/rc revoke`.")
					.defineInRange("PERMISSION_LEVEL_REVOKE", 2, 0, 4);

			TYPE_SEPARATED = BUILDER.comment("Set true if you don't want all recipe types mess up with each other. For example, minecraft:stone is uncraftable in vanilla but you can get it from furnace - if you set this to false, you may get it from crafting instead of smelting.")
					.define("TYPE_SEPARATED", true);
		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
