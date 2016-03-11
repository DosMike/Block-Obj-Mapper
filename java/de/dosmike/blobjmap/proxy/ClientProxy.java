package de.dosmike.blobjmap.proxy;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import de.dosmike.blobjmap.BOMlog;
import de.dosmike.blobjmap.BlObjMapper;
import de.dosmike.blobjmap.blocks.DynamicTileEntityRenderer;
import de.dosmike.forgeinit.Registra;
import de.dosmike.forgeinit.Registra.RegType;

public class ClientProxy extends CommonProxy {

	private static final String MODID = BlObjMapper.MODID;
    private static List<DynamicTileEntityRenderer> dter = new LinkedList<DynamicTileEntityRenderer>();
    private static Registra reg = new Registra(); 
    //static Map<String, Block> replacer = new HashMap<String, Block>();
    //static Map<String, Block> deplacer = new HashMap<String, Block>();
    
	public void scanAndRegister() {
		Class<? extends Block> retval;
		//Object param1 = getObjectParam("ic2.core.init.InternalName", "blockGenerator");
		try {
			Class<? extends Enum> enum1;
			enum1 = (Class<? extends Enum>) Class.forName("ic2.core.init.InternalName");
			//we can't use a enum twice for IC2
			Object param1 = getEnumVal(enum1, "blockGenerator");	
			//BOMlog.i("Enum Value: %s", param1.toString());
			retval = wrapBlock("minecraft:glass");
			reg.registerBlock(RegType.Reregister, "minecraft:glass", retval, param1);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	/** returns true on success */
	private static Class<? extends Block> wrapBlock(String blockId) {
    	Block cbn = Block.getBlockFromName(blockId);
    	String modId = blockId.substring(0, blockId.indexOf(':'));
    	
    	DynamicTileEntityRenderer dr = new DynamicTileEntityRenderer(cbn);
    	if (!dr.loadedResources()) {
    		BOMlog.i("\n\n\tYour config and resource pack do not match!\n\tMissing files for " + modId + "'s " + blockId + "\n\n");
    		return null;
    	}
    	dter.add(dr); //keep instance until idc
		return reg.bindTE(modId, cbn, dr);
	}

	//Move this in a Utils?
    private File resolveLocation(ResourceLocation rl) {
    	return new File("assets/"+rl.getResourceDomain()+"/"+rl.getResourcePath());
    }
    
    private Object getObjectParam(String className, String... fieldNames) { //throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		//Class<?> resClass = Class.forName("ic2.core.init.InternalName");
			try {
				Class<?> rclass = Class.forName(className);
				Object result = null;
				if (fieldNames.length < 1) throw new IllegalArgumentException("You need to spec at least one field Name");
				for (int i = 0; i < fieldNames.length; i++) {
					result = getFieldOrMethodResult(result==null?rclass:result.getClass(), result, fieldNames[i]);
				}
				return result;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
    }
    private Object getFieldOrMethodResult(Class<? extends Object> clazz, Object value, String fieldName) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    	Field[] fields = clazz.getDeclaredFields();
    	for (Field f : fields) {
    		if (f.getName().equals(fieldName) && f.isAccessible()) {
    			return f.get(value);
    		}
    	}
    	fields = null;
    	Method[] methods = clazz.getDeclaredMethods();
    	for (Method m : methods) {
    		if (m.getName().equals(fieldName) && m.isAccessible() && m.getParameterCount() == 0) {
    			return m.invoke(value);
    		}
    	}
    	BOMlog.i("BlockGen", "Unable to resolve value for %s > %s\n\t%s", clazz.getName(), fieldName, value == null ? "The first object needs to be static!" : "Please call a coder!");
    	return null;
    }
    
    <E extends Enum<E>> E getEnumVal(Class<E> enumClass, String enumKey) {
    	return Enum.valueOf(enumClass, enumKey);
    }
}
