package ovh.lumen.NKbridge.commands.root.group;

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

public class CreateGroup implements SubCommand
{
	public CreateGroup()
	{

	}

	public boolean execute(CommandSender sender, String[] args)
	{
		if(args.length == 2)
		{
			sender.sendMessage(Usages.ROOT_GROUP_CREATE_CMD.toString());

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
			if(GroupManager.existGroup(groupName))
			{
				MessageParser messageParser = new MessageParser(Messages.GROUP_ALREADY_EXIST.toString());
				messageParser.addArg(args[2]);

				sender.sendMessage(messageParser.parse());

				return true;
			}

			AsyncQueueManager.addToQueue(o ->
			{
				try
				{
					GroupManager.createGroup(groupName);
				}
				catch(SetupException e)
				{
					NKLogger.error(e.getMessage());
				}

				MessageParser messageParser = new MessageParser(Messages.GROUP_CREATED.toString());
				messageParser.addArg(args[2]);

				sender.sendMessage(messageParser.parse());

				return null;
			});
		}
		return true;
	}
}
