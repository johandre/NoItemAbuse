/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.config;

import java.lang.reflect.Field;

import reflectlib.bukkit.config.Reflecturation;
import noitemabuse.action.Action;
import noitemabuse.util.Executor;

public class Options extends Reflecturation {
    public boolean enabled;
    private Executor executor;

    public Options(Executor executor) {
        super(executor.getPlugin());
        this.executor = executor;
    }

    public String getExecutorName() {
        return executor.getClass().getSimpleName();
    }

    @Override
    protected String getKeyName(Field field) {
        return (executor instanceof Action ? "actions" : "checks") + "." + getExecutorName() + "." + super.getKeyName(field);
    }
}
