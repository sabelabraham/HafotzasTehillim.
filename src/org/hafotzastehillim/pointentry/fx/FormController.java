package org.hafotzastehillim.pointentry.fx;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hafotzastehillim.pointentry.fx.notes.DateTimePicker;
import org.hafotzastehillim.pointentry.fx.notes.Note;
import org.hafotzastehillim.pointentry.fx.notes.NoteManager;
import org.hafotzastehillim.pointentry.fx.util.DialogUtils;
import org.hafotzastehillim.pointentry.fx.util.EnglishToHebrewKeyInterceptor;
import org.hafotzastehillim.pointentry.fx.util.Util;
import org.hafotzastehillim.pointentry.spreadsheet.Column;
import org.hafotzastehillim.pointentry.spreadsheet.Entry;
import org.hafotzastehillim.pointentry.spreadsheet.Tab;
import org.reactfx.value.Val;
import org.reactfx.value.Var;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
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
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
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
	private Label modified;
	@FXML
	private Label modifiedLabel;
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
	TextField cellPhone;
	@FXML
	private TextField fatherName;
	@FXML
	private TextField firstNameYiddish;
	@FXML
	private TextField lastNameYiddish;

	private Var<Instant> alarm;

	private Val<Boolean> due;

	@FXML
	TextArea notes;
	@FXML
	private JFXButton createAlert;
	@FXML
	private Label alarmText;
	@FXML
	private Node alarmIcon;

	@FXML
	ToggleButton delete;
	@FXML
	private Label deleteLabel;

	private List<TextField> fields;
	private Entry entry;

	private ChangeListener<Boolean> genderListener;

	private static final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
	private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM. d, ''yy");
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM. d, ''yy \t hh:mma");
	private static final DateTimeFormatter LONG_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM. d, yyyy h:mm a")
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

		due = alarmProperty().map(a -> !a.isAfter(Instant.now()));
		
		alarmIcon.styleProperty().bind(due.map(d -> d ? "-fx-fill: red;" : "-fx-fill: aaaaaa;").orElseConst(null));
		alarmText.textProperty().bind(alarmProperty().map(a -> {
			JewishCalendar cal = new JewishCalendar(Date.from(a));
			return LONG_DATE_FORMATTER.format(a) + "\n" + HEBREW_FORMATTER.format(cal);
		}).orElseConst(""));
		
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

				Util.resetCaret(change, old);
			}

			return change;
		}));

		phone.setTextFormatter(Util.asYouTypePhoneFormatter());
		cellPhone.setTextFormatter(Util.asYouTypePhoneFormatter());

		Function<String, Boolean> validPhone = str -> {
			if (str.isEmpty())
				return true;

			try {
				PhoneNumber p = phoneUtil.parse(str, "US");
				if (!phoneUtil.isValidNumber(p))
					return false;
			} catch (NumberParseException e) {
				return false;
			}

			return true;
		};

		Util.validateField(phone, validPhone);
		Util.validateField(cellPhone, validPhone);

		phone.setOnAction(evt -> {
			if (phone.getText().isEmpty())
				return;

			if (!cityYiddish.getEditor().getText().isEmpty())
				return;

			ObjectProperty<Entry> consumer = new SimpleObjectProperty<>();
			consumer.addListener((obs, ov, nv) -> {
				DialogUtils.createAlert(AlertType.INFORMATION, "Match Found", "Match Found",
						"An existing family has this phone number.\nPlease use the + tab to add siblings");
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

		Util.selectOnFocus(notes);

		deleteLabel.visibleProperty().bind(delete.visibleProperty().and(delete.selectedProperty()));
		delete.setVisible(false);
	}

	public void setEntry(Entry e) {
		detach();

		memberSince.setVisible(e != null);
		memberSinceLabel.setVisible(e != null);
		modified.setVisible(e != null);
		modifiedLabel.setVisible(e != null);
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
		Instant mod = entry.getModifiedInstant();

		memberSince.setText(SHORT_DATE_FORMATTER.format(created.atZone(ZoneId.systemDefault())) + "\t"
				+ HEBREW_FORMATTER.format(new JewishDate(Date.from(created))));
		modified.setText(
				DATE_TIME_FORMATTER.format(mod.atZone(ZoneId.systemDefault())).replace("AM", "am").replace("PM", "pm"));

		id.setText(entry.getId());

		delete.setVisible(entry.getRow() >= 0);
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
		modified.setText("");
		id.setText("");

		delete.setVisible(false);
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
		setAlarm(DateTimePicker.show(getAlarm()));
	}
}
