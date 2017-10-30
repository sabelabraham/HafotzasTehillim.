package org.hafotzastehillim.fx.spreadsheet;

import javafx.fxml.FXML;

import javafx.scene.text.Text;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;

import org.hafotzastehillim.fx.Model;
import org.w3c.dom.NodeList;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;

import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;

public class LoginController {

	@FXML
	private TilePane root;
	@FXML
	private GridPane view;
	@FXML
	private Text text;
	@FXML
	private Hyperlink logout;
	@FXML
	private JFXButton signin;
	@FXML
	private CheckBox staySignedIn;
	@FXML
	private WebView web;

	private static final String loginString = "Please sign in to Google to continue";
	private static final String logoutString = "You are currently logged in, ";

	private String customMessage;

	@FXML
	public void initialize() {
		text.setText(loginString);
		logout.visibleProperty().bind(text.textProperty().isEqualTo(logoutString));

		signin.setOnMouseReleased(evt -> signin.setMouseTransparent(true));

		web.getEngine().getLoadWorker().stateProperty().addListener((obs, ov, nv) -> {
			if (nv == Worker.State.SUCCEEDED) {
				String url = web.getEngine().getLocation();
				if (url.startsWith("http://localhost")) {
					web.getEngine().loadContent("<html><p align=center><img src="
							+ getClass().getResource("/resources/images/wait.gif").toExternalForm() + "></html>");
				}
			}
		});
	}

	public void setCustomMessage(String msg) {
		customMessage = msg;
		if (!logout.isVisible()) {
			text.setText(loginString + " to " + msg);
		}
	}

	public void setMessageByLoggedIn() {
		try {
			if (SheetsAPI.isLoggedIn()) {
				text.setText(logoutString);
			} else {
				initLoginMessage();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initLoginMessage() {
		if (customMessage == null)
			text.setText(loginString);
		else
			text.setText(loginString + " to " + customMessage);
	}

	public void setSignInAction(EventHandler<ActionEvent> handler) {
		signin.setOnAction(handler);
	}

	@FXML
	public void logout(ActionEvent event) {
		try {
			SheetsAPI.logout();
		} catch (IOException e) {
		}

		Model.getInstance().setSpreadsheet(null);
		CookieHandler.setDefault(new CookieManager());
		initLoginMessage();
	}
	
	WebView getWebView() {
		return web;
	}
//
//	public void setWebAddress(String url) {
//		web.getEngine().load(url);
//
//		web.getEngine().documentProperty().addListener((obs, ov, nv) -> {
//			if (nv != null) {
//				NodeList list = nv.getChildNodes();
//				iterateNodes(list, 0);
//			}
//		});
//	}

//	private void iterateNodes(NodeList list, int depth) {
//		for (int i = 0; i < list.getLength(); i++) {
//			for (int space = 0; space < depth; space++) {
//				System.out.print(" ");
//			}
//			String content = list.item(i).getTextContent();
//			if (content != null && content.contains("Create account")) {
//				list.item(i).setTextContent(list.item(i).getTextContent());
//			}
//			iterateNodes(list.item(i).getChildNodes(), depth + 1);
//		}
//	}

	public final boolean isStaySignedIn() {
		return staySignedIn.isSelected();
	}

	public final void setStaySignedIn(boolean bool) {
		staySignedIn.setSelected(bool);
	}

	public BooleanProperty staySignedInProperty() {
		return staySignedIn.selectedProperty();
	}

	public final void setShowStaySignedIn(boolean bool) {
		staySignedIn.setVisible(bool);
	}

	public final boolean isShowingStaySignedIn() {
		return staySignedIn.isVisible();
	}

	public BooleanProperty showStaySignedInProperty() {
		return staySignedIn.visibleProperty();
	}
}
