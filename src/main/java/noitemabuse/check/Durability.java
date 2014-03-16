/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.check;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import noitemabuse.Executor;
import noitemabuse.config.Options;

public class Durability extends Check {
    DurabilityOptions options = new DurabilityOptions(this);

    public Durability(Plugin plugin) {
        super(plugin);
    };

    @Override
    public boolean check(Player player, ItemStack item) {
        return item.getDurability() < options.min_durability;
    }

    @Override
    public Options getOptions() {
        return options;
    }

    class DurabilityOptions extends Options {
        public int min_durability = 0;

        public DurabilityOptions(Executor executor) {
            super(executor);
        }
    }
}
