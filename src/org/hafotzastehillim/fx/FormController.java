package org.hafotzastehillim.fx;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.hafotzastehillim.fx.spreadsheet.Column;
import org.hafotzastehillim.fx.spreadsheet.Entry;
import org.hafotzastehillim.fx.spreadsheet.Tab;
import org.hafotzastehillim.fx.util.EnglishToHebrewKeyInterceptor;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.jfoenix.controls.JFXRadioButton;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;

public class FormController {

	@FXML
	private TextField id;
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
	ComboBox<String> cityYiddish;
	@FXML
	private TextField age;
	@FXML
	private TextField school;
	@FXML
	private TextField phone;
	@FXML
	private TextField fatherName;
	@FXML
	private TextField firstNameYiddish;
	@FXML
	private TextField lastNameYiddish;

	private List<TextField> fields;
	private Entry entry;

	private ChangeListener<Boolean> genderListener;

	private AsYouTypeFormatter phoneFormatter;

	@FXML
	public void initialize() {
		fields = Arrays.asList(id, firstName, lastName, addressNumber, addressName, apt, city, state, zip, age, school,
				phone, fatherName, lastNameYiddish, firstNameYiddish);

		cityYiddish.getItems().addAll(Tab.namedCities().stream().map(t -> t.toString()).collect(Collectors.toList()));

		genderListener = (obs, ov, nv) -> {
			if (nv) {
				if (boy.isSelected())
					entry.setGender("Boy");
				if (girl.isSelected())
					entry.setGender("Girl");
			}
		};

		phoneFormatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter("US");

		phone.setTextFormatter(new TextFormatter<Integer>(change -> {
			if (!change.isContentChange())
				return change;

			String originalText = change.getControlNewText();
			String newText = originalText.replaceAll("[^\\d]", "");

			phoneFormatter.clear();
			for (char c : newText.toCharArray())
				newText = phoneFormatter.inputDigit(c);

			change.setRange(0, change.getControlText().length());
			change.setText(newText);

			// https://uwesander.de/?p=208
			int diff = newText.length() - originalText.length();
			if (diff != 0) {
				change.setAnchor(Math.max(0, change.getAnchor() + diff));
				change.setCaretPosition(Math.max(0, change.getCaretPosition() + diff));
			}

			return change;
		}));

		phone.setOnAction(evt -> {
			if (phone.getText().isEmpty())
				return;

			if (entry != null)
				return;

			ObjectProperty<Entry> consumer = new SimpleObjectProperty<>();
			consumer.addListener((obs, ov, nv) -> {
				Entry newEntry = new Entry(Model.getInstance().getSpreadsheet());
				newEntry.setTab(nv.getTab());

				newEntry.setLastName(nv.getLastName());
				newEntry.setAddressNumber(nv.getAddressNumber());
				newEntry.setAddressName(nv.getAddressName());
				newEntry.setApt(nv.getApt());
				newEntry.setCity(nv.getCity());
				newEntry.setState(nv.getState());
				newEntry.setZip(nv.getZip());
				newEntry.setPhone(nv.getPhone());
				newEntry.setFatherName(nv.getFatherName());
				newEntry.setLastNameYiddish(nv.getLastNameYiddish());
				newEntry.setCityYiddish(nv.getCityYiddish());

				setEntry(newEntry);
			});

			Model.getInstance().getSpreadsheet().findEntry(phone.getText().replaceAll("[^\\d]", ""), consumer,
					(q, v, c) -> q.equals(v), Column.PHONE.ordinal());
		});

		phone.sceneProperty().addListener((obs, ov, nv) -> {
			if (nv != null && entry == null) {
				phone.requestFocus();
			}
		});

		EventHandler<KeyEvent> hebrew = new EnglishToHebrewKeyInterceptor();

		firstNameYiddish.setOnKeyTyped(hebrew);
		lastNameYiddish.setOnKeyTyped(hebrew);
		fatherName.setOnKeyTyped(hebrew);
		school.setOnKeyTyped(hebrew);
		age.setOnKeyTyped(hebrew);
		cityYiddish.getEditor().setOnKeyTyped(hebrew);
	}

	private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, ''yy");
	private static final HebrewDateFormatter HEBREW_FORMATTER = new HebrewDateFormatter();
	static {
		HEBREW_FORMATTER.setHebrewFormat(true);
	}

	public void setEntry(Entry e) {
		detach();

		memberSince.setVisible(e != null);
		memberSinceLabel.setVisible(e != null);

		if (e == null)
			return;

		entry = e;

		boy.setSelected(entry.getGender().equalsIgnoreCase("Boy"));
		girl.setSelected(entry.getGender().equalsIgnoreCase("Girl"));
		id.textProperty().bindBidirectional(entry.idProperty());
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
		fatherName.textProperty().bindBidirectional(entry.fatherNameProperty());
		lastNameYiddish.textProperty().bindBidirectional(entry.lastNameYiddishProperty());
		firstNameYiddish.textProperty().bindBidirectional(entry.firstNameYiddishProperty());
		cityYiddish.valueProperty().bindBidirectional(entry.cityYiddishProperty());

		boy.selectedProperty().addListener(genderListener);
		girl.selectedProperty().addListener(genderListener);

		cityYiddish.setMouseTransparent(!cityYiddish.getSelectionModel().isEmpty());

		Instant created = entry.getCreatedInstant();

		memberSince.setText(SHORT_DATE_FORMATTER.format(created.atZone(ZoneId.systemDefault())) + "\t"
				+ HEBREW_FORMATTER.format(new JewishDate(Date.from(created))));
	}

	private void detach() {
		if (entry == null)
			return;

		id.textProperty().unbindBidirectional(entry.idProperty());
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
		fatherName.textProperty().unbindBidirectional(entry.fatherNameProperty());
		lastNameYiddish.textProperty().unbindBidirectional(entry.lastNameYiddishProperty());
		firstNameYiddish.textProperty().unbindBidirectional(entry.firstNameYiddishProperty());
		cityYiddish.valueProperty().unbindBidirectional(entry.cityYiddishProperty());

		boy.selectedProperty().removeListener(genderListener);
		girl.selectedProperty().removeListener(genderListener);
	}

	public Entry getEntry() {
		if (entry != null) {
			return entry;
		}

		Entry e = new Entry(Model.getInstance().getSpreadsheet());

		if (cityYiddish.getValue().isEmpty())
			throw new IllegalStateException("Required field.");

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
		e.setFatherName(fatherName.getText());
		e.setLastNameYiddish(lastNameYiddish.getText());
		e.setFirstNameYiddish(firstNameYiddish.getText());

		e.setGender(boy.isSelected() ? "Boy" : girl.isSelected() ? "Girl" : "");
		e.setCityYiddish(cityYiddish.getValue());

		return e;
	}
}
