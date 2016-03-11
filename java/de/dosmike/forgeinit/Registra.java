package de.dosmike.forgeinit;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Loader;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryNamespaced;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import de.dosmike.blobjmap.BOMlog;
import de.dosmike.blobjmap.RegistryWrapper;
import de.dosmike.blobjmap.blocks.BlockCloner;
import de.dosmike.blobjmap.blocks.dummyBlock;
import de.dosmike.blobjmap.blocks.dummyTileEntity;

public class Registra {

	final static String tpack = dummyBlock.class.getPackage().getName();
	
	private static ClassPool cp;
	
	public Registra() {
		new File("BOMpatch/de/dosmike/blobjmap/blocks/").mkdirs();
		try {
			Launch.classLoader.addURL(new File("BOMpatch").toURL());
		} catch (MalformedURLException e3) {
			e3.printStackTrace();
		}
		
		
		ClassPool cp = ClassPool.getDefault();
		Loader myCL = new Loader(Launch.classLoader, cp);
		//try {
			//cp.makePackage(BlObjMapper.instance.getClass().getClassLoader(), tpack);
		cp.insertClassPath(new ClassClassPath(dummyBlock.class));
		try {
			cp.insertClassPath("net.minecraft");
			//cp.appendClassPath("aor");	//obfuscated name for TileEntity
			//cp.getClassLoader().loadClass("aor");
			//cp.appendClassPath("aji");	//obfuscated name for World
			cp.insertClassPath("net.minecraft.tileentity.TileEntity");
			cp.importPackage("net.minecraft.tileentity");
			cp.getClassLoader().loadClass("net.minecraft.tileentity.TileEntity");
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}
	
	public Class<? extends Block> bindTE(String modId, Block block, TileEntitySpecialRenderer renderer) {
		String rcBaseN = modId + "_" + block.getClass().getSimpleName();
		Class<? extends Block> result = null;
		String bpack = null;
		String tecname = rcBaseN + "_TileEntity";
		BOMlog.i("BlockGen", "=== CREATE RENDERER FOR " + block.getUnlocalizedName() + " ===");
		
		//create new tileEntity class
		Class<? extends TileEntity> tec;
		try {
			bpack = block.getClass().getName();//cp.get(block.getClass().getName()).getPackageName();
			bpack = bpack.substring(0, bpack.lastIndexOf('.'));
			CtClass cc = cp.makeClass(tpack + "." + tecname);
			cc.setSuperclass(cp.getCtClass(dummyTileEntity.class.getName()));
			//cp.makePackage(cp.getClassLoader(), tpack);
			cc.writeFile("BOMpatch");
			tec = (Class<? extends TileEntity>) Class.forName(cc.getName(), false, Launch.classLoader);
			//tec = cc.toClass(Launch.classLoader, Launch.class.getProtectionDomain());
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
			cc = cp.makeClass(tpack + ".BOM_"+rcBaseN);
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
		if ((cc=injectMethod("public de.dosmike.blobjmap.blocks.dummyTileEntity createTileEntity(aji world, int i) { System.out.println(\"[BLOBJMAP] Placing Tile entity: " + tec.getName() + "\"); return new " + tec.getName() + "(); }", cc)) == null) return null;
		
		//BOMlog.i("BlockGen", "Replacing existing Block class...");
		try {
			cc.writeFile("BOMpatch");
			//Class<? extends Block> ccr = cc.toClass(Launch.classLoader, Launch.class.getProtectionDomain());//cc.toClass(); //recompile as subclass
			//actually loading the classes into forge
			Class<? extends Block> tcc = (Class<? extends Block>) Class.forName(cc.getName(), false, Launch.classLoader);
			BOMlog.i("BlockGen", "Check " + tcc.getName() + " > " + tcc.getSuperclass().getName() + " - " + block.getClass().getName() + " - InstanceOf: " + tcc.getSuperclass().getName().equals(block.getClass().getName()) );
			BOMlog.i("BlockGen", "Check CL %s > FCL %s", tcc.getClassLoader(), Launch.classLoader);
			
			result = tcc;
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
	
	private CtClass injectMethod(String method, CtClass cc) {
		BOMlog.i("Injecting " + method);
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
	
	private void addAnnotation(CtClass cc, CtMethod cm, String annotation) {
		//add override annotation
		ClassFile cfile = cc.getClassFile();
        ConstPool cpool = cfile.getConstPool();
 
        AnnotationsAttribute attr = new AnnotationsAttribute(cpool, AnnotationsAttribute.visibleTag);
        Annotation annot = new Annotation("java.lang." + annotation, cpool);
        attr.addAnnotation(annot);
        cm.getMethodInfo().addAttribute(attr);
	}
	
	public enum RegType { SubstitutionAlias, Reregister, InstanceRegisters }; 
	public Block registerBlock(RegType type, String blockId, Class<? extends Block> repClass, Object... constructorValues) {
		Block cbn = Block.getBlockFromName(blockId);
    	String modId = blockId.substring(0, blockId.indexOf(':'));
		BOMlog.i("BlockGen", "Substitute Name: " + blockId);		
		
		if (type == RegType.SubstitutionAlias)
			return registerBlockSubstitutionAlias(blockId, cbn, repClass, constructorValues);
		else if (type == RegType.InstanceRegisters)
			return registerBlockConstructor(blockId, cbn, repClass, constructorValues);
		else {
			BOMlog.i("RegType", "Type 'Reregister' is not yet implemented");
			return null;
		}
	}
	
	//this function exists to be able to use subclass casts
	private <T extends Block, N extends T> N registerBlockSubstitutionAlias(String name, T block, Class<N> instance, Object[] constValues) {
		try {
			Class<? extends Block> ct = block.getClass();
			N change = (N)BlockCloner.clone(block, instance, /*constructor, */constValues);
			GameRegistry.addSubstitutionAlias(name, GameRegistry.Type.BLOCK, change); //TODO WILL TELL, THE WORLD IS CORRUPT :@
			return change;
		} catch (Exception e) {
			BOMlog.i("ERROR: Unable to clone Block class");
			e.printStackTrace();
		}
		return null;
	}
	private <T extends Block, N extends T> N registerBlockConstructor(String name, T block, Class<N> instance, Object[] constValues) {
		
		RegistryNamespaced breg = (RegistryNamespaced)GameData.getBlockRegistry();
		RegistryWrapper wreg = (RegistryWrapper)breg;
		int id = wreg.removeEntry(name);
		N registerBlock = null;
		try {
			registerBlock = (N) instance.getConstructors()[0].newInstance(constValues);
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getCause() != null)
				e.getCause().printStackTrace();
		}
		wreg.ensureId(name, id);
		
		return registerBlock;
	}
	
	private String getPackageName(Class clazz) {
		String cname = clazz.getName();
		cname = cname.substring(0, cname.lastIndexOf('.'));
		BOMlog.i("Will import " + cname);
		return cname;
	}
}
