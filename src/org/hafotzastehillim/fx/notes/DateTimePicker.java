package org.hafotzastehillim.fx.notes;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.hafotzastehillim.fx.util.Util;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class DateTimePicker extends GridPane {

	public DateTimePicker() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/DateTimePicker.fxml"));
			loader.setController(this);
			loader.setRoot(this);
			loader.load();
		} catch (IOException e) {
			Util.showErrorDialog(e);
			return;
		}

		getStyleClass().add("date-time-picker");
		// getStylesheets().add(getClass().getResource("/resources/css/date-time-picker.css").toExternalForm());
	}

	@FXML
	private JFXDatePicker datePicker;
	@FXML
	private JFXTimePicker timePicker;
	@FXML
	private JFXButton ok;
	@FXML
	private JFXButton noAlarm;

	private ObjectProperty<Instant> value;

	private ReadOnlyBooleanWrapper validValue;

	@FXML
	private void initialize() {
		Util.fixDatePicker(datePicker);

		validValue = new ReadOnlyBooleanWrapper(this, "validValue", false);
		validValue.bind(
				Bindings.createBooleanBinding(() -> datePicker.getValue() != null && timePicker.getValue() != null,
						datePicker.valueProperty(), timePicker.valueProperty()));

		ok.disableProperty().bind(validValue.not());
		ok.setDefaultButton(true);
	}

	public void showValue(Instant i) {
		if (i == null) {
			datePicker.setValue(null);
			timePicker.setValue(null);
		} else {
			datePicker.setValue(LocalDate.from(i.atZone(ZoneId.systemDefault())));
			timePicker.setValue(LocalTime.from(i.atZone(ZoneId.systemDefault())));
		}
	}

	public ReadOnlyBooleanProperty validValueProperty() {
		return validValue.getReadOnlyProperty();
	}

	public final boolean isValidValue() {
		return validValueProperty().get();
	}

	@FXML
	private void okAction(ActionEvent evt) {
		if (datePicker.getValue() == null || timePicker.getValue() == null) {
			setValue(null);
		}

		setValue(datePicker.getValue().atTime(timePicker.getValue()).toInstant(ZoneOffset.of("-5")));

		((Stage) getScene().getWindow()).close();
	}

	@FXML
	private void noAlarmAction(ActionEvent evt) {
		setValue(null);
		((Stage) getScene().getWindow()).close();
	}

	public ObjectProperty<Instant> valueProperty() {
		if (value == null) {
			value = new SimpleObjectProperty<>(this, "value");
			value.addListener((obs, ov, nv) -> {
				showValue(nv);
			});
		}
		return value;
	}

	public final Instant getValue() {
		return valueProperty().get();
	}

	public final void setValue(Instant value) {
		valueProperty().set(value);
	}
}
