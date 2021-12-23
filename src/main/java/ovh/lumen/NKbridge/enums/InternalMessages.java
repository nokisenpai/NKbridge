package ovh.lumen.NKbridge.enums;

import org.bukkit.ChatColor;

public enum InternalMessages
{
	PREFIX_USAGE(ChatColor.BLUE + "Usage : "),
	CONFIG_KEY_LOGLEVEL_BAD_VALUE("Key 'log-level' has bad value in config.yml. Default value 'LOG' used."),
	NKCORE_MISSING("Can't find NKcore plugin."),
	NKCORE_CANT_GET_API("Can't get NKcore API."),
	DATABASE_CANT_CONNECT("Error while attempting database connection."),
	DATABASE_CANT_CHECK_TABLES("Error while testing tables existance."),
	DATABASE_CANT_CREATE_TABLES("Error while creating database structure."),
	DATABASE_CREATE_TABLES_SUCCESS(ChatColor.GREEN + "Database tables created."),
	REGISTER_COMMAND_FAIL("Can't register " + ChatColor.DARK_AQUA + "%0%" + ChatColor.DARK_PURPLE + " command."),
	REGISTER_COMPLETER_FAIL("Can't register " + ChatColor.DARK_AQUA + "%0%" + ChatColor.DARK_PURPLE + " completer."),
	RELOAD_ANNOUNCE(ChatColor.BLUE + "Plugin reloading ..."),
	GROUP_LOAD_ERROR("Error while loading groups from database."),
	PLAYER_DATA_LOAD_ERROR("Error while loading player data from database."),
	PLAYER_DATA_SAVE_ERROR("Error while saving player data in database."),
	PLAYER_ADVANCEMENT_LOAD_ERROR("Error while loading player advancement from database."),
	PLAYER_ADVANCEMENT_STORE_ERROR("Error while storing player advancement in database."),
	PLAYER_ADVANCEMENT_SAVE_ERROR("Error while saving player advancement in database."),
	PLAYER_STATISTIC_LOAD_ERROR("Error while loading player statistic from database."),
	PLAYER_STATISTIC_RESET_ERROR("Error while resetting player statistic."),
	PLAYER_STATISTIC_SAVE_ERROR("Error while saving player statistic in database."),
	WORLDS_IN_GROUP_LOAD_ERROR("Error while loading worlds in groups from database."),
	INSERT_GROUP_ERROR("Error while inserting group in database."),
	DELETE_GROUP_ERROR("Error while deleting group in database."),
	INSERT_WORLD_IN_GROUP_ERROR("Error while inserting world in group in database."),
	DELETE_WORLD_FROM_GROUP_ERROR("Error while deleting world from group in database.");

	private final String value;

	InternalMessages(String value)
	{
		this.value = value;
	}

	public String toString()
	{
		return this.value;
	}
}
