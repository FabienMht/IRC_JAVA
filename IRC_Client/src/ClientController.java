import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 Classe qui gère les interaction des utilisateurs avec la GUI.
 */
public class ClientController {

    private ClientGui gui;
    private ClientModel model;
    private Stage stage;
    private DateFormat df = new SimpleDateFormat("HH:mm:ss");
    private Date date;

    public ClientController(ClientGui a, ClientModel b,Stage c){
        this.gui=a;
        this.model=b;
        this.stage=c;
        initListenners();
    }

    /**
     Méthode qui permet l'écoute des actions sur les objets de la GUI.
     */
    private void initListenners () {

        /**
         Permet d'éteindre le serveur en cas d'arret inatendu.
         */
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {

                if (gui.getBoutton(0).isDisable()) {

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Fermeture Client");
                    alert.setHeaderText("");
                    alert.setContentText("Etes-vous sur de déco le client");

                    Optional<ButtonType> result = alert.showAndWait();

                    if (result.get() == ButtonType.OK){

                        ClientCompute.sendMsg("/quit", model.getClientSocket());

                        model.setStop(false);

                    } else {

                        we.consume();

                    }

                }

            }
        });

        gui.getMenuItems(0).setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) { ;
                System.exit(0);
            }
        });

        /**
         Permet d'enregistrer le contenu des msgs du textarea dans un fichier.
         */
        gui.getMenuItems(1).setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {

                File file=null;
                try {
                    file=gui.showMsgSaver(stage);
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(gui.getTextMsg().replaceAll("\n", System.getProperty("line.separator")));
                    fileWriter.close();

                } catch (IOException |NullPointerException ex) {
                    return;
                }

            }
        });

        gui.getMenuItems(2).setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {

                gui.setLicenceWindow();
            }
        });

        /**
         Permet d'effectuer les actions au lors de la connexion au serveur.
         - Vérification des inputs
         - Création de l'objet compute qui gère les socketchannel
         - Modification des boutons de la gui
         */
        gui.getBoutton(0).setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {

                String Ip;
                Integer Port;

                gui.getTextField(0).setText("127.0.0.1");
                gui.getTextField(1).setText("27001");

                // Vérification du format de l'adresse IP
                if(checkIPAddress(gui.getTextField(0).getText())){
                    Ip=gui.getTextField(0).getText();
                } else {
                    gui.setTextMsg("Format de l'adresse IP incorrect 0-255.0-255.0-255.0-255" + "\n");
                    return;
                }

                // Vérification du format du port
                try {
                    if (Integer.parseInt(gui.getTextField(1).getText()) < 65635 && Integer.parseInt(gui.getTextField(1).getText()) > 1024) {
                        Port = Integer.parseInt(gui.getTextField(1).getText());
                        model.setIpPort(Ip, Port);
                        model.setStop(true);
                    } else {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex){
                    gui.setTextMsg("Le numéro de port est incorrect : 1024 < port < 65534" + "\n");
                    return;
                }

                if (  ! gui.getTextField(2).getText().trim().equalsIgnoreCase("")) {
                    model.setNickname(gui.getTextField(2).getText());
                } else {
                    gui.setTextMsg("Format du nickname incorrect" + "\n");
                    return;
                }

                model.setSalonWork("Principal");

                // Création de l'objet compute
                ClientCompute compute=new ClientCompute(model,gui);
                compute.start();

                //Modification des boutons de la gui
                gui.getBoutton(0).setDisable(true);
                gui.getBoutton(1).setDisable(false);
                gui.getBoutton(2).setDisable(false);
                gui.getBoutton(3).setDisable(false);

                gui.majClient();
                gui.majSalon();

                gui.setStatus(ClientGui.Status.Connected,"Salon " + model.getSalonWork() + " / Nickname " + model.getNickname());

            }
        });

        /**
         Permet d'effectuer les actions a la deconnexion du serveur.
         */
        gui.getBoutton(1).setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                disconnect();
            }
        });

        /**
         Permet de gérer les events lors de la création de salon.
         */
        gui.getBoutton(2).setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (!gui.getTextField(3).getText().equals("")) {
                    setSalon(gui.getTextField(3).getText());
                    gui.getTextField(3).clear();
                }
            }
        });

        gui.getTextField(3).setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke)
            {
                if(gui.getBoutton(0).isDisable()) {
                    if (ke.getCode().equals(KeyCode.ENTER)) {
                        if (!gui.getTextField(3).getText().equals("")) {
                            setSalon(gui.getTextField(3).getText());
                            gui.getTextField(3).clear();
                        }
                    }
                }
            }
        });

        /**
         Permet de gérer les events lors de l'envoi de message.
         */
        gui.getBoutton(3).setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (!gui.getTextField(4).getText().equals("")) {
                    sendMsg();
                    gui.getTextField(4).clear();
                }
            }
        });

        gui.getTextField(4).setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke)
            {
                if(gui.getBoutton(0).isDisable()) {
                    if (ke.getCode().equals(KeyCode.ENTER)) {
                        if (!gui.getTextField(4).getText().equals("")) {
                            sendMsg();
                            gui.getTextField(4).clear();
                        }
                    }
                }
            }
        });

        /**
         Permet de gérer le changement de salon lors de la selection de celui-ci.
         */
        gui.getListView(0).getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    public void changed(ObservableValue<? extends String> ov, String old_val, String new_val) {



                        if (new_val!=null && !model.getSalonWork().equals(new_val)) {

                            ClientCompute.sendMsg("/setSalon " + new_val, model.getClientSocket());
                            gui.getAreaMsg().clear();

                            model.setSalonWork(new_val);
                            gui.setStatus(ClientGui.Status.Connected,"Salon " + model.getSalonWork() + " / Nickname " + model.getNickname());

                        }

                    }
                });
    }

    /**
     Permet d'effectuer des traitements avant l'envoi de messages au serveur.
     */
    public void sendMsg (){

        String msg=gui.getTextField(4).getText();

        if (msg.length()>200){

            gui.setStatus(ClientGui.Status.Error,"Msg trop long");

        } else if (msg.startsWith("/setNickname",0)) {

            ClientCompute.sendMsg(msg, model.getClientSocket());
            model.updateNickname(model.getNickname(),msg.replaceFirst("/setNickname","").trim());
            gui.majClient();

        } else if (msg.startsWith("/addSalon",0)) {

            ClientCompute.sendMsg(msg, model.getClientSocket());

        } else if (msg.startsWith("/help",0)){

            String [] msgHelp = new String [] {
                    "   - /setNickname : Change de pseudo",
                    "   - /getNickname : Affiche la liste des cients connectes au salon",
                    "   - /getSalon : Affiche la liste des salons",
                    "   - /setSalon : Modifie le salon en cours",
                    "   - /addSalon : Ajoute in salon",
                    "   - /quit : Se deconnecte du serveur",};

            gui.setTextMsg("Console d aide (Commandes) : " + "\n");

            for (int j = 0; j < msgHelp.length; j++) {
                gui.setTextMsg(msgHelp[j] + "\n");
            }

        } else if (msg.startsWith("/quit",0)){

            disconnect();

        } else {

            date = new Date();

            ClientCompute.sendMsg(df.format(date) + " " + model.getNickname() + " : " + msg, model.getClientSocket());

            gui.setTextMsg(df.format(date) + " " + model.getNickname() + " : " + msg);
            gui.setTextMsg("\n");

        }


    }

    /**
     Permet d'effectuer les actions a la deconnexion du serveur.
     - Déconnexion du client / Suppression des clients dans le model
     - Suppression de tous les salons
     - Arret du selecteur, socket channel
     - Modification des boutons de la gui
     */
    public void disconnect () {

        ClientCompute.sendMsg("/quit",model.getClientSocket());

        model.setStop(false);

        model.deleteAllClients();
        model.deleteAllSalons();

        gui.majClient();
        gui.majSalon();

        gui.getAreaMsg().clear();

        gui.getBoutton(0).setDisable(false);
        gui.getBoutton(1).setDisable(true);
        gui.getBoutton(2).setDisable(true);
        gui.getBoutton(3).setDisable(true);

        gui.setStatus(ClientGui.Status.Disconnected,"");

    }

    /**
     Vérifie si une chaine de caractère est au format d'une adresse IP.
     @return Vrai si format ok et faux sinon
     */
    public boolean checkIPAddress( String ipAddress ) {

        String[] tokens = ipAddress.split("\\.");

        if (tokens.length != 4) {
            return false;
        }

        for (String str : tokens) {
            int i = Integer.parseInt(str);
            if ((i < 0) || (i > 255)) {
                return false;
            }
        }

        return true;
    }

    /**
     Permet d'ajouter un salon sur le serveur.
     */
    public void setSalon (String Salon){

        ClientCompute.sendMsg("/addSalon " + Salon,model.getClientSocket());
        gui.majSalon();

    }

}
