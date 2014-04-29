/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.check;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import noitemabuse.config.*;
import noitemabuse.util.*;
import noitemabuse.util.AttributeList.Attribute;

public class Attributes extends Check {
    AttributesOptions options = new AttributesOptions(this);

    public Attributes(Plugin plugin) {
        super(plugin);
    }
    
    @Override
    public boolean check(Player player, ItemStack item) {
        AttributeList list = new AttributeList(item);
        for (Attribute attribute : list) {
            System.out.println(attribute.getType() + " = " + attribute.getAmount()); // XXX
            if (attribute.getAmount() > options.max_attribute_value) return true;
        }
        return false;
    }

    @Override
    public String getLogMessage(Player player, ItemStack item) {
        AttributeList list = new AttributeList(item);
        Attribute bad = null;
        for (Attribute attribute : list) {
            if (attribute.getAmount() > options.max_attribute_value) {
                bad = attribute;
                break;
            }
        }
        String type = "undefined";
        double amount = 0;
        if (bad != null) {
            type = bad.getType().toString();
            amount = bad.getAmount();
        }
        return Message.format(player, Message.REASON_ATTRIBUTES, "$attribute:" + type, "$level:" + amount, "$max:" + options.max_attribute_value);
    }

    @Override
    public Options getOptions() {
        return options;
    }

    public class AttributesOptions extends Options {
        public int max_attribute_value = 10;

        public AttributesOptions(Executor executor) {
            super(executor);
        }
    }
}
