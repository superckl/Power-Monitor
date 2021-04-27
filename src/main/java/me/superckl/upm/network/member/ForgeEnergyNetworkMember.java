package me.superckl.upm.network.member;

import java.lang.ref.WeakReference;
import java.util.Optional;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class ForgeEnergyNetworkMember extends NetworkMember{

	private final WeakReference<IEnergyStorage> storage;

	public ForgeEnergyNetworkMember(final IEnergyStorage storage, final MemberType type) {
		super(type);
		this.storage = new WeakReference<>(storage);
	}

	@Override
	public int getMaxStorage() {
		return this.storage.get().getMaxEnergyStored();
	}

	@Override
	public int getCurrentEnergy() {
		return this.storage.get().getEnergyStored();
	}

	@Override
	public boolean valid() {
		return this.storage.get() != null;
	}

	@Override
	public boolean canExtract() {
		return this.storage.get().canExtract();
	}

	@Override
	public boolean canInsert() {
		return this.storage.get().canReceive();
	}

	@Override
	public boolean isSameStorage(final NetworkMember member) {
		return member instanceof ForgeEnergyNetworkMember &&
				this.storage.get() == ((ForgeEnergyNetworkMember)member).storage.get();
	}

	public static class Resolver extends NetworkMemberResolver<ForgeEnergyNetworkMember>{

		@Override
		public Optional<ForgeEnergyNetworkMember> getNetworkMember(final TileEntity tile, final Direction side) {
			return tile.getCapability(CapabilityEnergy.ENERGY, side).map(storage -> new ForgeEnergyNetworkMember(storage, this.typeFromTag(tile.getType())));
		}

	}

}
