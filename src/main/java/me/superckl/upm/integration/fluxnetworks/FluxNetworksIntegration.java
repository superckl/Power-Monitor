package me.superckl.upm.integration.fluxnetworks;

import me.superckl.upm.ModRegisters;
import me.superckl.upm.api.MemberType;
import me.superckl.upm.integration.IntegrationModule;
import net.minecraft.data.TagsProvider.Builder;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import sonar.fluxnetworks.common.registry.RegistryBlocks;

public class FluxNetworksIntegration extends IntegrationModule{

	public static RegistryObject<FluxNetworkMember.Resolver> RESOLVER;

	public FluxNetworksIntegration() {
		FluxNetworksIntegration.RESOLVER = ModRegisters.RESOLVER_REGISTER.register("flux_networks", FluxNetworkMember.Resolver::new);
	}

	@Override
	public void addTETags(final Builder<TileEntityType<?>> builder, final MemberType type) {
		switch(type) {
		case CABLE:
			builder.addOptional(RegistryBlocks.FLUX_PLUG_TILE.getRegistryName());
			builder.addOptional(RegistryBlocks.FLUX_POINT_TILE.getRegistryName());
			break;
		case STORAGE:
			builder.addOptional(RegistryBlocks.BASIC_FLUX_STORAGE_TILE.getRegistryName());
			builder.addOptional(RegistryBlocks.HERCULEAN_FLUX_STORAGE_TILE.getRegistryName());
			builder.addOptional(RegistryBlocks.GARGANTUAN_FLUX_STORAGE_TILE.getRegistryName());
			break;
		case MACHINE:
			builder.addOptional(RegistryBlocks.FLUX_CONTROLLER_TILE.getRegistryName());
			break;
		default:
			break;
		}
	}

}
