package de.dosmike.blobjmap;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import de.dosmike.blobjmap.proxy.CommonProxy;

public class BlockPlacementManager {
	//TODO: handle block placement
	//TODO: handle block breackage
	
	CommonProxy proxy;
	
	public BlockPlacementManager() {
		proxy = BlObjMapper.proxy;
	}
	
	@SubscribeEvent
	public void onPlaceBlock(PlaceEvent event) {
		if (proxy.replacer.containsKey(event.block.getUnlocalizedName())) {
			event.setCanceled(true);
			
			MinecraftForge.EVENT_BUS.post(new PlaceEvent(
					new BlockSnapshot(event.blockSnapshot.world, event.blockSnapshot.x, event.blockSnapshot.y, event.blockSnapshot.z,
							proxy.replacer.get(event.block.getUnlocalizedName()), event.blockSnapshot.meta, event.blockSnapshot.flag),
					event.placedAgainst, event.player));
		}
	}
	
//	@SubscribeEvent
//	public void onBreakBlock(BreakEvent event) {
//		if (BlockObjMapperMod.deplacer.containsKey(event.block.getUnlocalizedName())) {
//			event.block.drop
//		}
//	}
}
