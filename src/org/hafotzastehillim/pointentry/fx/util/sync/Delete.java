package org.hafotzastehillim.pointentry.fx.util.sync;

import java.nio.file.Path;

public class Delete {

	public final Path path;
	public final int version;
	public final boolean requiresRestart;
	
	public Delete(Path path, int version, boolean requiresRestart) {
		this.path = path;
		this.version = version;
		this.requiresRestart = requiresRestart;
	}
}
