package de.dosmike.blobjmap;

import net.minecraft.util.RegistryNamespaced;
import cpw.mods.fml.common.registry.GameRegistry;

public class RegistryWrapper extends RegistryNamespaced {

	/** Replace the object, ensureing the ID stays the same.
	 * Returns -1, if it couldn't be replaced - It probably didn't exist - or the id otherwise
	 */
	public int replaceEntry(String key, Object replacer) {
		String eNS = ensureNamespaced(key);
		Object old;
		if (!registryObjects.containsKey(eNS))
			return -1;
		old = registryObjects.get(eNS);
		int id = underlyingIntegerMap.func_148747_b(old); //getIdByObject()
		if (id < 0)
			return -1;
		
		//functions called in addObject:
		underlyingIntegerMap.func_148746_a(replacer, id);
        putObject(eNS, replacer);
        return id;
	}
	
	public int removeEntry(String key) {
		String eNS = ensureNamespaced(key);
		Object obj;
		int id;
		if (registryObjects.containsKey(eNS) && (obj = registryObjects.get(eNS)) != null && (id = underlyingIntegerMap.func_148747_b(obj)) != -1) {
			registryObjects.remove(eNS);
			ObjectIntMapWrapper imw = (ObjectIntMapWrapper)underlyingIntegerMap;
			imw.get_148749_a().remove(obj);
			imw.get_148748_b().set(id, null);
			return id;
		} else
			return -1;
		
	}
	
	/** returns the previous id */
	public int ensureId(String key, int id) {
		String eNS = ensureNamespaced(key);
		Object obj;
		int oid;
		if (registryObjects.containsKey(eNS) && (obj = registryObjects.get(eNS)) != null && (oid = underlyingIntegerMap.func_148747_b(obj)) != -1) {
			registryObjects.remove(eNS);
			ObjectIntMapWrapper imw = (ObjectIntMapWrapper)underlyingIntegerMap;
			imw.get_148748_b().set(id, null);
			underlyingIntegerMap.func_148746_a(obj, id);
			
			return oid;
		} else
			return -1;
	}
}
