package ovh.lumen.NKbridge.commands.root.group;

import org.bukkit.command.CommandSender;
import ovh.lumen.NKbridge.enums.Messages;
import ovh.lumen.NKbridge.interfaces.SubCommand;
import ovh.lumen.NKbridge.managers.GroupManager;
import ovh.lumen.NKbridge.utils.CheckType;
import ovh.lumen.NKbridge.utils.MessageParser;

public class ListGroup implements SubCommand
{
	public ListGroup()
	{

	}

	public boolean execute(CommandSender sender, String[] args)
	{
		if(args.length == 2)
		{
			StringBuilder msg = new StringBuilder(Messages.GROUP_LIST.toString());
			for(String group : GroupManager.getGroupList())
			{
				msg.append("\n   ").append(group);
			}
			sender.sendMessage(msg.toString());

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

			MessageParser messageParser = new MessageParser(Messages.GROUP_WORLDS_LIST.toString());
			messageParser.addArg(groupName);

			StringBuilder msg = new StringBuilder(messageParser.parse());
			for(String world : GroupManager.getGroup(groupName))
			{
				msg.append("\n   ").append(world);
			}
			sender.sendMessage(msg.toString());
		}
		return true;
	}
}
