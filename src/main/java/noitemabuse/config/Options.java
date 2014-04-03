/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.config;

import java.lang.reflect.Field;

import reflectlib.bukkit.config.Reflecturation;
import noitemabuse.Executor;
import noitemabuse.action.Action;

public class Options extends Reflecturation {
    public boolean enabled;
    private Executor executor;

    public Options(Executor executor) {
        super(executor.getPlugin());
        this.executor = executor;
    }

    @Override
    protected String getKeyName(Field field) {
        return (executor instanceof Action ? "actions" : "checks") + "." + getName() + "." + super.getKeyName(field);
    }
}
