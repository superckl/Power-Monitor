package me.superckl.upm.integration;

import me.superckl.upm.network.member.MemberType;
import net.minecraft.data.TagsProvider.Builder;
import net.minecraft.tileentity.TileEntityType;

public abstract class IntegrationModule {

	public void addTETags(final Builder<TileEntityType<?>> builder, final MemberType type) {}

}
