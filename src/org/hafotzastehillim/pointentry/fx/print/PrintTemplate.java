package org.hafotzastehillim.pointentry.fx.print;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hafotzastehillim.pointentry.fx.Model;
import org.hafotzastehillim.pointentry.fx.util.DialogUtils;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;

public class PrintTemplate extends StackPane {

	@FXML
	private Label hebrewName;
	@FXML
	private Label date;
	@FXML
	private Label hebrewDate;
	@FXML
	private Label account;
	@FXML
	private Label sum;
	@FXML
	private Label by;
	@FXML
	private Label name;
	@FXML
	private Label address;
	@FXML
	private Label city;
	@FXML
	private Label accountLabel;
	@FXML
	private Label sumLabel;
	@FXML
	private Label byLabel;
	@FXML
	private Label title;
	@FXML
	private Label hebrewTitle;
	@FXML
	private Label hebrewTitleEnding;
	@FXML
	private GridPane children;
	@FXML
	private VBox addressBox;

	private StatementType statementType;

	private static final int WIDTH = 685;
	private static final int HEIGHT = 883;

	PrintTemplate() {

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/PrintTemplate.fxml"));
			loader.setController(this);
			loader.setRoot(this);
			loader.load();
		} catch (IOException e) {
			DialogUtils.showErrorDialog(e);
			return;
		}

	}

	@FXML
	private void initialize() {
		accountLabel.visibleProperty().bind(account.textProperty().isNotEmpty());
		sumLabel.visibleProperty().bind(sum.textProperty().isNotEmpty());
		byLabel.visibleProperty().bind(by.textProperty().isNotEmpty());
		title.visibleProperty().bind(name.textProperty().isNotEmpty());
		hebrewTitle.visibleProperty().bind(hebrewName.textProperty().isNotEmpty());
		hebrewTitleEnding.visibleProperty().bind(hebrewName.textProperty().isNotEmpty());

		Font f = Font.loadFont(getClass().getResource("/resources/fonts/DavidBd.ttf").toExternalForm(), 20);

		hebrewTitle.setFont(f);
		hebrewName.setFont(f);
		hebrewTitleEnding.setFont(f);

		children.getColumnConstraints().setAll(new ColumnConstraints(10, 50, 100, Priority.SOMETIMES, HPos.LEFT, true),
				new ColumnConstraints(10, 50, 100, Priority.SOMETIMES, HPos.RIGHT, true));
	}

	public static PrintTemplate create() {
		return new PrintTemplate();
	}

	public PrintTemplate hebrewName(String name) {
		this.hebrewName.setText(name);
		return this;
	}

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM. d, ''yy");
	private static final HebrewDateFormatter HEBREW_FORMATTER = new HebrewDateFormatter();
	static {
		HEBREW_FORMATTER.setHebrewFormat(true);
	}

	public PrintTemplate date(LocalDate date) {
		this.date.setText(FORMATTER.format(date));
		hebrewDate.setText(HEBREW_FORMATTER
				.format(new JewishDate(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()))));
		return this;
	}

	public PrintTemplate name(String name) {
		this.name.setText(name);
		return this;
	}

	public PrintTemplate account(String acct) {
		this.account.setText(acct);
		return this;
	}

	public PrintTemplate sum(String sum) {
		this.sum.setText(sum);
		return this;
	}

	public PrintTemplate by(String by) {
		this.by.setText(by);
		return this;
	}

	public PrintTemplate address(String address) {
		this.address.setText(address);
		return this;
	}

	public PrintTemplate city(String city) {
		this.city.setText(city);
		return this;
	}

	public PrintTemplate statementType(StatementType type) {
		statementType = type;

		switch (type) {
		case PLEDGE:
			sumLabel.setText("Sum Pledged: $");
			break;
		case RECEIPT:
			sumLabel.setText("Sum Received: $");
			break;
		default:
			sumLabel.setText("Sum: $");
		}

		return this;
	}

	private static final Font font = Font.font(10);

	public PrintTemplate children(List<? extends ChildName> list) {
		children.getRowConstraints().clear();
		children.getRowConstraints().add(new RowConstraints(20));
		children.getRowConstraints().addAll(Collections.nCopies(list.size(), new RowConstraints(10, 15, 20)));

		if (list.size() == 0)
			return this;

		Label kinderLabel = new Label();
		kinderLabel.setUnderline(true);
		children.add(kinderLabel, 0, 0, 2, 1);
		GridPane.setHalignment(kinderLabel, HPos.CENTER);

		for (int i = 0; i < list.size(); i++) {
			Label id = new Label("ID #" + list.get(i).id);
			id.setFont(font);
			Label name = new Label(list.get(i).name);
			name.setFont(font);

			children.add(id, 0, i + 1);
			children.add(name, 1, i + 1);
		}

		if (list.size() == 1) {
			kinderLabel.setText("תהלים קינד");
		} else {
			kinderLabel.setText("תהלים קינדער");
		}

		return this;
	}

	public boolean print(boolean showDialog) {
		if (statementType == StatementType.RECEIPT) {
			setTranslateY(-10);
		} else if (statementType == StatementType.PLEDGE) {
			addressBox.setTranslateY(-10);
		}

		PrinterJob job = PrinterJob.createPrinterJob();
		if (job != null) {
			if (showDialog) {
				boolean ok = job.showPrintDialog(Model.getInstance().getCurrentStage());
				if (!ok)
					return false;
			}
			PageLayout layout = job.getPrinter().createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT,
					Printer.MarginType.HARDWARE_MINIMUM);

			double scaleX = layout.getPrintableWidth() / WIDTH;
			double scaleY = layout.getPrintableHeight() / HEIGHT;
			getTransforms().add(new Scale(scaleX, scaleY));

			boolean success = job.printPage(layout, this);
			if (success) {
				job.endJob();
				return true;
			}
		}
		return false;
	}

	public boolean print() {
		return print(false);
	}
}
