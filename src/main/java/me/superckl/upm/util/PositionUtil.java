package me.superckl.upm.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class PositionUtil {

	public static GlobalPos getGlobalPos(final TileEntity te) {
		return GlobalPos.of(te.getLevel().dimension(), te.getBlockPos());
	}

	public static BlockState getState(final GlobalPos pos) {
		return PositionUtil.getLevel(pos).getBlockState(pos.pos());
	}

	public static Block getBlock(final GlobalPos pos) {
		return PositionUtil.getState(pos).getBlock();
	}

	public static TileEntity getTileEntity(final GlobalPos pos) {
		return PositionUtil.getLevel(pos).getBlockEntity(pos.pos());
	}

	public static boolean isLoaded(final GlobalPos pos) {
		return PositionUtil.getLevel(pos).isLoaded(pos.pos());
	}

	public static ServerWorld getLevel(final GlobalPos pos) {
		return ServerLifecycleHooks.getCurrentServer().getLevel(pos.dimension());
	}

}
