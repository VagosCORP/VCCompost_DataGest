package com.vagoscorp.vccompost.datagest;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import com.vagoscorp.vccompost.recursos.GestVagones;
import com.vagoscorp.vccompost.recursos.Jabalinas;
import com.vagoscorp.vccompost.sensors.Adquirir_Temperaturas;
import com.vagoscorp.vccompost.sensors.Adquirir_Temperaturas.listener;
import com.vagoscorp.vccompost.tablet.Actualizar_Tablet;

public class LayoutDataGestController implements Initializable {

	@FXML TextField IP;
	@FXML TextField Port;
	@FXML Button updIP;

	@FXML Button btnCrearVagon;
	@FXML Button btnAgregarJabalina;
	@FXML Button btnGetVagon;
	@FXML Button btnInitSample;
	@FXML Button btnEndSample;
	@FXML Button btnCrear;

	@FXML TextField txtFldPath;
	@FXML TextField txtFldNumero;
	@FXML TextField txtFldNv;
	@FXML TextField txtFldPrioridad;

	@FXML TextField txtFldPosX;
	@FXML TextField txtFldPosY;
	@FXML TextField txtFldDx;
	@FXML TextField txtFldDy;

	@FXML TextField txtFldJ1S1;
	@FXML TextField txtFldJ1S2;
	@FXML TextField txtFldJ1S3;
	@FXML TextField txtFldJ1S4;

	@FXML TextField txtFldJ2S1;
	@FXML TextField txtFldJ2S2;
	@FXML TextField txtFldJ2S3;
	@FXML TextField txtFldJ2S4;

	@FXML TextArea txtAreaLog;
	
	Adquirir_Temperaturas adq_temp;
	Actualizar_Tablet act_tab;
	Thread temperatureThread;
	Thread tabletThread;
	GestVagones vagones;

	// /////////////////////Eventos GUI////////////////////////
	@FXML
	private void handleButtonInicializarGest(ActionEvent event) {
		String path = txtFldPath.getText();
		try {
			vagones = new GestVagones(path);
			txtAreaLog.appendText("Archivo de Registro Cargado con Exito! \n");
		} catch (IOException ex) {
			txtAreaLog.appendText(ex.getMessage() + "\n");
			txtAreaLog.appendText("Archivo de Registro No Encontrado, "
					+ "Nuevo Registro Creado! \n");
			vagones = new GestVagones(32, path);
		}
	}

	@FXML
	public void setIP() {
		act_tab.Actualizar_IP(IP.getText(), Integer.parseInt(Port.getText()));
	}

	@FXML
	private void handleButtonCrearVagon(ActionEvent event) {
		// agregamos nuevo vagon al sistema
		vagones.addVagon("Vagon imaginariooooo xD");
	}

	@FXML
	private void handleButtonAgregarJabalina(ActionEvent event) {
		adq_temp = new Adquirir_Temperaturas();
		adq_temp.setListener(new listener() {

			@Override
			public void dataRCV() {

			}

			@Override
			public void procTerminated() {
				try {
					int nvag = Integer.parseInt(txtFldNumero.getText());
					vagones.addSample(nvag,
						new Jabalinas(
							Integer.parseInt(txtFldPosX.getText()),
							Integer.parseInt(txtFldPosY.getText()),
							Integer.parseInt(txtFldDx.getText()),
							Integer.parseInt(txtFldDy.getText()),
							Math.round(adq_temp.resF[0]),// */ Integer.parseInt(txtFldJ1S1.getText()),
							Math.round(adq_temp.resF[1]),// */ Integer.parseInt(txtFldJ1S2.getText()),
							Math.round(adq_temp.resF[2]),// */ Integer.parseInt(txtFldJ1S3.getText()),
							Math.round(adq_temp.resF[3]),// */ Integer.parseInt(txtFldJ1S4.getText()),
							Math.round(adq_temp.resF[4]),// */ Integer.parseInt(txtFldJ2S1.getText()),
							Math.round(adq_temp.resF[5]),// */ Integer.parseInt(txtFldJ2S2.getText()),
							Math.round(adq_temp.resF[6]),// */ Integer.parseInt(txtFldJ2S3.getText()),
							Math.round(adq_temp.resF[7])));// */ Integer.parseInt(txtFldJ2S4.getText())));
					act_tab.enviarDatos(vagones.getVag(nvag));
				} catch (NullPointerException ex) {
					Logger.getLogger(LayoutDataGestController.class.getName())
							.log(Level.SEVERE, null, ex);
					txtAreaLog.appendText(ex.getMessage() + "\n");
				} catch (IOException ex) {
					Logger.getLogger(LayoutDataGestController.class.getName())
							.log(Level.SEVERE, null, ex);
					txtAreaLog.appendText(ex.getMessage() + "\n");
				}
			}
		});
		temperatureThread = new Thread(adq_temp);
		temperatureThread.setDaemon(true);
		temperatureThread.start();
	}

	@FXML
	private void handleButtonGetVagon(ActionEvent event) {
		try {
			txtAreaLog.appendText(vagones.getVag(Integer.parseInt(txtFldNumero
					.getText())) + "\n");
		} catch (NullPointerException ex) {
			Logger.getLogger(LayoutDataGestController.class.getName()).log(
					Level.SEVERE, null, ex);// fuera de rango
			txtAreaLog.appendText(ex.getMessage() + "\n");
		} catch (IOException ex) {
			Logger.getLogger(LayoutDataGestController.class.getName()).log(
					Level.SEVERE, null, ex);// fallo con el archivo
			txtAreaLog.appendText(ex.getMessage() + "\n");
		}
	}

	@FXML
	private void handleButtonInitSample(ActionEvent event) {
		vagones.initSampling(Integer.parseInt(txtFldNumero.getText()));
	}

	@FXML
	private void handleButtonEndSample(ActionEvent event) {
		vagones.endSampling(Integer.parseInt(txtFldNumero.getText()));
	}

	// //////////////////////////////////////////////////////////

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		act_tab = new Actualizar_Tablet(IP.getText(), Integer.parseInt(Port
				.getText()));
		txtFldPath.setText("J://Documents//Compost//");
	}
}
