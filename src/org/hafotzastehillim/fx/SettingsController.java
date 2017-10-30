package org.hafotzastehillim.fx;

import javafx.fxml.FXML;

import com.jfoenix.controls.JFXCheckBox;

public class SettingsController {
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
}
