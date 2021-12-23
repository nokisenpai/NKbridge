package ovh.lumen.NKbridge.listeners;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ovh.lumen.NKbridge.data.NKData;
import ovh.lumen.NKbridge.exceptions.NKException;
import ovh.lumen.NKbridge.exceptions.SetupException;
import ovh.lumen.NKbridge.managers.AsyncQueueManager;
import ovh.lumen.NKbridge.managers.PlayerDataManager;
import ovh.lumen.NKbridge.managers.NKcoreAPIManager;
import ovh.lumen.NKbridge.utils.NKLogger;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class PlayerListener implements Listener
{
	public PlayerListener()
	{

	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void PlayerJoinEvent(final PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		AsyncQueueManager.addToQueue(o ->
		{
			if(NKData.CANT_LOAD.contains(player.getUniqueId().toString()))
			{
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(ChatColor.RED + "Loading ...").create()); //TODO
				return null;
			}

			try
			{
				PlayerDataManager.loadPlayerData(player);
			}
			catch(NKException e)
			{
				NKLogger.error(e.getMessage());
			}

			return null;
		});
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuitEvent(final PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		NKcoreAPIManager.nKcoreAPI.broadcastNetworkData(NKData.PLUGIN_NAME, "CANT_LOAD_INVENTORY|ADD|" + player.getUniqueId(), NKData.PLUGIN_NAME);

		AsyncQueueManager.addToQueue(o ->
		{
			try
			{
				PlayerDataManager.savePlayerData(player);
				PlayerDataManager.saveAdvancements(player);
				PlayerDataManager.saveStatistics(player);
			}
			catch(NKException e)
			{
				NKLogger.error(e.getMessage());
			}

			NKcoreAPIManager.nKcoreAPI.broadcastNetworkData(NKData.PLUGIN_NAME, "CANT_LOAD_INVENTORY|REMOVE|" + player.getUniqueId(), NKData.PLUGIN_NAME);

			return null;
		});
	}
}