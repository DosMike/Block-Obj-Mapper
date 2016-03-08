package de.dosmike.blobjmap.blocks;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import net.minecraft.block.Block;
import de.dosmike.blobjmap.BOMlog;

public class CloneableBlock {

	public static <T extends Block, N extends T>  T clone(T fromBlock, Class<N> toClass,  Object... constructorValues) throws IllegalAccessException,  NoSuchFieldException, InstantiationException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		//Class<? extends Block> toClass = fromBlock.getClass();
		
		T toBlock = null;
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
		toBlock = (T) con.newInstance(constructorValues);
		
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