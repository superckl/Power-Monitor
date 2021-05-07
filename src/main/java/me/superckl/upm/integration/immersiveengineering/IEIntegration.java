package me.superckl.upm.integration.immersiveengineering;

import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.metal.EnergyConnectorTileEntity;
import me.superckl.upm.ModRegisters;
import me.superckl.upm.integration.IntegrationModule;
import me.superckl.upm.network.member.MemberType;
import net.minecraft.data.TagsProvider.Builder;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

public class IEIntegration extends IntegrationModule{

	public static RegistryObject<IEFluxNetworkMember.Resolver> RESOLVER;

	public IEIntegration() {
		IEIntegration.RESOLVER = ModRegisters.RESOLVER_REGISTER.register("ieflux", IEFluxNetworkMember.Resolver::new);
	}

	@Override
	public void addTETags(final Builder<TileEntityType<?>> builder, final MemberType type) {
		if(type == MemberType.MACHINE) {
			builder.add(IETileTypes.ALLOY_SMELTER.get());
			builder.add(IETileTypes.ARC_FURNACE.get());
			builder.add(IETileTypes.ASSEMBLER.get());
			builder.add(IETileTypes.AUTO_WORKBENCH.get());
			builder.add(IETileTypes.BLASTFURNACE_PREHEATER.get());
			builder.add(IETileTypes.BOTTLING_MACHINE.get());
			builder.add(IETileTypes.CHARGING_STATION.get());
			builder.add(IETileTypes.CLOCHE.get());
			builder.add(IETileTypes.CRUSHER.get());
			builder.add(IETileTypes.ELECTRIC_LANTERN.get());
			builder.add(IETileTypes.EXCAVATOR.get());
			builder.add(IETileTypes.FERMENTER.get());
			builder.add(IETileTypes.FLUID_PUMP.get());
			builder.add(IETileTypes.FURNACE_HEATER.get());
			builder.add(IETileTypes.METAL_PRESS.get());
			builder.add(IETileTypes.MIXER.get());
			builder.add(IETileTypes.REFINERY.get());
			builder.add(IETileTypes.SAMPLE_DRILL.get());
			builder.add(IETileTypes.SAWMILL.get());
			builder.add(IETileTypes.SQUEEZER.get());
			builder.add(IETileTypes.TESLACOIL.get());
			builder.add(IETileTypes.TURRET_CHEM.get());
			builder.add(IETileTypes.TURRET_GUN.get());
		}else if(type == MemberType.STORAGE) {
			builder.add(IETileTypes.CAPACITOR_CREATIVE.get());
			builder.add(IETileTypes.CAPACITOR_HV.get());
			builder.add(IETileTypes.CAPACITOR_LV.get());
			builder.add(IETileTypes.CAPACITOR_MV.get());
		}else if(type == MemberType.GENERATOR) {
			builder.add(IETileTypes.DIESEL_GENERATOR.get());
			builder.add(IETileTypes.DYNAMO.get());
			builder.add(IETileTypes.LIGHTNING_ROD.get());
			builder.add(IETileTypes.THERMOELECTRIC_GEN.get());
		}else if(type == MemberType.CABLE) {
			builder.add(IETileTypes.POST_TRANSFORMER.get());
			builder.add(IETileTypes.TRANSFORMER.get());
			builder.add(IETileTypes.TRANSFORMER_HV.get());
			EnergyConnectorTileEntity.SPEC_TO_TYPE.values().forEach(tileType -> builder.add(tileType.get()));
		}
	}

}
