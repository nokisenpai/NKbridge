package be.noki_senpai.NKbridge.managers;

import be.noki_senpai.NKbridge.NKbridge;
import be.noki_senpai.NKbridge.utils.SQLConnect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

import java.sql.*;

public class DatabaseManager
{
	private static Connection bdd = null;

	private ConsoleCommandSender console = null;
	private ConfigManager configManager = null;

	public DatabaseManager(ConfigManager configManager)
	{
		this.console = Bukkit.getConsoleSender();
		this.configManager = configManager;
	}

	public enum common
	{
		PLAYERS("NK_players"), SERVERS("NK_servers"), WORLDS("NK_worlds");

		private String name = "";

		common(String name)
		{
			this.name = name;
		}

		public String toString()
		{
			return name;
		}

		public static int size()
		{
			return common.values().length;
		}
	}

	public enum table
	{
		INV(ConfigManager.PREFIX + "inv"),
		GROUP(ConfigManager.PREFIX + "group"),
		GROUP_LINK(ConfigManager.PREFIX + "group_link");
		//PLAYER(ConfigManager.PREFIX + "player");

		private String name = "";

		table(String name)
		{
			this.name = name;
		}

		public String toString()
		{
			return name;
		}

		public static int size()
		{
			return table.values().length;
		}
	}

	public boolean loadDatabase()
	{
		// Setting database informations
		SQLConnect.setInfo(configManager.getDbHost(), configManager.getDbPort(), configManager.getDbName(), configManager.getDbUser(), configManager.getDbPassword());

		// Try to connect to database
		try
		{
			bdd = SQLConnect.getHikariDS().getConnection();
		}
		catch(SQLException e)
		{
			bdd = null;
			console.sendMessage(
					ChatColor.DARK_RED + NKbridge.PNAME + " Error while attempting database connexion. Verify your access informations in config.yml");
			e.printStackTrace();
			return false;
		}

		try
		{
			// Check if tables already exist on database
			if(!existTables())
			{
				// Create database structure if not exist
				createTable();
			}

		}
		catch(SQLException e)
		{
			console.sendMessage(ChatColor.DARK_RED + NKbridge.PNAME + " Error while creating database structure. (Error#A.2.002)");
			return false;
		}

		return true;
	}

	public void unloadDatabase()
	{
		if(bdd != null)
		{
			try
			{
				bdd.close();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	private boolean existTables() throws SQLException
	{
		// Select all tables beginning with the prefix
		String req = "SHOW TABLES FROM " + configManager.getDbName() + " LIKE '" + ConfigManager.PREFIX + "%'";
		ResultSet resultat = null;
		PreparedStatement ps = null;

		try
		{
			ps = bdd.prepareStatement(req);
			resultat = ps.executeQuery();
			int count = 0;
			while(resultat.next())
			{
				count++;
			}

			// if all tables are missing
			if(count == 0)
			{
				console.sendMessage(ChatColor.GREEN + NKbridge.PNAME + " Missing table(s). First start.");
				return false;
			}
			resultat.close();
			ps.close();

			// if 1 or more tables are missing
			if(count < table.size())
			{
				console.sendMessage(ChatColor.DARK_RED + NKbridge.PNAME
						+ " Missing table(s). Please don't alter tables name or structure in database. (Error#main.Storage.002)");
				return false;
			}
		}
		catch(SQLException e1)
		{
			console.sendMessage(ChatColor.DARK_RED + NKbridge.PNAME + " Error while testing existance of tables. (Error#main.Storage.003)");
		}
		finally
		{
			if(ps != null)
			{
				ps.close();
			}
			if(resultat != null)
			{
				resultat.close();
			}
		}

		return true;
	}

	private void createTable() throws SQLException
	{
		try
		{
			bdd = getConnection();

			String req = null;
			Statement s = null;

			console.sendMessage(ChatColor.GREEN + NKbridge.PNAME + " Creating Database structure ...");

			try
			{
				// Creating group table
				req = "CREATE TABLE IF NOT EXISTS `" + table.GROUP + "` (" + ""
						+ "`id` int(11) NOT NULL AUTO_INCREMENT,"
						+ "`name` varchar(100) NOT NULL,"
						+ "PRIMARY KEY (`id`),"
						+ "UNIQUE INDEX `name` (`name`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				s = bdd.createStatement();
				s.execute(req);
				s.close();

				// Creating group_link table
				req = "CREATE TABLE IF NOT EXISTS `" + table.GROUP_LINK + "` (" + ""
						+ "`id` int(11) NOT NULL AUTO_INCREMENT,"
						+ "`group_id` INT NOT NULL,"
						+ "`server_id` INT NOT NULL,"
						+ "`world_id` INT NOT NULL,"
						+ "PRIMARY KEY (`id`),"
						+ "UNIQUE INDEX `group_server_world` (`group_id` , `server_id` , `world_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				s = bdd.createStatement();
				s.execute(req);
				s.close();

				// Creating group_link table
				req = "CREATE TABLE IF NOT EXISTS `" + table.INV + "` (" + ""
						+ "`id` int(11) NOT NULL AUTO_INCREMENT,"
						+ "`group_id` INT NOT NULL,"
						+ "`player_id` INT NOT NULL,"
						+ "`type` varchar(100) NOT NULL,"
						+ "`inventory` TEXT NOT NULL,"
						+ "`enderchest` TEXT NOT NULL,"
						+ "`health` DOUBLE NOT NULL,"
						+ "`food` DOUBLE NOT NULL,"
						+ "`food_saturation` DOUBLE NOT NULL,"
						+ "`experience` DOUBLE NOT NULL,"
						+ "PRIMARY KEY (`id`),"
						+ "UNIQUE INDEX `group_player_type` (`group_id` , `player_id` , `type`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				s = bdd.createStatement();
				s.execute(req);
				s.close();

				console.sendMessage(ChatColor.GREEN + NKbridge.PNAME + " Database structure created.");
			}
			catch(SQLException e)
			{
				console.sendMessage(ChatColor.DARK_RED + NKbridge.PNAME + " Error while creating database structure. (Error#main.Storage.000)");
				e.printStackTrace();
			}
			finally
			{
				if(s != null)
				{
					s.close();
				}
			}
		}
		catch(SQLException e)
		{
			console.sendMessage(ChatColor.DARK_RED + NKbridge.PNAME + " Error while creating database structure. (Error#main.Storage.001)");
		}
	}

	// Getter 'bdd'
	public static Connection getConnection()
	{
		try
		{
			if(!bdd.isValid(1))
			{
				if(!bdd.isClosed())
				{
					bdd.close();
				}
				bdd = SQLConnect.getHikariDS().getConnection();
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return bdd;
	}
}
