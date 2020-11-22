package be.noki_senpai.NKbridge.managers;

import be.noki_senpai.NKbridge.NKbridge;
import be.noki_senpai.NKbridge.data.NKPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class InventoryManager
{
	GroupManager groupManager = null;
	PlayerManager playerManager = null;

	public InventoryManager(GroupManager groupManager, PlayerManager playerManager)
	{
		this.groupManager = groupManager;
		this.playerManager = playerManager;
	}

	public void loadData()
	{
		loadInventories();
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				saveInventories();
			}
		}.runTaskTimerAsynchronously(NKbridge.getPlugin(), 60 * 20, 60 * 20);
	}

	public void loadInventory(Player player, NKPlayer nkPlayer)
	{
		int player_id = nkPlayer.getId();
		//System.out.println(ChatColor.GOLD + "loadInventory - called for player " + ChatColor.AQUA + player.getName());
		//System.out.println(ChatColor.GOLD + "loadInventory - player gamemode used for load " + ChatColor.AQUA + player.getGameMode().name());

		int group_id = groupManager.getGroupId(player.getWorld().getName().toLowerCase());

		/*System.out.println(ChatColor.GOLD + "loadInventory - data - "
				+ ChatColor.GOLD + "group_id : " + ChatColor.AQUA + group_id
				+ ChatColor.GOLD + " | player_id : " + ChatColor.AQUA + player_id
				+ ChatColor.GOLD + " | gamemode : " + ChatColor.AQUA + player.getGameMode().name());*/

		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "SELECT * FROM " + DatabaseManager.table.INV + " WHERE group_id = ? AND player_id = ? AND type = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, group_id);
			ps.setInt(2, player_id);
			ps.setString(3, player.getGameMode().name());
			resultat = ps.executeQuery();

			//System.out.println(ChatColor.GOLD + "loadInventory - execute query for gamemode " + ChatColor.AQUA + player.getGameMode().name());

			if(resultat.next())
			{
				//System.out.println(ChatColor.GOLD + "loadInventory - an inventory has been found for gamemode " + ChatColor.AQUA + player.getGameMode().name());
				YamlConfiguration restoreInv = new YamlConfiguration();

				restoreInv.loadFromString(resultat.getString("inventory"));
				ArrayList invData = (ArrayList) restoreInv.get("inventory", ItemStack[].class);
				player.getInventory().setContents((ItemStack[]) invData.toArray(new ItemStack[invData.size()]));

				restoreInv.loadFromString(resultat.getString("enderchest"));
				ArrayList ecData = (ArrayList) restoreInv.get("enderchest", ItemStack[].class);
				player.getEnderChest().setContents((ItemStack[]) ecData.toArray(new ItemStack[ecData.size()]));

				player.setHealth(resultat.getDouble("health"));
				player.setSaturation((float) resultat.getDouble("food_saturation"));
				player.setFoodLevel(resultat.getInt("food"));
				setTotalExperience(player, resultat.getInt("experience"));
			}
			else
			{
				//System.out.println(ChatColor.GOLD + "loadInventory - none inventory has been found for gamemode " + ChatColor.AQUA + player.getGameMode().name());
				ps.close();
				resultat.close();

				YamlConfiguration invData = new YamlConfiguration();
				invData.set("inventory", player.getInventory().getContents());

				YamlConfiguration ecData = new YamlConfiguration();
				ecData.set("enderchest", player.getEnderChest().getContents());

				req = "INSERT INTO " + DatabaseManager.table.INV
						+ " ( group_id, player_id , type, inventory, enderchest, health, food, food_saturation, experience ) VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? )";
				ps = bdd.prepareStatement(req);
				ps.setInt(1, group_id);
				ps.setInt(2, player_id);
				ps.setString(3, player.getGameMode().name());
				ps.setString(4, invData.saveToString());
				ps.setString(5, ecData.saveToString());
				ps.setDouble(6, player.getHealth());
				ps.setDouble(7, player.getFoodLevel());
				ps.setDouble(8, player.getSaturation());
				ps.setDouble(9, getTotalExperience(player));

				ps.execute();

				//System.out.println(ChatColor.GOLD + "loadInventory - new inventory inserted for gamemode " + ChatColor.AQUA + player.getGameMode().name());

				ps.close();
			}

			ps.close();
			resultat.close();

			nkPlayer.setCanSave(true);
		}
		catch(SQLException | InvalidConfigurationException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKbridge.PNAME + " Error while getting players items.");
			e.printStackTrace();
			nkPlayer.setCanSave(true);
		}
	}

	public void saveInventory(Player player, int player_id, String gameMode, String worldName)
	{
		//System.out.println(ChatColor.GOLD + "saveInventory - called for player " + ChatColor.AQUA + player.getName());
		//System.out.println(ChatColor.GOLD + "saveInventory - player gamemode used for save " + ChatColor.AQUA + gameMode);
		int group_id = groupManager.getGroupId(worldName.toLowerCase());

		/*System.out.println(ChatColor.GOLD + "saveInventory - data - "
				+ ChatColor.GOLD + "group_id : " + ChatColor.AQUA + group_id
				+ ChatColor.GOLD + " | player_id : " + ChatColor.AQUA + player_id
				+ ChatColor.GOLD + " | gamemode : " + ChatColor.AQUA + gameMode);*/

		Connection bdd = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			YamlConfiguration invData = new YamlConfiguration();
			invData.set("inventory", player.getInventory().getContents());

			YamlConfiguration ecData = new YamlConfiguration();
			ecData.set("enderchest", player.getEnderChest().getContents());

			req = "UPDATE " + DatabaseManager.table.INV
					+ " SET inventory = ? , enderchest = ? , health = ? , food = ? , food_saturation = ? , experience = ? WHERE group_id = ? AND player_id = ? AND type = ?";
			ps = bdd.prepareStatement(req);

			ps.setString(1, invData.saveToString());
			ps.setString(2, ecData.saveToString());
			ps.setDouble(3, player.getHealth());
			ps.setDouble(4, player.getFoodLevel());
			ps.setDouble(5, player.getSaturation());
			ps.setDouble(6, getTotalExperience(player));
			ps.setInt(7, group_id);
			ps.setInt(8, player_id);
			ps.setString(9, gameMode);

			ps.executeUpdate();

			ps.close();
		}
		catch(SQLException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKbridge.PNAME + " Error while getting players items.");
			e.printStackTrace();
		}
	}

	public void saveInventory(Player player, int player_id, World world)
	{
		saveInventory(player, player_id, player.getGameMode().name(), world.getName());
	}

	public void saveInventory(Player player, int player_id, String gameMode)
	{
		saveInventory(player, player_id, gameMode, player.getWorld().getName());
	}

	public void saveInventory(Player player, NKPlayer nkPlayer)
	{
		if(nkPlayer.canSave())
		{
			saveInventory(player, nkPlayer.getId(), player.getGameMode().name(), player.getWorld().getName());
		}
	}

	public void loadInventories()
	{
		Bukkit.getOnlinePlayers().forEach(player -> loadInventory(player, playerManager.getPlayer(player.getName())));
	}

	public void saveInventories()
	{
		Bukkit.getOnlinePlayers().forEach(player -> saveInventory(player, playerManager.getPlayer(player.getName())));
	}

	public void loadInventories(String worldName)
	{
		Bukkit.getWorld(worldName).getPlayers().forEach(player -> loadInventory(player, playerManager.getPlayer(player.getName())));
	}

	public void saveInventories(String worldName)
	{
		Bukkit.getWorld(worldName).getPlayers().forEach(player -> saveInventory(player, playerManager.getPlayer(player.getName())));
	}

	public static int getTotalExperience(int level)
	{
		int xp = 0;

		if(level >= 0 && level <= 15)
		{
			xp = (int) Math.round(Math.pow(level, 2) + 6 * level);
		}
		else if(level > 15 && level <= 30)
		{
			xp = (int) Math.round((2.5 * Math.pow(level, 2) - 40.5 * level + 360));
		}
		else if(level > 30)
		{
			xp = (int) Math.round(((4.5 * Math.pow(level, 2) - 162.5 * level + 2220)));
		}
		return xp;
	}

	public static int getTotalExperience(Player player)
	{
		return Math.round(player.getExp() * player.getExpToLevel()) + getTotalExperience(player.getLevel());
	}

	public static void setTotalExperience(Player player, int amount)
	{
		int level = 0;
		int xp = 0;
		float a = 0;
		float b = 0;
		float c = -amount;

		if(amount > getTotalExperience(0) && amount <= getTotalExperience(15))
		{
			a = 1;
			b = 6;
		}
		else if(amount > getTotalExperience(15) && amount <= getTotalExperience(30))
		{
			a = 2.5f;
			b = -40.5f;
			c += 360;
		}
		else if(amount > getTotalExperience(30))
		{
			a = 4.5f;
			b = -162.5f;
			c += 2220;
		}
		level = (int) Math.floor((-b + Math.sqrt(Math.pow(b, 2) - (4 * a * c))) / (2 * a));
		xp = amount - getTotalExperience(level);
		player.setLevel(level);
		player.setExp(0);
		player.giveExp(xp);
	}
}
