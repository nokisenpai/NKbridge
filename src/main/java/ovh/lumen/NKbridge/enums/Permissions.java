package ovh.lumen.NKbridge.enums;

import ovh.lumen.NKbridge.data.NKData;

public enum Permissions
{
	USER("user"),
	ADMIN("admin"),

	ROOT_CMD(""),
	ROOT_RELOAD_CMD(".reload"),
	ROOT_GROUP_CMD(".group");

	private final String value;

	Permissions(String value)
	{
		this.value = value;
	}

	public String toString()
	{
		return NKData.PLUGIN_NAME + this.value;
	}
}
