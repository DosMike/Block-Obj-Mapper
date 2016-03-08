package de.dosmike.forgeinit;

import java.io.File;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import de.dosmike.blobjmap.BOMlog;
import de.dosmike.blobjmap.blocks.CloneableBlock;
import de.dosmike.blobjmap.blocks.dummyBlock;
import de.dosmike.blobjmap.blocks.dummyTileEntity;

public class Registra {

	final static String tpack = dummyBlock.class.getPackage().getName();
	
	public static Block bindTE(String modId, Block block, TileEntitySpecialRenderer renderer, /*String cloneConstructor, */Object[] cloneConstValues) {
		Block result = null;
		String bpack = null;
		String tecname = block.getClass().getSimpleName() + "_TileEntity";
		BOMlog.i("BlockGen", "=== CREATE RENDERER FOR " + block.getUnlocalizedName() + " ===");
		
		ClassPool cp = ClassPool.getDefault();
		//try {
			//cp.makePackage(BlObjMapper.instance.getClass().getClassLoader(), tpack);
		cp.insertClassPath(new ClassClassPath(dummyBlock.class));
		try {
			cp.appendClassPath("net.minecraft");
			cp.appendClassPath("aoq");	//obfuscated name for TileEntity
			cp.appendClassPath("aji");	//obfuscated name for World
			cp.appendClassPath("net.minecraft.tileentity.TileEntity");
		} catch (NotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		//create new tileEntity class
		Class<? extends TileEntity> tec;
		try {
			bpack = block.getClass().getName();//cp.get(block.getClass().getName()).getPackageName();
			bpack = bpack.substring(0, bpack.lastIndexOf('.'));
			CtClass cc = cp.makeClass(tpack + "." + tecname);
			cc.setSuperclass(cp.getCtClass(dummyTileEntity.class.getName()));
			//cp.makePackage(cp.getClassLoader(), tpack);
			tec = cc.toClass();
		} catch (Exception e1) {
			BOMlog.i("BlockGen", "ERROR: Looks like " + block.getUnlocalizedName() + " was obuscated. No class instance could be created.");
			BOMlog.i("BlockGen", "INFO: If you're trying to mod vanilla block, try to set CCC to deobfuscate names");
			e1.printStackTrace();
			return null;
		}
		//co("Created class: " + tec.getName());
		
		BOMlog.i("BlockGen", "Block data: {\n\tUnlocalozedName=\"%s\",\n\tClass=\"%s\",\n\tPackage=\"%s\",\n\tnew Class=\"BOM_%s\",\n\tnew Package=\"%s\",\n\tTile Entity ClassName=\"%s\",\n\tTile Entity ID=\"BOMauto_%s\"\n}",
				block.getUnlocalizedName(),
				block.getClass().getSimpleName(),
				bpack,
				block.getClass().getSimpleName(),
				tpack,
				tec.getName(),
				tecname);
		
		//Method[] jms = block.getClass().getDeclaredMethods();
		//boolean hasTEfunc = false;
		//for (Method jm : jms)
		//	if (jm.getName().equals("createTileEntity")) {
		//		co("INFO: " + block.getUnlocalizedName() + " already has createTileEntity");
		//		hasTEfunc = true;
		//	}
		
		GameRegistry.registerTileEntity(tec, "BOMauto_" + tecname);
		ClientRegistry.bindTileEntitySpecialRenderer(tec, renderer);
		BOMlog.i("BlockGen", "Bound " + block.getUnlocalizedName() + " to TileEntity" + tec.getName() + " < BOMauto_" + tecname + " >");
		
		//get JavAssist class
		CtClass cc = null;
		try {
			CtClass csc = cp.get(block.getClass().getName());
			//cp.makePackage(cp.getClassLoader(), tpack);
			cc = cp.makeClass(tpack + ".BOM_"+block.getClass().getSimpleName());
			cc.setSuperclass(csc);
			//if (bpack != null) cp.makePackage(cp.getClassLoader(), bpack);
			BOMlog.i("BlockGen", "Check PGK " + cc.getPackageName());
		} catch (NotFoundException e) {
			BOMlog.i("BlockGen", "Class " + block.getClass().getName() + " not found");
			return null;
		} catch (CannotCompileException e) {
			BOMlog.i("BlockGen", "Unable to rebuild Package " + bpack);
		}
		try {
			cc.makeClassInitializer();
		} catch (CannotCompileException e1) {
			BOMlog.i("BlockGen", "ERROR: Won't be able to instatize the block");
			return null;
		}
		
		BOMlog.i("BlockGen", "Injecting functions to " + block.getUnlocalizedName() + "...");
		
		if ((cc=injectMethod("public boolean renderAsNormalBlock() { return false; }", cc)) == null) return null;
		if ((cc=injectMethod("public aoq createTileEntity(aji world, int i) { System.out.println(\"[BLOBJMAP] Placing Tile entity: " + tec.getName() + "\"); return new " + tec.getName() + "(); }", cc)) == null) return null;
		
		BOMlog.i("BlockGen", "Replacing existing Block class...");
		try {
			Class<? extends Block> ccr = cc.toClass(); //recompile as subclass
			BOMlog.i("BlockGen", "Check " + ccr.getName() + " > " + ccr.getSuperclass().getName() + " - " + block.getClass().getName() + " - InstanceOf: " + ccr.getSuperclass().getName().equals(block.getClass().getName()) );
			
			String name = GameRegistry.findUniqueIdentifierFor(block).toString();
			//String name = block.getUnlocalizedName();
			//if (name.startsWith("tile.")) name = name.substring(5);
			//name = modId + ":" + name;
			BOMlog.i("BlockGen", "Substitute Name: " + name);			
			
			result = registerBlockSubstitutionAlias(name, block, ccr, /*cloneConstructor, */cloneConstValues);
		} catch (CannotCompileException e) {
			BOMlog.i("BlockGen", "ERROR: Unable to recompile class");
			e.printStackTrace();
		//} catch (ExistingSubstitutionException e) {
		//	co("INFO: This block was already replaced - Skipped!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		BOMlog.i("BlockGen", "___ " + block.getUnlocalizedName() + " DONE ___");
		
		return result;
	}
	
	private static CtClass injectMethod(String method, CtClass cc) {
		BOMlog.i("[BLOBJMAP] Injecting " + method);
		try {
			CtMethod cm = CtNewMethod.make(method, cc);
			addAnnotation(cc, cm, "Override");
			cc.addMethod(cm);
		} catch (CannotCompileException e) {
			BOMlog.i("Unable to inject and compile the function");
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			BOMlog.i("Unable to attach override annotation to function");
			e.printStackTrace();
			return null;
		}
		return cc;
	}
	
	private static void addAnnotation(CtClass cc, CtMethod cm, String annotation) {
		//add override annotation
		ClassFile cfile = cc.getClassFile();
        ConstPool cpool = cfile.getConstPool();
 
        AnnotationsAttribute attr = new AnnotationsAttribute(cpool, AnnotationsAttribute.visibleTag);
        Annotation annot = new Annotation("java.lang." + annotation, cpool);
        attr.addAnnotation(annot);
        cm.getMethodInfo().addAttribute(attr);
	}
	
	//this function exists to be able to use subclass casts
	private static <T extends Block, N extends T> N registerBlockSubstitutionAlias(String name, T block, Class<N> instance, /*String constructor,*/ Object[] constValues) {
		try {
			//Block base = (Block) block;
			String regName = name.substring(name.indexOf(':')+1);
			Class<? extends Block> ct = block.getClass();
			N change = (N)CloneableBlock.clone(block, instance, /*constructor, */constValues);
			//N change = (N)block;
			GameRegistry.addSubstitutionAlias(name, GameRegistry.Type.BLOCK, change); //TODO WILL TELL, THE WORLD IS CORRUPT :@
			//GameRegistry.registerBlock(change, regName);
			return change;
		//} catch (ExistingSubstitutionException e) {
		//	co("INFO: This block was already replaced - Skipped!");
		//	e.printStackTrace();
		} catch (Exception e) {
			BOMlog.i("ERROR: Unable to clone Block class");
			e.printStackTrace();
		}
		return null;
	}
	
	private static String getPackageName(Class clazz) {
		String cname = clazz.getName();
		cname = cname.substring(0, cname.lastIndexOf('.'));
		BOMlog.i("[BLOBJMAP] Will import " + cname);
		return cname;
	}
}
