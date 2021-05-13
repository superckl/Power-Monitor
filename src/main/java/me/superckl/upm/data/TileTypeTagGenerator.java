package me.superckl.upm.data;

import me.superckl.upm.UPM;
import me.superckl.upm.api.MemberType;
import me.superckl.upm.api.UPMAPI;
import net.minecraft.data.DataGenerator;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeRegistryTagsProvider;
import net.minecraftforge.registries.ForgeRegistries;

public class TileTypeTagGenerator extends ForgeRegistryTagsProvider<TileEntityType<?>>{

	public TileTypeTagGenerator(final DataGenerator generatorIn, final ExistingFileHelper existingFileHelper) {
		super(generatorIn, ForgeRegistries.TILE_ENTITIES, UPMAPI.MOD_ID, existingFileHelper);
	}

	@Override
	public String getName() {
		return "Tags: "+UPMAPI.MOD_ID;
	}

	@Override
	protected void addTags() {
		for(final MemberType type:MemberType.values()) {
			if(type == MemberType.UNKNOWN)
				continue;
			final Builder<TileEntityType<?>> builder = this.tag(UPMAPI.TAGS.get(type));
			//Mods with many tile entities are handled within their integration to avoid typos
			UPM.getINSTANCE().getIntegrations().forEach(integ -> integ.addTETags(builder, type));
			//Add some entries for mods with only a few tile entities to account for
			this.addThermal(builder, type);
			this.addBiggerReactors(builder, type);
			this.addPowah(builder, type);
			this.addRFTools(builder, type);
			//IF has a ton of machines but there's no good way to easily grab them from the mod source :/
			this.addIndustrialForegoing(builder, type);
			this.addSilent(builder, type);

			//IE is handled in its integration because it has a lot and uses a resolver
			//Mekanism is handled in its integration because it has an inane amount of tiles
		}
	}

	private void addThermal(final Builder<TileEntityType<?>> builder, final MemberType type) {
		final String thermalName = "thermal";
		switch(type) {
		case GENERATOR:
			builder.addOptional(new ResourceLocation(thermalName, "dynamo_compression"));
			builder.addOptional(new ResourceLocation(thermalName, "dynamo_lapidary"));
			builder.addOptional(new ResourceLocation(thermalName, "dynamo_magmatic"));
			builder.addOptional(new ResourceLocation(thermalName, "dynamo_numismatic"));
			builder.addOptional(new ResourceLocation(thermalName, "dynamo_stirling"));
			break;
		case MACHINE:
			builder.addOptional(new ResourceLocation(thermalName, "charge_bench"));
			builder.addOptional(new ResourceLocation(thermalName, "tinker_bench"));

			builder.addOptional(new ResourceLocation(thermalName, "machine_bottler"));
			builder.addOptional(new ResourceLocation(thermalName, "machine_brewer"));
			builder.addOptional(new ResourceLocation(thermalName, "machine_centrifuge"));
			builder.addOptional(new ResourceLocation(thermalName, "machine_chiller"));
			builder.addOptional(new ResourceLocation(thermalName, "machine_crafter"));
			builder.addOptional(new ResourceLocation(thermalName, "machine_crucible"));
			builder.addOptional(new ResourceLocation(thermalName, "machine_furnace"));
			builder.addOptional(new ResourceLocation(thermalName, "machine_insolator"));
			builder.addOptional(new ResourceLocation(thermalName, "machine_press"));
			builder.addOptional(new ResourceLocation(thermalName, "machine_pulverizer"));
			builder.addOptional(new ResourceLocation(thermalName, "machine_pyrolyzer"));
			builder.addOptional(new ResourceLocation(thermalName, "machine_refinery"));
			builder.addOptional(new ResourceLocation(thermalName, "machine_sawmill"));
			builder.addOptional(new ResourceLocation(thermalName, "machine_smelter"));
			break;
		case STORAGE:
			builder.addOptional(new ResourceLocation(thermalName, "energy_cell"));
			break;
		default:
			break;
		}
	}

	private void addBiggerReactors(final Builder<TileEntityType<?>> builder, final MemberType type) {
		final String reactorsName = "biggerreactors";
		switch(type) {
		case GENERATOR:
			builder.addOptional(new ResourceLocation(reactorsName, "reactor_power_tap"));
			builder.addOptional(new ResourceLocation(reactorsName, "turbine_power_tap"));
			break;
		case MACHINE:
			builder.addOptional(new ResourceLocation(reactorsName, "cyanite_reprocessor"));
		default:
			break;
		}
	}

