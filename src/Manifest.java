
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fxlauncher.CreateManifest;
import fxlauncher.FXManifest;
import fxlauncher.LibraryFile;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.stage.Stage;

public class Manifest extends Application {

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	@Override
	public void start(Stage arg0) throws Exception {

		KeyStore ks = KeyStore.getInstance("JKS");

		try (FileInputStream in = new FileInputStream("C:\\Users\\Yossel\\Desktop\\manifest\\keystore")) {
			ks.load(in, "MM122994".toCharArray());
		}

		PrivateKey key = (PrivateKey) ks.getKey("mordechai", "MM122994".toCharArray());

		FXManifest man = CreateManifest.create(URI.create("file:///C:/users/yossel/desktop/manifest/"),
				"org.hafotzastehillim.pointentry.fx.Main",
				Paths.get(URI.create("file:///C:/users/yossel/desktop/manifest/")), key);
		man.cacheDir = "USERLIB/PointEntry/lib/";

		String str = man.toString();

		Pattern p1 = Pattern.compile("<lib file=\"pointentry\\.jar\".*(href=\".+\" ).*>");
		Matcher m1 = p1.matcher(str);

		m1.find();

		Pattern p2 = Pattern.compile("(<lib file=\"pointentry\\.jar\".*>)");
		Matcher m2 = p2.matcher(str);

		m2.find();

		String output = m2.group(1).replace(m1.group(1),
				"href=\"https://www.dropbox.com/s/ldvgqfkwxtal9z9/pointentry.jar?dl=1\" ");

		System.out.println(man);
		Map<DataFormat, Object> map = new HashMap<>();
		map.put(DataFormat.PLAIN_TEXT, output + "\n");

		Platform.runLater(() -> Clipboard.getSystemClipboard().setContent(map));
		Platform.exit();

	}
}
