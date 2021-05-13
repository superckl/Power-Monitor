package me.superckl.upm.network.member;

import java.util.Set;

import me.superckl.upm.api.MemberType;
import me.superckl.upm.api.NetworkMember;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * This class is used on the client as a placeholder in wrapped members.
 * It denies most operations that should not be performed on the client.
 */
public class PhantomNetworkMember extends NetworkMember{

	public PhantomNetworkMember(final MemberType type) {
		super(null, type);
	}

	@Override
	public long getMaxStorage() {
		throw this.err();
	}

	@Override
	public long getCurrentEnergy() {
		throw this.err();
	}

	@Override
	public int addEnergy(final int energy) {
		throw this.err();
	}

	@Override
	public int removeEnergy(final int energy) {
		throw this.err();
	}

	@Override
	public boolean isSameStorage(final NetworkMember member) {
		throw this.err();
	}

	@Override
	public boolean valid() {
		throw this.err();
	}

	@Override
	public Set<BlockPos> getConnections() {
		throw this.err();
	}

	@Override
	public TileEntity getTileEntity() {
		throw this.err();
	}

	private UnsupportedOperationException err() {
		return new UnsupportedOperationException("Cannot perform operations on phantom member");
	}

}
