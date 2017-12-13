package org.hafotzastehillim.pointentry.fx.util.sync;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import org.hafotzastehillim.pointentry.fx.Main;
import org.hafotzastehillim.pointentry.fx.util.DialogUtils;
import org.hafotzastehillim.pointentry.fx.util.Util;

public class Synchronize {

	private Synchronize() {

	}

	private static final int VERSION = 4;

	private static final Preferences pref = Preferences.userNodeForPackage(Synchronize.class);
	private static final String KEY = "SYNC_VERSION";

	public static final Path APP = Paths.get(System.getProperty("user.dir"));
	public static final Path HOME = APP.getParent();
	public static final Path LIB = HOME.resolve("lib");

	private static final List<Copy> copies = new ArrayList<>();
	private static final List<Delete> deletes = new ArrayList<>();

	static {
		copies.add(new Copy(LIB.resolve("PointEntry.cfg"), APP.resolve("PointEntry.cfg"), 4, true));

		deletes.add(new Delete(LIB.resolve("to-app-PointEntry.cfg"), 4, false));
	}

	public static void sync() {
		if (Main.debug)
			return;

		try {
			while (requiresSync()) {
				int syncVersion = getLastSyncVersion() + 1;
				boolean restart = false;

				for (Copy copy : copies) {
					if (copy.version == syncVersion) {
						if (Files.exists(copy.source)) {
							Files.copy(copy.source, copy.target, StandardCopyOption.REPLACE_EXISTING);

							restart |= copy.requiresRestart;
						}
					}
				}

				for (Delete delete : deletes) {
					if (delete.version == syncVersion) {
						if (Files.exists(delete.path)) {
							Files.delete(delete.path);

							restart |= delete.requiresRestart;
						}
					}
				}

				incrementSyncVersion();

				if (restart) {
					restart();
				}
			}
		} catch (Exception e) {
			DialogUtils.showErrorDialog(e);
		}

	}

	public static int currentVersion() {
		return VERSION;
	}

	public static int getLastSyncVersion() {
		return pref.getInt(KEY, 0);
	}

	public static boolean requiresSync() {
		return currentVersion() > getLastSyncVersion();
	}

	public static void incrementSyncVersion() {
		pref.putInt(KEY, getLastSyncVersion() + 1);
	}

	public static void restart() throws IOException {
		new ProcessBuilder("PointEntry").directory(HOME.toFile()).inheritIO().start();
		System.exit(0);
	}
}
