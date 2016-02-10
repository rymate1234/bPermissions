package de.bananaco.bpermissions.imp;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.CalculableType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Common functions used within commands (and tab completers)
 *
 * Created by rymate1234 on 25/05/2015.
 */
public abstract class BaseCommand {

    public static String format(String message) {
        ChatColor vary = ChatColor.GREEN;
        if (message.contains("!")) {
            vary = ChatColor.RED;
        } else if (message.contains(":")) {
            vary = ChatColor.AQUA;
        }
        return ChatColor.BLUE + "[bPermissions] " + vary + message;
    }

    public static boolean hasPermission(Player player, String node) {
        return ApiLayer.hasPermission(player.getWorld().getName(), CalculableType.USER, player.getName(), node);
    }

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(format(message));
    }

    public String getName(CommandSender sender) {
        if (sender instanceof Player) {
            return sender.getName();
        }
        return "CONSOLE";
    }

}
