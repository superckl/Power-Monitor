package me.superckl.upm.integration.immersiveengineering;

import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.metal.EnergyConnectorTileEntity;
import me.superckl.upm.ModRegisters;
import me.superckl.upm.api.MemberType;
import me.superckl.upm.integration.IntegrationModule;
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
			builder.addOptional(IETileTypes.ALLOY_SMELTER.getId());
			builder.addOptional(IETileTypes.ARC_FURNACE.getId());
			builder.addOptional(IETileTypes.ASSEMBLER.getId());
			builder.addOptional(IETileTypes.AUTO_WORKBENCH.getId());
			builder.addOptional(IETileTypes.BLASTFURNACE_PREHEATER.getId());
			builder.addOptional(IETileTypes.BOTTLING_MACHINE.getId());
			builder.addOptional(IETileTypes.CHARGING_STATION.getId());
			builder.addOptional(IETileTypes.CLOCHE.getId());
			builder.addOptional(IETileTypes.CRUSHER.getId());
			builder.addOptional(IETileTypes.ELECTRIC_LANTERN.getId());
			builder.addOptional(IETileTypes.EXCAVATOR.getId());
			builder.addOptional(IETileTypes.FERMENTER.getId());
			builder.addOptional(IETileTypes.FLUID_PUMP.getId());
			builder.addOptional(IETileTypes.FURNACE_HEATER.getId());
			builder.addOptional(IETileTypes.METAL_PRESS.getId());
			builder.addOptional(IETileTypes.MIXER.getId());
			builder.addOptional(IETileTypes.REFINERY.getId());
			builder.addOptional(IETileTypes.SAMPLE_DRILL.getId());
			builder.addOptional(IETileTypes.SAWMILL.getId());
			builder.addOptional(IETileTypes.SQUEEZER.getId());
			builder.addOptional(IETileTypes.TESLACOIL.getId());
			builder.addOptional(IETileTypes.TURRET_CHEM.getId());
			builder.addOptional(IETileTypes.TURRET_GUN.getId());
		}else if(type == MemberType.STORAGE) {
			builder.addOptional(IETileTypes.CAPACITOR_CREATIVE.getId());
			builder.addOptional(IETileTypes.CAPACITOR_HV.getId());
			builder.addOptional(IETileTypes.CAPACITOR_LV.getId());
			builder.addOptional(IETileTypes.CAPACITOR_MV.getId());
		}else if(type == MemberType.GENERATOR) {
			builder.addOptional(IETileTypes.DIESEL_GENERATOR.getId());
			builder.addOptional(IETileTypes.DYNAMO.getId());
			builder.addOptional(IETileTypes.LIGHTNING_ROD.getId());
			builder.addOptional(IETileTypes.THERMOELECTRIC_GEN.getId());
		}else if(type == MemberType.CABLE) {
			builder.addOptional(IETileTypes.POST_TRANSFORMER.getId());
			builder.addOptional(IETileTypes.TRANSFORMER.getId());
			builder.addOptional(IETileTypes.TRANSFORMER_HV.getId());
			EnergyConnectorTileEntity.SPEC_TO_TYPE.values().forEach(tileType -> builder.addOptional(tileType.getId()));
		}
	}

}
