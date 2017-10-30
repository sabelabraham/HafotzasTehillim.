package org.hafotzastehillim.fx;

import java.io.IOException;

import org.hafotzastehillim.fx.util.Util;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;

public class DateTimeBar extends StackPane {
	
	public DateTimeBar() {
		try {
			getChildren().add(FXMLLoader.load(getClass().getResource("/resources/fxml/DateTimeBar.fxml")));
		} catch (IOException e) {
			Util.showErrorDialog(e);
			return;
		}
		
		getStyleClass().add("date-time-bar");
		getStylesheets().add(getClass().getResource("/resources/css/date-time-bar.css").toExternalForm());
	}

}
