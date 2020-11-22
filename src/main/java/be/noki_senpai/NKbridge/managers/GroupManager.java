package be.noki_senpai.NKbridge.managers;

import be.noki_senpai.NKbridge.NKbridge;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GroupManager
{
	// Players datas
	private Map<String, String> groups = null;
	private Map<String, Integer> worldsId = null;
	private Map<String, Integer> groupsId = null;
	private ConsoleCommandSender console = null;

	public static int SERVERID = -1;

	public GroupManager()
	{
		this.groups = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		this.worldsId = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
		this.groupsId = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
		;
		this.console = Bukkit.getConsoleSender();
	}

	// Load data from database
	public boolean loadData()
	{
		if(!makeServerId())
		{
			return false;
		}

		if(!makeWorldsId())
		{
			return false;
		}

		if(!loadGroup())
		{
			return false;
		}

		if(!loadWorldsInGroup())
		{
			return false;
		}

		return true;
	}

	// ######################################
	// Get server id
	// ######################################

	public boolean makeServerId()
	{
		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "SELECT id FROM " + DatabaseManager.common.SERVERS + " WHERE name = ?";
			ps = bdd.prepareStatement(req);
			ps.setString(1, ConfigManager.SERVERNAME);

			resultat = ps.executeQuery();

			if(resultat.next())
			{
				SERVERID = resultat.getInt(1);
			}
			else
			{
				ps.close();
				resultat.close();
				return false;
			}

			ps.close();
			resultat.close();
		}
		catch(SQLException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKbridge.PNAME + " Error while getting server id.");
			e.printStackTrace();
		}
		return true;
	}

	// ######################################
	// Get worlds id
	// ######################################

	public boolean makeWorldsId()
	{
		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			for(World world : Bukkit.getWorlds())
			{
				req = "SELECT id FROM " + DatabaseManager.common.WORLDS + " WHERE server_id = ? AND name = ?";
				ps = bdd.prepareStatement(req);
				ps.setInt(1, SERVERID);
				ps.setString(2, world.getName());

				resultat = ps.executeQuery();

				if(resultat.next())
				{
					worldsId.put(world.getName().toLowerCase(), resultat.getInt(1));
					Bukkit.getConsoleSender().sendMessage(
							ChatColor.DARK_RED + NKbridge.PNAME + "[DEBUG] worldName=" + world.getName() + " | worldId=" + resultat.getInt(1));
				}
				else
				{
					ps.close();
					resultat.close();
					Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKbridge.PNAME + " Error while getting worlds id.");
					return false;
				}

				ps.close();
				resultat.close();
			}
		}
		catch(SQLException e1)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKbridge.PNAME + " Error while getting blocks timer data.");
			e1.printStackTrace();
		}
		return true;
	}

	public boolean loadGroup()
	{
		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "SELECT * FROM " + DatabaseManager.table.GROUP;
			ps = bdd.prepareStatement(req);
			resultat = ps.executeQuery();

			while(resultat.next())
			{
				if(!groups.containsKey(resultat.getString("name")))
				{
					groupsId.put(resultat.getString("name").toLowerCase(), resultat.getInt("id"));
				}
			}

			ps.close();
			resultat.close();
		}
		catch(SQLException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKbridge.PNAME + " Error while getting players items.");
			e.printStackTrace();
		}
		return true;
	}

	public boolean loadWorldsInGroup()
	{
		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "SELECT g.name AS group_name, gl.* FROM " + DatabaseManager.table.GROUP + " g LEFT JOIN " + DatabaseManager.table.GROUP_LINK
					+ " gl ON g.id = gl.group_id WHERE gl.server_id = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, SERVERID);
			resultat = ps.executeQuery();

			while(resultat.next())
			{
				String worldName = (String) getKeyFromValue(worldsId, resultat.getInt("world_id"));
				Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKbridge.PNAME + "[DEBUG] " + worldsId.toString());
				Bukkit.getConsoleSender().sendMessage(
						ChatColor.DARK_RED + NKbridge.PNAME + "[DEBUG] worldName=" + worldName + " | group_name=" + resultat.getString("group_name")
								+ " | worldId=" + resultat.getInt("world_id"));
				if(!groups.containsKey(worldName))
				{
					groups.put(worldName, resultat.getString("group_name").toLowerCase());
				}
			}

			ps.close();
			resultat.close();
		}
		catch(SQLException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKbridge.PNAME + " Error while getting players items.");
			e.printStackTrace();
		}
		return true;
	}

	public void unloadGroup()
	{
		groups.clear();
	}

	public void createGroup(String groupName)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		ResultSet resultat = null;
		String req = "INSERT INTO " + DatabaseManager.table.GROUP + " ( name )" + " VALUES ( ? )";
		try
		{
			bdd = DatabaseManager.getConnection();
			ps = bdd.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, groupName.toLowerCase());
			ps.executeUpdate();
			resultat = ps.getGeneratedKeys();

			if(resultat.next())
			{
				groupsId.put(groupName.toLowerCase(), resultat.getInt(1));
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void deleteGroup(String groupName)
	{
		groups.values().removeIf(entries -> entries.equals(groupName));
		groupsId.remove(groupName.toLowerCase());

		Connection bdd = null;
		PreparedStatement ps = null;
		String req = "DELETE FROM " + DatabaseManager.table.GROUP + " WHERE name = ?";
		try
		{
			bdd = DatabaseManager.getConnection();
			ps = bdd.prepareStatement(req);
			ps.setString(1, groupName);

			ps.execute();
			ps.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void addToGroup(String groupName, String worldName)
	{
		groups.put(worldName.toLowerCase(), groupName.toLowerCase());

		Connection bdd = null;
		PreparedStatement ps = null;
		String req = "INSERT INTO " + DatabaseManager.table.GROUP_LINK + " ( group_id, server_id, world_id )" + " VALUES ( ? , ? , ? )";
		try
		{
			bdd = DatabaseManager.getConnection();
			ps = bdd.prepareStatement(req);
			ps.setInt(1, groupsId.get(groupName.toLowerCase()));
			ps.setInt(2, SERVERID);
			ps.setInt(3, worldsId.get(worldName.toLowerCase()));

			ps.executeUpdate();
			ps.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void removeFromGroup(String groupName, String worldName)
	{
		groups.remove(worldName.toLowerCase());

		Connection bdd = null;
		PreparedStatement ps = null;
		String req = "DELETE FROM " + DatabaseManager.table.GROUP_LINK + " WHERE group_id = ? AND server_id = ? AND world_id = ?";
		try
		{
			bdd = DatabaseManager.getConnection();
			ps = bdd.prepareStatement(req);
			ps.setInt(1, groupsId.get(groupName.toLowerCase()));
			ps.setInt(2, SERVERID);
			ps.setInt(3, worldsId.get(worldName.toLowerCase()));

			ps.executeUpdate();
			ps.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static Object getKeyFromValue(Map m, Object value)
	{
		for(Object o : m.keySet())
		{
			if(value.equals(m.get(o)))
			{
				return o;
			}
		}

		return null;
	}

	public List<String> getGroupList()
	{
		return new ArrayList<>(groupsId.keySet());
	}

	public List<String> getWorldList()
	{
		return new ArrayList<>(worldsId.keySet());
	}

	public boolean existGroup(String groupName)
	{
		return groupsId.containsKey(groupName);
	}

	public boolean isWorldInGroup(String groupName, String worldName)
	{
		if(groups.containsKey(worldName.toLowerCase()))
		{
			return groups.get(worldName.toLowerCase()).equals(groupName.toLowerCase());
		}
		return false;
	}

	public List<String> getGroup(String groupName)
	{
		List<String> worlds = new ArrayList<>();

		for(Map.Entry<String, String> entry : groups.entrySet())
		{
			if(entry.getValue().equals(groupName.toLowerCase()))
			{
				worlds.add(entry.getKey());
			}
		}
		return worlds;
	}

	public int getGroupId(String worldName)
	{
		if(groups.containsKey(worldName.toLowerCase()))
		{
			if(groupsId.containsKey(groups.get(worldName.toLowerCase())))
			{
				return groupsId.get(groups.get(worldName.toLowerCase()));
			}
		}
		return -1;
	}

	public boolean isSameGroup(String worldName1, String worldName2)
	{
		if(groups.containsKey(worldName1.toLowerCase()) && groups.containsKey(worldName2.toLowerCase()))
		{
			return groups.get(worldName1.toLowerCase()).equalsIgnoreCase(groups.get(worldName2.toLowerCase()));
		}
		if((!groups.containsKey(worldName1.toLowerCase())) && (!groups.containsKey(worldName2.toLowerCase())))
		{
			return true;
		}
		return false;
	}
}
