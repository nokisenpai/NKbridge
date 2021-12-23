package ovh.lumen.NKbridge;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import ovh.lumen.NKbridge.data.NKData;
import ovh.lumen.NKbridge.enums.InternalMessages;
import ovh.lumen.NKbridge.exceptions.NKException;
import ovh.lumen.NKbridge.exceptions.SetupException;
import ovh.lumen.NKbridge.interfaces.NKplugin;
import ovh.lumen.NKbridge.managers.*;
import ovh.lumen.NKbridge.registers.CommandRegister;
import ovh.lumen.NKbridge.registers.CompleterRegister;
import ovh.lumen.NKbridge.registers.ListenerRegister;
import ovh.lumen.NKbridge.utils.NKLogger;

import java.io.File;

public class Main extends JavaPlugin implements NKplugin
{
	@Override
	public void onEnable()
	{
		setup();
	}

	@Override
	public void onDisable()
	{
		clean();
	}

	@Override
	public void setup()
	{
		NKData.PLUGIN = this;
		NKData.PLUGIN_NAME = this.getName();
		NKData.PLUGIN_VERSION = this.getDescription().getVersion();
		NKData.PLUGIN_AUTHOR = this.getDescription().getAuthors().get(0);

		this.saveDefaultConfig();

		// Init
		NKLogger.init(Bukkit.getConsoleSender());
		ConfigManager.init(this.getConfig());
		AdvancementManager.init(new File(this.getDataFolder() + "/advancements_FR.yaml"));

		// Load
		try
		{
			NKcoreAPIManager.load(this);
			ConfigManager.load();
			AdvancementManager.load();
			DatabaseManager.load();
			GroupManager.load();
			PlayerDataManager.load();
		}
		catch(SetupException|NKException e)
		{
			NKLogger.error(e.getMessage());
			disablePlugin();

			return;
		}

		//Register
		ListenerRegister.registerAllListeners(this);
		CommandRegister.registerAllCommands(this);
		CompleterRegister.registerAllCompleters(this);

		displayNKSuccess();
	}

	@Override
	public void clean()
	{
		DatabaseManager.unload();
		GroupManager.unload();
		PlayerDataManager.unload();
		ListenerRegister.unregisterAllListeners(this);
	}

	@Override
	public void reload()
	{
		NKLogger.send(InternalMessages.RELOAD_ANNOUNCE.toString());
		clean();
		setup();
	}

	public void disablePlugin()
	{
		getServer().getPluginManager().disablePlugin(this);
	}

	private void displayNKSuccess()
	{
		NKLogger.show("\n"
				+ ChatColor.WHITE + "\u00A0 \u00A0 \u00A0.--.\n"
				+ "\u00A0 \u00A0 \u00A0| \u00A0 '. \u00A0" + ChatColor.GREEN + NKData.PLUGIN_NAME + " v" + NKData.PLUGIN_VERSION + " by " + NKData.PLUGIN_AUTHOR
				+ " - successfully enabled !\n"
				+ ChatColor.WHITE + "'-..___.-'");
	}
}
