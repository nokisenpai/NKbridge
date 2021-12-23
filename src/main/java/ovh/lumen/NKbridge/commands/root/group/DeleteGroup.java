package ovh.lumen.NKbridge.commands.root.group;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import ovh.lumen.NKbridge.enums.Messages;
import ovh.lumen.NKbridge.enums.Usages;
import ovh.lumen.NKbridge.exceptions.SetupException;
import ovh.lumen.NKbridge.interfaces.SubCommand;
import ovh.lumen.NKbridge.managers.AsyncQueueManager;
import ovh.lumen.NKbridge.managers.GroupManager;
import ovh.lumen.NKbridge.utils.CheckType;
import ovh.lumen.NKbridge.utils.MessageParser;
import ovh.lumen.NKbridge.utils.NKLogger;

public class DeleteGroup implements SubCommand
{
	public DeleteGroup()
	{

	}

	public boolean execute(CommandSender sender, String[] args)
	{
		if(args.length == 2)
		{
			sender.sendMessage(Usages.ROOT_GROUP_DELETE_CMD.toString());

			return true;
		}

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

			if(GroupManager.getGroup(groupName).size() > 0)
			{
				sender.sendMessage(Messages.GROUP_DELETE_NONE_EMPTY.toString()); //TODO

				return true;
			}

			AsyncQueueManager.addToQueue(o ->
			{
				try
				{
					GroupManager.deleteGroup(groupName);
				}
				catch(SetupException e)
				{
					NKLogger.error(e.getMessage());
				}

				MessageParser messageParser = new MessageParser(Messages.GROUP_DELETED.toString());
				messageParser.addArg(args[2]);

				sender.sendMessage(messageParser.parse());

				return null;
			});
		}
		return true;
	}
}
