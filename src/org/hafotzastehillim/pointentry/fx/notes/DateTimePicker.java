package org.hafotzastehillim.pointentry.fx.notes;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.hafotzastehillim.pointentry.fx.util.DialogUtils;
import org.hafotzastehillim.pointentry.fx.util.Util;
import org.scenicview.ScenicView;

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
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar.ButtonData;
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
			DialogUtils.showErrorDialog(e);
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
	private JFXButton noReminder;

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
		
		DialogUtils.dialogButtonStyling(ok);
		DialogUtils.dialogButtonStyling(noReminder);
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

	@FXML
	private void okAction(ActionEvent evt) {
		if (datePicker.getValue() == null || timePicker.getValue() == null) {
			setValue(null);
		}

		setValue(datePicker.getValue().atTime(timePicker.getValue()).toInstant(ZoneOffset.of("-5")));
	}

	@FXML
	private void noReminderAction(ActionEvent evt) {
		setValue(null);
	}

	public static Instant show(Instant initialValue, Instant showValue) {
		DateTimePicker picker = new DateTimePicker();
		picker.setValue(initialValue);
		picker.showValue(showValue);
		
		picker.ok.addEventHandler(ActionEvent.ACTION, evt -> ((Stage)picker.getScene().getWindow()).close());
		picker.noReminder.addEventHandler(ActionEvent.ACTION, evt -> ((Stage)picker.getScene().getWindow()).close());

		DialogUtils.createDialog(picker, "Reminder", picker.validValueProperty().not());
		return picker.getValue();
	}

	public static Instant show(Instant initialValue) {
		if (initialValue == null || !initialValue.isAfter(Instant.now()))
			return show(initialValue,
					LocalDate.now().plusDays(1).atTime(LocalTime.MIDNIGHT).atZone(ZoneOffset.systemDefault()).toInstant());

		return show(initialValue, initialValue);
	}
}
