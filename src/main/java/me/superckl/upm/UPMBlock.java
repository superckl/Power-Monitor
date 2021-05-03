package me.superckl.upm;

import me.superckl.upm.packet.OpenUPMScreenPacket;
import me.superckl.upm.packet.UPMPacketHandler;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class UPMBlock extends HorizontalBlock{

	public UPMBlock() {
		super(AbstractBlock.Properties.of(Material.METAL).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL));
	}

	@Override
	public boolean hasTileEntity(final BlockState state) {
		return true;
	}

	@Override
	public UPMTile createTileEntity(final BlockState state, final IBlockReader world) {
		return new UPMTile();
	}

	@Override
	public BlockState getStateForPlacement(final BlockItemUseContext context) {
		return this.defaultBlockState().setValue(HorizontalBlock.FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public int getSignal(final BlockState state, final IBlockReader level, final BlockPos position, final Direction direction) {
		final Direction facing = state.getValue(HorizontalBlock.FACING);
		if(direction != facing)
			return 0;
		final TileEntity te = level.getBlockEntity(position);
		if(te instanceof UPMTile && ((UPMTile)te).isRedstoneOutput())
			return 15;
		return 0;
	}

	@Override
	public int getDirectSignal(final BlockState state, final IBlockReader level, final BlockPos position, final Direction direction) {
		return state.getSignal(level, position, direction);
	}

	@Override
	public boolean isSignalSource(final BlockState state) {
		return true;
	}

	@Override
	public ActionResultType use(final BlockState state, final World level, final BlockPos pos,
			final PlayerEntity player, final Hand hand, final BlockRayTraceResult trace) {
		if(!level.isClientSide)
			UPMPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
					new OpenUPMScreenPacket(pos));
		return ActionResultType.sidedSuccess(level.isClientSide);
	}

	@Override
	protected void createBlockStateDefinition(final Builder<Block, BlockState> builder) {
		builder.add(HorizontalBlock.FACING);
		super.createBlockStateDefinition(builder);
	}

}
