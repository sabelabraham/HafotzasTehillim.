package org.hafotzastehillim.fx;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.hafotzastehillim.fx.spreadsheet.Entry;
import org.hafotzastehillim.fx.util.Util;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXSpinner;

public class ReportQueryController {

	@FXML
	private JFXRadioButton lastNameAllToggle;
	@FXML
	private ToggleGroup lastNameGroup;
	@FXML
	private JFXRadioButton lastNameRangeToggle;
	@FXML
	private TextField lastNameFrom;
	@FXML
	private TextField lastNameTo;
	@FXML
	private JFXRadioButton genderAllToggle;
	@FXML
	private ToggleGroup genderGroup;
	@FXML
	private JFXRadioButton genderBoyToggle;
	@FXML
	private JFXRadioButton genderGirlToggle;
	@FXML
	private JFXRadioButton lastNameSelectToggle;
	@FXML
	private TextField lastNameSelect;
	@FXML
	private JFXRadioButton pointsAllToggle;
	@FXML
	private ToggleGroup pointsGroup;
	@FXML
	private JFXRadioButton pointsMinimumToggle;
	@FXML
	private Spinner<Integer> pointsMinimum;
	@FXML
	private JFXRadioButton pointsRangeToggle;
	@FXML
	private Spinner<Integer> pointsFrom;
	@FXML
	private Spinner<Integer> pointsTo;
	@FXML
	private JFXRadioButton campaignAllToggle;
	@FXML
	private ToggleGroup campaignGroup;
	@FXML
	private JFXRadioButton campaignSelectToggle;
	@FXML
	private Spinner<Integer> campaignSelect;
	@FXML
	private JFXRadioButton campaignRangeToggle;
	@FXML
	private Spinner<Integer> campaignFrom;
	@FXML
	private Spinner<Integer> campaignTo;
	@FXML
	private JFXRadioButton shavuosAllToggle;
	@FXML
	private ToggleGroup shavuosGroup;
	@FXML
	private JFXRadioButton shavuosSelectToggle;
	@FXML
	private Spinner<Integer> shavuosSelect;
	@FXML
	private JFXRadioButton shavuosRangeToggle;
	@FXML
	private Spinner<Integer> shavuosFrom;
	@FXML
	private Spinner<Integer> shavuosTo;
	@FXML
	private GridPane grid;
	@FXML
	private JFXButton run;
	@FXML
	private JFXSpinner running;

	@FXML
	public void initialize() {
		lastNameSelect.sceneProperty().addListener((obs, ov, nv) -> {
			nv.getRoot().disableProperty().bind(Model.getInstance().getSpreadsheet().loadService().runningProperty());
		});
		lastNameSelect.disableProperty().bind(lastNameSelectToggle.selectedProperty().not());
		lastNameFrom.disableProperty().bind(lastNameRangeToggle.selectedProperty().not());
		lastNameTo.disableProperty().bind(lastNameFrom.disabledProperty());

		campaignSelect.disableProperty().bind(campaignSelectToggle.selectedProperty().not());
		campaignFrom.disableProperty().bind(campaignRangeToggle.selectedProperty().not());
		campaignTo.disableProperty().bind(campaignFrom.disabledProperty());

		pointsMinimum.disableProperty().bind(pointsMinimumToggle.selectedProperty().not());
		pointsFrom.disableProperty().bind(pointsRangeToggle.selectedProperty().not());
		pointsTo.disableProperty().bind(pointsFrom.disabledProperty());

		shavuosSelect.disableProperty().bind(shavuosSelectToggle.selectedProperty().not());
		shavuosFrom.disableProperty().bind(shavuosRangeToggle.selectedProperty().not());
		shavuosTo.disableProperty().bind(shavuosFrom.disabledProperty());

		Util.commitOnFocusLose(campaignSelect);
		Util.commitOnFocusLose(campaignFrom);
		Util.commitOnFocusLose(campaignTo);

		Util.commitOnFocusLose(pointsMinimum);
		Util.commitOnFocusLose(pointsFrom);
		Util.commitOnFocusLose(pointsTo);

		Util.commitOnFocusLose(shavuosSelect);
		Util.commitOnFocusLose(shavuosFrom);
		Util.commitOnFocusLose(shavuosTo);

		Util.selectOnFocus(lastNameFrom);
		Util.selectOnFocus(lastNameTo);

		((SpinnerValueFactory.IntegerSpinnerValueFactory) campaignTo.getValueFactory()).minProperty()
				.bind(campaignFrom.valueProperty());
		((SpinnerValueFactory.IntegerSpinnerValueFactory) pointsTo.getValueFactory()).minProperty()
				.bind(pointsFrom.valueProperty());
		((SpinnerValueFactory.IntegerSpinnerValueFactory) shavuosTo.getValueFactory()).minProperty()
				.bind(shavuosFrom.valueProperty());

		grid.disableProperty().bind(Model.getInstance().getSpreadsheet().searchService().runningProperty());
		run.disableProperty().bind(grid.disabledProperty());
		running.visibleProperty().bind(grid.disabledProperty());
	}

