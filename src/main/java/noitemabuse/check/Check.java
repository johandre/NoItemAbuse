/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.check;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import noitemabuse.Executor;

public abstract class Check extends Executor {
    public Check(Plugin plugin) {
        super(plugin);
    }

    public abstract boolean check(Player player, ItemStack item);

    public abstract String getLogMessage(Player player, ItemStack item);

    public String getPermission() {
        return "noitemabuse.checks." + getClass().getSimpleName().toLowerCase();
    }
}
