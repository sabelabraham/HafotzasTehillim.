

import java.io.FileInputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import fxlauncher.CreateManifest;
import fxlauncher.FXManifest;

public class Manifest {

	public static void main(String[] args) throws Exception {

		KeyStore ks = KeyStore.getInstance("JKS");

		try (FileInputStream in = new FileInputStream("C:\\Users\\Yossel\\workspace\\fxlauncher\\certs\\.keystore")) {
			ks.load(in, "MM122994".toCharArray());
		}

		PrivateKey key = (PrivateKey) ks.getKey("mordechai", "MM122994".toCharArray());

		FXManifest man = CreateManifest.create(URI.create("file:///C:/users/yossel/desktop/manifest/"),
				"org.hafotzastehillim.fx.Main", Paths.get(URI.create("file:///C:/users/yossel/desktop/manifest/")), key);
		man.cacheDir = "USERLIB/PointEntry/lib/";
		
		System.out.println(man);
	}
}
