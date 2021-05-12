package me.superckl.upm.integration.thermal;

import cofh.thermal.core.init.TCoreReferences;
import me.superckl.upm.api.MemberType;
import me.superckl.upm.integration.IntegrationModule;
import net.minecraft.data.TagsProvider;
import net.minecraft.tileentity.TileEntityType;

public class CoFHCoreIntegration extends IntegrationModule{

	@Override
	public void addTETags(final TagsProvider.Builder<TileEntityType<?>> builder, final MemberType type) {
		if(type == MemberType.MACHINE) {
			builder.add(TCoreReferences.CHARGE_BENCH_TILE);
			builder.add(TCoreReferences.TINKER_BENCH_TILE);

		}else if(type == MemberType.STORAGE)
			builder.add(TCoreReferences.ENERGY_CELL_TILE);
	}

}
