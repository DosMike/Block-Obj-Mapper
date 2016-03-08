package de.dosmike.blobjmap;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ChatComponentText;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameRegistry;

public class DiscoverCommand implements ICommand {

	private List aliases = new ArrayList();
	public DiscoverCommand() {
		this.aliases.add("blobjmapDiscover");
		this.aliases.add("blobjmap");
		this.aliases.add("blomap");
	}
	
	@Override
	public int compareTo(Object arg0) {
		return 0;
	}

	@Override
	public String getCommandName() {
		return "blobjmapDiscover";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "Take a block into your hand and Issue /" + getCommandName();
	}

	@Override
	public List getCommandAliases() {
		return aliases;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		EntityPlayer player;
		if(canCommandSenderUseCommand(sender)) {
			player = (EntityPlayer)sender;
	    } else {
			sendMessage(sender, "This command is Client Only!");
			return;
	    }
		Item iih = player.getHeldItem().getItem(); 
		if (iih == null) {
			sendMessage(sender, getCommandUsage(sender));
			return;
		}
		Block block = Block.getBlockFromItem(iih);
		if (block == Blocks.air) {
			sendMessage(sender, getCommandUsage(sender));
			return;
		}
		//TODO Return usefull information ;D
		String name = GameRegistry.findUniqueIdentifierFor(block).toString();
		String constructor = block.getClass().getConstructors()[0].toGenericString();
		sendMessage(sender, "Block: " + name);
		sendMessage(sender, "Constructor: " + constructor);
		sendMessage(sender, "Calling Args: ");
		int n = name.indexOf(':');
		String modid = (n > 0 ? name.substring(0, n) : "");
		
		List<ModContainer> mods = Loader.instance().getActiveModList();
		for (ModContainer mod : mods) {
			if (mod.getModId().equalsIgnoreCase(modid)) {
				mod.getMod().getClass().getName();
				sendMessage (sender, "modName: " + mod.getMod().getClass().getName());
			}
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return (sender instanceof EntityPlayer);
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] p_8args2358_1_, int i) {
		return false;
	}
	
	public void sendMessage(ICommandSender sender, String Message) {
		sender.addChatMessage(new ChatComponentText(Message));
	}
}
