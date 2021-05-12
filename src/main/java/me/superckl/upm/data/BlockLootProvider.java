package me.superckl.upm.data;

import com.google.common.collect.ImmutableList;

import me.superckl.upm.ModRegisters;
import net.minecraft.block.Block;
import net.minecraft.data.loot.BlockLootTables;

public class BlockLootProvider extends BlockLootTables{

	@Override
	protected void addTables() {
		this.dropSelf(ModRegisters.UPM_BLOCK.get());
	}

	@Override
	protected Iterable<Block> getKnownBlocks() {
		return ImmutableList.of(ModRegisters.UPM_BLOCK.get());
	}

}
