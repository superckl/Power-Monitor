package me.superckl.upm.api;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class UPMAPI {

	/**
	 * Id for the UPM mod
	 */
	public static final String MOD_ID = "upm";
	
	/**
	 * Registry for resolvers, populated by UPM.
	 */
	public static Supplier<IForgeRegistry<NetworkMemberResolver<?>>> RESOLVER_REGISTRY;
	
	/**
	 * Map of all tags used for determining tile entity member types
	 */
	public static final Map<MemberType, INamedTag<TileEntityType<?>>> TAGS = Util.make(new EnumMap<>(MemberType.class), map -> {
		for(final MemberType type:MemberType.values())
			if(type != MemberType.UNKNOWN)
				map.put(type, ForgeTagHandler.makeWrapperTag(ForgeRegistries.TILE_ENTITIES, new ResourceLocation(UPMAPI.MOD_ID, type.name().toLowerCase())));
	});

	/**
	 * Attempt to determine the tile entity type's member type by tag
	 * @param tileType The tile entity type
	 * @return The tagged member type, or empty if the tile is not tagged
	 */
	public static Optional<MemberType> memberTypeByTag(TileEntityType<?> tileType) {
		for(MemberType type:TAGS.keySet())
			if(TAGS.get(type).contains(tileType))
				return Optional.of(type);
		return Optional.empty();
	}
	
}
