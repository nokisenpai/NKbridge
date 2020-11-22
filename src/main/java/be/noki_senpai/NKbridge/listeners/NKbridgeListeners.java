package be.noki_senpai.NKbridge.listeners;

import be.noki_senpai.NKbridge.data.NKPlayer;
import be.noki_senpai.NKbridge.managers.GroupManager;
import be.noki_senpai.NKbridge.managers.InventoryManager;
import be.noki_senpai.NKbridge.managers.PlayerManager;
import be.noki_senpai.NKbridge.managers.QueueManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

import java.util.function.Function;

public class NKbridgeListeners implements Listener
{
	private PlayerManager playerManager = null;
	private InventoryManager inventoryManager = null;
	private QueueManager queueManager = null;
	private GroupManager groupManager = null;

	public NKbridgeListeners(PlayerManager playerManager, InventoryManager inventoryManager, QueueManager queueManager, GroupManager groupManager)
	{
		this.playerManager = playerManager;
		this.inventoryManager = inventoryManager;
		this.queueManager = queueManager;
		this.groupManager = groupManager;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerDropEvent(final PlayerDropItemEvent event)
	{
		if(!playerManager.getPlayer(event.getPlayer().getName()).canSave())
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerGameModeChangeEvent(final PlayerGameModeChangeEvent event)
	{
		Player player = event.getPlayer();
		String oldGameMode = player.getGameMode().name();

		//System.out.println(ChatColor.GOLD + "GameModeChangeEvent - sync - fired for player " + ChatColor.AQUA + player.getName());
		//System.out.println(ChatColor.GOLD + "GameModeChangeEvent - sync - old player gamemode " + ChatColor.AQUA + oldGameMode);

		queueManager.addToQueue(new Function()
		{
			@Override
			public Object apply(Object o)
			{
				NKPlayer nkPlayer = playerManager.getPlayer(event.getPlayer().getName());
				//System.out.println(ChatColor.GOLD + "GameModeChangeEvent - async - actual player gamemode " + ChatColor.AQUA + player.getGameMode().name());
				if(nkPlayer.canSave())
				{
					inventoryManager.saveInventory(player, nkPlayer.getId(), oldGameMode);
				}
				else
				{
					System.out.println(
							ChatColor.GOLD + "[" + nkPlayer.getPlayerName() + "] GameModeChangeEvent - cannot save inventory - old gamemode : "
									+ ChatColor.AQUA + oldGameMode + ChatColor.GOLD + " - actual gamemode " + ChatColor.AQUA
									+ player.getGameMode().name());
				}
				inventoryManager.loadInventory(player, nkPlayer);
				return null;
			}
		});
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerChangedWorldEvent(final PlayerChangedWorldEvent event)
	{
		Player player = event.getPlayer();

		//System.out.println(ChatColor.GOLD + "PlayerChangedWorldEvent - sync - fired for player " + ChatColor.AQUA + player.getName());
		//System.out.println(ChatColor.GOLD + "PlayerChangedWorldEvent - sync - player gamemode " + ChatColor.AQUA + player.getGameMode().name());

		if(!groupManager.isSameGroup(event.getFrom().getName(), player.getWorld().getName()))
		{
			//System.out.println(ChatColor.GOLD + "PlayerChangedWorldEvent - sync - groups are different. Inventory must be save and load.");
			queueManager.addToQueue(new Function()
			{
				@Override
				public Object apply(Object o)
				{

					//System.out.println(ChatColor.GOLD + "PlayerChangedWorldEvent - async - player gamemode " + ChatColor.AQUA + player.getGameMode().name());
					NKPlayer nkPlayer = playerManager.getPlayer(event.getPlayer().getName());
					if(nkPlayer.canSave())
					{
						inventoryManager.saveInventory(player, nkPlayer.getId(), event.getFrom());
					}
					else
					{
						System.out.println(
								ChatColor.GOLD + "[" + nkPlayer.getPlayerName() + "] PlayerChangedWorldEvent - cannot save inventory - from : "
										+ ChatColor.AQUA + event.getFrom() + ChatColor.GOLD + " - to " + ChatColor.AQUA
										+ player.getWorld().getName());
					}
					inventoryManager.saveInventory(player, nkPlayer.getId(), event.getFrom());
					inventoryManager.loadInventory(player, nkPlayer);
					return null;
				}
			});
		}
	}
}
