package org.hafotzastehillim.fx;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.hafotzastehillim.fx.notes.DateTimePicker;
import org.hafotzastehillim.fx.notes.Note;
import org.hafotzastehillim.fx.notes.NoteManager;
import org.hafotzastehillim.fx.spreadsheet.Column;
import org.hafotzastehillim.fx.spreadsheet.Entry;
import org.hafotzastehillim.fx.spreadsheet.Tab;
import org.hafotzastehillim.fx.util.EnglishToHebrewKeyInterceptor;
import org.hafotzastehillim.fx.util.Util;
import org.reactfx.value.Var;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyEvent;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;

public class FormController {

	@FXML
	private TextField account;
	@FXML
	private JFXRadioButton boy;
	@FXML
	private JFXRadioButton girl;
	@FXML
	private TextField firstName;
	@FXML
	private TextField lastName;
	@FXML
	private TextField addressNumber;
	@FXML
	private TextField addressName;
	@FXML
	private TextField apt;
	@FXML
	private TextField city;
	@FXML
	private TextField state;
	@FXML
	private TextField zip;
	@FXML
	private Label memberSince;
	@FXML
	private Label memberSinceLabel;
	@FXML
	private Label id;
	@FXML
	private Label idLabel;
	@FXML
	ComboBox<String> cityYiddish;
	@FXML
	private TextField age;
	@FXML
	private TextField school;
	@FXML
	TextField phone;
	@FXML
	private TextField cellPhone;
	@FXML
	private TextField fatherName;
	@FXML
	private TextField firstNameYiddish;
	@FXML
	private TextField lastNameYiddish;

	private Var<Instant> alarm;

	@FXML
	TextArea notes;
	@FXML
	private JFXButton createAlert;
	@FXML
	private Label alarmText;
	@FXML
	private Node due;
	@FXML
	private Node addReminder;

	private List<TextField> fields;
	private Entry entry;

	private ChangeListener<Boolean> genderListener;

	private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM. d, ''yy");
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM. d, yyyy h:mm a")
			.withZone(ZoneId.systemDefault());
	private static final HebrewDateFormatter HEBREW_FORMATTER = new HebrewDateFormatter();
	static {
		HEBREW_FORMATTER.setHebrewFormat(true);
	}

	@FXML
	public void initialize() {
		fields = Arrays.asList(account, firstName, lastName, addressNumber, addressName, apt, city, state, zip, age,
				school, phone, cellPhone, fatherName, lastNameYiddish, firstNameYiddish);
		fields.forEach(f -> f.focusTraversableProperty().bind(f.editableProperty()));

		createAlert.disableProperty().addListener((obs, ov, nv) -> due
				.setVisible(!createAlert.isDisabled() && getAlarm() != null && !getAlarm().isAfter(Instant.now())));
		alarmProperty().addListener((obs, ov, nv) -> {
			due.setVisible(!createAlert.isDisabled() && nv != null && !nv.isAfter(Instant.now()));
			if (nv != null) {
				JewishCalendar cal = new JewishCalendar(Date.from(nv));
				alarmText.setText(FORMATTER.format(nv) + "\n" + HEBREW_FORMATTER.format(cal));
			}
		});

		createAlert.disableProperty().bind(notes.textProperty().isEmpty());

		cityYiddish.focusTraversableProperty()
				.bind(cityYiddish.getEditor().editableProperty().and(cityYiddish.mouseTransparentProperty().not()));

		cityYiddish.getItems().addAll(Tab.namedCities().stream().map(t -> t.toString()).collect(Collectors.toList()));

		genderListener = (obs, ov, nv) -> {
			if (nv) {
				if (boy.isSelected())
					entry.setGender("Boy");
				if (girl.isSelected())
					entry.setGender("Girl");
			}
		};

		account.setTextFormatter(new TextFormatter<>(change -> {
			if (change.getText().matches("[\\d]*"))
				return change;
			return null;
		}));

		apt.setTextFormatter(new TextFormatter<>(change -> {
			if (change.isAdded() && change.getText().contains("#")) {
				String old = change.getText();
				String newText = old.replace("#", "");

				change.setText(newText);

				// https://uwesander.de/?p=208
				int diff = newText.length() - old.length();
				if (diff != 0) {
					change.setAnchor(Math.max(0, change.getAnchor() + diff));
					change.setCaretPosition(Math.max(0, change.getCaretPosition() + diff));
				}
			}

			return change;
		}));

		phone.setTextFormatter(Util.asYouTypePhoneFormatter());
		cellPhone.setTextFormatter(Util.asYouTypePhoneFormatter());

		phone.setOnAction(evt -> {
			if (phone.getText().isEmpty())
				return;

			if (entry != null)
				return;

			ObjectProperty<Entry> consumer = new SimpleObjectProperty<>();
			consumer.addListener((obs, ov, nv) -> {
				Util.createAlert(AlertType.INFORMATION, "Information", "Notice",
						"Please use the new + tab to add siblings");
			});

			Model.getInstance().getSpreadsheet().findEntry(phone.getText().replaceAll("[^\\d]", ""), consumer,
					(q, v, c) -> q.equals(v), Column.PHONE.ordinal());
		});

		EventHandler<KeyEvent> hebrew = new EnglishToHebrewKeyInterceptor();

		firstNameYiddish.setOnKeyTyped(hebrew);
		lastNameYiddish.setOnKeyTyped(hebrew);
		fatherName.setOnKeyTyped(hebrew);
		school.setOnKeyTyped(hebrew);
		age.setOnKeyTyped(hebrew);
		
		addReminder.visibleProperty().bind(due.visibleProperty().not());

		Util.selectOnFocus(notes);
	}

