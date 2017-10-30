package org.hafotzastehillim.fx;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.hafotzastehillim.fx.spreadsheet.Entry;
import org.hafotzastehillim.fx.spreadsheet.Tab;
import org.hafotzastehillim.fx.util.HebrewChronology;
import org.hafotzastehillim.fx.util.RangeParser;
import org.hafotzastehillim.fx.util.Util;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXSpinner;

import static org.hafotzastehillim.fx.spreadsheet.Column.*;

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
	private JFXRadioButton memberSinceAllToggle;
	@FXML
	private JFXRadioButton memberSinceSelectToggle;
	@FXML
	private DatePicker memberSinceSelectField;
	@FXML
	private JFXRadioButton memberSinceBeforeToggle;
	@FXML
	private DatePicker memberSinceBeforeField;
	@FXML
	private JFXRadioButton memberSinceFromToggle;
	@FXML
	private DatePicker memberSinceFromField;
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
	private TextField campaignSelect;
	@FXML
	private JFXRadioButton campaignRangeToggle;
	@FXML
	private JFXCheckBox campaignAllMatch;
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
	private JFXCheckBox familyGrouping;
	@FXML
	private JFXButton run;
	@FXML
	private JFXSpinner running;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("LL/dd");
	private static final StringConverter<LocalDate> SHORT_CONVERTER = new StringConverter<LocalDate>() {

		@Override
		public String toString(LocalDate date) {
			if (date == null)
				return "";

			return DATE_FORMATTER.format(date);
		}

		@Override
		public LocalDate fromString(String string) {
			throw new UnsupportedOperationException();
		}
	};

	@FXML
	public void initialize() {
		lastNameSelect.sceneProperty().addListener((obs, ov, nv) -> {
			nv.getRoot().disableProperty().bind(Model.getInstance().getSpreadsheet().loadService().runningProperty());
		});
		lastNameSelect.disableProperty().bind(lastNameSelectToggle.selectedProperty().not());
		lastNameFrom.disableProperty().bind(lastNameRangeToggle.selectedProperty().not());
		lastNameTo.disableProperty().bind(lastNameFrom.disabledProperty());

		campaignSelect.disableProperty().bind(campaignSelectToggle.selectedProperty().not());
		campaignAllMatch.disableProperty().bind(campaignSelect.disabledProperty());

		memberSinceSelectField.disableProperty().bind(memberSinceSelectToggle.selectedProperty().not());
		memberSinceBeforeField.disableProperty().bind(memberSinceBeforeToggle.selectedProperty().not());
		memberSinceFromField.disableProperty().bind(memberSinceFromToggle.selectedProperty().not());

		pointsMinimum.disableProperty().bind(pointsMinimumToggle.selectedProperty().not());
		pointsFrom.disableProperty().bind(pointsRangeToggle.selectedProperty().not());
		pointsTo.disableProperty().bind(pointsFrom.disabledProperty());

		shavuosSelect.disableProperty().bind(shavuosSelectToggle.selectedProperty().not());
		shavuosFrom.disableProperty().bind(shavuosRangeToggle.selectedProperty().not());
		shavuosTo.disableProperty().bind(shavuosFrom.disabledProperty());

		memberSinceSelectField.setConverter(SHORT_CONVERTER);
		memberSinceBeforeField.setConverter(SHORT_CONVERTER);
		memberSinceFromField.setConverter(SHORT_CONVERTER);

		memberSinceSelectField.setValue(LocalDate.now());
		memberSinceBeforeField.setValue(LocalDate.now());
		memberSinceFromField.setValue(LocalDate.now());

		Util.commitOnFocusLose(pointsMinimum);
		Util.commitOnFocusLose(pointsFrom);
		Util.commitOnFocusLose(pointsTo);

		Util.commitOnFocusLose(shavuosSelect);
		Util.commitOnFocusLose(shavuosFrom);
		Util.commitOnFocusLose(shavuosTo);

		Util.selectOnFocus(lastNameFrom);
		Util.selectOnFocus(lastNameTo);

		campaignSelect.setTextFormatter(new TextFormatter<String>(change -> {
			if (change.getText().matches("[^\\s\\d,-]"))
				return null;

			return change;
		}));

		((SpinnerValueFactory.IntegerSpinnerValueFactory) pointsTo.getValueFactory()).minProperty()
				.bind(pointsFrom.valueProperty());
		((SpinnerValueFactory.IntegerSpinnerValueFactory) shavuosTo.getValueFactory()).minProperty()
				.bind(shavuosFrom.valueProperty());

		grid.disableProperty().bind(Model.getInstance().getSpreadsheet().searchService().runningProperty());
		run.disableProperty().bind(grid.disabledProperty());
		running.visibleProperty().bind(grid.disabledProperty());
	}

	private LocalDate getDate(String date) {
		return LocalDate.from(Instant.parse(date).atZone(ZoneId.systemDefault()));
	}

	@FXML
	public void run(ActionEvent event) {

		if (campaignSelectToggle.isSelected()) {
			try {
				RangeParser.parse(campaignSelect.getText());
			} catch (IllegalArgumentException e) {
				Util.createAlert(AlertType.ERROR, "Error", "Error", "Error in Campaign select:\n" + e.getMessage());
				return;
			}
		}

		ObservableList<Entry> all = FXCollections.observableArrayList();
		Model.getInstance().getSpreadsheet().searchEntries(all, row -> {
			if (row.get(ID_NUMBER.ordinal()).isEmpty())
				return false;

			if (genderBoyToggle.isSelected()) {
				if (!row.get(GENDER.ordinal()).equalsIgnoreCase("Boy")) {
					return false;
				}
			}
			if (genderGirlToggle.isSelected()) {
				if (!row.get(GENDER.ordinal()).equalsIgnoreCase("Girl")) {
					return false;
				}
			}
			if (lastNameSelectToggle.isSelected()) {
				if (!row.get(LAST_NAME.ordinal()).equalsIgnoreCase(lastNameSelect.getText())
						&& !row.get(LAST_NAME_YIDDISH.ordinal()).equals(lastNameSelect.getText()))
					return false;
			}
			if (lastNameRangeToggle.isSelected()) {
				if (!(row.get(LAST_NAME.ordinal()).compareToIgnoreCase(lastNameFrom.getText()) >= 0
						&& row.get(LAST_NAME.ordinal()).compareToIgnoreCase(lastNameTo.getText()) <= 0)
						&& !(row.get(LAST_NAME_YIDDISH.ordinal()).compareTo(lastNameFrom.getText()) >= 0
								&& row.get(LAST_NAME_YIDDISH.ordinal()).compareTo(lastNameTo.getText()) <= 0))
					return false;
			}
			if (campaignSelectToggle.isSelected()) {
				boolean noMatch = true;

				for (int index : RangeParser.parse(campaignSelect.getText())) {
					String point = row.size() <= FIRST_CAMPAIGN.ordinal() + index - 1 ? ""
							: row.get(FIRST_CAMPAIGN.ordinal() + index - 1);

					if (point.isEmpty() || point.equals("0")) {
						if (campaignAllMatch.isSelected())
							return false;
					} else {
						noMatch = false;
					}
				}
				if (noMatch)
					return false;
			}
			if (memberSinceSelectToggle.isSelected()) {
				LocalDate memberSince = getDate(row.get(CREATED.ordinal()));
				LocalDate selectedDate = memberSinceSelectField.getValue();

				if (!memberSince.equals(selectedDate))
					return false;
			}
			if (memberSinceBeforeToggle.isSelected()) {
				LocalDate memberSince = getDate(row.get(CREATED.ordinal()));
				LocalDate selectedDate = memberSinceBeforeField.getValue();

				if (!memberSince.isBefore(selectedDate))
					return false;
			}
			if (memberSinceFromToggle.isSelected()) {
				LocalDate memberSince = getDate(row.get(CREATED.ordinal()));
				LocalDate selectedDate = memberSinceFromField.getValue();

				if (memberSince.isBefore(selectedDate))
					return false;
			}
			if (pointsMinimumToggle.isSelected()) {
				if (Integer.parseInt(row.get(TOTAL_POINTS.ordinal())) < pointsMinimum.getValue())
					return false;
			}

			return true;
		});

		Model.getInstance().getSpreadsheet().searchService().setOnSucceeded(evt -> {

			Model.getInstance().getSpreadsheet().searchService().setOnSucceeded(null);

			all.removeIf(entry -> {
				boolean remove = false;

				if (shavuosSelectToggle.isSelected()) {
					remove |= entry.getShavuosData(shavuosSelect.getValue() - Main.FIRST_SHAVUOS_YEAR) == 0;
				}
				if (pointsRangeToggle.isSelected()) {
					remove |= entry.getTotal() < pointsFrom.getValue() || entry.getTotal() > pointsTo.getValue();
				}

				return remove;
			});

			Map<String, ObservableList<Entry>> map = new LinkedHashMap<>();

			for (Tab t : Tab.namedCities()) {
				ObservableList<Entry> list = FXCollections.observableArrayList(
						all.stream().filter(e -> e.getCityYiddish().equals(t.toString())).collect(Collectors.toList()));
				all.removeAll(list);
				map.put(t.toPrettyString(), list);
			}

			map.put(Tab.OTHER_CITIES.toPrettyString(), all);

			ReportResultsView report = new ReportResultsView(map, familyGrouping.isSelected());
			Platform.runLater(() -> Util.createDialog(report, "Reports", ButtonType.OK));
		});

	}
}
