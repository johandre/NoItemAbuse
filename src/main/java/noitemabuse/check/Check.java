/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.check;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import noitemabuse.util.Executor;

public abstract class Check extends Executor {
    public Check(Plugin plugin) {
        super(plugin);
    }

    public abstract boolean check(Player player, ItemStack item);

    public abstract String getLogMessage(Player player, ItemStack item);

    public String getPermission() {
        return "noitemabuse.checks." + getClass().getSimpleName().toLowerCase();
    }

    public boolean hasItemPermissions(Player player, ItemStack item) {
        String type = item.getType().toString();
        return player.hasPermission("noitemabuse.item." + type) || player.hasPermission(getPermission() + ".item." + type);
    }

    /**
     * Checks whether the player has permission to any of the nodes in the provided permissions list.
     */
    public boolean hasPermissionList(Player player, String base, Object... permissions) {
        if (permissions.length == 0) throw new IllegalArgumentException("At least one permission is required.");
        String prev = base + "." + permissions[0];
        StringBuilder prv = new StringBuilder(prev);
        for (int i = 1; i < permissions.length; i++) {
            if (player.hasPermission(prv.toString())) return true;
            prv.append(".").append(permissions[i]);
        }
        return player.hasPermission(prv.toString());
    }

    public boolean hasPermissions(Player player, ItemStack item) {
        return player.hasPermission(getPermission()) || hasWorldPermissions(player) || hasItemPermissions(player, item);
    }

    public boolean hasWorldPermissions(Player player) {
        String world = player.getWorld().getName();
        return player.hasPermission("noitemabuse.world." + world) || player.hasPermission(getPermission() + ".world." + world);
    }
}
