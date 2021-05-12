package me.superckl.upm.integration.mekanism;

import me.superckl.upm.api.MemberType;
import me.superckl.upm.integration.IntegrationModule;
import mekanism.generators.common.registries.GeneratorsTileEntityTypes;
import net.minecraft.data.TagsProvider.Builder;
import net.minecraft.tileentity.TileEntityType;

public class MekanismGeneratorsIntegration extends IntegrationModule{

	@Override
	public void addTETags(final Builder<TileEntityType<?>> builder, final MemberType type) {
		if(type == MemberType.GENERATOR) {
			builder.addOptional(GeneratorsTileEntityTypes.ADVANCED_SOLAR_GENERATOR.get().getRegistryName());
			builder.addOptional(GeneratorsTileEntityTypes.BIO_GENERATOR.get().getRegistryName());
			builder.addOptional(GeneratorsTileEntityTypes.FISSION_REACTOR_PORT.get().getRegistryName());
			builder.addOptional(GeneratorsTileEntityTypes.FUSION_REACTOR_PORT.get().getRegistryName());
			builder.addOptional(GeneratorsTileEntityTypes.GAS_BURNING_GENERATOR.get().getRegistryName());
			builder.addOptional(GeneratorsTileEntityTypes.HEAT_GENERATOR.get().getRegistryName());
			builder.addOptional(GeneratorsTileEntityTypes.SOLAR_GENERATOR.get().getRegistryName());
			builder.addOptional(GeneratorsTileEntityTypes.WIND_GENERATOR.get().getRegistryName());
		}
	}

}
