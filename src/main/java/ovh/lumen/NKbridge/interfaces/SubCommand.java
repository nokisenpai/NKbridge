package ovh.lumen.NKbridge.interfaces;

import org.bukkit.command.CommandSender;

public interface SubCommand
{
	boolean execute(CommandSender sender, String[] args);
}
