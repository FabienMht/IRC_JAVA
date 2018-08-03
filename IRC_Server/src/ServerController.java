import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import javafx.scene.control.ButtonType;

/**
 Classe qui gère les interaction des utilisateurs avec la GUI.
 */
public class ServerController {

    private ServerGui gui;
    private ServerModel model;
    private ServerLog log;
    private Stage stage;
    private ServerCompute compute;

    public ServerController(ServerGui a, ServerModel b,Stage c,ServerLog d){
        this.gui=a;
        this.model=b;
        this.stage=c;
        this.log=d;
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
                    alert.setTitle("Fermeture Server");
                    alert.setHeaderText("");
                    alert.setContentText("Etes-vous sur de fermer le serveur");

                    Optional<ButtonType> result = alert.showAndWait();

                    if (result.get() == ButtonType.OK){

                        compute.sendMsg("/quit", model.getClients());

                        model.setStop(false);

                        try {
                            model.deleteClients(model.getClients());
                        } catch (Exception e) {
                            log.setLogContent("Echec de deco des clients !", ServerLog.Level.ERROR, ServerLog.Facility.SERVER);
                            return;
                        }

                        model.deleteAllSalons();

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
         Permet d'enregistrer le contenu des logs du textarea dans un fichier.
         */
        gui.getMenuItems(1).setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {

                File file=null;
                try {
                    file=gui.showLogSaver(stage);
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(gui.getTextLog().replaceAll("\n", System.getProperty("line.separator")));
                    fileWriter.close();

                } catch (IOException|NullPointerException ex) {
                    log.setLogContent(ex.getMessage(), ServerLog.Level.ERROR, ServerLog.Facility.SERVER);
                    return;
                }

                log.setLogContent("Fichier enregistre : " + file.getAbsolutePath(), ServerLog.Level.INFO, ServerLog.Facility.SERVER);
            }
        });

