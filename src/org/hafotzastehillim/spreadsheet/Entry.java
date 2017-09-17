package org.hafotzastehillim.spreadsheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.hafotzastehillim.fx.Util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class Entry {
	private Spreadsheet sheet;
	private int tab = -1;
	private int row = -1;

	private StringProperty id;
	private StringProperty gender;
	private StringProperty firstName;
	private StringProperty lastName;
	private StringProperty addressNumber;
	private StringProperty addressName;
	private StringProperty apt;
	private StringProperty city;
	private StringProperty state;
	private StringProperty zip;
	private StringProperty phone;
	private StringProperty firstNameYiddish;
	private StringProperty lastNameYiddish;
	private StringProperty fatherName;
	private StringProperty school;
	private StringProperty age;
	private StringProperty cityYiddish;

	private ObservableList<Integer> points;
	private ReadOnlyIntegerWrapper total;
	private ReadOnlyBooleanWrapper detailsChanged;

	private Entry() {
		points = FXCollections.observableArrayList();
		total = new ReadOnlyIntegerWrapper();

		points.addListener((ListChangeListener.Change<? extends Integer> change) -> {
			total.set(points.stream().mapToInt(i -> i == null ? 0 : i.intValue()).sum());
		});

		detailsChanged = new ReadOnlyBooleanWrapper(this, "detailsChanged", false);
	}

	public Entry(Spreadsheet sheet) {
		this();
		this.sheet = sheet;
	}

	public Entry(Spreadsheet sheet, int tab, int row) {
		this();
		this.tab = tab;
		this.row = row;
		this.sheet = sheet;

		reload();
	}

	public final String getId() {
		return idProperty().get();
	}

	public final String getGender() {

		return genderProperty().get();
	}

	public final String getFirstName() {

		return firstNameProperty().get();
	}

	public final String getLastName() {

		return lastNameProperty().get();
	}

	public final String getAddressNumber() {

		return addressNumberProperty().get();
	}

	public final String getAddressName() {

		return addressNameProperty().get();
	}

	public final String getApt() {

		return aptProperty().get();
	}

	public final String getCity() {

		return cityProperty().get();
	}

	public final String getState() {

		return stateProperty().get();
	}

	public final String getZip() {

		return zipProperty().get();
	}

	public final String getPhone() {

		return phoneProperty().get();
	}

	public final String getFirstNameYiddish() {

		return firstNameYiddishProperty().get();
	}

	public final String getLastNameYiddish() {

		return lastNameYiddishProperty().get();
	}

	public final String getFatherName() {

		return fatherNameProperty().get();
	}

	public final String getSchool() {

		return schoolProperty().get();
	}

	public final String getAge() {

		return ageProperty().get();
	}

	public final String getCityYiddish() {

		return cityYiddishProperty().get();
	}

	public boolean isDetailsChanged() {
		return detailsChangedProperty().get();
	}

	public final void setId(String str) {
		idProperty().set(str);
	}

	public final void setGender(String str) {
		genderProperty().set(str);
	}

	public final void setFirstName(String str) {
		firstNameProperty().set(str);
	}

	public final void setLastName(String str) {
		lastNameProperty().set(str);
	}

	public final void setAddressNumber(String str) {
		addressNumberProperty().set(str);
	}

	public final void setAddressName(String str) {
		addressNameProperty().set(str);
	}

	public final void setApt(String str) {
		aptProperty().set(str);
	}

	public final void setCity(String str) {
		cityProperty().set(str);
	}

	public final void setState(String str) {
		stateProperty().set(str);
	}

	public final void setZip(String str) {
		zipProperty().set(str);
	}

	public final void setPhone(String str) {
		phoneProperty().set(str);
	}

	public final void setFirstNameYiddish(String str) {
		firstNameYiddishProperty().set(str);
	}

	public final void setLastNameYiddish(String str) {
		lastNameYiddishProperty().set(str);
	}

	public final void setFatherName(String str) {
		fatherNameProperty().set(str);
	}

	public final void setSchool(String str) {
		schoolProperty().set(str);
	}

	public final void setAge(String str) {
		ageProperty().set(str);
	}

	public final void setCityYiddish(String str) {
		cityYiddishProperty().set(str);
	}

	public StringProperty idProperty() {
		if(id == null) {
			id = new SimpleStringProperty(this, "id", "");
			id.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return id;
	}

	public StringProperty genderProperty() {
		if(gender == null) {
			gender = new SimpleStringProperty(this, "gender", "");
			gender.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return gender;
	}

	public StringProperty firstNameProperty() {
		if(firstName == null) {
			firstName = new SimpleStringProperty(this, "firstName", "");
			firstName.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return firstName;
	}

	public StringProperty lastNameProperty() {
		if(lastName == null) {
			lastName = new SimpleStringProperty(this, "lastName", "");
			lastName.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return lastName;
	}

	public StringProperty addressNumberProperty() {
		if(addressNumber == null) {
			addressNumber = new SimpleStringProperty(this, "addressNumber", "");
			addressNumber.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return addressNumber;
	}

	public StringProperty addressNameProperty() {
		if(addressName == null) {
			addressName = new SimpleStringProperty(this, "addressName", "");
			addressName.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return addressName;
	}

	public StringProperty aptProperty() {
		if(apt == null) {
			apt = new SimpleStringProperty(this, "apt", "");
			apt.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return apt;
	}

	public StringProperty cityProperty() {
		if(city == null) {
			city = new SimpleStringProperty(this, "city", "");
			city.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return city;
	}

	public StringProperty stateProperty() {
		if(state == null) {
			state = new SimpleStringProperty(this, "state", "");
			state.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return state;
	}

	public StringProperty zipProperty() {
		if(zip == null) {
			zip = new SimpleStringProperty(this, "zip", "");
			zip.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return zip;
	}

	public StringProperty phoneProperty() {
		if(phone == null) {
			phone = new SimpleStringProperty(this, "phone", "");
			phone.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return phone;
	}

	public StringProperty firstNameYiddishProperty() {
		if(firstNameYiddish == null) {
			firstNameYiddish = new SimpleStringProperty(this, "firstNameYiddish", "");
			firstNameYiddish.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return firstNameYiddish;
	}

	public StringProperty lastNameYiddishProperty() {
		if(lastNameYiddish == null) {
			lastNameYiddish = new SimpleStringProperty(this, "lastNameYiddish", "");
			lastNameYiddish.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return lastNameYiddish;
	}

	public StringProperty fatherNameProperty() {
		if(fatherName == null) {
			fatherName = new SimpleStringProperty(this, "fatherName", "");
			fatherName.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return fatherName;
	}

	public StringProperty schoolProperty() {
		if(school == null) {
			school = new SimpleStringProperty(this, "school", "");
			school.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return school;
	}

	public StringProperty ageProperty() {
		if(age == null) {
			age = new SimpleStringProperty(this, "age", "");
			age.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return age;
	}

	public StringProperty cityYiddishProperty() {
		if(cityYiddish == null) {
			cityYiddish = new SimpleStringProperty(this, "cityYiddish", "");
			cityYiddish.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return cityYiddish;
	}

	public ReadOnlyBooleanProperty detailsChangedProperty() {
		return detailsChanged.getReadOnlyProperty();
	}

	private static final PhoneNumberUtil util = PhoneNumberUtil.getInstance();

	public void reload() {
		if (tab == -1) {
			throw new IllegalStateException("Tab value not set.");
		}
		if (row == -1) {
			throw new IllegalStateException("Row value not set.");
		}

		List<String> data = sheet.getRow(tab, row);

		// format phone number
		if (data.size() > Column.PHONE.getColumn()) {
			String phone = data.get(Column.PHONE.getColumn());
			try {
				phone = util.format(util.parse(phone, "US"), PhoneNumberFormat.NATIONAL);
				data.set(Column.PHONE.getColumn(), phone);
			} catch (NumberParseException e) {
				e.printStackTrace();
			}
		}

		setData(data);

		detailsChanged.set(false);
	}

	public void saveDetails() {
		if (tab == -1) {
			throw new IllegalStateException("Tab value not set.");
		}

		if (!detailsChanged.get())
			return;

		List<String> data = getDetails();

		// un-format phone
		if (data.size() > Column.PHONE.getColumn()) {
			String phone = data.get(Column.PHONE.getColumn()).replaceAll("[^\\d]", "");
			data.set(Column.PHONE.getColumn(), phone);
		}

		if (row >= 0) {
			sheet.updateRow(tab, row, data);
		} else {
			row = sheet.addRow(tab, data);
			sheet.setCellValue(tab, row, Column.TOTAL_POINTS.getColumn(),
					"=SUM(" + Column.toName(Column.FIRST_CAMPAIGN.getColumn()) + (row + 1) + ":AAI" + (row + 1) + ")");
			overwriteConcurrentPointsWithLocalValues();
		}
	}

	public void overwriteConcurrentPointsWithLocalValues() {
		if (tab == -1) {
			throw new IllegalStateException("Tab value not set.");
		}
		if (row == -1) {
			throw new IllegalStateException("Row value not set.");
		}

		for (int i = 0; i < points.size(); i++) {
			if (points.get(i) == 0) {
				sheet.setCellValue(tab, row, Column.FIRST_CAMPAIGN.getColumn() + i, "");
			} else {
				sheet.setCellValue(tab, row, Column.FIRST_CAMPAIGN.getColumn() + i, points.get(i));
			}
		}
	}

	public int getTab() {
		return tab;
	}

	public void setTab(int t) {
		tab = t;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int r) {
		row = r;
	}

	public ObservableList<Integer> getPoints() {
		return points;
	}

	private void setPoints(List<Integer> p) {
		points.setAll(p);
	}

	public void putPoint(int index, int p) {
		if (tab < 0) {
			throw new IllegalStateException("Tab value not set.");
		}

		if (index < points.size()) {
			points.set(index, p);
		} else {
			while (index > points.size()) {
				points.add(0);
			}

			points.add(p);
		}

		if (row >= 0) {
			if (p <= 0)
				sheet.setCellValue(tab, row, Column.FIRST_CAMPAIGN.getColumn() + index, "");
			else
				sheet.setCellValue(tab, row, Column.FIRST_CAMPAIGN.getColumn() + index, p);
		}
	}

	public int getPoint(int index) {
		if (index < points.size())
			return points.get(index);

		return 0;
	}

	public ReadOnlyIntegerProperty totalProperty() {
		return total.getReadOnlyProperty();
	}

	public int getTotal() {
		return totalProperty().get();
	}

	private void setData(List<String> data) {
		Column[] cols = Column.values();

		for (int i = 0; i < Column.TOTAL_POINTS.getColumn(); i++) {
			set(cols[i], cols[i].getData(data));
		}

		if (data.size() <= Column.FIRST_CAMPAIGN.getColumn()) {
			setPoints(Collections.emptyList());
			return;
		}

		setPoints(data.subList(Column.FIRST_CAMPAIGN.getColumn(), data.size()).stream().mapToInt(cell -> {
			if (cell == null || "".equals(cell))
				return 0;
			return Integer.parseInt(cell);
		}).boxed().collect(Collectors.toList()));
	}

	private List<String> getDetails() {
		List<String> data = new ArrayList<>();
		Column[] cols = Column.values();

		for (int i = 0; i < Column.TOTAL_POINTS.getColumn(); i++) {
			data.add(get(cols[i]));
		}

		data.add("=SUM(" + Column.toName(Column.FIRST_CAMPAIGN.getColumn()) + (row + 1) + ":AAI" + (row + 1) + ")");

		return data;
	}

	public String get(Column column) {
		switch (column) {
		case ID_NUMBER:
			return id.get();
		case GENDER:
			return gender.get();
		case FIRST_NAME:
			return firstName.get();
		case LAST_NAME:
			return lastName.get();
		case ADDRESS_NUMBER:
			return addressNumber.get();
		case ADDRESS_NAME:
			return addressName.get();
		case APT:
			return apt.get();
		case CITY:
			return city.get();
		case STATE:
			return state.get();
		case ZIP:
			return zip.get();
		case CITY_YIDDISH:
			return cityYiddish.get();
		case CLASS:
			return age.get();
		case SCHOOL:
			return school.get();
		case PHONE:
			return phone.get();
		case FATHER_NAME:
			return fatherName.get();
		case LAST_NAME_YIDDISH:
			return lastNameYiddish.get();
		case FIRST_NAME_YIDDISH:
			return firstNameYiddish.get();
		default:
			throw new RuntimeException("Unknown Column");
		}

	}

	public void set(Column column, String data) {
		switch (column) {
		case ID_NUMBER:
			setId(data);
			break;
		case GENDER:
			setGender(data);
			break;
		case FIRST_NAME:
			setFirstName(data);
			break;
		case LAST_NAME:
			setLastName(data);
			break;
		case ADDRESS_NUMBER:
			setAddressNumber(data);
			break;
		case ADDRESS_NAME:
			setAddressName(data);
			break;
		case APT:
			setApt(data);
			break;
		case CITY:
			setCity(data);
			break;
		case STATE:
			setState(data);
			break;
		case ZIP:
			setZip(data);
			break;
		case CITY_YIDDISH:
			setCityYiddish(data);
			break;
		case CLASS:
			setAge(data);
			break;
		case SCHOOL:
			setSchool(data);
			break;
		case PHONE:
			setPhone(data);
			break;
		case FATHER_NAME:
			setFatherName(data);
			break;
		case LAST_NAME_YIDDISH:
			setLastNameYiddish(data);
			break;
		case FIRST_NAME_YIDDISH:
			setFirstNameYiddish(data);
			break;
		default:
			throw new RuntimeException("Unknown Column");
		}
	}

	@Override
	public String toString() {
		return getFirstNameYiddish() + " " + getLastNameYiddish();
	}

}