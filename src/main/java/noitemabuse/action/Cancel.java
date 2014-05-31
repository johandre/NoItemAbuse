/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.action;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import noitemabuse.check.Check;
import noitemabuse.config.Options;
import noitemabuse.util.Executor;

public class Cancel extends Action {
    private CancelOptions options = new CancelOptions(this);

    public Cancel(Plugin plugin) {
        super(plugin);
    }

    @Override
    public Options getOptions() {
        return options;
    }

    @Override
    public void perform(Player player, ItemStack item, Event event, String message, List<Check> failedChecks) {
        if ((event instanceof InventoryEvent || event instanceof PlayerItemHeldEvent) && !options.cancel_inventory_events) return;
        if (event instanceof Cancellable) {
            ((Cancellable) event).setCancelled(true);
        }
    }

    public class CancelOptions extends Options {
        public boolean cancel_inventory_events = false;

        public CancelOptions(Executor executor) {
            super(executor);
        }
    }
}
