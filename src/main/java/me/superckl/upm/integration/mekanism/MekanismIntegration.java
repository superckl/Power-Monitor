package me.superckl.upm.integration.mekanism;

import me.superckl.upm.api.MemberType;
import me.superckl.upm.integration.IntegrationModule;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismTileEntityTypes;
import net.minecraft.data.TagsProvider.Builder;
import net.minecraft.tileentity.TileEntityType;

public class MekanismIntegration extends IntegrationModule{

	@Override
	public void addTETags(final Builder<TileEntityType<?>> builder, final MemberType type) {
		if(type == MemberType.MACHINE) {
			for(final TileEntityTypeRegistryObject<?> te:MekanismTileEntityTypes.getFactoryTiles())
				builder.addOptional(te.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.CHARGEPAD.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.CHEMICAL_CRYSTALLIZER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.CHEMICAL_DISSOLUTION_CHAMBER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.CHEMICAL_INFUSER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.CHEMICAL_INJECTION_CHAMBER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.CHEMICAL_OXIDIZER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.CHEMICAL_WASHER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.COMBINER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.CRUSHER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.DIGITAL_MINER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.ELECTRIC_PUMP.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.ELECTROLYTIC_SEPARATOR.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.ENERGIZED_SMELTER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.ENRICHMENT_CHAMBER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.FLUIDIC_PLENISHER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.FORMULAIC_ASSEMBLICATOR.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.LASER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.LASER_AMPLIFIER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.LASER_TRACTOR_BEAM.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.METALLURGIC_INFUSER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.OSMIUM_COMPRESSOR.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.PRECISION_SAWMILL.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.PRESSURE_DISPERSER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.PRESSURIZED_REACTION_CHAMBER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.PURIFICATION_CHAMBER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.QUANTUM_ENTANGLOPORTER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.RESISTIVE_HEATER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.MODIFICATION_STATION.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.ISOTOPIC_CENTRIFUGE.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.NUTRITIONAL_LIQUIFIER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.ROTARY_CONDENSENTRATOR.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.SECURITY_DESK.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.SEISMIC_VIBRATOR.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.SOLAR_NEUTRON_ACTIVATOR.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.SUPERHEATING_ELEMENT.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.TELEPORTER.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.ANTIPROTONIC_NUCLEOSYNTHESIZER.get().getRegistryName());
		}else if(type == MemberType.CABLE) {
			builder.addOptional(MekanismTileEntityTypes.BASIC_UNIVERSAL_CABLE.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.ADVANCED_UNIVERSAL_CABLE.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.ELITE_UNIVERSAL_CABLE.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.ULTIMATE_UNIVERSAL_CABLE.get().getRegistryName());
		}else if(type == MemberType.STORAGE) {
			builder.addOptional(MekanismTileEntityTypes.BASIC_ENERGY_CUBE.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.ADVANCED_ENERGY_CUBE.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.ELITE_ENERGY_CUBE.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.ULTIMATE_ENERGY_CUBE.get().getRegistryName());
			builder.addOptional(MekanismTileEntityTypes.CREATIVE_ENERGY_CUBE.get().getRegistryName());
		}
	}

}
