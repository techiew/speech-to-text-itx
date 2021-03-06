package gui;

import java.io.File;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class GuiController {

    @FXML
    private Button buttonChooseFiles;

    @FXML
    private Button buttonStartProcessing;

    @FXML
    private ImageView imageLogo;

    @FXML
    private ImageView imageLoading;

    @FXML
    private Label labelHelp;

    @FXML
    private Label labelHelp2;

    @FXML
    private Label labelFeedbackMsg;

    // Kode for "velg filer" knappen
    @FXML
    public void chooseFiles(ActionEvent event) {

 	   Stage s = Gui.primaryStage;

 	   FileChooser fileChooser = new FileChooser();
 	   fileChooser.setInitialDirectory(new File("."));
 	   fileChooser.setTitle("Velg lydfiler");
 	   fileChooser.getExtensionFilters().addAll(
 	           new ExtensionFilter(".wav Single-Channel Audio File(s)", "*.wav")
 	           );

 	   List<File> selectedFiles = fileChooser.showOpenMultipleDialog(s);

 	   if (selectedFiles == null) {
 		   showErrorMsg("Du valgte ingen filer/Feil med valgte filer");
 		   System.out.println("Du valgte ingen filer/Feil med valgte filer");
 		   buttonStartProcessing.setDisable(true);
 		   return;
 	   }

 	   if(selectedFiles.size() > 1) {
 		   System.out.println("Du har valgt " + selectedFiles.size() + " filer");
 		   showFeedbackMsg("Du har valgt " + selectedFiles.size() + " filer");
 	   } else {
 		   System.out.println("Du har valgt " + selectedFiles.size() + " fil");
 		   showFeedbackMsg("Du har valgt " + selectedFiles.size() + " fil");
 	   }
 	   
       Gui.parentObject.setSelectedFiles(selectedFiles);
       buttonStartProcessing.setDisable(false);
    }

    // Kode for "start analyse" knappen
    @FXML
    void startFileAnalysis(ActionEvent event) {

    	if(Gui.parentObject.getSelectedFiles().size() > 0) {
        	buttonStartProcessing.setDisable(true);
        	showFeedbackMsg("Analyserer filene...");

    		System.out.println("Disse filene vil bli analysert: ");
        	for(int i = 0; i < Gui.parentObject.getSelectedFiles().size(); i++) {
        		System.out.println(Gui.parentObject.getSelectedFiles().get(i).getName());
        	}

        	Gui.parentObject.startProcess();
        	labelHelp.setText("Vennligst vent mens lydfilene blir analysert");
        	labelHelp2.setVisible(false);
        	imageLoading.setVisible(true);
        	buttonChooseFiles.setVisible(false);
        	buttonStartProcessing.setVisible(false);
        	buttonChooseFiles.setDisable(true);
    	} else {
    		System.out.println("Kunne ikke starte analysen, feil med valgte filer");
    		showErrorMsg("Kunne ikke starte analysen, feil med valgte filer");
    	}

    }
    
    // Kode for "Om Programmet" knappen under "Hjelp" i menybaren
    @FXML
    void showInfoBox() {
    	Alert alert = new Alert(AlertType.INFORMATION);
    	alert.setTitle("Om Programmet");
    	alert.setHeaderText(null);
    	alert.setContentText("Dette programmet sin funksjon er � transkribere tale fra en eller flere lydfiler"
    			+ " om til lesbar tekst. Programmet tar i bruk Speech-to-Text API laget av Google."
    			+ "\n\nKriterier for filer som kan brukes i dette programmet:\nLydfilene m� v�re av typen .wav, og de m� v�re monochannel."
    			+ "\n\nLaget for bacheloroppgave i IT p� Universitetet i S�r�st-Norge.");
    	alert.showAndWait();
    }
    
    public void showErrorMsg(String error) {
    	labelFeedbackMsg.setTextFill(Color.RED);
    	labelFeedbackMsg.setText(error);
    }
    
    public void showFeedbackMsg(String feedback) {
    	labelFeedbackMsg.setTextFill(Color.web("#1548FF"));
    	labelFeedbackMsg.setText(feedback);
    }
    
    // Nullstiller vinduet n�r vi er ferdig med en analyse, slik at vi kan bruke vinduet igjen
    public void resetWindowState() {
        buttonStartProcessing.setDisable(true);
    	buttonChooseFiles.setDisable(false);
    	buttonChooseFiles.setVisible(true);
    	buttonStartProcessing.setVisible(true);
    	imageLoading.setVisible(false);
        labelHelp.setText("Velg en eller flere lydfiler med tale");
        labelFeedbackMsg.setText("Lydfilene ble analysert og resultatene lagret.");
        labelHelp.setVisible(true);
        labelHelp2.setVisible(true);
    }

}

