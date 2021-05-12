package me.superckl.upm.api;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

import it.unimi.dsi.fastutil.ints.IntComparators;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class NetworkMemberResolver<T extends NetworkMember> extends ForgeRegistryEntry<NetworkMemberResolver<?>>{

	public static Function<TileEntity, Comparator<NetworkMemberResolver<?>>> REVERSE_COMPARATOR_FACTORY = tile -> (member1, member2) ->
	-IntComparators.NATURAL_COMPARATOR.compare(member1.getPriority(tile), member2.getPriority(tile));

	/**
	 * Attempts to resolve the passed tile entity, discovered on the passed side, into
	 * a network member.
	 * @param tile The tile entity that needs to be resolved to a network member
	 * @param side The side the tile entity was discovered on.
	 * @return The network member if this resolver can handle the passed tile entity
	 */
	public abstract Optional<T> getNetworkMember(TileEntity tile, Direction side);

	/**
	 * @param tile The tile entity that needs to be resolved to a network member
	 * @return The priority of this resolver for the given tile entity. Use this to take priority
	 * over default resolvers for your own tile entities
	 */
	public abstract int getPriority(TileEntity tile);

	/**
	 * Determines the type of a tile entity from its {@link TileEntityType} tags
	 * @param teType The type of the tile entity
	 * @return The determined type, or {@link MemberType#UNKNOWN} if there is no tag on the type
	 */
	public MemberType typeFromTag(final TileEntityType<?> teType) {
		for(final MemberType type:MemberType.values())
			if(type != MemberType.UNKNOWN && teType.getTags().contains(type.tag()))
				return type;
		return MemberType.UNKNOWN;
	}

}
