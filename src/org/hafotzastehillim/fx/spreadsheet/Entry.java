package org.hafotzastehillim.fx.spreadsheet;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.hafotzastehillim.fx.Model;
import org.hafotzastehillim.fx.util.Util;
import org.reactfx.Change;
import org.reactfx.EventStream;
import org.reactfx.Subscription;

import static org.reactfx.EventStreams.*;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.sun.javafx.application.PlatformImpl;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class Entry implements Selectable, Comparable<Entry> {

	public static final String CHECKMARK = "\u2713";

	private Spreadsheet sheet;
	private int tab = -1;
	private int row = -1;
	private int shavuosRow = -1;
	private int giftsRow = -1;

	private StringProperty created;
	private StringProperty modified;

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
	private BooleanProperty selected;

	private ObservableList<Integer> points;
	private ObservableList<Integer> shavuosData;
	private ObservableList<Boolean> giftsReceived;

	private ReadOnlyIntegerWrapper total;
	private ReadOnlyBooleanWrapper detailsChanged;

	private EventStream<Change<String>> detailsChangeStream;
	private EventStream<Object> changeStream;

	private Subscription modificationSubscription;

	private Entry() {
		points = FXCollections.observableArrayList();
		shavuosData = FXCollections.observableArrayList();
		giftsReceived = FXCollections.observableArrayList();

		total = new ReadOnlyIntegerWrapper();

		points.addListener((ListChangeListener.Change<? extends Integer> change) -> {
			total.set(points.stream().mapToInt(i -> i == null ? 0 : i.intValue()).sum());
		});

		detailsChanged = new ReadOnlyBooleanWrapper(this, "detailsChanged", false);

		created();

		detailsChangeStream = merge(changesOf(idProperty()), changesOf(genderProperty()),
				changesOf(firstNameProperty()), changesOf(lastNameProperty()), changesOf(addressNumberProperty()),
				changesOf(addressNameProperty()), changesOf(aptProperty()), changesOf(cityProperty()),
				changesOf(stateProperty()), changesOf(zipProperty()), changesOf(phoneProperty()),
				changesOf(firstNameYiddishProperty()), changesOf(lastNameYiddishProperty()),
				changesOf(fatherNameProperty()), changesOf(schoolProperty()), changesOf(ageProperty()),
				changesOf(cityYiddishProperty()));

		changeStream = merge(detailsChangeStream, changesOf(getPoints()), changesOf(getShavuosData()),
				changesOf(getGiftsReceived()));

		modificationSubscription = changeStream.subscribe(obj -> modified());
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

		modificationSubscription.unsubscribe();

		loadDetails();

		shavuosRow = sheet.getRow(Tab.SHAVUOS.ordinal(), getId(), (q, v, col) -> q.equals(v), 0);
		giftsRow = sheet.getRow(Tab.GIFTS.ordinal(), getId(), (q, v, col) -> q.equals(v), 0);

		loadShavuosData();
		loadGiftsData();

		modificationSubscription.unsubscribe();
	}

	public final String getCreated() {
		return createdProperty().get();
	}

	public Instant getCreatedInstant() {
		return Instant.parse(getCreated());
	}

	public final String getModified() {
		return modifiedProperty().get();
	}

	public Instant getModifiedInstant() {
		return Instant.parse(getModified());
	}

	public final void setCreated(String str) {
		createdProperty().set(str);
	}

	public final void setCreated(long instant) {
		createdProperty().set(Instant.ofEpochMilli(instant).toString());
	}

	public final void setCreated(Instant instant) {
		setCreated(instant.toString());
	}

	public final void created() {
		setCreated(Instant.now());
	}

	public final void setModified(String str) {
		modifiedProperty().set(str);
	}

	public final void setModified(long instant) {
		modifiedProperty().set(Instant.ofEpochMilli(instant).toString());
	}

	public final void setModified(Instant instant) {
		setModified(instant.toString());
	}

	public final void modified() {
		setModified(Instant.now());
	}

	public StringProperty createdProperty() {
		if (created == null) {
			created = new SimpleStringProperty(this, "created", "");
			created.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return created;
	}

	public StringProperty modifiedProperty() {
		if (modified == null) {
			modified = new SimpleStringProperty(this, "modified", "");
			modified.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return modified;
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

	public final boolean isDetailsChanged() {
		return detailsChangedProperty().get();
	}

	public final boolean isSelected() {
		return selectedProperty().get();
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

	public final void setSelected(Boolean bool) {
		selectedProperty().set(bool);
	}

	public StringProperty idProperty() {
		if (id == null) {
			id = new SimpleStringProperty(this, "id", "");
			id.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return id;
	}

	public StringProperty genderProperty() {
		if (gender == null) {
			gender = new SimpleStringProperty(this, "gender", "");
			gender.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return gender;
	}

	public StringProperty firstNameProperty() {
		if (firstName == null) {
			firstName = new SimpleStringProperty(this, "firstName", "");
			firstName.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return firstName;
	}

	public StringProperty lastNameProperty() {
		if (lastName == null) {
			lastName = new SimpleStringProperty(this, "lastName", "");
			lastName.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return lastName;
	}

	public StringProperty addressNumberProperty() {
		if (addressNumber == null) {
			addressNumber = new SimpleStringProperty(this, "addressNumber", "");
			addressNumber.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return addressNumber;
	}

	public StringProperty addressNameProperty() {
		if (addressName == null) {
			addressName = new SimpleStringProperty(this, "addressName", "");
			addressName.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return addressName;
	}

	public StringProperty aptProperty() {
		if (apt == null) {
			apt = new SimpleStringProperty(this, "apt", "");
			apt.addListener((obs, ov, nv) -> detailsChanged.set(true));
			apt.addListener((obs, ov, nv) -> {
				if (!nv.isEmpty() && !nv.contains("#"))
					apt.set("#" + nv);
			});
		}

		return apt;
	}

	public StringProperty cityProperty() {
		if (city == null) {
			city = new SimpleStringProperty(this, "city", "");
			city.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return city;
	}

	public StringProperty stateProperty() {
		if (state == null) {
			state = new SimpleStringProperty(this, "state", "");
			state.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return state;
	}

	public StringProperty zipProperty() {
		if (zip == null) {
			zip = new SimpleStringProperty(this, "zip", "");
			zip.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return zip;
	}

	public StringProperty phoneProperty() {
		if (phone == null) {
			phone = new SimpleStringProperty(this, "phone", "");
			phone.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return phone;
	}

	public StringProperty firstNameYiddishProperty() {
		if (firstNameYiddish == null) {
			firstNameYiddish = new SimpleStringProperty(this, "firstNameYiddish", "");
			firstNameYiddish.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return firstNameYiddish;
	}

	public StringProperty lastNameYiddishProperty() {
		if (lastNameYiddish == null) {
			lastNameYiddish = new SimpleStringProperty(this, "lastNameYiddish", "");
			lastNameYiddish.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return lastNameYiddish;
	}

	public StringProperty fatherNameProperty() {
		if (fatherName == null) {
			fatherName = new SimpleStringProperty(this, "fatherName", "");
			fatherName.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return fatherName;
	}

	public StringProperty schoolProperty() {
		if (school == null) {
			school = new SimpleStringProperty(this, "school", "");
			school.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return school;
	}

	public StringProperty ageProperty() {
		if (age == null) {
			age = new SimpleStringProperty(this, "age", "");
			age.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return age;
	}

	public StringProperty cityYiddishProperty() {
		if (cityYiddish == null) {
			cityYiddish = new SimpleStringProperty(this, "cityYiddish", "");
			cityYiddish.addListener((obs, ov, nv) -> detailsChanged.set(true));
		}

		return cityYiddish;
	}

	public ReadOnlyBooleanProperty detailsChangedProperty() {
		return detailsChanged.getReadOnlyProperty();
	}

	public BooleanProperty selectedProperty() {
		if (selected == null) {
			selected = new SimpleBooleanProperty(this, "selected", false);
		}

		return selected;
	}

	private static final PhoneNumberUtil util = PhoneNumberUtil.getInstance();

	private void loadShavuosData() {
		if (shavuosRow >= 0) {
			List<String> data = sheet.getRow(Tab.SHAVUOS.ordinal(), shavuosRow);
			setShavuosData(data.subList(1, data.size()).stream().map(i -> i.isEmpty() ? 0 : Integer.parseInt(i))
					.collect(Collectors.toList()));
		}
	}

	private void loadGiftsData() {
		if (giftsRow >= 0) {
			List<String> data = sheet.getRow(Tab.GIFTS.ordinal(), giftsRow);
			setGiftsReceived(data.subList(1, data.size()).stream().map(str -> str.equals(CHECKMARK))
					.collect(Collectors.toList()));
		}
	}

	private void loadDetails() {
		if (tab == -1) {
			throw new IllegalStateException("Tab value not set.");
		}
		if (row == -1) {
			throw new IllegalStateException("Row value not set.");
		}

		List<String> data = sheet.getRow(tab, row);

		// format phone number
		if (data.size() > Column.PHONE.ordinal()) {
			String phone = data.get(Column.PHONE.ordinal());
			try {
				PhoneNumber pn = util.parse(phone, "US");
				phone = util.format(pn, PhoneNumberFormat.NATIONAL);

				data.set(Column.PHONE.ordinal(), phone);
				if (!Model.getInstance().isIgnoreInvalidPhone() && !util.isValidNumber(pn)) {
					AsYouTypeFormatter fmt = util.getAsYouTypeFormatter("US");
					String input = "";
					for (char c : phone.toCharArray())
						input = fmt.inputDigit(c);

					String p = input;
					Util.createAlert(AlertType.WARNING, "Invalid Phone", "Invalid Phone Number",
							"Entry \"" + data.get(Column.ID_NUMBER.ordinal()) + "\" has an invalid phone number\n" + p,
							ButtonType.OK);
				}

			} catch (NumberParseException e) {
				if (!Model.getInstance().isIgnoreInvalidPhone()) {
					Util.createAlert(AlertType.WARNING, "Missing Phone", "Missing Phone Number",
							"Entry \"" + data.get(Column.ID_NUMBER.ordinal()) + "\" phone number is empty",
							ButtonType.OK);
				}
			}
		}

		setData(data);
		detailsChanged.set(false);
	}

	public void reload() {
		modificationSubscription.unsubscribe();

		loadDetails();
		loadShavuosData();
		loadGiftsData();

		modificationSubscription = changeStream.subscribe(obj -> modified());
	}

	public void saveDetails() {
		if (tab == -1) {
			throw new IllegalStateException("Tab value not set.");
		}
		if (row == -1) {
			throw new IllegalStateException("Row value not set.");
		}

		if (!detailsChanged.get())
			return;

		List<String> data = getDetails();

		// un-format phone
		if (data.size() > Column.PHONE.ordinal()) {
			String phone = data.get(Column.PHONE.ordinal()).replaceAll("[^\\d]", "");
			data.set(Column.PHONE.ordinal(), phone);
		}

		sheet.updateRow(tab, row, data);

	}

	public void persist(Consumer<Integer> onDone) {
		sheet.persist(this, onDone);
	}

	public void persist() {
		persist(null);
	}

	public void saveModified() {
		if (tab == -1) {
			throw new IllegalStateException("Tab value not set.");
		}
		if (row == -1) {
			throw new IllegalStateException("Row value not set.");
		}

		sheet.setCellValue(tab, row, Column.MODIFIED.ordinal(), getModified());
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
				sheet.setCellValue(tab, row, Column.FIRST_CAMPAIGN.ordinal() + i, "");
			} else {
				sheet.setCellValue(tab, row, Column.FIRST_CAMPAIGN.ordinal() + i, points.get(i));
			}
		}

		modified();
		saveModified();
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

	public int getShavuosRow() {
		return shavuosRow;
	}

	public void setShavuosRow(int row) {
		shavuosRow = row;
	}

	public int getGiftsRow() {
		return giftsRow;
	}

	public void setGiftsRow(int row) {
		giftsRow = row;
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
		if (row < 0) {
			throw new IllegalStateException("Row value not set.");
		}

		if (index < points.size()) {
			points.set(index, p);
		} else {
			while (index > points.size()) {
				points.add(0);
			}

			points.add(p);
		}

		if (p <= 0)
			sheet.setCellValue(tab, row, Column.FIRST_CAMPAIGN.ordinal() + index, "");
		else
			sheet.setCellValue(tab, row, Column.FIRST_CAMPAIGN.ordinal() + index, p);

		modified();
		saveModified();
	}

	public int getPoint(int index) {
		if (index < points.size())
			return points.get(index);

		return 0;
	}

	public ObservableList<Integer> getShavuosData() {
		return shavuosData;
	}

	private void setShavuosData(List<Integer> data) {
		shavuosData.setAll(data);
	}

	public void putShavuosData(int index, int data) {
		if (shavuosRow < 0) {
			sheet.addRow(Tab.SHAVUOS.ordinal(), Collections.singletonList(getId()), row -> {
				shavuosRow = row;
				putShavuosData(index, data);
			});

			return;
		}

		if (index < shavuosData.size()) {
			shavuosData.set(index, data);
		} else {
			while (index > shavuosData.size()) {
				shavuosData.add(0);
			}

			shavuosData.add(data);
		}

		if (data <= 0)
			sheet.setCellValue(Tab.SHAVUOS.ordinal(), shavuosRow, 1 + index, "");
		else
			sheet.setCellValue(Tab.SHAVUOS.ordinal(), shavuosRow, 1 + index, data);

		modified();
		saveModified();

	}

	public int getShavuosData(int index) {
		if (index < shavuosData.size())
			return shavuosData.get(index);

		return 0;
	}

	public ObservableList<Boolean> getGiftsReceived() {
		return giftsReceived;
	}

	private void setGiftsReceived(List<Boolean> data) {
		giftsReceived.setAll(data);
	}

	public void putGiftReceived(int index, boolean data) {

		if (giftsRow < 0) {
			sheet.addRow(Tab.GIFTS.ordinal(), Collections.singletonList(getId()), row -> {
				giftsRow = row;
				putGiftReceived(index, data);
			});

			return;
		}

		if (index < giftsReceived.size()) {
			giftsReceived.set(index, data);
		} else {
			while (index > giftsReceived.size()) {
				giftsReceived.add(false);
			}

			giftsReceived.add(data);
		}

		if (!data)
			sheet.setCellValue(Tab.GIFTS.ordinal(), giftsRow, 1 + index, "");
		else
			sheet.setCellValue(Tab.GIFTS.ordinal(), giftsRow, 1 + index, CHECKMARK);

		modified();
		saveModified();
	}

	public boolean isGiftRecieved(int index) {
		if (index < giftsReceived.size())
			return giftsReceived.get(index);

		return false;
	}

	public boolean isEligibleForGift(int index) {
		return getTotal() / 100 >= index + 1 && !isGiftRecieved(index);
	}

	public ReadOnlyIntegerProperty totalProperty() {
		return total.getReadOnlyProperty();
	}

	public int getTotal() {
		return totalProperty().get();
	}

	private void setData(List<String> data) {
		Column[] cols = Column.values();

		for (int i = 0; i < Column.TOTAL_POINTS.ordinal(); i++) {
			set(cols[i], cols[i].getData(data));
		}

		if (data.size() <= Column.FIRST_CAMPAIGN.ordinal()) {
			setPoints(Collections.emptyList());
			return;
		}

		setPoints(data.subList(Column.FIRST_CAMPAIGN.ordinal(), data.size()).stream().mapToInt(cell -> {
			if (cell == null || "".equals(cell))
				return 0;
			return Integer.parseInt(cell);
		}).boxed().collect(Collectors.toList()));
	}

	private List<String> getDetails() {
		List<String> data = new ArrayList<>();
		Column[] cols = Column.values();

		for (int i = 0; i < Column.TOTAL_POINTS.ordinal(); i++) {
			data.add(get(cols[i]));
		}

		data.add("=SUM(" + Column.toName(Column.FIRST_CAMPAIGN.ordinal()) + (row + 1) + ":AAI" + (row + 1) + ")");

		return data;
	}

	public String get(Column column) {
		switch (column) {
		case ID_NUMBER:
			return getId();
		case CREATED:
			return getCreated();
		case MODIFIED:
			return getModified();
		case GENDER:
			return getGender();
		case FIRST_NAME:
			return getFirstName();
		case LAST_NAME:
			return getLastName();
		case ADDRESS_NUMBER:
			return getAddressNumber();
		case ADDRESS_NAME:
			return getAddressName();
		case APT:
			return getApt();
		case CITY:
			return getCity();
		case STATE:
			return getState();
		case ZIP:
			return getZip();
		case CITY_YIDDISH:
			return getCityYiddish();
		case CLASS:
			return getAge();
		case SCHOOL:
			return getSchool();
		case PHONE:
			return getPhone();
		case FATHER_NAME:
			return getFatherName();
		case LAST_NAME_YIDDISH:
			return getLastNameYiddish();
		case FIRST_NAME_YIDDISH:
			return getFirstNameYiddish();
		default:
			throw new RuntimeException("Unknown Column");
		}

	}

	public void set(Column column, String data) {
		switch (column) {
		case ID_NUMBER:
			setId(data);
			break;
		case CREATED:
			setCreated(data);
			break;
		case MODIFIED:
			setModified(data);
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

	public EventStream<Change<String>> getDetailsChangeStream() {
		return detailsChangeStream;
	}

	public EventStream<Object> getChangeStream() {
		return changeStream;
	}

	@Override
	public String toString() {
		return getFirstNameYiddish() + " " + getLastNameYiddish();
	}

	@Override
	public int compareTo(Entry other) {
		int ln = getLastNameYiddish().compareTo(other.getLastNameYiddish());
		if (ln != 0)
			return ln;

		return getPhone().compareTo(other.getPhone());
	}

}
