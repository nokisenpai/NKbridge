package ovh.lumen.NKbridge.managers;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import ovh.lumen.NKbridge.data.Criteria;
import ovh.lumen.NKbridge.data.NKData;
import ovh.lumen.NKbridge.data.Statistik;
import ovh.lumen.NKbridge.enums.InternalMessages;
import ovh.lumen.NKbridge.enums.StatisticType;
import ovh.lumen.NKbridge.exceptions.NKException;
import ovh.lumen.NKbridge.utils.NKLogger;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public final class PlayerDataManager
{
	private static int saveDataTaskId1 = -1;
	private static int saveDataTaskId2 = -1;

	private PlayerDataManager() {}

	public static void load() throws NKException
	{
		loadPlayersData();

		saveDataTaskId1 = new BukkitRunnable()
		{
			@Override
			public void run()
			{
				try
				{
					savePlayersData();
				}
				catch(NKException e)
				{
					NKLogger.error(e.getMessage());
				}
			}
		}.runTaskTimerAsynchronously((Plugin) NKData.PLUGIN, NKData.AUTO_SAVE_INVENTORIES * 20L, NKData.AUTO_SAVE_INVENTORIES * 20L).getTaskId();

		saveDataTaskId2 = new BukkitRunnable()
		{
			@Override
			public void run()
			{
				try
				{
					savePlayersAdvancements();
					savePlayersStatistics();
				}
				catch(NKException e)
				{
					NKLogger.error(e.getMessage());
				}
			}
		}.runTaskTimerAsynchronously((Plugin) NKData.PLUGIN, NKData.AUTO_SAVE_ADVANCEMENTS * 20L, NKData.AUTO_SAVE_ADVANCEMENTS * 20L).getTaskId();
	}

	public static void unload()
	{
		Bukkit.getScheduler().cancelTask(saveDataTaskId1);
		Bukkit.getScheduler().cancelTask(saveDataTaskId2);

		try
		{
			savePlayersData();
			savePlayersAdvancements();
			savePlayersStatistics();
		}
		catch(NKException e)
		{
			NKLogger.error(e.getMessage());
		}
	}

	public static void loadPlayerData(Player player) throws NKException
	{
		int groupId = GroupManager.getGroupId(player.getWorld().getName().toLowerCase());

		try
		{
			Connection bdd = DatabaseManager.getConnection();
			String req = "SELECT * FROM " + DatabaseManager.Tables.INV + " WHERE group_id = ? AND player_uuid = ? AND type = ?";
			PreparedStatement ps = bdd.prepareStatement(req);
			ps.setInt(1, groupId);
			ps.setString(2, player.getUniqueId().toString());
			ps.setString(3, player.getGameMode().name());
			ResultSet result = ps.executeQuery();

			NKData.CAN_SAVE.remove(player.getUniqueId().toString());

			if(result.next())
			{
				YamlConfiguration restoreInv = new YamlConfiguration();

				restoreInv.loadFromString(result.getString("inventory"));
				ArrayList invData = (ArrayList) restoreInv.get("inventory", ItemStack[].class);
				player.getInventory().setContents((ItemStack[]) invData.toArray(new ItemStack[invData.size()]));

				restoreInv.loadFromString(result.getString("enderchest"));
				ArrayList ecData = (ArrayList) restoreInv.get("enderchest", ItemStack[].class);
				player.getEnderChest().setContents((ItemStack[]) ecData.toArray(new ItemStack[ecData.size()]));

				player.setHealth(result.getDouble("health"));
				player.setSaturation((float) result.getDouble("food_saturation"));
				player.setFoodLevel(result.getInt("food"));
				setTotalExperience(player, result.getInt("experience"));

				loadAdvancements(player, groupId);
				loadStatistics(player, groupId);
			}
			else
			{
				ps.close();

				YamlConfiguration invData = new YamlConfiguration();
				player.getInventory().clear();
				invData.set("inventory", player.getInventory().getContents());

				player.getEnderChest().clear();
				YamlConfiguration ecData = new YamlConfiguration();
				ecData.set("enderchest", player.getEnderChest().getContents());

				player.setHealth(20);
				player.setFoodLevel(20);
				player.setSaturation(5);
				player.setLevel(0);
				player.setExp(0);

				req = "INSERT INTO " + DatabaseManager.Tables.INV
						+ " ( group_id, player_uuid , type, inventory, enderchest, health, food, food_saturation, experience ) VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? )";
				ps = bdd.prepareStatement(req);
				ps.setInt(1, groupId);
				ps.setString(2, player.getUniqueId().toString());
				ps.setString(3, player.getGameMode().name());
				ps.setString(4, invData.saveToString());
				ps.setString(5, ecData.saveToString());
				ps.setDouble(6, player.getHealth());
				ps.setDouble(7, player.getFoodLevel());
				ps.setDouble(8, player.getSaturation());
				ps.setDouble(9, getTotalExperience(player));

				storeAdvancements(player, groupId);
				resetStatistics(player);
			}

			ps.close();
			result.close();

			NKData.CAN_SAVE.add(player.getUniqueId().toString());
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(ChatColor.GREEN + "Loaded").create());
		}
		catch(SQLException | InvalidConfigurationException e)
		{
			NKData.CAN_SAVE.remove(player.getUniqueId().toString());

			throw new NKException(InternalMessages.PLAYER_DATA_LOAD_ERROR.toString());
		}
	}

	public static void savePlayerData(Player player, String gameMode, String worldName) throws NKException
	{
		if(NKData.CAN_SAVE.contains(player.getUniqueId().toString()))
		{
			int groupId = GroupManager.getGroupId(worldName.toLowerCase());

			try
			{
				Connection bdd = DatabaseManager.getConnection();

				YamlConfiguration invData = new YamlConfiguration();
				invData.set("inventory", player.getInventory().getContents());

				YamlConfiguration ecData = new YamlConfiguration();
				ecData.set("enderchest", player.getEnderChest().getContents());

				String req = "UPDATE " + DatabaseManager.Tables.INV
						+ " SET inventory = ? , enderchest = ? , health = ? , food = ? , food_saturation = ? , experience = ? WHERE group_id = ? AND player_uuid = ? AND type = ?";
				PreparedStatement ps = bdd.prepareStatement(req);

				ps.setString(1, invData.saveToString());
				ps.setString(2, ecData.saveToString());
				ps.setDouble(3, player.getHealth());
				ps.setDouble(4, player.getFoodLevel());
				ps.setDouble(5, player.getSaturation());
				ps.setDouble(6, getTotalExperience(player));
				ps.setInt(7, groupId);
				ps.setString(8, player.getUniqueId().toString());
				ps.setString(9, gameMode);

				ps.executeUpdate();
				ps.close();
			}
			catch(SQLException e)
			{
				throw new NKException(InternalMessages.PLAYER_DATA_SAVE_ERROR.toString());
			}
		}
	}

	public static void savePlayerData(Player player, World world) throws NKException
	{
		savePlayerData(player, player.getGameMode().name(), world.getName());
	}

	public static void savePlayerData(Player player, String gameMode) throws NKException
	{
		savePlayerData(player, gameMode, player.getWorld().getName());
	}

	public static void savePlayerData(Player player) throws NKException
	{
		savePlayerData(player, player.getGameMode().name(), player.getWorld().getName());
	}

	public static void loadPlayersData() throws NKException
	{
		for(Player player : Bukkit.getOnlinePlayers())
		{
			loadPlayerData(player);
		}
	}

	public static void savePlayersData() throws NKException
	{
		for(Player player : Bukkit.getOnlinePlayers())
		{
			savePlayerData(player);
		}
	}

	public static void loadPlayersData(String worldName) throws NKException
	{
		for(Player player : Bukkit.getWorld(worldName).getPlayers())
		{
			loadPlayerData(player);
		}
	}

	public static void savePlayersData(String worldName) throws NKException
	{
		for(Player player : Bukkit.getWorld(worldName).getPlayers())
		{
			savePlayerData(player);
		}
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
		int level;
		int xp;
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

	private static void loadAdvancements(Player player, int groupId) throws NKException
	{
		if(player.getGameMode().name().equalsIgnoreCase("SURVIVAL"))
		{
			try
			{
				Connection bdd = DatabaseManager.getConnection();
				String req = "SELECT * FROM " + DatabaseManager.Tables.ADVANCEMENT + " WHERE group_id = ? AND player_uuid = ?";
				PreparedStatement ps = bdd.prepareStatement(req);
				ps.setInt(1, groupId);
				ps.setString(2, player.getUniqueId().toString());
				ResultSet result = ps.executeQuery();

				while(result.next())
				{
					Advancement a = Bukkit.getServer().getAdvancement(NamespacedKey.minecraft(result.getString("advancement")));
					AdvancementProgress progress = player.getAdvancementProgress(a);

					if(result.getBoolean("done"))
					{
						progress.awardCriteria(result.getString("criteria"));
					}
					else
					{
						progress.revokeCriteria(result.getString("criteria"));
					}
				}
			}
			catch(SQLException e)
			{
				throw new NKException(InternalMessages.PLAYER_ADVANCEMENT_LOAD_ERROR.toString());
			}
		}
	}

	private static void storeAdvancements(Player player, int groupId) throws NKException
	{
		if(player.getGameMode().name().equalsIgnoreCase("SURVIVAL"))
		{
			try
			{
				Map<String, List<Criteria>> advancements = new HashMap<>();
				Iterator<Advancement> it = Bukkit.getServer().advancementIterator();
				while(it.hasNext())
				{
					Advancement a = it.next();
					if(!a.getKey().getKey().contains("recipes/"))
					{
						List<Criteria> criterias = new ArrayList<>();

						AdvancementProgress progress = player.getAdvancementProgress(a);

						for(String criteria : progress.getAwardedCriteria())
						{
							progress.revokeCriteria(criteria);
							criterias.add(new Criteria(criteria, false));
						}
						for(String criteria : progress.getRemainingCriteria())
						{
							criterias.add(new Criteria(criteria, false));
						}

						advancements.put(a.getKey().getKey(), criterias);
					}
				}

				Connection bdd = DatabaseManager.getConnection();
				String req = "INSERT INTO " + DatabaseManager.Tables.ADVANCEMENT
						+ " ( group_id, player_uuid , advancement, criteria, done ) VALUES ( ? , ? , ? , ? , ? )";
				PreparedStatement ps = bdd.prepareStatement(req);

				for(Map.Entry<String, List<Criteria>> a : advancements.entrySet())
				{
					for(Criteria c : a.getValue())
					{
						ps.setInt(1, groupId);
						ps.setString(2, player.getUniqueId().toString());
						ps.setString(3, a.getKey());
						ps.setString(4, c.getName());
						ps.setBoolean(5, c.isDone());
						ps.addBatch();
					}
				}

				ps.executeBatch();
				ps.close();
			}
			catch(SQLException e)
			{
				throw new NKException(InternalMessages.PLAYER_ADVANCEMENT_STORE_ERROR.toString());
			}
		}
	}

	public static void savePlayersAdvancements() throws NKException
	{
		for(Player player : Bukkit.getOnlinePlayers())
		{
			saveAdvancements(player);
		}
	}

	public static void saveAdvancements(Player player) throws NKException
	{
		if(NKData.CAN_SAVE.contains(player.getUniqueId().toString()))
		{
			int groupId = GroupManager.getGroupId(player.getWorld().getName().toLowerCase());

			if(player.getGameMode().name().equalsIgnoreCase("SURVIVAL"))
			{
				try
				{
					Map<String, List<Criteria>> advancements = new HashMap<>();
					Iterator<Advancement> it = Bukkit.getServer().advancementIterator();
					while(it.hasNext())
					{
						Advancement a = it.next();
						if(!a.getKey().getKey().contains("recipes/"))
						{
							List<Criteria> criterias = new ArrayList<>();

							AdvancementProgress progress = player.getAdvancementProgress(a);

							for(String criteria : progress.getAwardedCriteria())
							{
								criterias.add(new Criteria(criteria, true));
							}
							for(String criteria : progress.getRemainingCriteria())
							{
								criterias.add(new Criteria(criteria, false));
							}

							advancements.put(a.getKey().getKey(), criterias);
						}
					}

					Connection bdd = DatabaseManager.getConnection();
					String req = "INSERT INTO " + DatabaseManager.Tables.ADVANCEMENT
							+ " ( group_id, player_uuid , advancement, criteria, done ) VALUES ( ? , ? , ? , ? , ? )"
							+ " ON DUPLICATE KEY UPDATE done=VALUES(done)";
					PreparedStatement ps = bdd.prepareStatement(req);

					for(Map.Entry<String, List<Criteria>> a : advancements.entrySet())
					{
						for(Criteria c : a.getValue())
						{
							ps.setInt(1, groupId);
							ps.setString(2, player.getUniqueId().toString());
							ps.setString(3, a.getKey());
							ps.setString(4, c.getName());
							ps.setBoolean(5, c.isDone());
							ps.addBatch();
						}
					}

					ps.executeBatch();
					ps.close();
				}
				catch(SQLException e)
				{
					throw new NKException(InternalMessages.PLAYER_ADVANCEMENT_SAVE_ERROR.toString());
				}
			}
		}
	}

	private static void loadStatistics(Player player, int groupId) throws NKException
	{
		if(player.getGameMode().name().equalsIgnoreCase("SURVIVAL"))
		{
			try
			{
				resetStatistics(player);

				Map<String, List<Statistik>> statistics = new HashMap<>();

				Connection bdd = DatabaseManager.getConnection();
				String req = "SELECT * FROM " + DatabaseManager.Tables.STATISTIC + " WHERE group_id = ? AND player_uuid = ?";
				PreparedStatement ps = bdd.prepareStatement(req);
				ps.setInt(1, groupId);
				ps.setString(2, player.getUniqueId().toString());
				ResultSet result = ps.executeQuery();

				while(result.next())
				{
					statistics.putIfAbsent(result.getString("parent"), new ArrayList<>());
					statistics.get(result.getString("parent")).add(new Statistik(result.getString("statistic"), result.getInt("amount")));
				}

				statistics.forEach((key, value) ->
				{
					switch(StatisticType.getFromValue(key))
					{
						case CUSTOM -> value.forEach((statistik) -> player.setStatistic(getCustomStatistique(statistik.getName()), statistik.getAmount()));
						case KILLED -> value.forEach((statistik) -> player.setStatistic(Statistic.valueOf("KILL_ENTITY"), getEntityType(statistik.getName()), statistik.getAmount()));
						case KILLED_BY -> value.forEach((statistik) -> player.setStatistic(Statistic.valueOf("ENTITY_KILLED_BY"), getEntityType(statistik.getName()), statistik.getAmount()));
						case USED -> value.forEach((statistik) -> player.setStatistic(Statistic.valueOf("USE_ITEM"), getMaterial(statistik.getName()), statistik.getAmount()));
						case CRAFTED -> value.forEach((statistik) -> player.setStatistic(Statistic.valueOf("CRAFT_ITEM"), getMaterial(statistik.getName()), statistik.getAmount()));
						case BROKEN -> value.forEach((statistik) -> player.setStatistic(Statistic.valueOf("BREAK_ITEM"), getMaterial(statistik.getName()), statistik.getAmount()));
						case MINED -> value.forEach((statistik) -> player.setStatistic(Statistic.valueOf("MINE_BLOCK"), getMaterial(statistik.getName()), statistik.getAmount()));
						case PICKED_UP -> value.forEach((statistik) -> player.setStatistic(Statistic.valueOf("PICKUP"), getMaterial(statistik.getName()), statistik.getAmount()));
						case DROPPED -> value.forEach((statistik) -> player.setStatistic(Statistic.valueOf("DROP"), getMaterial(statistik.getName()), statistik.getAmount()));
						default -> {
						}
					}
				});
			}
			catch(SQLException e)
			{
				throw new NKException(InternalMessages.PLAYER_STATISTIC_LOAD_ERROR.toString());
			}
		}
	}

	public static void resetStatistics(Player player) throws NKException
	{
		JSONParser jsonParser = new JSONParser();

		try(FileReader reader = new FileReader(NKData.DEFAULT_WORLD + "/stats/" + player.getUniqueId() + ".json"))
		{
			Object obj = jsonParser.parse(reader);

			JSONObject data = (JSONObject) obj;
			JSONObject stats = (JSONObject) data.getOrDefault("stats", new JSONObject());

			stats.forEach((key, value) ->
			{
				switch(StatisticType.getFromValue((String) key))
				{
					case CUSTOM -> ((JSONObject) value).forEach((statsName, amount) -> player.setStatistic(getCustomStatistique((String) statsName), 0));
					case KILLED -> ((JSONObject) value).forEach((entity, amount) -> player.setStatistic(Statistic.valueOf("KILL_ENTITY"), getEntityType((String) entity), 0));
					case KILLED_BY -> ((JSONObject) value).forEach((entity, amount) -> player.setStatistic(Statistic.valueOf("ENTITY_KILLED_BY"), getEntityType((String) entity), 0));
					case USED -> ((JSONObject) value).forEach((material, amount) -> player.setStatistic(Statistic.valueOf("USE_ITEM"), getMaterial((String) material), 0));
					case CRAFTED -> ((JSONObject) value).forEach((material, amount) -> player.setStatistic(Statistic.valueOf("CRAFT_ITEM"), getMaterial((String) material), 0));
					case BROKEN -> ((JSONObject) value).forEach((material, amount) -> player.setStatistic(Statistic.valueOf("BREAK_ITEM"), getMaterial((String) material), 0));
					case MINED -> ((JSONObject) value).forEach((material, amount) -> player.setStatistic(Statistic.valueOf("MINE_BLOCK"), getMaterial((String) material), 0));
					case PICKED_UP -> ((JSONObject) value).forEach((material, amount) -> player.setStatistic(Statistic.valueOf("PICKUP"), getMaterial((String) material), 0));
					case DROPPED -> ((JSONObject) value).forEach((material, amount) -> player.setStatistic(Statistic.valueOf("DROP"), getMaterial((String) material), 0));
					default -> {
					}
				}
			});
		}
		catch(Exception e)
		{
			throw new NKException(InternalMessages.PLAYER_STATISTIC_RESET_ERROR.toString());
		}
	}

	public static void savePlayersStatistics() throws NKException
	{
		for(Player player : Bukkit.getOnlinePlayers())
		{
			saveStatistics(player);
		}
	}

	public static void saveStatistics(Player player) throws NKException
	{
		if(NKData.CAN_SAVE.contains(player.getUniqueId().toString()))
		{
			int groupId = GroupManager.getGroupId(player.getWorld().getName().toLowerCase());

			if(player.getGameMode().name().equalsIgnoreCase("SURVIVAL"))
			{
				Map<String, List<Statistik>> statistics = new HashMap<>();

				JSONParser jsonParser = new JSONParser();

				try(FileReader reader = new FileReader(NKData.DEFAULT_WORLD + "/stats/" + player.getUniqueId() + ".json"))
				{
					Object obj = jsonParser.parse(reader);

					JSONObject data = (JSONObject) obj;
					JSONObject stats = (JSONObject) data.getOrDefault("stats", new JSONObject());
					stats.forEach((key, value) ->
					{
						List<Statistik> statistiks = new ArrayList<>();
						switch(StatisticType.getFromValue((String) key))
						{
							case CUSTOM -> ((JSONObject) value).forEach((statName, amount) -> statistiks.add(new Statistik((String) statName, player.getStatistic(getCustomStatistique((String) statName)))));
							case KILLED -> ((JSONObject) value).forEach((entity, amount) -> statistiks.add(new Statistik((String) entity, player.getStatistic(Statistic.valueOf("KILL_ENTITY"), getEntityType((String) entity)))));
							case KILLED_BY -> ((JSONObject) value).forEach((entity, amount) -> statistiks.add(new Statistik((String) entity, player.getStatistic(Statistic.valueOf("ENTITY_KILLED_BY"), getEntityType((String) entity)))));
							case USED -> ((JSONObject) value).forEach((material, amount) -> statistiks.add(new Statistik((String) material, player.getStatistic(Statistic.valueOf("USE_ITEM"), getMaterial((String) material)))));
							case CRAFTED -> ((JSONObject) value).forEach((material, amount) -> statistiks.add(new Statistik((String) material, player.getStatistic(Statistic.valueOf("CRAFT_ITEM"), getMaterial((String) material)))));
							case BROKEN -> ((JSONObject) value).forEach((material, amount) -> statistiks.add(new Statistik((String) material, player.getStatistic(Statistic.valueOf("BREAK_ITEM"), getMaterial((String) material)))));
							case MINED -> ((JSONObject) value).forEach((material, amount) -> statistiks.add(new Statistik((String) material, player.getStatistic(Statistic.valueOf("MINE_BLOCK"), getMaterial((String) material)))));
							case PICKED_UP -> ((JSONObject) value).forEach((material, amount) -> statistiks.add(new Statistik((String) material, player.getStatistic(Statistic.valueOf("PICKUP"), getMaterial((String) material)))));
							case DROPPED -> ((JSONObject) value).forEach((material, amount) -> statistiks.add(new Statistik((String) material, player.getStatistic(Statistic.valueOf("DROP"), getMaterial((String) material)))));
							default -> {
							}
						}

						statistics.put((String) key, statistiks);
					});

					Connection bdd = DatabaseManager.getConnection();
					String req = "INSERT INTO " + DatabaseManager.Tables.STATISTIC
							+ " ( group_id, player_uuid , parent, statistic, amount ) VALUES ( ? , ? , ? , ? , ? )"
							+ " ON DUPLICATE KEY UPDATE amount=VALUES(amount)";
					PreparedStatement ps = bdd.prepareStatement(req);

					for(Map.Entry<String, List<Statistik>> a : statistics.entrySet())
					{
						for(Statistik s : a.getValue())
						{
							ps.setInt(1, groupId);
							ps.setString(2, player.getUniqueId().toString());
							ps.setString(3, a.getKey());
							ps.setString(4, s.getName());
							ps.setLong(5, s.getAmount());
							ps.addBatch();
						}
					}

					ps.executeBatch();
					ps.close();
				}
				catch(Exception e)
				{
					throw new NKException(InternalMessages.PLAYER_STATISTIC_SAVE_ERROR.toString());
				}
			}
		}
	}

	private static EntityType getEntityType(String entity)
	{
		entity = entity.replace("minecraft:", "");

		return EntityType.valueOf(entity.toUpperCase());
	}

	private static Material getMaterial(String material)
	{
		material = material.replace("minecraft:", "");

		return Material.valueOf(material.toUpperCase());
	}

	private static Statistic getCustomStatistique(String statistic)
	{
		statistic = statistic.replace("minecraft:", "");

		switch(statistic)
		{
			case "drop":
				return Statistic.DROP_COUNT;
			case "interact_with_crafting_table":
				return Statistic.CRAFTING_TABLE_INTERACTION;
			case "play_time":
				return Statistic.PLAY_ONE_MINUTE;
			case "fill_cauldron":
				return Statistic.CAULDRON_FILLED;
			case "use_cauldron":
				return Statistic.CAULDRON_USED;
			case "eat_cake_slice":
				return Statistic.CAKE_SLICES_EATEN;
			case "clean_armor":
				return Statistic.ARMOR_CLEANED;
			case "open_enderchest":
				return Statistic.ENDERCHEST_OPENED;
			case "inspect_dispenser":
				return Statistic.DISPENSER_INSPECTED;
			case "play_noteblock":
				return Statistic.NOTEBLOCK_PLAYED;
			case "interact_with_brewingstand":
				return Statistic.BREWINGSTAND_INTERACTION;
			case "play_record":
				return Statistic.RECORD_PLAYED;
			case "tune_noteblock":
				return Statistic.NOTEBLOCK_TUNED;
			case "open_chest":
				return Statistic.CHEST_OPENED;
			case "trigger_trapped_chest":
				return Statistic.TRAPPED_CHEST_TRIGGERED;
			case "pot_flower":
				return Statistic.FLOWER_POTTED;
			case "inspect_dropper":
				return Statistic.DROPPER_INSPECTED;
			case "interact_with_furnace":
				return Statistic.FURNACE_INTERACTION;
			case "clean_banner":
				return Statistic.BANNER_CLEANED;
			case "enchant_item":
				return Statistic.ITEM_ENCHANTED;
			case "interact_with_beacon":
				return Statistic.BEACON_INTERACTION;
			case "inspect_hopper":
				return Statistic.HOPPER_INSPECTED;
			case "open_shulker_box":
				return Statistic.SHULKER_BOX_OPENED;
			default:
				Statistic.valueOf(statistic.toUpperCase());
		}

		return Statistic.valueOf(statistic.toUpperCase());
	}
}
