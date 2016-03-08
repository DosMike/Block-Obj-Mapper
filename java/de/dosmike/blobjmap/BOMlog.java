package de.dosmike.blobjmap;

import cpw.mods.fml.common.FMLLog;

public class BOMlog {
	public static void i(String act, String msg, Object... param) {
		FMLLog.info("[BLOBJMAP%s] %s", (act==null?"":"|"+act), String.format(msg, param));
	}
	public static void i(String msg, Object... param) {
		FMLLog.info("[BLOBJMAP] %s", msg);
	}
}