	private void addPowah(final Builder<TileEntityType<?>> builder, final MemberType type) {
		final String powahName = "powah";
		switch(type) {
		case CABLE:
			builder.addOptional(new ResourceLocation(powahName, "ender_gate"));
			builder.addOptional(new ResourceLocation(powahName, "energy_cable"));
			builder.addOptional(new ResourceLocation(powahName, "energy_hopper"));
			break;
		case STORAGE:
			builder.addOptional(new ResourceLocation(powahName, "energy_cell"));
			builder.addOptional(new ResourceLocation(powahName, "ender_cell"));
			break;
		case MACHINE:
			builder.addOptional(new ResourceLocation(powahName, "energy_discharger"));
			builder.addOptional(new ResourceLocation(powahName, "player_transmitter"));
			builder.addOptional(new ResourceLocation(powahName, "energizing_rod"));
			break;
		case GENERATOR:
			builder.addOptional(new ResourceLocation(powahName, "reactor"));
			builder.addOptional(new ResourceLocation(powahName, "reactor_part"));
			builder.addOptional(new ResourceLocation(powahName, "solar_panel"));
			builder.addOptional(new ResourceLocation(powahName, "furnator"));
			builder.addOptional(new ResourceLocation(powahName, "magmator"));
			builder.addOptional(new ResourceLocation(powahName, "thermo_gen"));
			break;
		default:
			break;
		}
	}

	private void addRFTools(final Builder<TileEntityType<?>> builder, final MemberType type) {
		final String builderName = "rftoolsbuilder";
		switch(type) {
		case MACHINE:
			builder.addOptional(new ResourceLocation(builderName, "builder"));
			builder.addOptional(new ResourceLocation(builderName, "shield_block1"));
			builder.addOptional(new ResourceLocation(builderName, "shield_block2"));
			builder.addOptional(new ResourceLocation(builderName, "shield_block3"));
			builder.addOptional(new ResourceLocation(builderName, "shield_block4"));
			break;
		default:
			break;
		}
	}

