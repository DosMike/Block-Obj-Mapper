package de.dosmike.blobjmap.blocks;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.client.model.ModelFormatException;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.registry.GameRegistry;

public class DynamicTileEntityRenderer extends TileEntitySpecialRenderer {
    public IModelCustom Object = null;
    public ResourceLocation Texture = null;
    

    public DynamicTileEntityRenderer(Block block) {
    	String fileBaseName = GameRegistry.findUniqueIdentifierFor(block).toString().replaceAll(":", "_");//block.getLocalizedName().replaceAll(":", "_");
    	
        //Model
    	ResourceLocation rl = new ResourceLocation("blobjmap", "models/" + fileBaseName + ".obj");
    	
    	System.out.println("Trying to load OBJ from " + rl.getResourcePath());
    	try {
	        Object = AdvancedModelLoader.loadModel(rl);
	        Texture = new ResourceLocation("blobjmap", "textures/models/" + fileBaseName + ".png");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
        //Bowl
        GL11.glPushMatrix();//Matrix to Stack
        GL11.glTranslated(x, y, z);//Translate

        Minecraft.getMinecraft().renderEngine.bindTexture(Texture);
        Object.renderAll();//seems to be the whole Drawing code...
        GL11.glPopMatrix();//Matrix unbind
    }
    
    public boolean loadedResources() {
    	return (Object != null && Texture != null);
    }
}
