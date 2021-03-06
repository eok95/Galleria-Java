/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import GUI.tools.MyRadioButtonsWrapper;
import controlloAccesso.ControlloAccesso;
import controlloAccesso.Permesso;
import java.io.IOException;

import static java.lang.Thread.sleep;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.Main;

/**
 * FXML Controller class
 *
 * @author Lorenzo
 */
public class MainController implements Initializable {
    //Costanti
    public static final int LOGIN_W=320, LOGIN_H=320, APP_W=800, APP_H=600;
    
    @FXML
    private StackPane mainPane;                                                 // Trattato come Pane
    @FXML
    private VBox boxApplicazione;
    @FXML
    private TabPane tabMenu;
    @FXML
    private Tab tabIlluminazione;
    @FXML
    private Tab tabPAI;
    @FXML
    private Tab tabTraffico;
    @FXML
    private Button buttonAccetta;
    @FXML
    private Button buttonAnnulla;
    @FXML
    private Label valorePermesso;
    @FXML
    private Label valoreUser;
    @FXML
    private Button buttonLogout;
    @FXML
    private VBox boxLogin;
    @FXML
    private TextField fieldUsername;
    @FXML
    private PasswordField fieldPassword;
    @FXML
    private Button buttonLogin;
    @FXML
    private Button buttonManutenzione;
    
