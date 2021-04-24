package me.superckl.upm.network;

import java.lang.ref.WeakReference;
import java.util.Optional;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class ForgeEnergyNetworkMember extends NetworkMember{

	private final WeakReference<IEnergyStorage> storage;

	public ForgeEnergyNetworkMember(final IEnergyStorage storage) {
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
	public Direction[] childDirections() {
		if(this.storage.get().canExtract())
			return Direction.values();
		return new Direction[0];
	}

	@Override
	public MemberType type() {
		final IEnergyStorage cap = this.storage.get();
		if(cap.canExtract() && cap.canReceive())
			return MemberType.STORAGE;
		if(cap.canReceive() && !cap.canExtract())
			return MemberType.MACHINE;
		return MemberType.UNKNOWN;
	}

	@Override
	public boolean valid() {
		return this.storage.get() != null;
	}

	@Override
	public boolean isSameStorage(final NetworkMember member) {
		return member instanceof ForgeEnergyNetworkMember &&
				this.storage.get() == ((ForgeEnergyNetworkMember)member).storage.get();
	}

	public static class Resolver extends NetworkMemberResolver<ForgeEnergyNetworkMember>{

		@Override
		public Optional<ForgeEnergyNetworkMember> getNetworkMember(final TileEntity tile) {
			return tile.getCapability(CapabilityEnergy.ENERGY).map(ForgeEnergyNetworkMember::new);
		}

	}

}