	@FXML
	public void run(ActionEvent event) {
		ObservableList<Entry> all = FXCollections.observableArrayList();
		Model.getInstance().getSpreadsheet().searchEntries("ALL", all, (q, v, col) -> true, 0);
		Model.getInstance().getSpreadsheet().searchService().setOnSucceeded(evt -> {

			all.removeIf(entry -> { // FIXME try not to add in first place by using column matcher
				boolean remove = false;

				if (genderBoyToggle.isSelected()) {
					remove |= !entry.getGender().equalsIgnoreCase("Boy");
				}
				if (genderGirlToggle.isSelected()) {
					remove |= !entry.getGender().equalsIgnoreCase("Girl");
				}

				if (lastNameSelectToggle.isSelected()) {
					remove |= !entry.getLastName().equalsIgnoreCase(lastNameSelect.getText())
							&& !entry.getLastNameYiddish().equals(lastNameSelect.getText());
				}
				if (lastNameRangeToggle.isSelected()) {
					remove |= !(entry.getLastName().compareToIgnoreCase(lastNameFrom.getText()) >= 0
							&& entry.getLastName().compareToIgnoreCase(lastNameTo.getText()) <= 0)
							&& !(entry.getLastNameYiddish().compareTo(lastNameFrom.getText()) >= 0
									&& entry.getLastNameYiddish().compareTo(lastNameTo.getText()) <= 0);
				}

				if (campaignSelectToggle.isSelected()) {
					remove |= entry.getPoint(campaignSelect.getValue() - 1) == 0;
				}
				if (campaignRangeToggle.isSelected()) {
					int total = 0;
					for (int i = campaignFrom.getValue() - 1; i < campaignTo.getValue(); i++) {
						total += entry.getPoint(i);
					}
					remove |= total == 0;
				}

				if (pointsMinimumToggle.isSelected()) {
					remove |= entry.getTotal() < pointsMinimum.getValue();
				}
				if (pointsRangeToggle.isSelected()) {
					remove |= entry.getTotal() < pointsFrom.getValue() || entry.getTotal() > pointsTo.getValue();
				}

				if (shavuosSelectToggle.isSelected()) {
					remove |= entry.getShavuosData(shavuosSelect.getValue() - Main.FIRST_SHAVUOS_YEAR) == 0;
				}

				return remove;
			});

			Model.getInstance().getSpreadsheet().searchService().setOnSucceeded(null);

			Map<String, ObservableList<Entry>> map = new LinkedHashMap<>();

			map.put("Test1", all);
			map.put("Test2", all);
			map.put("Test3", all);
			map.put("Test4", all);
			map.put("Test5", all);
			map.put("Test6", all);

			ReportResultsView report = new ReportResultsView(map);
			Util.createDialog(report, "Testing Reports", ButtonType.OK);
		});

	}
}
