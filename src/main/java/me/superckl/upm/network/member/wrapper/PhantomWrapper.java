package me.superckl.upm.network.member.wrapper;

import java.util.List;
import java.util.stream.Collectors;

import me.superckl.upm.api.MemberType;
import me.superckl.upm.network.member.PhantomNetworkMember;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class PhantomWrapper extends WrappedNetworkMember{

	public static final String TYPES_KEY = "tiles_types";
	public static final String BLOCKS_KEY = "blocks";

	private final List<Block> blocks;

	public PhantomWrapper(final MemberType type, final List<TileEntityType<?>> tileTypes, final List<Block> blocks) {
		super(new PhantomNetworkMember(type), tileTypes);
		this.blocks = blocks;
	}

	@Override
	public List<Block> toBlocks() {
		return this.blocks;
	}

	@Override
	public CompoundNBT serialize(final boolean toClient) {
		final CompoundNBT nbt = new CompoundNBT();
		nbt.put(WrappedNetworkMember.OVERRIDE_KEY, StringNBT.valueOf(this.getType().name()));
		nbt.put(PhantomWrapper.TYPES_KEY, ResourceLocation.CODEC.listOf().encodeStart(NBTDynamicOps.INSTANCE,
				this.tileTypes.stream().map(TileEntityType::getRegistryName).collect(Collectors.toList())).getOrThrow(false, val -> {}));
		nbt.put(PhantomWrapper.BLOCKS_KEY, ResourceLocation.CODEC.listOf().encodeStart(NBTDynamicOps.INSTANCE,
				this.blocks.stream().map(Block::getRegistryName).collect(Collectors.toList())).getOrThrow(false, val -> {}));
		return nbt;
	}

	public static PhantomWrapper deserialize(final CompoundNBT nbt) {
		final MemberType type = MemberType.valueOf(nbt.getString(WrappedNetworkMember.OVERRIDE_KEY));
		final List<TileEntityType<?>> tileTypes = ResourceLocation.CODEC.listOf().decode(NBTDynamicOps.INSTANCE,
				nbt.get(PhantomWrapper.TYPES_KEY)).getOrThrow(false, val -> {}).getFirst().stream()
				.map(rLoc -> ForgeRegistries.TILE_ENTITIES.getValue(rLoc)).collect(Collectors.toList());
		final List<Block> blocks = ResourceLocation.CODEC.listOf().decode(NBTDynamicOps.INSTANCE,
				nbt.get(PhantomWrapper.BLOCKS_KEY)).getOrThrow(false, val -> {}).getFirst().stream()
				.map(rLoc -> ForgeRegistries.BLOCKS.getValue(rLoc)).collect(Collectors.toList());
		return new PhantomWrapper(type, tileTypes, blocks);
	}

}