        gui.getMenuItems(2).setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {
                gui.setBlacklistWindow();
            }
        });

        gui.getMenuItems(3).setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {
                gui.cleanTextLog();
            }
        });

        gui.getMenuItems(4).setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {
                gui.setLicenceWindow();
            }
        });

        /**
         Modification du model en fonction des valeurs des choicebox.
         */
        gui.getChoiceBox(0).valueProperty().addListener(new ChangeListener<String>() {

            public void changed(ObservableValue ov, String oldValue, String newValue) {

                log.setLogContent("LogLevel : " + newValue, ServerLog.Level.WARNING, ServerLog.Facility.SERVER);
                log.setLogLevel(ServerLog.Level.valueOf(newValue));
            }
        });

        gui.getChoiceBox(1).valueProperty().addListener(new ChangeListener<String>() {

            public void changed(ObservableValue ov, String oldValue, String newValue) {

                log.setLogContent("Nombre de msg par salon : " + newValue, ServerLog.Level.INFO, ServerLog.Facility.SERVER);
                model.setNbMsg(Integer.parseInt(newValue));
            }
        });

        /**
         Permet d'effectuer les actions au démarrage du serveur.
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
                    log.setLogContent("Format de l'adresse IP incorrect 0-255.0-255.0-255.0-255",ServerLog.Level.ERROR,ServerLog.Facility.SERVER);
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
                    log.setLogContent("Le numéro de port est incorrect : 1024 < port < 65534",ServerLog.Level.ERROR,ServerLog.Facility.SERVER);
                    return;
                }

                log.setLogContent("Demarrage du serveur IP : " + model.getIpAddress().getHostAddress() + " Port : " + model.getPort(), ServerLog.Level.INFO, ServerLog.Facility.SERVER);

                // Création de l'objet compute
                compute=new ServerCompute(log,model,gui);
                compute.start();

                model.setSalons("Principal");

                //Modification des boutons de la gui
                gui.getBoutton(0).setDisable(true);
                gui.getBoutton(1).setDisable(false);
                gui.getBoutton(2).setDisable(false);
                gui.getBoutton(3).setDisable(false);
                gui.getBoutton(4).setDisable(false);
                gui.getBoutton(5).setDisable(false);
                gui.getBoutton(6).setDisable(false);

                gui.majClientSalon();

                gui.setStatus(ServerLog.Status.Started,"");

            }
        });

        /**
         Permet d'effectuer les actions a l'arret du serveur.
            - Déconnexion des clients / Suppression des clients dans le model
            - Suppression de tous les salons
            - Arret du selecteur,server socket channel
            - Modification des boutons de la gui
         */
        gui.getBoutton(1).setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Fermeture Server");
                alert.setHeaderText("");
                alert.setContentText("Etes-vous sur de fermer le serveur");

                Optional<ButtonType> result = alert.showAndWait();

                if (result.get() == ButtonType.OK){

                    log.setLogContent("Arret du serveur",ServerLog.Level.WARNING,ServerLog.Facility.SERVER);

                    compute.sendMsg("/quit",model.getClients());

                    model.setStop(false);

                    try {
                        model.deleteClients(model.getClients());
                    } catch (Exception e) {
                        log.setLogContent("Echec de deco des clients !", ServerLog.Level.ERROR, ServerLog.Facility.SERVER);
                        return;
                    }

                    model.deleteAllSalons();

                    gui.majClientSalon();

                    //Modification des boutons de la gui
                    gui.getBoutton(0).setDisable(false);
                    gui.getBoutton(1).setDisable(true);
                    gui.getBoutton(2).setDisable(true);
                    gui.getBoutton(3).setDisable(true);
                    gui.getBoutton(4).setDisable(true);
                    gui.getBoutton(5).setDisable(true);
                    gui.getBoutton(6).setDisable(true);

                    gui.setStatus(ServerLog.Status.Stop,"");

                }

            }
        });

        /**
         Déconnexion du client selectionné dans la tableview / Suppression du client dans le modèle.
         */
        gui.getBoutton(2).setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {

                if (gui.getTableView().getSelectionModel().getSelectedItem()!=null) {

                    log.setLogContent("Deconnection client !", ServerLog.Level.INFO, ServerLog.Facility.SERVER);

                    ServerClients client = (ServerClients) gui.getTableView().getSelectionModel().getSelectedItem();

                    compute.sendMsg("/quit", client.getSocketChannel());

                    compute.sendMsg("/deleteClient " + client.getNickname(), model.getClients(client.getSalon()), client.getSocketChannel());

                    try {
                        model.deleteClients(client);
                    } catch (Exception e) {
                        log.setLogContent("Echec de deco des clients !", ServerLog.Level.ERROR, ServerLog.Facility.SERVER);
                        return;
                    }

                    gui.majClientSalon();

                } else {

                    log.setLogContent("Aucun client selectionne", ServerLog.Level.WARNING, ServerLog.Facility.SERVER);

                }
            }
        });

        /**
         Déconnexion de tous les clients de tous les salons / Suppression des clients dans le modèle.
         */
        gui.getBoutton(3).setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {

                if (!model.getClients().isEmpty()) {

                    log.setLogContent("Deconnection tous les client!", ServerLog.Level.INFO, ServerLog.Facility.SERVER);

                    compute.sendMsg("/quit", model.getClients());

                    try {
                        model.deleteClients(model.getClients());
                    } catch (IOException e) {
                        log.setLogContent("Echec de deco des clients !", ServerLog.Level.ERROR, ServerLog.Facility.SERVER);
                        return;
                    }

                    gui.majClientSalon();

                } else {

                    log.setLogContent("Aucun client a deconnecter", ServerLog.Level.WARNING, ServerLog.Facility.SERVER);

                }

            }
        });

        /**
         Bannissement d'un client :
            - Déconnexion de celui-ci / Suppression du client dans le modèle
            - Ajout se l'adressse ip dans le modèle (base de données ip blacklist)
         */
        gui.getBoutton(4).setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {

                if (gui.getTableView().getSelectionModel().getSelectedItem() != null) {


                    ServerClients client = (ServerClients) gui.getTableView().getSelectionModel().getSelectedItem();

                    log.setLogContent("Client est banni : " + client.getNickname(), ServerLog.Level.INFO, ServerLog.Facility.SERVER);

                    compute.sendMsg("/quit Deconnecte car bani", client.getSocketChannel());
                    compute.sendMsg("/deleteClient " + client.getNickname(), model.getClients(client.getSalon()), client.getSocketChannel());

                    model.setBlackList(client.getIpAddress());

                    try {
                        model.deleteClients(client);
                    } catch (IOException e) {
                        log.setLogContent("Echec de deco des clients !", ServerLog.Level.ERROR, ServerLog.Facility.SERVER);
                        return;
                    }

                    gui.majClientSalon();

                } else {

                    log.setLogContent("Aucun client selectionne", ServerLog.Level.WARNING, ServerLog.Facility.SERVER);

                }

            }
        });

        /**
         Suppression du salon séléctionné dans la listview:
            - Déconnexion de tous les clients / Suppression du client dans le modèle
            - Supression du salon dans le modèle et maj de la GUI
         */
        gui.getBoutton(5).setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {

                if (gui.getListView(0).getSelectionModel().getSelectedItem() != null) {

                    String salonSupp = gui.getListView(0).getSelectionModel().getSelectedItem().toString();

                    if (salonSupp.equals("Principal")) {
                        return;
                    }

                    log.setLogContent("Salon supp : " + salonSupp, ServerLog.Level.INFO, ServerLog.Facility.SERVER);

                    compute.sendMsg("/quit", model.getClients(salonSupp));

                    try {
                        model.deleteClients(model.getClients(salonSupp));
                    } catch (IOException e) {
                        log.setLogContent("Echec de deco des clients !", ServerLog.Level.ERROR, ServerLog.Facility.SERVER);
                        return;
                    }

                    model.deleteSalons(salonSupp);
                    gui.majClientSalon();

                    compute.sendMsg("/deleteSalon" + salonSupp ,model.getClients());

                } else {

                    log.setLogContent("Aucun salon selectionne", ServerLog.Level.WARNING, ServerLog.Facility.SERVER);

                }
            }
        });

        /**
         Permet de supprimer une IP dans la blacklist.
         */
        gui.getBoutton(6).setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {

                if (gui.getListView(1).getSelectionModel().getSelectedItem() != null) {

                    String clientBlacklistSupp = gui.getListView(1).getSelectionModel().getSelectedItem().toString();

                    log.setLogContent("Supp client Blacklist : " + clientBlacklistSupp, ServerLog.Level.INFO, ServerLog.Facility.SERVER);

                    model.deleteBlackListClient(clientBlacklistSupp);

                    gui.getListView(1).setItems(FXCollections.observableList(model.getBlackList()));

                } else {

                    log.setLogContent("Aucun client selectionne", ServerLog.Level.WARNING, ServerLog.Facility.SERVER);

                }
            }
        });

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
}