	public void setEntry(Entry e) {
		detach();

		memberSince.setVisible(e != null);
		memberSinceLabel.setVisible(e != null);
		id.setVisible(e != null && !e.getId().isEmpty());
		idLabel.setVisible(e != null && !e.getId().isEmpty());

		if (e == null)
			return;

		entry = e;

		account.textProperty().bindBidirectional(entry.accountProperty());
		boy.setSelected(entry.getGender().equalsIgnoreCase("Boy"));
		girl.setSelected(entry.getGender().equalsIgnoreCase("Girl"));
		firstName.textProperty().bindBidirectional(entry.firstNameProperty());
		lastName.textProperty().bindBidirectional(entry.lastNameProperty());
		addressNumber.textProperty().bindBidirectional(entry.addressNumberProperty());
		addressName.textProperty().bindBidirectional(entry.addressNameProperty());
		apt.textProperty().bindBidirectional(entry.aptProperty());
		city.textProperty().bindBidirectional(entry.cityProperty());
		state.textProperty().bindBidirectional(entry.stateProperty());
		zip.textProperty().bindBidirectional(entry.zipProperty());
		age.textProperty().bindBidirectional(entry.ageProperty());
		school.textProperty().bindBidirectional(entry.schoolProperty());
		phone.textProperty().bindBidirectional(entry.phoneProperty());
		cellPhone.textProperty().bindBidirectional(entry.cellPhoneProperty());
		fatherName.textProperty().bindBidirectional(entry.fatherNameProperty());
		lastNameYiddish.textProperty().bindBidirectional(entry.lastNameYiddishProperty());
		firstNameYiddish.textProperty().bindBidirectional(entry.firstNameYiddishProperty());
		cityYiddish.valueProperty().bindBidirectional(entry.cityYiddishProperty());

		boy.selectedProperty().addListener(genderListener);
		girl.selectedProperty().addListener(genderListener);

		cityYiddish.setMouseTransparent(!cityYiddish.getSelectionModel().isEmpty());

		Note n = NoteManager.getInstance().getNote(entry.getPhone());
		if (n != null) {
			notes.textProperty().bindBidirectional(n.noteProperty());
			alarmProperty().bindBidirectional(n.alarmProperty());
			phone.textProperty().bindBidirectional(n.phoneProperty());
		}

		Instant created = entry.getCreatedInstant();

		memberSince.setText(SHORT_DATE_FORMATTER.format(created.atZone(ZoneId.systemDefault())) + "\t"
				+ HEBREW_FORMATTER.format(new JewishDate(Date.from(created))));
		id.setText(entry.getId());

	}

