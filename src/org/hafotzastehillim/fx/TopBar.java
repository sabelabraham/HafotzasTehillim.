package org.hafotzastehillim.fx;

import java.io.IOException;

import org.hafotzastehillim.fx.util.Util;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;

public class TopBar extends StackPane {

	public TopBar() {
		try {
			getChildren().add(FXMLLoader.load(getClass().getResource("/resources/fxml/TopBar.fxml")));
		} catch (IOException e) {
			Util.showErrorDialog(e);
			return;
		}
		
		getStyleClass().add("top-bar");
		getStylesheets().add(getClass().getResource("/resources/css/top-bar.css").toExternalForm());
	}
}