    //Elementi di supporto non grafici
    private Stage stageManutenzione;
    private Permesso permesso;
    private MyRadioButtonsWrapper myRbuttons;
    private UpdateThread aggiornaInterfaccia;
    protected boolean modifichePendenti;

    
    public MainController(){
        myRbuttons=new MyRadioButtonsWrapper();
        permesso=null;
        aggiornaInterfaccia=null;
        modifichePendenti=false;
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Vincoli dell'utente
        
        associaPulsantiAdEnum();

        // Collegamento dei controller GUI
        Main.GUIcontrollers.putInstance(MainController.class, this);
        
        // UI
        disabilitaPulsantiModifiche(true);
        
        // Creazione Stage per ManutenzioneView
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/ManutenzioneView.fxml"));
            Parent root = loader.load(getClass().getResource("/GUI/ManutenzioneView.fxml"));
            stageManutenzione = new Stage();
            stageManutenzione.setScene(new Scene(root));
            stageManutenzione.setResizable(false);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Da lanciare una sola volta DOPO aver assegnato la "scene" allo "stage" nel "Main"
     */
    public void avviaGUI(){
        showLoginBox();
        ((Stage)mainPane.getScene().getWindow()).setOnCloseRequest(ev -> {
            System.exit(0);
        });
        ((Stage)mainPane.getScene().getWindow()).setWidth(MainController.LOGIN_W);
        ((Stage)mainPane.getScene().getWindow()).setTitle("Galleria");
        ((Stage)mainPane.getScene().getWindow()).show();
    }

    @FXML
    private void accettaModifiche(ActionEvent event) {
        // TODO: Comunica con i relativi control e fornisci loro i nuovi dati da impostare
        
        // Disabilita i pulsanti
        disabilitaPulsantiModifiche(true);
        
        ((PAIController)Main.GUIcontrollers.getInstance(PAIController.class)).adottaNuoveImpostazioni();
        ((TrafficoController)Main.GUIcontrollers.getInstance(TrafficoController.class)).adottaNuoveImpostazioni();
        ((IlluminazioneController)Main.GUIcontrollers.getInstance(IlluminazioneController.class)).adottaNuoveImpostazioni();
        
        contestualizzaInterfaccia();

    }

    @FXML
    private void annullaModifiche(ActionEvent event) {
        recuperaValoriInUso();    
        disabilitaPulsantiModifiche(true);
    }

    @FXML
    private void effettuaLogout(ActionEvent event) {
        aggiornaInterfaccia.terminate();
        
        ControlloAccesso.getInstance().effettuaLogout();
        
        recuperaValoriInUso();
        disabilitaPulsantiModifiche(true);

        stageManutenzione.hide();
        showLoginBox();
    }

    @FXML
    private void effettuaLogin(ActionEvent event) {
        if(ControlloAccesso.getInstance().effettuaLogin(fieldUsername.textProperty().get(), fieldPassword.textProperty().get()) == null){
            // TODO: Gestire accesso non riuscito
            return;
        }
        permesso=ControlloAccesso.getInstance().getPermesso();
        
        System.out.println("Connesso come "+permesso+"::"+fieldUsername.getText());
        
        recuperaValoriInUso();
        disabilitaPulsantiModifiche(true);

        showAppBox();
        
        aggiornaInterfaccia=new UpdateThread(1000/60);
        aggiornaInterfaccia.start();     
    }
    
    /**************************************************************************/
    private void showLoginBox(){
        boxLogin.toFront();
        boxApplicazione.toBack();
        ((Stage)mainPane.getScene().getWindow()).setWidth(LOGIN_W);
        ((Stage)mainPane.getScene().getWindow()).setHeight(LOGIN_H);
        ((Stage)mainPane.getScene().getWindow()).setResizable(false);
    }
    private void showAppBox(){
        boxLogin.toBack();
        boxApplicazione.toFront();
        ((Stage)mainPane.getScene().getWindow()).setWidth(APP_W);
        ((Stage)mainPane.getScene().getWindow()).setHeight(APP_H);
        ((Stage)mainPane.getScene().getWindow()).setResizable(true);
    }
    @FXML
    private void showMaintenanceWindow(){
        stageManutenzione.show();
    }
    
    private void associaPulsantiAdEnum(){
        /* Nel caso in cui servano al MainController
        attualmente la MainView non gestisce alcun RadioButton
        */
    }
    
    protected void disabilitaPulsantiModifiche(boolean b){
        if(b==true)
            modifichePendenti=false;
        buttonAccetta.setDisable(b);
        buttonAnnulla.setDisable(b);
    }
    
    private void recuperaValoriInUso(){
        ((TrafficoController)Main.GUIcontrollers.getInstance(TrafficoController.class)).recuperaImpostazioniInUso();
        ((IlluminazioneController)Main.GUIcontrollers.getInstance(IlluminazioneController.class)).recuperaImpostazioniInUso();
        ((PAIController)Main.GUIcontrollers.getInstance(PAIController.class)).recuperaImpostazioniInUso();
        
        // TODO: Recupera da controlloAccesso
        contestualizzaInterfaccia();
    }    
    
    /**
     * Viene richiamata per consentire le funzionalità in base al tipo di accesso effettuato e allo stato della PAI
     */
    private void contestualizzaInterfaccia(){
        valoreUser.textProperty().set(fieldUsername.getText());
        switch(permesso){
            case OPERATORE:
                valorePermesso.textProperty().set("Operatore");
                ((PAIController)Main.GUIcontrollers.getInstance(PAIController.class)).disabilitaVista(false);
                boolean paiInCorso=((PAIController)Main.GUIcontrollers.getInstance(PAIController.class)).isPaiInCorso();
                ((IlluminazioneController)Main.GUIcontrollers.getInstance(IlluminazioneController.class)).disabilitaVista(paiInCorso);
                ((TrafficoController)Main.GUIcontrollers.getInstance(TrafficoController.class)).disabilitaVista(paiInCorso);
                buttonManutenzione.setDisable(false);
                break;
            case CONTROLLORE:
                valorePermesso.textProperty().set("Controllore");
                ((IlluminazioneController)Main.GUIcontrollers.getInstance(IlluminazioneController.class)).disabilitaVista(true);
                ((PAIController)Main.GUIcontrollers.getInstance(PAIController.class)).disabilitaVista(true);
                ((TrafficoController)Main.GUIcontrollers.getInstance(TrafficoController.class)).disabilitaVista(true);
                buttonManutenzione.setDisable(true);
                break;
        }       
    }
    
    /**************************************************************************/
    //Strumenti ausiliari
    
    private class UpdateThread extends Thread{
        
        private boolean stopRequested;
        private long freq;
        
        public UpdateThread(int freq) {
            super();
            stopRequested=false;
            this.freq=freq;
        }
            
        
        @Override
        public void run() {
            while(!stopRequested){
                // TODO: aggiornare temperatura e altri valori

                // Aggiornamento grafico e Pulsanti modifica
                ((IlluminazioneController)Main.GUIcontrollers.getInstance(IlluminazioneController.class)).aggiornaCurva();
                if(modifichePendenti)
                    disabilitaPulsantiModifiche(false);

                                
                // Attesa per il prossimo aggiornamento
                try {
                    sleep(freq);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        public void terminate(){
            stopRequested=true;
            this.interrupt();
        }
    }
}