	private void detach() {
		if (entry == null)
			return;

		account.textProperty().unbindBidirectional(entry.accountProperty());
		firstName.textProperty().unbindBidirectional(entry.firstNameProperty());
		lastName.textProperty().unbindBidirectional(entry.lastNameProperty());
		addressNumber.textProperty().unbindBidirectional(entry.addressNumberProperty());
		addressName.textProperty().unbindBidirectional(entry.addressNameProperty());
		apt.textProperty().unbindBidirectional(entry.aptProperty());
		city.textProperty().unbindBidirectional(entry.cityProperty());
		state.textProperty().unbindBidirectional(entry.stateProperty());
		zip.textProperty().unbindBidirectional(entry.zipProperty());
		age.textProperty().unbindBidirectional(entry.ageProperty());
		school.textProperty().unbindBidirectional(entry.schoolProperty());
		phone.textProperty().unbindBidirectional(entry.phoneProperty());
		cellPhone.textProperty().unbindBidirectional(entry.cellPhoneProperty());
		fatherName.textProperty().unbindBidirectional(entry.fatherNameProperty());
		lastNameYiddish.textProperty().unbindBidirectional(entry.lastNameYiddishProperty());
		firstNameYiddish.textProperty().unbindBidirectional(entry.firstNameYiddishProperty());
		cityYiddish.valueProperty().unbindBidirectional(entry.cityYiddishProperty());

		boy.selectedProperty().removeListener(genderListener);
		girl.selectedProperty().removeListener(genderListener);

		Note n = NoteManager.getInstance().getNote(entry.getPhone());
		if (n != null) {
			notes.textProperty().unbindBidirectional(n.noteProperty());
			alarmProperty().unbindBidirectional(n.alarmProperty());
		}

		memberSince.setText("");
		id.setText("");
	}

	public Entry getEntry() {
		if (entry != null) {
			return entry;
		}

		Entry e = new Entry(Model.getInstance().getSpreadsheet());

		if (phone.getText().isEmpty() || cityYiddish.getValue().isEmpty())
			throw new IllegalStateException("Required field.");

		e.setAccount(account.getText());
		e.setFirstName(firstName.getText());
		e.setLastName(lastName.getText());
		e.setAddressNumber(addressNumber.getText());
		e.setAddressName(addressName.getText());
		e.setApt(apt.getText());
		e.setCity(city.getText());
		e.setState(state.getText());
		e.setZip(zip.getText());
		e.setAge(age.getText());
		e.setSchool(school.getText());
		e.setPhone(phone.getText());
		e.setCellPhone(cellPhone.getText());
		e.setFatherName(fatherName.getText());
		e.setLastNameYiddish(lastNameYiddish.getText());
		e.setFirstNameYiddish(firstNameYiddish.getText());

		e.setGender(boy.isSelected() ? "Boy" : girl.isSelected() ? "Girl" : "");
		e.setCityYiddish(cityYiddish.getValue());

		return e;
	}

	public Var<Instant> alarmProperty() {
		if (alarm == null) {
			alarm = Var.newSimpleVar(null);
		}
		return alarm;
	}

	public final Instant getAlarm() {
		return alarmProperty().getValue();
	}

	public final void setAlarm(Instant alarm) {
		alarmProperty().setValue(alarm);
	}

	@FXML
	private void createAlertAction(ActionEvent evt) {
		DateTimePicker picker = new DateTimePicker();
		picker.setValue(getAlarm());
		if (getAlarm() == null || due.isVisible()) {
			picker.showValue(LocalDate.now().plusDays(1).atTime(LocalTime.MIDNIGHT).toInstant(ZoneOffset.of("-5")));
		}

		Util.createDialog(picker, "Reminder");
		setAlarm(picker.getValue());
	}

}
