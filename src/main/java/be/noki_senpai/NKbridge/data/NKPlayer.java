package be.noki_senpai.NKbridge.data;

import be.noki_senpai.NKbridge.NKbridge;
import be.noki_senpai.NKbridge.managers.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class NKPlayer
{
	private int id;
	private UUID playerUUID;
	private String playerName;
	private boolean canSave = false;

	public NKPlayer(UUID UUID)
	{
		setPlayerUUID(UUID);
		setPlayerName(Bukkit.getOfflinePlayer(playerUUID).getName());

		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;
		Integer homeTp = null;
		try
		{
			bdd = DatabaseManager.getConnection();

			// Get 'id', 'uuid', 'name', 'amount' and 'home_tp' from database
			req = "SELECT id, name FROM " + DatabaseManager.common.PLAYERS + " WHERE uuid = ?";
			ps = bdd.prepareStatement(req);
			ps.setString(1, getPlayerUUID().toString());

			resultat = ps.executeQuery();

			// If there is a result account exist
			if(resultat.next())
			{
				setId(resultat.getInt("id"));
			}
			else
			{
				Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKbridge.PNAME + " Error while setting a player. (#1)");
			}
			ps.close();
			resultat.close();
		}
		catch(SQLException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKbridge.PNAME + " Error while getting a player. (Error#data.Players.000)");
			e.printStackTrace();
		}
	}

	//######################################
	// Getters & Setters
	//######################################

	// Getter & Setter 'id'
	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	// Getter & Setter 'playerUUID'
	public UUID getPlayerUUID()
	{
		return playerUUID;
	}

	public void setPlayerUUID(UUID playerUUID)
	{
		this.playerUUID = playerUUID;
	}

	// Getter & Setter 'playerName'
	public String getPlayerName()
	{
		return playerName;
	}

	public void setPlayerName(String playerName)
	{
		this.playerName = playerName;
	}

	public boolean canSave()
	{
		return canSave;
	}

	public void setCanSave(boolean canSave)
	{
		this.canSave = canSave;
	}
}
