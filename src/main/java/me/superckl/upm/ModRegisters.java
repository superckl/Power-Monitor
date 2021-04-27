package me.superckl.upm;

import java.util.function.Supplier;

import me.superckl.upm.network.member.ForgeEnergyNetworkMember;
import me.superckl.upm.network.member.NetworkMemberResolver;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

public class ModRegisters {

	public static final DeferredRegister<TileEntityType<?>> TILE_REGISTER = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, UPM.MOD_ID);
	public static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, UPM.MOD_ID);
	public static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, UPM.MOD_ID);

	//We have to proxy through the raw type Class to let the wildcard be inferred (NetworkMemberResolver.class does not have the wildcard generic)
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final DeferredRegister<NetworkMemberResolver<?>> RESOLVER_REGISTER = DeferredRegister.create((Class) NetworkMemberResolver.class, UPM.MOD_ID);

	public static final Supplier<IForgeRegistry<NetworkMemberResolver<?>>> RESOLVER_REGISTRY = ModRegisters.RESOLVER_REGISTER.makeRegistry("network_member", RegistryBuilder::new);

	public static final RegistryObject<UPMBlock> UPM_BLOCK = ModRegisters.BLOCK_REGISTER.register("upm", UPMBlock::new);
	public static final RegistryObject<BlockItem> UPM_ITEM = ModRegisters.ITEM_REGISTER.register("upm", () -> new BlockItem(ModRegisters.UPM_BLOCK.get(), new Item.Properties().tab(ItemGroup.TAB_REDSTONE)));
	public static final RegistryObject<TileEntityType<UPMTile>> UPM_TILE_TYPE = ModRegisters.TILE_REGISTER.register("upm", () -> TileEntityType.Builder.of(UPMTile::new, ModRegisters.UPM_BLOCK.get()).build(null));
	public static final RegistryObject<ForgeEnergyNetworkMember.Resolver> FORGE_ENERGY_RESOLVER = ModRegisters.RESOLVER_REGISTER.register("forge_energy", ForgeEnergyNetworkMember.Resolver::new);

}
