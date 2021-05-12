package me.superckl.upm.data;

import me.superckl.upm.UPM;
import me.superckl.upm.api.MemberType;
import me.superckl.upm.api.UPMAPI;
import net.minecraft.data.DataGenerator;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeRegistryTagsProvider;
import net.minecraftforge.registries.ForgeRegistries;

public class TileTypeTagGenerator extends ForgeRegistryTagsProvider<TileEntityType<?>>{

	public TileTypeTagGenerator(final DataGenerator generatorIn, final ExistingFileHelper existingFileHelper) {
		super(generatorIn, ForgeRegistries.TILE_ENTITIES, UPMAPI.MOD_ID, existingFileHelper);
	}

	@Override
	public String getName() {
		return "Tags: "+UPMAPI.MOD_ID;
	}

	@Override
	protected void addTags() {
		for(final MemberType type:MemberType.values()) {
			if(type == MemberType.UNKNOWN)
				continue;
			final Builder<TileEntityType<?>> builder = this.tag(UPMAPI.TAGS.get(type));
			UPM.getINSTANCE().getIntegrations().forEach(integ -> integ.addTETags(builder, type));
		}
	}

}
