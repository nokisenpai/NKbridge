package ovh.lumen.NKbridge.data;

import ovh.lumen.NKbridge.enums.LogLevel;
import ovh.lumen.NKbridge.interfaces.NKplugin;
import ovh.lumen.NKcore.api.data.DBAccess;
import ovh.lumen.NKcore.api.data.NKServer;

import java.util.*;

public class NKData
{
	public static DBAccess DBACCESS = new DBAccess();
	public static NKServer SERVER_INFO = null;
	public static String PREFIX = null;
	public static LogLevel LOGLEVEL = null;
	public static NKplugin PLUGIN = null;
	public static String PLUGIN_NAME = null;
	public static String PLUGIN_VERSION = null;
	public static String PLUGIN_AUTHOR = null;
	public static Map<String, String> WORLDS_GROUP = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public static Map<String, Integer> WORLDS_ID = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public static Map<String, Integer> GROUPS_ID = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public static int AUTO_SAVE_INVENTORIES = 60;
	public static int AUTO_SAVE_ADVANCEMENTS = 600;
	public static List<String> CAN_SAVE = new ArrayList<>();
	public static List<String> CANT_LOAD = new ArrayList<>();
	public static Map<String, AdvancementInfo> ADVANCEMENTS_INFO = new HashMap<>();
	public static String DEFAULT_WORLD = null;
}
