package me.superckl.upm.network.member;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

import it.unimi.dsi.fastutil.ints.IntComparators;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class NetworkMemberResolver<T extends NetworkMember> extends ForgeRegistryEntry<NetworkMemberResolver<?>>{

	public static Function<TileEntity, Comparator<NetworkMemberResolver<?>>> REVERSE_COMPARATOR_FACTORY =
			tile -> (member1, member2) -> -IntComparators.NATURAL_COMPARATOR.compare(member1.getPriority(tile), member2.getPriority(tile));

			public abstract Optional<T> getNetworkMember(TileEntity tile, Direction side);
			public abstract int getPriority(TileEntity tile);

			public MemberType typeFromTag(final TileEntityType<?> teType) {
				if(teType.getTags().contains(MemberType.STORAGE.tag()))
					return MemberType.STORAGE;
				if(teType.getTags().contains(MemberType.CABLE.tag()))
					return MemberType.CABLE;
				if(teType.getTags().contains(MemberType.MACHINE.tag()))
					return MemberType.MACHINE;
				return MemberType.UNKNOWN;
			}

}