package org.hafotzastehillim.fx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.hafotzastehillim.fx.spreadsheet.Column;
import org.hafotzastehillim.fx.spreadsheet.Entry;
import org.hafotzastehillim.fx.spreadsheet.Tab;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.jfoenix.controls.JFXRadioButton;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.scene.shape.SVGPath;

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
	ComboBox<Tab> cityYiddish;
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
	private ChangeListener<Tab> cityYiddishListener;

	private AsYouTypeFormatter phoneFormatter;

	@FXML
	public void initialize() {
		fields = Arrays.asList(id, firstName, lastName, addressNumber, addressName, apt, city, state, zip, age, school,
				phone, fatherName, lastNameYiddish, firstNameYiddish);

		cityYiddish.getItems().addAll(Tab.namedCities());

		genderListener = (obs, ov, nv) -> {
			if (nv) {
				if (boy.isSelected())
					entry.setGender("Boy");
				if (girl.isSelected())
					entry.setGender("Girl");
			}
		};
		cityYiddishListener = (obs, ov, nv) -> {
			entry.setCityYiddish(nv.toString());
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
					(q, v, c) -> q.equals(v), Column.PHONE.getColumn());
		});
	}

	public void setEntry(Entry e) {
		detach();

		if (e == null)
			return;

		entry = e;

		boy.setSelected(entry.getGender().equalsIgnoreCase("Boy"));
		girl.setSelected(entry.getGender().equalsIgnoreCase("Girl"));

		if (!entry.getCityYiddish().isEmpty()) {
			cityYiddish.getSelectionModel().select(Tab.getTab(entry.getCityYiddish()));
		}

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

		boy.selectedProperty().addListener(genderListener);
		girl.selectedProperty().addListener(genderListener);

		cityYiddish.getSelectionModel().selectedItemProperty().addListener(cityYiddishListener);
		cityYiddish.setMouseTransparent(!cityYiddish.getSelectionModel().isEmpty());
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

		boy.selectedProperty().removeListener(genderListener);
		girl.selectedProperty().removeListener(genderListener);

		cityYiddish.getSelectionModel().selectedItemProperty().removeListener(cityYiddishListener);
	}

	public Entry getEntry() {
		if (entry != null) {
			if (entry.getId().isEmpty()) // will only happen on sibling copy
				entry.setId("" + Model.getInstance().getSpreadsheet().uniqueId(entry.getTab()));

			return entry;
		}

		Entry e = new Entry(Model.getInstance().getSpreadsheet());

		if (cityYiddish.getSelectionModel().isEmpty())
			throw new IllegalStateException("Required field.");

		e.setTab(cityYiddish.getSelectionModel().getSelectedIndex());

		e.setId("" + Model.getInstance().getSpreadsheet().uniqueId(e.getTab()));
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
		e.setCityYiddish(cityYiddish.getSelectionModel().getSelectedItem().toString());

		System.out.println(e);
		return e;
	}
}
