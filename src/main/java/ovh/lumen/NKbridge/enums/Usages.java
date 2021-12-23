package ovh.lumen.NKbridge.enums;

import ovh.lumen.NKbridge.data.NKData;

public enum Usages
{
	ROOT_CMD("/" + NKData.PLUGIN_NAME.toLowerCase() + " [reload|group]"),
	ROOT_GROUP_CMD("/" + NKData.PLUGIN_NAME.toLowerCase() + " group <subCommand>"),
	ROOT_GROUP_ADD_WORLD_CMD("/" + NKData.PLUGIN_NAME.toLowerCase() + " group addworld <groupName> [worldName]"),
	ROOT_GROUP_CREATE_CMD("/" + NKData.PLUGIN_NAME.toLowerCase() + " group create <groupName>"),
	ROOT_GROUP_DELETE_CMD("/" + NKData.PLUGIN_NAME.toLowerCase() + " group delete <groupName>"),
	ROOT_GROUP_REMOVE_WORLD_CMD("/" + NKData.PLUGIN_NAME.toLowerCase() + " group removeworld <groupName> [worldName]");

	private final String value;

	Usages(String value)
	{
		this.value = value;
	}

	public String toString()
	{
		return InternalMessages.PREFIX_USAGE + this.value;
	}
}
