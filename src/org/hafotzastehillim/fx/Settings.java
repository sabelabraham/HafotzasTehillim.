package org.hafotzastehillim.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;

import java.io.IOException;

import org.hafotzastehillim.fx.util.Util;

import com.jfoenix.controls.JFXCheckBox;

public class Settings extends GridPane {
	@FXML
	private JFXCheckBox ignoreInvalidPhone;
	@FXML
	private JFXCheckBox ignoreDuplicates;
	@FXML
	private JFXCheckBox ignoreFamilyConflicts;

	@FXML
	private void initialize() {
		ignoreInvalidPhone.selectedProperty().bindBidirectional(Model.getInstance().ignoreInvalidPhoneProperty());
		ignoreDuplicates.selectedProperty().bindBidirectional(Model.getInstance().ignoreDuplicatesProperty());
		ignoreFamilyConflicts.selectedProperty().bindBidirectional(Model.getInstance().ignoreFamilyConflictsProperty());
	}

	public Settings() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/Settings.fxml"));
			loader.setController(this);
			loader.setRoot(this);
			loader.load();
		} catch (IOException e) {
			Util.showErrorDialog(e);
			return;
		}

		getStyleClass().add("settings");
		// getStylesheets().add(getClass().getResource("/resources/css/date-time-bar.css").toExternalForm());
	}
}
