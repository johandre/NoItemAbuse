/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.task;

import eu.icecraft_mc.frozenlib_R1.Plugin;

public interface Task extends Runnable {
    public void schedule(Plugin plugin);
}
