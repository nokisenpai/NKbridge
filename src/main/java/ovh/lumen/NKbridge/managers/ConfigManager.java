package ovh.lumen.NKbridge.managers;

import org.bukkit.configuration.file.FileConfiguration;
import ovh.lumen.NKbridge.data.NKData;
import ovh.lumen.NKbridge.enums.InternalMessages;
import ovh.lumen.NKbridge.enums.LogLevel;
import ovh.lumen.NKbridge.utils.NKLogger;
import ovh.lumen.NKcore.api.data.DBAccess;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public final class ConfigManager
{
	private ConfigManager() {}

	private static FileConfiguration config = null;

	public static void init(FileConfiguration config)
	{
		ConfigManager.config = config;
	}

	public static void load()
	{
		boolean useNKcoreAccess = config.getBoolean("use-nkcore-access", true);

		if(useNKcoreAccess)
		{
			NKData.DBACCESS = NKcoreAPIManager.nKcoreAPI.getDBAccess();
		}
		else
		{
			DBAccess dbAccess = new DBAccess();
			dbAccess.setHost(config.getString("host"));
			dbAccess.setPort(config.getInt("port"));
			dbAccess.setDbName(config.getString("dbName"));
			dbAccess.setUser(config.getString("user"));
			dbAccess.setPassword(config.getString("password"));

			NKData.DBACCESS = dbAccess;
		}

		NKData.PREFIX = config.getString("table-prefix", NKData.PLUGIN_NAME + "_");

		NKData.SERVER_INFO = NKcoreAPIManager.nKcoreAPI.getNKServer();
		NKData.AUTO_SAVE_INVENTORIES = config.getInt("auto-save-inventories", 60);
		NKData.AUTO_SAVE_ADVANCEMENTS = config.getInt("auto-save-advancements", 600);

		try
		{
			NKData.LOGLEVEL = LogLevel.valueOf(config.getString("log-level", "LOG").toUpperCase());
		}
		catch(IllegalArgumentException e)
		{
			NKLogger.error(InternalMessages.CONFIG_KEY_LOGLEVEL_BAD_VALUE.toString());
			NKData.LOGLEVEL = LogLevel.LOG;
		}

		NKLogger.setLogLevel(NKData.LOGLEVEL);

		try
		{
			Properties pr = new Properties();
			FileInputStream in = new FileInputStream("server.properties");
			pr.load(in);
			NKData.DEFAULT_WORLD = pr.getProperty("level-name");
		}
		catch (IOException ignored)
		{
		}
	}

	private static void loadDataFromAPI()
	{
		NKData.SERVER_INFO = NKcoreAPIManager.nKcoreAPI.getNKServer();
		Map<String, Integer> worldsId = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		NKcoreAPIManager.nKcoreAPI.getNKWorlds().values().forEach(nkWorld -> {
			worldsId.put(nkWorld.getName(), nkWorld.getId());
		});

		NKData.WORLDS_ID = worldsId;
	}
}
