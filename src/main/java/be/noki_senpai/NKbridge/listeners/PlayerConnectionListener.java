package be.noki_senpai.NKbridge.listeners;

import be.noki_senpai.NKbridge.NKbridge;
import be.noki_senpai.NKbridge.managers.InventoryManager;
import be.noki_senpai.NKbridge.managers.PlayerManager;
import be.noki_senpai.NKbridge.managers.QueueManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Function;

public class PlayerConnectionListener implements Listener
{
	private PlayerManager playerManager = null;
	private InventoryManager inventoryManager = null;
	private QueueManager queueManager = null;

	public PlayerConnectionListener(PlayerManager playerManager, InventoryManager inventoryManager, QueueManager queueManager)
	{
		this.playerManager = playerManager;
		this.inventoryManager = inventoryManager;
		this.queueManager = queueManager;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void PlayerJoinEvent(final PlayerJoinEvent event)
	{
		//System.out.println(ChatColor.GOLD + "PlayerJoinEvent - sync - fired for player " + ChatColor.AQUA + event.getPlayer().getName());
		//System.out.println(ChatColor.GOLD + "PlayerJoinEvent - sync - player gamemode " + ChatColor.AQUA + event.getPlayer().getGameMode().name());

		queueManager.addToQueue(new Function()
		{
			@Override
			public Object apply(Object o)
			{
				try
				{
					Thread.sleep(500);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}

				playerManager.addPlayer(event.getPlayer());

				try
				{
					//System.out.println(ChatColor.GOLD + "PlayerJoinEvent - async - Waiting 1500 milliseconds");
					Thread.sleep(1500);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}

				//System.out.println(ChatColor.GOLD + "PlayerJoinEvent - async - player gamemode " + ChatColor.AQUA + event.getPlayer().getGameMode().name());
				inventoryManager.loadInventory(event.getPlayer(), playerManager.getPlayer(event.getPlayer().getName()));
				return null;
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuitEvent(final PlayerQuitEvent event)
	{
		//System.out.println(ChatColor.GOLD + "PlayerQuitEvent - sync - fired for player " + ChatColor.AQUA + event.getPlayer().getName());
		//System.out.println(ChatColor.GOLD + "PlayerQuitEvent - sync - player gamemode " + ChatColor.AQUA + event.getPlayer().getGameMode().name());
		queueManager.addToQueue(new Function()
		{
			@Override
			public Object apply(Object o)
			{
				//System.out.println(ChatColor.GOLD + "PlayerQuitEvent - async - player gamemode " + ChatColor.AQUA + event.getPlayer().getGameMode().name());
				inventoryManager.saveInventory(event.getPlayer(), playerManager.getPlayer(event.getPlayer().getName()));
				playerManager.delPlayer(event.getPlayer().getName());
				return null;
			}
		});
	}
}
