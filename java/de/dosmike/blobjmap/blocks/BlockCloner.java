package de.dosmike.blobjmap.blocks;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import net.minecraft.block.Block;
import de.dosmike.blobjmap.BOMlog;

public class BlockCloner {

	public static <T extends Block, N extends T>  N clone(T fromBlock, Class<N> toClass,  Object... constructorValues) throws IllegalAccessException,  NoSuchFieldException, InstantiationException, IllegalArgumentException, NoSuchMethodException, SecurityException {
		//Class<? extends Block> toClass = fromBlock.getClass();
		
		N toBlock = null;
		boolean foundconst = false;
		//Constructor<?>[] cons = toClass.getConstructors();
		//for (Constructor<?> con : cons) {
		//	if (con.toString().equalsIgnoreCase(usingConstructor)) {
		//		con.setAccessible(true);
		//		toBlock = (T) con.newInstance(constructorValues);
		//		foundconst = true;
		//	}
		//}
		//if (!foundconst) throw new NoSuchMethodException("No constructor with the specified name was found");
		Constructor con = toClass.getDeclaredConstructors()[0];
		BOMlog.i("CLONEC", "Using constructor %s", con.toGenericString());
		for (Object o : constructorValues) {
			BOMlog.i("CLONEC", "Argument %s [%s]", o.toString(), o.getClass().getName());
		}
		try {
			toBlock = (N) con.newInstance(constructorValues);
		} catch (InvocationTargetException ite) {
			BOMlog.i("CLONEC", "InvocationTargetException caused by:");
			ite.getCause().printStackTrace();
		}
		
		Field[] fromFields = fromBlock.getClass().getDeclaredFields();
		Field[]   toFields = toClass.getDeclaredFields();

		Object value = null;

		for (Field field : fromFields){
			if (!field.isAccessible()) continue;
		    Field field1 = toClass.getDeclaredField(field.getName());
		    value =field.get(fromBlock);
		    field1.set(toBlock,value);
		}
		
		return toBlock;
	}
		
}