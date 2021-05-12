package me.superckl.upm.network.member;

import java.lang.ref.WeakReference;
import java.util.Optional;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class ForgeEnergyNetworkMember extends NetworkMember{

	private final WeakReference<IEnergyStorage> storage;

	public ForgeEnergyNetworkMember(final TileEntity tile, final IEnergyStorage storage, final MemberType type) {
		super(tile, type);
		this.storage = new WeakReference<>(storage);
	}

	@Override
	public long getMaxStorage() {
		return this.storage.get().getMaxEnergyStored();
	}

	@Override
	public long getCurrentEnergy() {
		return this.storage.get().getEnergyStored();
	}

	@Override
	public boolean valid() {
		return super.valid() && this.storage.get() != null;
	}

	@Override
	public int addEnergy(final int energy) {
		return this.storage.get().receiveEnergy(energy, false);
	}

	@Override
	public int removeEnergy(final int energy) {
		return this.storage.get().extractEnergy(energy, false);
	}

	@Override
	public boolean isSameStorage(final NetworkMember member) {
		return member instanceof ForgeEnergyNetworkMember &&
				this.storage.get() == ((ForgeEnergyNetworkMember)member).storage.get();
	}

	public static class Resolver extends NetworkMemberResolver<ForgeEnergyNetworkMember>{

		@Override
		public Optional<ForgeEnergyNetworkMember> getNetworkMember(final TileEntity tile, final Direction side) {
			return tile.getCapability(CapabilityEnergy.ENERGY, side).map(storage -> new ForgeEnergyNetworkMember(tile, storage, this.typeFromTag(tile.getType())));
		}

		@Override
		public int getPriority(final TileEntity tile) {
			return 0;
		}

	}

}
