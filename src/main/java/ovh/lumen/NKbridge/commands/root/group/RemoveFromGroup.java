package ovh.lumen.NKbridge.commands.root.group;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ovh.lumen.NKbridge.enums.Messages;
import ovh.lumen.NKbridge.enums.Usages;
import ovh.lumen.NKbridge.exceptions.NKException;
import ovh.lumen.NKbridge.interfaces.SubCommand;
import ovh.lumen.NKbridge.managers.AsyncQueueManager;
import ovh.lumen.NKbridge.managers.GroupManager;
import ovh.lumen.NKbridge.managers.PlayerDataManager;
import ovh.lumen.NKbridge.utils.CheckType;
import ovh.lumen.NKbridge.utils.MessageParser;
import ovh.lumen.NKbridge.utils.NKLogger;

public class RemoveFromGroup implements SubCommand
{
	public RemoveFromGroup()
	{

	}

	public boolean execute(CommandSender sender, String[] args)
	{
		if(args.length == 2)
		{
			sender.sendMessage(Usages.ROOT_GROUP_REMOVE_WORLD_CMD.toString());

			return true;
		}

		if(sender instanceof Player)
		{
			if(args.length >= 3)
			{
				if(!CheckType.isAlphaNumeric(args[2]))
				{
					sender.sendMessage(Messages.ILLEGAL_CHAR_GROUP_NAME.toString());

					return true;
				}

				String groupName = args[2].toLowerCase();
				if(!GroupManager.existGroup(groupName))
				{
					MessageParser messageParser = new MessageParser(Messages.UNKNOW_GROUP.toString());
					messageParser.addArg(args[2]);

					sender.sendMessage(messageParser.parse());

					return true;
				}

				String worldName = ((Player) sender).getWorld().getName();
				if(args.length >= 4)
				{
					if(Bukkit.getWorld(args[3]) == null)
					{
						MessageParser messageParser = new MessageParser(Messages.UNKNOW_WORLD.toString());
						messageParser.addArg(args[3]);

						sender.sendMessage(messageParser.parse());

						return true;
					}
					worldName = args[3];
				}

				if(!GroupManager.isWorldInGroup(groupName, worldName))
				{
					MessageParser messageParser = new MessageParser(Messages.NOT_IN_GROUP.toString());
					messageParser.addArg(worldName);
					messageParser.addArg(args[2]);

					sender.sendMessage(messageParser.parse());

					return true;
				}

				String finalWorldName = worldName;
				AsyncQueueManager.addToQueue(o ->
				{
					try
					{
						PlayerDataManager.savePlayersData(finalWorldName);
					}
					catch(NKException e)
					{
						NKLogger.error(e.getMessage());
					}

					try
					{
						GroupManager.removeFromGroup(groupName, finalWorldName);
					}
					catch(NKException e)
					{
						NKLogger.error(e.getMessage());
					}

					try
					{
						PlayerDataManager.loadPlayersData(finalWorldName);
					}
					catch(NKException e)
					{
						NKLogger.error(e.getMessage());
					}

					MessageParser messageParser = new MessageParser(Messages.REMOVED_FROM_GROUP.toString());
					messageParser.addArg(finalWorldName);
					messageParser.addArg(args[2]);

					sender.sendMessage(messageParser.parse());

					return null;
				});
			}
		}

		return true;
	}
}
