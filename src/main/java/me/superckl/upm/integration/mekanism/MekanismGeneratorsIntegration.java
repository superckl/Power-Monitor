package me.superckl.upm.integration.mekanism;

import me.superckl.upm.integration.IntegrationModule;
import me.superckl.upm.network.member.MemberType;
import mekanism.generators.common.registries.GeneratorsTileEntityTypes;
import net.minecraft.data.TagsProvider.Builder;
import net.minecraft.tileentity.TileEntityType;

public class MekanismGeneratorsIntegration extends IntegrationModule{

	@Override
	public void addTETags(final Builder<TileEntityType<?>> builder, final MemberType type) {
		if(type == MemberType.GENERATOR) {
			builder.add(GeneratorsTileEntityTypes.ADVANCED_SOLAR_GENERATOR.get());
			builder.add(GeneratorsTileEntityTypes.BIO_GENERATOR.get());
			builder.add(GeneratorsTileEntityTypes.FISSION_REACTOR_PORT.get());
			builder.add(GeneratorsTileEntityTypes.FUSION_REACTOR_PORT.get());
			builder.add(GeneratorsTileEntityTypes.GAS_BURNING_GENERATOR.get());
			builder.add(GeneratorsTileEntityTypes.HEAT_GENERATOR.get());
			builder.add(GeneratorsTileEntityTypes.SOLAR_GENERATOR.get());
			builder.add(GeneratorsTileEntityTypes.WIND_GENERATOR.get());
		}
	}

}