	private void addIndustrialForegoing(final Builder<TileEntityType<?>> builder, final MemberType type) {
		final String name = "industrialforegoing";
		switch(type) {
		case GENERATOR:
			builder.addOptional(new ResourceLocation(name, "pitiful_generator"));
			builder.addOptional(new ResourceLocation(name, "mycelial_reactor"));
			builder.addOptional(new ResourceLocation(name, "biofuel_generator"));
			builder.addOptional(new ResourceLocation(name, "mycelial_generator_furnace"));
			builder.addOptional(new ResourceLocation(name, "mycelial_generator_slimey"));
			builder.addOptional(new ResourceLocation(name, "mycelial_generator_culinary"));
			builder.addOptional(new ResourceLocation(name, "mycelial_generator_potion"));
			builder.addOptional(new ResourceLocation(name, "mycelial_generator_disenchantment"));
			builder.addOptional(new ResourceLocation(name, "mycelial_generator_ender"));
			builder.addOptional(new ResourceLocation(name, "mycelial_generator_explosive"));
			builder.addOptional(new ResourceLocation(name, "mycelial_generator_frosty"));
			builder.addOptional(new ResourceLocation(name, "mycelial_generator_halitosis"));
			builder.addOptional(new ResourceLocation(name, "mycelial_generator_magma"));
			builder.addOptional(new ResourceLocation(name, "mycelial_generator_pink"));
			builder.addOptional(new ResourceLocation(name, "mycelial_generator_netherstar"));
			builder.addOptional(new ResourceLocation(name, "mycelial_generator_death"));
			builder.addOptional(new ResourceLocation(name, "mycelial_generator_rocket"));
			builder.addOptional(new ResourceLocation(name, "mycelial_generator_crimed"));
			builder.addOptional(new ResourceLocation(name, "mycelial_generator_meatallurgic"));
			builder.addOptional(new ResourceLocation(name, "bioreactor"));
			break;
		case MACHINE:
			builder.addOptional(new ResourceLocation(name, "animal_rancher"));
			builder.addOptional(new ResourceLocation(name, "hydroponic_bed"));
			builder.addOptional(new ResourceLocation(name, "plant_gatherer"));
			builder.addOptional(new ResourceLocation(name, "plant_sower"));
			builder.addOptional(new ResourceLocation(name, "plant_fertilizer"));
			builder.addOptional(new ResourceLocation(name, "animal_feeder"));
			builder.addOptional(new ResourceLocation(name, "mob_crusher"));
			builder.addOptional(new ResourceLocation(name, "mob_duplicator"));
			builder.addOptional(new ResourceLocation(name, "wither_builder"));
			builder.addOptional(new ResourceLocation(name, "mob_slaughter_factory"));
			builder.addOptional(new ResourceLocation(name, "sewer"));
			builder.addOptional(new ResourceLocation(name, "sewage_composter"));
			builder.addOptional(new ResourceLocation(name, "animal_baby_separator"));
			builder.addOptional(new ResourceLocation(name, "washing_factory"));
			builder.addOptional(new ResourceLocation(name, "material_stonework_factory"));
			builder.addOptional(new ResourceLocation(name, "fluid_sieving_machine"));
			builder.addOptional(new ResourceLocation(name, "mechanical_dirt"));
			builder.addOptional(new ResourceLocation(name, "block_breaker"));
			builder.addOptional(new ResourceLocation(name, "fermentation_station"));
			builder.addOptional(new ResourceLocation(name, "marine_fisher"));
			builder.addOptional(new ResourceLocation(name, "fluid_placer"));
			builder.addOptional(new ResourceLocation(name, "block_placer"));
			builder.addOptional(new ResourceLocation(name, "resourceful_furnace"));
			builder.addOptional(new ResourceLocation(name, "ore_laser_base"));
			builder.addOptional(new ResourceLocation(name, "fluid_collector"));
			builder.addOptional(new ResourceLocation(name, "spores_recreator"));
			builder.addOptional(new ResourceLocation(name, "potion_brewer"));
			builder.addOptional(new ResourceLocation(name, "sludge_refiner"));
			builder.addOptional(new ResourceLocation(name, "water_condensator"));
			builder.addOptional(new ResourceLocation(name, "dye_mixer"));
			builder.addOptional(new ResourceLocation(name, "laser_drill"));
			builder.addOptional(new ResourceLocation(name, "fluid_laser_base"));
			builder.addOptional(new ResourceLocation(name, "dissolution_chamber"));
			builder.addOptional(new ResourceLocation(name, "fluid_extractor"));
			builder.addOptional(new ResourceLocation(name, "latex_processing_unit"));
			builder.addOptional(new ResourceLocation(name, "enchantment_factory"));
			builder.addOptional(new ResourceLocation(name, "infinity_charger"));
			builder.addOptional(new ResourceLocation(name, "enchantment_sorter"));
			builder.addOptional(new ResourceLocation(name, "enchantment_applicator"));
			builder.addOptional(new ResourceLocation(name, "stasis_chamber"));
			builder.addOptional(new ResourceLocation(name, "enchantment_extractor"));
			builder.addOptional(new ResourceLocation(name, "mob_detector"));
			break;
		default:
			break;

		}
	}

	private void addSilent(final Builder<TileEntityType<?>> builder, final MemberType type) {
		final String name = "silents_mechanisms";
		switch(type) {
		case MACHINE:
			builder.addOptional(new ResourceLocation(name, "basic_alloy_smelter"));
			builder.addOptional(new ResourceLocation(name, "alloy_smelter"));
			builder.addOptional(new ResourceLocation(name, "basic_crusher"));
			builder.addOptional(new ResourceLocation(name, "crusher"));
			builder.addOptional(new ResourceLocation(name, "compressor"));
			builder.addOptional(new ResourceLocation(name, "electric_furnace"));
			builder.addOptional(new ResourceLocation(name, "mixer"));
			builder.addOptional(new ResourceLocation(name, "infuser"));
			builder.addOptional(new ResourceLocation(name, "refinery"));
			builder.addOptional(new ResourceLocation(name, "solidifier"));
			break;
		case STORAGE:
			builder.addOptional(new ResourceLocation(name, "battery_box"));
			break;
		case GENERATOR:
			builder.addOptional(new ResourceLocation(name, "coal_generator"));
			builder.addOptional(new ResourceLocation(name, "diesel_generator"));
			builder.addOptional(new ResourceLocation(name, "lava_generator"));
			break;
		case CABLE:
			builder.addOptional(new ResourceLocation(name, "wire"));
			break;
		default:
			break;
		}
	}

}
