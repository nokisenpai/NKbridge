package be.noki_senpai.NKbridge.managers;

import be.noki_senpai.NKbridge.NKbridge;
import be.noki_senpai.NKbridge.data.NKPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class PlayerManager
{
	// Players datas
	private Map<String, NKPlayer> players = null;
	private ConsoleCommandSender console = null;

	public PlayerManager()
	{
		this.players = new TreeMap<String, NKPlayer>(String.CASE_INSENSITIVE_ORDER);
		this.console = Bukkit.getConsoleSender();
	}

	public void loadPlayer()
	{
		// Get all connected players
		Bukkit.getOnlinePlayers().forEach(player -> players.put(player.getDisplayName(), new NKPlayer(player.getUniqueId())));
	}

	public void unloadPlayer()
	{
		players.clear();
	}

	// ######################################
	// Getters & Setters
	// ######################################

	// getPlayer
	public NKPlayer getPlayer(String playerName)
	{
		if(players.containsKey(playerName))
		{
			return players.get(playerName);
		}
		else
		{
			Connection bdd = null;
			ResultSet resultat = null;
			PreparedStatement ps = null;
			String req = null;

			try
			{
				bdd = DatabaseManager.getConnection();

				req = "SELECT id, name, uuid FROM " + DatabaseManager.common.PLAYERS + " WHERE name = ?";
				ps = bdd.prepareStatement(req);
				ps.setString(1, playerName);
				resultat = ps.executeQuery();

				if(resultat.next())
				{
					return new NKPlayer(UUID.fromString(resultat.getString("uuid")));
				}
			}
			catch(SQLException e1)
			{
				Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKbridge.PNAME + " Error while getting a player.");
			}
		}
		return null;
	}

	public void addPlayer(Player player)
	{
		players.put(player.getName(), new NKPlayer(player.getUniqueId()));
	}

	public void delPlayer(String playerName)
	{
		players.remove(playerName);
	}
}
