/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.action;

import java.util.*;
import java.util.Map.Entry;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import noitemabuse.Executor;
import noitemabuse.check.Check;
import noitemabuse.config.Options;

public class Purify extends Action {
    private PurifyOptions options = new PurifyOptions(this);
    private static final short NORMAL_DURABILITY = 0;

    public Purify(Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean defaultEnabled() {
        return false;
    }

    @Override
    public Options getOptions() {
        return options;
    }

    @Override
    public void perform(Player player, ItemStack item, Event event, String message, List<Check> failedChecks) {
        if (item.getDurability() < config.values.getInt("checks.Durability.min_durability")) {
            item.setDurability(NORMAL_DURABILITY);
        }
        if (options.purify_all) {
            // item.getEnchantments().clear(); // ImmutableMap... we meet again.
            // Great documentation, and great clearEnchantments() method provided.
            // The API is inconsistent. Some methods
            // do provide immutable lists and maps, but a few of them
            // (for example: Block.getDrops()) are modifiable.
            // Again, none of these documented and can only know via trial and error.
            // Some/most of the API documentation is useless
            // and only explains what you already knew using common sense.
            for (Enchantment enchant : item.getEnchantments().keySet()) {
                item.removeEnchantment(enchant);
            }
        } else {
            for (Entry<Enchantment, Integer> ent : item.getEnchantments().entrySet()) {
                Enchantment enchant = ent.getKey();
                final int level = ent.getValue(), max = enchant.getMaxLevel();
                if (level > max) {
                    item.removeEnchantment(enchant);
                    return;
                }
            }
        }
    }

    public class PurifyOptions extends Options {
        public boolean purify_all = true;

        public PurifyOptions(Executor executor) {
            super(executor);
        }
    }
}
