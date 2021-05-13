package me.superckl.upm.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.GlobalPos;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class PositionUtil {

	public static GlobalPos getGlobalPos(final TileEntity te) {
		return GlobalPos.of(te.getLevel().dimension(), te.getBlockPos());
	}

	public static BlockState getState(final GlobalPos pos) {
		return ServerLifecycleHooks.getCurrentServer().getLevel(pos.dimension()).getBlockState(pos.pos());
	}

	public static Block getBlock(final GlobalPos pos) {
		return PositionUtil.getState(pos).getBlock();
	}

	public static TileEntity getTileEntity(final GlobalPos pos) {
		return ServerLifecycleHooks.getCurrentServer().getLevel(pos.dimension()).getBlockEntity(pos.pos());
	}

}
