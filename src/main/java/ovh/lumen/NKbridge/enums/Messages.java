package ovh.lumen.NKbridge.enums;

import org.bukkit.ChatColor;

public enum Messages
{
	PERMISSION_MISSING(ChatColor.RED + "Vous n'avez pas la permission."),
	UNKNOW_SUBCOMMAND(ChatColor.RED + "Sous-commande inconnue."),
	ROOT_PLUGIN_INFO_MSG(ChatColor.GREEN + "%0% v%1%" + ChatColor.ITALIC + " by %2%"),
	ROOT_RELOAD_MSG(ChatColor.GREEN + "%0% a été rechargé."),
	EVENT_ADVANCEMENT(ChatColor.BLUE + "%0%" + ChatColor.AQUA + " a obtenu le succès "),
	ILLEGAL_CHAR_GROUP_NAME(ChatColor.RED + "Nom de groupe non conforme."),
	UNKNOW_GROUP(ChatColor.RED + "Le groupe " + ChatColor.DARK_RED + "%0%" + ChatColor.RED + " n'existe pas."),
	UNKNOW_WORLD(ChatColor.RED + "Le monde " + ChatColor.DARK_RED + "%0%" + ChatColor.RED + " n'existe pas."),
	ALREADY_IN_SPECIFIED_GROUP(
			ChatColor.RED + "Le monde " + ChatColor.DARK_RED + "%0%" + ChatColor.RED + " est déjà dans le groupe " + ChatColor.DARK_RED + "%1%" + ChatColor.RED + "."),
	ALREADY_IN_GROUP(ChatColor.RED + "Le monde " + ChatColor.DARK_RED + "%0%" + ChatColor.RED + " est déjà dans un groupe."),
	ADDED_IN_GROUP(ChatColor.GREEN + "Le monde " + ChatColor.DARK_GREEN + "%0%" + ChatColor.GREEN + " a été ajouté dans le groupe " + ChatColor.DARK_GREEN + "%1%"
			+ ChatColor.GREEN + "."),
	REMOVED_FROM_GROUP(ChatColor.GREEN + "Le monde " + ChatColor.DARK_GREEN + "%0%" + ChatColor.GREEN + " a été retiré du groupe " + ChatColor.DARK_GREEN + "%1%"
			+ ChatColor.GREEN + "."),
	GROUP_ALREADY_EXIST(ChatColor.RED + "Le groupe " + ChatColor.DARK_RED + "%0%" + ChatColor.RED + " existe déjà."),
	GROUP_CREATED(ChatColor.GREEN + "Le groupe " + ChatColor.DARK_GREEN + "%0%" + ChatColor.GREEN + " a été créé."),
	GROUP_DELETED(ChatColor.GREEN + "Le groupe " + ChatColor.DARK_GREEN + "%0%" + ChatColor.GREEN + " a été supprimé."),
	GROUP_DELETE_NONE_EMPTY(ChatColor.RED + "Ce groupe est lié à plusieurs monde. Retirez d'abord tous les mondes avant de supprimer ce groupe."),
	GROUP_LIST(ChatColor.GREEN + "Liste des groupes"),
	GROUP_WORLDS_LIST(ChatColor.GREEN + "Liste des mondes du groupe " + ChatColor.DARK_GREEN + "%0%" + ChatColor.GREEN),
	NOT_IN_GROUP(ChatColor.RED + "Le monde " + ChatColor.DARK_RED + "%0%" + ChatColor.RED + " n'est pas dans le groupe " + ChatColor.DARK_RED + "%1%" + ChatColor.RED + ".");

	private final String value;

	Messages(String value)
	{
		this.value = value;
	}

	public String toString()
	{
		return this.value;
	}
}
