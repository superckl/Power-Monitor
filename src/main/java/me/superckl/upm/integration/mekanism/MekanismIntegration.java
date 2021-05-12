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
				builder.add(te.get());
			builder.add(MekanismTileEntityTypes.CHARGEPAD.get());
			builder.add(MekanismTileEntityTypes.CHEMICAL_CRYSTALLIZER.get());
			builder.add(MekanismTileEntityTypes.CHEMICAL_DISSOLUTION_CHAMBER.get());
			builder.add(MekanismTileEntityTypes.CHEMICAL_INFUSER.get());
			builder.add(MekanismTileEntityTypes.CHEMICAL_INJECTION_CHAMBER.get());
			builder.add(MekanismTileEntityTypes.CHEMICAL_OXIDIZER.get());
			builder.add(MekanismTileEntityTypes.CHEMICAL_WASHER.get());
			builder.add(MekanismTileEntityTypes.COMBINER.get());
			builder.add(MekanismTileEntityTypes.CRUSHER.get());
			builder.add(MekanismTileEntityTypes.DIGITAL_MINER.get());
			builder.add(MekanismTileEntityTypes.ELECTRIC_PUMP.get());
			builder.add(MekanismTileEntityTypes.ELECTROLYTIC_SEPARATOR.get());
			builder.add(MekanismTileEntityTypes.ENERGIZED_SMELTER.get());
			builder.add(MekanismTileEntityTypes.ENRICHMENT_CHAMBER.get());
			builder.add(MekanismTileEntityTypes.FLUIDIC_PLENISHER.get());
			builder.add(MekanismTileEntityTypes.FORMULAIC_ASSEMBLICATOR.get());
			builder.add(MekanismTileEntityTypes.LASER.get());
			builder.add(MekanismTileEntityTypes.LASER_AMPLIFIER.get());
			builder.add(MekanismTileEntityTypes.LASER_TRACTOR_BEAM.get());
			builder.add(MekanismTileEntityTypes.METALLURGIC_INFUSER.get());
			builder.add(MekanismTileEntityTypes.OSMIUM_COMPRESSOR.get());
			builder.add(MekanismTileEntityTypes.PRECISION_SAWMILL.get());
			builder.add(MekanismTileEntityTypes.PRESSURE_DISPERSER.get());
			builder.add(MekanismTileEntityTypes.PRESSURIZED_REACTION_CHAMBER.get());
			builder.add(MekanismTileEntityTypes.PURIFICATION_CHAMBER.get());
			builder.add(MekanismTileEntityTypes.QUANTUM_ENTANGLOPORTER.get());
			builder.add(MekanismTileEntityTypes.RESISTIVE_HEATER.get());
			builder.add(MekanismTileEntityTypes.MODIFICATION_STATION.get());
			builder.add(MekanismTileEntityTypes.ISOTOPIC_CENTRIFUGE.get());
			builder.add(MekanismTileEntityTypes.NUTRITIONAL_LIQUIFIER.get());
			builder.add(MekanismTileEntityTypes.ROTARY_CONDENSENTRATOR.get());
			builder.add(MekanismTileEntityTypes.SECURITY_DESK.get());
			builder.add(MekanismTileEntityTypes.SEISMIC_VIBRATOR.get());
			builder.add(MekanismTileEntityTypes.SOLAR_NEUTRON_ACTIVATOR.get());
			builder.add(MekanismTileEntityTypes.SUPERHEATING_ELEMENT.get());
			builder.add(MekanismTileEntityTypes.TELEPORTER.get());
			builder.add(MekanismTileEntityTypes.ANTIPROTONIC_NUCLEOSYNTHESIZER.get());
		}else if(type == MemberType.CABLE) {
			builder.add(MekanismTileEntityTypes.BASIC_UNIVERSAL_CABLE.get());
			builder.add(MekanismTileEntityTypes.ADVANCED_UNIVERSAL_CABLE.get());
			builder.add(MekanismTileEntityTypes.ELITE_UNIVERSAL_CABLE.get());
			builder.add(MekanismTileEntityTypes.ULTIMATE_UNIVERSAL_CABLE.get());
		}else if(type == MemberType.STORAGE) {
			builder.add(MekanismTileEntityTypes.BASIC_ENERGY_CUBE.get());
			builder.add(MekanismTileEntityTypes.ADVANCED_ENERGY_CUBE.get());
			builder.add(MekanismTileEntityTypes.ELITE_ENERGY_CUBE.get());
			builder.add(MekanismTileEntityTypes.ULTIMATE_ENERGY_CUBE.get());
			builder.add(MekanismTileEntityTypes.CREATIVE_ENERGY_CUBE.get());
		}
	}

}
