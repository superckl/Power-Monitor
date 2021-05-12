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

}
