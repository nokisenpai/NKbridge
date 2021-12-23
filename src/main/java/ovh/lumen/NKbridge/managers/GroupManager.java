package ovh.lumen.NKbridge.managers;

import org.bukkit.Bukkit;
import ovh.lumen.NKbridge.data.NKData;
import ovh.lumen.NKbridge.enums.InternalMessages;
import ovh.lumen.NKbridge.exceptions.NKException;
import ovh.lumen.NKbridge.exceptions.SetupException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class GroupManager
{
	private GroupManager() {}

	public static void load() throws SetupException
	{
		loadGroup();
		loadWorldsInGroup();
	}

	private static void loadGroup() throws SetupException
	{
		try
		{
			Connection bdd = DatabaseManager.getConnection();
			String req = "SELECT * FROM " + DatabaseManager.Tables.GROUP;
			PreparedStatement ps = bdd.prepareStatement(req);
			ResultSet result = ps.executeQuery();

			while(result.next())
			{
				NKData.GROUPS_ID.putIfAbsent(result.getString("name").toLowerCase(), result.getInt("id"));
			}

			ps.close();
			result.close();
		}
		catch(SQLException e)
		{
			throw new SetupException(InternalMessages.GROUP_LOAD_ERROR.toString());
		}
	}

	private static void loadWorldsInGroup() throws SetupException
	{
		try
		{
			Connection bdd = DatabaseManager.getConnection();
			String req = "SELECT g.name AS group_name, gl.* FROM " + DatabaseManager.Tables.GROUP + " g LEFT JOIN " + DatabaseManager.Tables.GROUP_LINK
					+ " gl ON g.id = gl.group_id WHERE gl.server_id = ?";
			PreparedStatement ps = bdd.prepareStatement(req);
			ps.setInt(1, NKData.SERVER_INFO.getId());
			ResultSet result = ps.executeQuery();

			while(result.next())
			{
				String worldName = (String) getKeyFromValue(NKData.WORLDS_ID, result.getInt("world_id"));
				NKData.WORLDS_GROUP.putIfAbsent(worldName, result.getString("group_name").toLowerCase());
			}

			Bukkit.getServer().getWorlds().forEach(world -> NKData.WORLDS_GROUP.putIfAbsent(world.getName(), null));

			ps.close();
			result.close();
		}
		catch(SQLException e)
		{
			throw new SetupException(InternalMessages.WORLDS_IN_GROUP_LOAD_ERROR.toString());
		}
	}

	public static void unload()
	{
		NKData.WORLDS_GROUP.clear();
		NKData.GROUPS_ID.clear();
		NKData.WORLDS_ID.clear();
	}

	public static void createGroup(String groupName) throws SetupException
	{
		try
		{
			Connection bdd = DatabaseManager.getConnection();
			String req = "INSERT INTO " + DatabaseManager.Tables.GROUP + " ( name )" + " VALUES ( ? )";
			PreparedStatement ps = bdd.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, groupName.toLowerCase());
			ps.executeUpdate();
			ResultSet result = ps.getGeneratedKeys();

			if(result.next())
			{
				NKData.GROUPS_ID.put(groupName.toLowerCase(), result.getInt(1));
			}
		}
		catch(SQLException e)
		{
			throw new SetupException(InternalMessages.INSERT_GROUP_ERROR.toString());
		}
	}

	public static void deleteGroup(String groupName) throws SetupException
	{
		NKData.WORLDS_GROUP.values().removeIf(entries -> entries.equals(groupName));
		NKData.GROUPS_ID.remove(groupName.toLowerCase());

		try
		{
			Connection bdd = DatabaseManager.getConnection();
			String req = "DELETE FROM " + DatabaseManager.Tables.GROUP + " WHERE name = ?";
			PreparedStatement ps = bdd.prepareStatement(req);
			ps.setString(1, groupName);

			ps.execute();
			ps.close();
		}
		catch(SQLException e)
		{
			throw new SetupException(InternalMessages.DELETE_GROUP_ERROR.toString());
		}
	}

	public static void addToGroup(String groupName, String worldName) throws NKException
	{
		NKData.WORLDS_GROUP.put(worldName.toLowerCase(), groupName.toLowerCase());

		try
		{
			Connection bdd = DatabaseManager.getConnection();
			String req = "INSERT INTO " + DatabaseManager.Tables.GROUP_LINK + " ( group_id, server_id, world_id )" + " VALUES ( ? , ? , ? )";
			PreparedStatement ps = bdd.prepareStatement(req);
			ps.setInt(1, NKData.GROUPS_ID.get(groupName.toLowerCase()));
			ps.setInt(2, NKData.SERVER_INFO.getId());
			ps.setInt(3, NKData.WORLDS_ID.get(worldName.toLowerCase()));

			ps.executeUpdate();
			ps.close();
		}
		catch(SQLException e)
		{
			throw new NKException(InternalMessages.INSERT_WORLD_IN_GROUP_ERROR.toString());
		}
	}

	public static void removeFromGroup(String groupName, String worldName) throws NKException
	{
		NKData.WORLDS_GROUP.remove(worldName.toLowerCase());

		try
		{
			Connection bdd = DatabaseManager.getConnection();
			String req = "DELETE FROM " + DatabaseManager.Tables.GROUP_LINK + " WHERE group_id = ? AND server_id = ? AND world_id = ?";
			PreparedStatement ps = bdd.prepareStatement(req);
			ps.setInt(1, NKData.GROUPS_ID.get(groupName.toLowerCase()));
			ps.setInt(2, NKData.SERVER_INFO.getId());
			ps.setInt(3, NKData.WORLDS_ID.get(worldName.toLowerCase()));

			ps.executeUpdate();
			ps.close();
		}
		catch(SQLException e)
		{
			throw new NKException(InternalMessages.DELETE_WORLD_FROM_GROUP_ERROR.toString()); //TODO
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

	public static boolean existGroup(String groupName)
	{
		return NKData.GROUPS_ID.containsKey(groupName);
	}

	public static boolean isWorldInGroup(String groupName, String worldName)
	{
		if(NKData.WORLDS_GROUP.containsKey(worldName.toLowerCase()))
		{
			return NKData.WORLDS_GROUP.get(worldName.toLowerCase()).equals(groupName.toLowerCase());
		}

		return false;
	}

	public static List<String> getGroup(String groupName)
	{
		List<String> worlds = new ArrayList<>();

		for(Map.Entry<String, String> entry : NKData.WORLDS_GROUP.entrySet())
		{
			if(entry.getValue().equals(groupName.toLowerCase()))
			{
				worlds.add(entry.getKey());
			}
		}

		return worlds;
	}

	public static int getGroupId(String worldName)
	{
		if(NKData.WORLDS_GROUP.containsKey(worldName.toLowerCase()))
		{
			if(NKData.GROUPS_ID.containsKey(NKData.WORLDS_GROUP.get(worldName.toLowerCase())))
			{
				return NKData.GROUPS_ID.get(NKData.WORLDS_GROUP.get(worldName.toLowerCase()));
			}
		}

		return -1;
	}

	public static boolean isSameGroup(String worldName1, String worldName2)
	{
		if(NKData.WORLDS_GROUP.containsKey(worldName1.toLowerCase()) && NKData.WORLDS_GROUP.containsKey(worldName2.toLowerCase()))
		{
			return NKData.WORLDS_GROUP.get(worldName1.toLowerCase()).equalsIgnoreCase(NKData.WORLDS_GROUP.get(worldName2.toLowerCase()));
		}

		return (!NKData.WORLDS_GROUP.containsKey(worldName1.toLowerCase())) && (!NKData.WORLDS_GROUP.containsKey(worldName2.toLowerCase()));
	}

	public static List<String> getGroupList()
	{
		return new ArrayList<>(NKData.GROUPS_ID.keySet());
	}

	public static List<String> getWorldList()
	{
		return new ArrayList<>(NKData.WORLDS_ID.keySet());
	}

	public static String getGroupName(String worldName)
	{
		if(NKData.WORLDS_GROUP.containsKey(worldName))
		{
			return NKData.WORLDS_GROUP.get(worldName);
		}
		return null;
	}

	public static List<String> getWorldsInGroup(String groupName)
	{
		List<String> worlds = new ArrayList<>();

		NKData.WORLDS_GROUP.forEach((s, s2) ->
		{
			if(s2.equals(groupName))
			{
				worlds.add(s);
			}
		});

		return worlds;
	}
}
