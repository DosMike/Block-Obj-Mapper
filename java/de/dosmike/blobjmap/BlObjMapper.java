package de.dosmike.blobjmap;

import java.io.File;

import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import de.dosmike.blobjmap.proxy.CommonProxy;

@Mod(modid = BlObjMapper.MODID, version = BlObjMapper.VERSION)
public class BlObjMapper
{
    public static final String MODID = "blobjmap";
    public static final String VERSION = "1.0";
    
    
    // The instance of your mod that Forge uses.
	@Instance(MODID)
	public static BlObjMapper  instance;

	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide="de.dosmike.blobjmap.proxy.ClientProxy", serverSide="de.dosmike.blobjmap.proxy.CommonProxy")
	public static CommonProxy proxy;
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
    	if (!(new File("resourcepacks/blobjmap.zip").exists())) {
    		BOMlog.i("You do not have the resource pack for models installed!");
    		return;
    	}
    	proxy.scanAndRegister();
    	
    	//MinecraftForge.EVENT_BUS.register(new BlockPlacementManager());
    }
    
    @EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
    	event.registerServerCommand(new DiscoverCommand());
    }
}
