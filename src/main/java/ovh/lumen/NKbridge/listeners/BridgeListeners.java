package ovh.lumen.NKbridge.listeners;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

import ovh.lumen.NKbridge.data.AdvancementInfo;
import ovh.lumen.NKbridge.data.NKData;
import ovh.lumen.NKbridge.data.NKcoreEventAPIimpl;
import ovh.lumen.NKbridge.enums.Messages;
import ovh.lumen.NKbridge.exceptions.NKException;
import ovh.lumen.NKbridge.exceptions.SetupException;
import ovh.lumen.NKbridge.managers.AsyncQueueManager;
import ovh.lumen.NKbridge.managers.GroupManager;
import ovh.lumen.NKbridge.managers.NKcoreAPIManager;
import ovh.lumen.NKbridge.managers.PlayerDataManager;
import ovh.lumen.NKbridge.utils.MessageParser;
import ovh.lumen.NKbridge.utils.NKLogger;

public class BridgeListeners implements Listener
{
	public BridgeListeners()
	{

	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerDropEvent(final PlayerDropItemEvent event)
	{
		if(!NKData.CAN_SAVE.contains(event.getPlayer().getUniqueId().toString()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void PlayerAdvancementDoneEvent(final PlayerAdvancementDoneEvent event)
	{
		String advancementKey = event.getAdvancement().getKey().getKey();
		Player player = event.getPlayer();
		String worldName = player.getWorld().getName();

		if(!advancementKey.contains("recipe/"))
		{
			String data = "ADVANCEMENT_DONE|" + player.getDisplayName() + "|" + GroupManager.getGroupName(worldName) + "|" + advancementKey;

			NKcoreAPIManager.nKcoreAPI.broadcastNetworkData(NKData.PLUGIN_NAME, data, NKData.PLUGIN_NAME);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerGameModeChangeEvent(final PlayerGameModeChangeEvent event)
	{
		Player player = event.getPlayer();
		String oldGameMode = player.getGameMode().name();

		AsyncQueueManager.addToQueue(o ->
		{
			try
			{
				PlayerDataManager.savePlayerData(player, oldGameMode);
			}
			catch(NKException e)
			{
				NKLogger.error(e.getMessage());
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
	public void PlayerChangedWorldEvent(final PlayerChangedWorldEvent event)
	{
		Player player = event.getPlayer();

		if(!GroupManager.isSameGroup(event.getFrom().getName(), player.getWorld().getName()))
		{
			AsyncQueueManager.addToQueue(o ->
			{
				try
				{
					PlayerDataManager.savePlayerData(player, event.getFrom());
				}
				catch(NKException e)
				{
					NKLogger.error(e.getMessage());
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
	}
}
