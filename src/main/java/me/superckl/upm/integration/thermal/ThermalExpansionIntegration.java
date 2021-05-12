package me.superckl.upm.integration.thermal;

import cofh.thermal.expansion.init.TExpReferences;
import me.superckl.upm.api.MemberType;
import me.superckl.upm.integration.IntegrationModule;
import net.minecraft.data.TagsProvider;
import net.minecraft.tileentity.TileEntityType;

public class ThermalExpansionIntegration extends IntegrationModule{

	@Override
	public void addTETags(final TagsProvider.Builder<TileEntityType<?>> builder, final MemberType type) {
		if(type == MemberType.MACHINE) {
			builder.add(TExpReferences.MACHINE_BOTTLER_TILE);
			builder.add(TExpReferences.MACHINE_BREWER_TILE);
			builder.add(TExpReferences.MACHINE_CENTRIFUGE_TILE);
			builder.add(TExpReferences.MACHINE_CHILLER_TILE);
			builder.add(TExpReferences.MACHINE_CRAFTER_TILE);
			builder.add(TExpReferences.MACHINE_CRUCIBLE_TILE);
			builder.add(TExpReferences.MACHINE_FURNACE_TILE);
			builder.add(TExpReferences.MACHINE_INSOLATOR_TILE);
			builder.add(TExpReferences.MACHINE_PRESS_TILE);
			builder.add(TExpReferences.MACHINE_PULVERIZER_TILE);
			builder.add(TExpReferences.MACHINE_PYROLYZER_TILE);
			builder.add(TExpReferences.MACHINE_REFINERY_TILE);
			builder.add(TExpReferences.MACHINE_SAWMILL_TILE);
			builder.add(TExpReferences.MACHINE_SMELTER_TILE);
		}else if(type == MemberType.GENERATOR) {
			builder.add(TExpReferences.DYNAMO_COMPRESSION_TILE);
			builder.add(TExpReferences.DYNAMO_LAPIDARY_TILE);
			builder.add(TExpReferences.DYNAMO_MAGMATIC_TILE);
			builder.add(TExpReferences.DYNAMO_NUMISMATIC_TILE);
			builder.add(TExpReferences.DYNAMO_STIRLING_TILE);
		}
	}

}
