package ovh.lumen.NKbridge.commands.root;

import org.bukkit.command.CommandSender;
import ovh.lumen.NKbridge.commands.root.group.*;
import ovh.lumen.NKbridge.enums.Messages;
import ovh.lumen.NKbridge.enums.Permissions;
import ovh.lumen.NKbridge.enums.Usages;
import ovh.lumen.NKbridge.interfaces.SubCommand;

public class Group implements SubCommand
{
	public Group()
	{

	}

	public boolean execute(CommandSender sender, String[] args)
	{
		if(!hasGroupPermissions(sender))
		{
			sender.sendMessage(Messages.PERMISSION_MISSING.toString());

			return true;
		}

		if(args.length == 1)
		{
			sender.sendMessage(Usages.ROOT_GROUP_CMD.toString());

			return true;
		}

		args[1] = args[1].toLowerCase();
		switch(args[1])
		{
			case "create":
				return new CreateGroup().execute(sender, args);
			case "delete":
				return new DeleteGroup().execute(sender, args);
			case "addworld":
				return new AddToGroup().execute(sender, args);
			case "removeworld":
				return new RemoveFromGroup().execute(sender, args);
			case "list":
				return new ListGroup().execute(sender, args);
			default:
				sender.sendMessage(Messages.UNKNOW_SUBCOMMAND.toString());
				return true;
		}
	}

	private boolean hasGroupPermissions(CommandSender sender)
	{
		return sender.hasPermission(Permissions.ROOT_GROUP_CMD.toString()) ||
				sender.hasPermission(Permissions.ADMIN.toString());
	}
}
