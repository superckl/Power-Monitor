package me.superckl.upm.integration.immersiveengineering;

import me.superckl.upm.ModRegisters;
import net.minecraftforge.fml.RegistryObject;

public class IEIntegration {

	public static RegistryObject<IENetworkMember.Resolver> RESOLVER;

	public static void onConstruction() {
		IEIntegration.RESOLVER = ModRegisters.RESOLVER_REGISTER.register("iewire", IENetworkMember.Resolver::new);
	}

}
