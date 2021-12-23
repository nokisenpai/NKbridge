package ovh.lumen.NKbridge.managers;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;
import ovh.lumen.NKbridge.data.AdvancementInfo;
import ovh.lumen.NKbridge.data.NKData;
import ovh.lumen.NKbridge.enums.InternalMessages;
import ovh.lumen.NKbridge.enums.Messages;
import ovh.lumen.NKbridge.exceptions.SetupException;
import ovh.lumen.NKbridge.utils.MessageParser;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

public final class AdvancementManager
{
	private static File advancementFile = null;
	private static String error = "";

	public static void init(File file)
	{
		if(!file.isFile())
		{
			((JavaPlugin) NKData.PLUGIN).saveResource(file.getName(), false);
		}

		AdvancementManager.advancementFile = file;
	}

	public static void load() throws SetupException
	{
		NKData.ADVANCEMENTS_INFO.clear();

		Yaml yaml = new Yaml();
		InputStream inputStream;

		inputStream = ((JavaPlugin) NKData.PLUGIN).getResource(advancementFile.getName());

		Map<Object, Object> obj = yaml.load(inputStream);

		checkKey(obj, "advancements");
		checkErrors();

		Map<String, Object> translations = (Map<String, Object>) obj.get("advancements");

		for(Map.Entry<String, Object> advancement : translations.entrySet())
		{
			String advancementKey = advancement.getKey();

			Map<String, String> advancementInfo = (Map<String, String>) advancement.getValue();

			NKData.ADVANCEMENTS_INFO.put(advancementKey, new AdvancementInfo(advancementKey, advancementInfo.get("name"), advancementInfo.get("description")));
		}

		Bukkit.getWorlds().forEach(world -> world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false));
	}

	private static void checkKey(Map<Object, Object> obj, String key)
	{
		if(!(obj.containsKey(key)))
		{
			error += "\n> Key '" + key + "' not found. Verify syntax of keys.";
		}
	}

	private static void checkErrors() throws SetupException
	{
		if(!error.equals(""))
		{
			throw new SetupException(error);
		}
	}

	public static void announceAdvancement(String playerName, String groupName, String advancementKey)
	{
		AdvancementInfo advancementInfo = NKData.ADVANCEMENTS_INFO.get(advancementKey);

		GroupManager.getWorldsInGroup(groupName).forEach(s ->
		{
			World world = Bukkit.getWorld(s);
			if(world != null)
			{
				for(Player p : world.getPlayers())
				{
					MessageParser messageParser = new MessageParser(Messages.EVENT_ADVANCEMENT.toString());
					messageParser.addArg(playerName);
					TextComponent msg = new TextComponent(messageParser.parse());

					TextComponent tc = new TextComponent(ChatColor.GREEN + "[" + advancementInfo.getName() + "]");
					HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GREEN + advancementInfo.getDescription()));
					tc.setHoverEvent(he);

					p.spigot().sendMessage(msg, tc);
				}
			}
		});
	}
}
