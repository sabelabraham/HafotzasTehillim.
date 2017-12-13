package org.hafotzastehillim.pointentry.fx.util.sync;

import java.nio.file.Path;

public class Copy {

	public final Path source;
	public final Path target;
	public final int version;
	public final boolean requiresRestart;
	
	public Copy(Path source, Path target, int version, boolean requiresRestart) {
		this.source = source;
		this.target = target;
		this.version = version;
		this.requiresRestart = requiresRestart;
	}
}
