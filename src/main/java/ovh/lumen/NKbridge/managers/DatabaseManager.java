package ovh.lumen.NKbridge.managers;

import ovh.lumen.NKbridge.data.NKData;
import ovh.lumen.NKbridge.enums.InternalMessages;
import ovh.lumen.NKbridge.exceptions.SetupException;
import ovh.lumen.NKbridge.utils.NKLogger;
import ovh.lumen.NKbridge.utils.SQLConnect;

import java.sql.*;

public final class DatabaseManager
{
	private static Connection bdd = null;

	private DatabaseManager() {}

	public enum Tables
	{
		INV("inv"),
		GROUP("group"),
		GROUP_LINK("group_link"),
		ADVANCEMENT("advancement"),
		STATISTIC("statistic");

		private final String name;

		Tables(String name)
		{
			this.name = name;
		}

		public String toString()
		{
			return NKData.PREFIX + name;
		}
	}

	public static void load() throws SetupException
	{
		SQLConnect.setInfo(NKData.DBACCESS);

		try
		{
			connect();
		}
		catch(SQLException e)
		{
			throw new SetupException(InternalMessages.DATABASE_CANT_CONNECT.toString());
		}

		try
		{
			if(!checkTables())
			{
				createTable();
			}
		}
		catch(SQLException e)
		{
			throw new SetupException(InternalMessages.DATABASE_CANT_CREATE_TABLES.toString());
		}
	}

	public static void unload()
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

	private static void connect() throws SQLException
	{
		bdd = SQLConnect.getHikariDS().getConnection();
	}

	private static void createTable() throws SQLException
	{
		try(Statement s = bdd.createStatement())
		{
			String req = "CREATE TABLE IF NOT EXISTS `" + Tables.GROUP + "` (" + ""
					+ "`id` int NOT NULL AUTO_INCREMENT,"
					+ "`name` varchar(100) NOT NULL,"
					+ "PRIMARY KEY (`id`),"
					+ "UNIQUE INDEX `name` (`name`)) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;";
			s.execute(req);

			req = "CREATE TABLE IF NOT EXISTS `" + Tables.GROUP_LINK + "` (" + ""
					+ "`id` int NOT NULL AUTO_INCREMENT,"
					+ "`group_id` int NOT NULL,"
					+ "`server_id` int NOT NULL,"
					+ "`world_id` int NOT NULL,"
					+ "PRIMARY KEY (`id`),"
					+ "UNIQUE INDEX `group_server_world` (`group_id` , `server_id` , `world_id`)) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;";
			s.execute(req);

			req = "CREATE TABLE IF NOT EXISTS `" + Tables.INV + "` (" + ""
					+ "`id` int NOT NULL AUTO_INCREMENT,"
					+ "`group_id` int NOT NULL,"
					+ "`player_uuid` varchar(40) NOT NULL,"
					+ "`type` varchar(100) NOT NULL,"
					+ "`inventory` text NOT NULL,"
					+ "`enderchest` text NOT NULL,"
					+ "`health` double NOT NULL,"
					+ "`food` double NOT NULL,"
					+ "`food_saturation` double NOT NULL,"
					+ "`experience` double NOT NULL,"
					+ "PRIMARY KEY (`id`),"
					+ "UNIQUE INDEX `group_player_type` (`group_id` , `player_uuid` , `type`)) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;";
			s.execute(req);

			req = "CREATE TABLE IF NOT EXISTS `" + Tables.ADVANCEMENT + "` (" + ""
					+ "`group_id` int NOT NULL,"
					+ "`player_uuid` varchar(40) NOT NULL,"
					+ "`advancement` varchar(150) NOT NULL,"
					+ "`criteria` varchar(150) NOT NULL,"
					+ "`done` boolean NOT NULL,"
					+ "UNIQUE INDEX `group_player_adv_crit` (`group_id` , `player_uuid` , `advancement`, `criteria`)) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;";
			s.execute(req);

			req = "CREATE TABLE IF NOT EXISTS `" + Tables.STATISTIC + "` (" + ""
					+ "`group_id` int NOT NULL,"
					+ "`player_uuid` varchar(40) NOT NULL,"
					+ "`parent` varchar(150) NOT NULL,"
					+ "`statistic` varchar(150) NOT NULL,"
					+ "`amount` bigint NOT NULL,"
					+ "UNIQUE INDEX `group_player_par_stat` (`group_id` , `player_uuid` , `parent`, `statistic`)) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;";
			s.execute(req);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

		NKLogger.log(InternalMessages.DATABASE_CREATE_TABLES_SUCCESS.toString());
	}

	private static boolean checkTables()
	{
		String req = "SHOW TABLES FROM " + NKData.DBACCESS.getDbName() + " LIKE '" + NKData.PREFIX + "%'";

		try(PreparedStatement ps = bdd.prepareStatement(req); ResultSet result = ps.executeQuery())
		{
			int count = 0;

			while(result.next())
			{
				count++;
			}

			result.close();
			ps.close();

			if(count < Tables.values().length)
			{
				return false;
			}
		}
		catch(SQLException e)
		{
			NKLogger.error(InternalMessages.DATABASE_CANT_CHECK_TABLES.toString());

			return false;
		}

		return true;
	}

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

				connect();
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

		return bdd;
	}
}
