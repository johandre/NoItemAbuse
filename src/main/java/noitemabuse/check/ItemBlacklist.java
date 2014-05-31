/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.check;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import noitemabuse.config.*;
import noitemabuse.util.Executor;

public class ItemBlacklist extends Check {
    BlacklistOptions options;
    Material[] blacklistMaterials;

    public ItemBlacklist(Plugin plugin) {
        super(plugin);
        options = new BlacklistOptions(this);
    }

    @Override
    public boolean check(Player player, ItemStack item) {
        if (player.hasPermission(getPermission() + "." + item.getType()) && !player.hasPermission("*")) return true;
        Material type = item.getType();
        for (Material blacklist : blacklistMaterials) {
            if (blacklist == type) return true;
        }
        return false;
    }

    @Override
    public boolean defaultEnabled() {
        return false;
    }

    @Override
    public String getLogMessage(Player player, ItemStack item) {
        return Message.format(player, Message.REASON_BLACKLISTED_ITEM);
    }

    @Override
    public Options getOptions() {
        return options;
    }

    public class BlacklistOptions extends Options {
        public String[] blacklist = { "sponge", "bedrock", "skull_item" };

        public BlacklistOptions(Executor executor) {
            super(executor);
        }

        @Override
        public void init() {
            super.init();
            blacklistMaterials = new Material[blacklist.length];
            for (int i = 0; i < blacklist.length; i++) {
                String name = blacklist[i];
                Material mat = Material.matchMaterial(name);
                blacklistMaterials[i] = mat;
                if (mat == null) {
                    plugin.getLogger().warning("Material in blacklist could not be matched: " + name);
                }
            }
        }
    }
}
