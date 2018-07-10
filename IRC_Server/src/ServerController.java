import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

public class ServerController {

    private ServerGui gui;
    private ServerModel model;
    private ServerLog log;
    private Stage stage;


    public ServerController(ServerGui a, ServerModel b,Stage c,ServerLog d){
        this.gui=a;
        this.model=b;
        this.stage=c;
        this.log=d;
        initListenners();
    }

    private void initListenners () {

        // Action event.

        gui.getMenuItems(0).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) { ;
                System.exit(0);
            }
        });

        gui.getMenuItems(1).setOnAction(new EventHandler<ActionEvent>() {
            @Override
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
            @Override
            public void handle(ActionEvent event) {
                gui.setBlacklistWindow();
            }
        });

        gui.getMenuItems(3).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                gui.cleanTextLog();
            }
        });

        gui.getMenuItems(4).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                gui.setLicenceWindow();
            }
        });

        gui.getChoiceBox().valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String oldValue, String newValue) {
                log.setLogContent("LogLevel : " + newValue, ServerLog.Level.ERROR, ServerLog.Facility.SERVER);
                log.setLogLevel(ServerLog.Level.valueOf(newValue));
            }
        });

        gui.getBoutton(0).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                gui.getTextField(0).setText("127.0.0.1");
                gui.getTextField(1).setText("27001");

                try {
                    model.setIpPort(gui.getTextField(0).getText(), Integer.parseInt(gui.getTextField(1).getText()));

                } catch (Exception ex) {
                    log.setLogContent(ex.getMessage(), ServerLog.Level.ERROR, ServerLog.Facility.SERVER);
                    return;
                }

                log.setLogContent("Demarrage du serveur !", ServerLog.Level.INFO, ServerLog.Facility.SERVER);
                ServerCompute compute=new ServerCompute(log,model,gui);
                compute.start();
                model.setSalons("Principal");
                model.setSalons("Principal2");
                model.setSalons("Principal3");

                gui.getTableView().setItems(FXCollections.observableList(model.getClients()));
                gui.getListView(0).setItems(FXCollections.observableList(model.getSalons()));

            }
        });

        gui.getBoutton(1).setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                log.setLogContent("Arret du serveur !",ServerLog.Level.INFO,ServerLog.Facility.SERVER);

                Iterator itr=model.getClients().iterator();

                while(itr.hasNext()){

                    ServerClients st=(ServerClients)itr.next();

                    try {
                        st.getSocketChannel().close();
                    } catch (IOException e) {
                        log.setLogContent("Echec de l arret du socket channel !",ServerLog.Level.ERROR,ServerLog.Facility.SERVER);
                    }
                }

                try {
                    model.getServerSocket().close();
                } catch (IOException e) {
                    log.setLogContent("Echec de l arret du server socket channel !",ServerLog.Level.ERROR,ServerLog.Facility.SERVER);
                }

                try {
                    model.getSelector().close();
                } catch (IOException e) {
                    log.setLogContent("Echec de l arret du selecteur !",ServerLog.Level.ERROR,ServerLog.Facility.SERVER);
                }

                gui.clearClientSalon();
            }
        });

        gui.getBoutton(2).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                log.setLogContent("Deconnection client !",ServerLog.Level.INFO,ServerLog.Facility.SERVER);

                model.deleteClients((ServerClients) gui.getTableView().getSelectionModel().getSelectedItem());
                gui.getTableView().setItems(FXCollections.observableList(model.getClients()));
            }
        });

        gui.getBoutton(3).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                log.setLogContent("Deconnection tous les client!",ServerLog.Level.INFO,ServerLog.Facility.SERVER);

                model.deleteAllClients();
                gui.getTableView().getItems().clear();
                gui.getTableView().setItems(FXCollections.observableList(model.getClients()));
            }
        });

        gui.getBoutton(4).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                log.setLogContent("Client est banni!",ServerLog.Level.INFO,ServerLog.Facility.SERVER);
                model.setBlackList(((ServerClients) gui.getTableView().getSelectionModel().getSelectedItem()).getIpAddress());
                model.deleteClients((ServerClients) gui.getTableView().getSelectionModel().getSelectedItem());
                gui.getTableView().setItems(FXCollections.observableList(model.getClients()));
                gui.getListView(1).setItems(FXCollections.observableList(model.getBlackList()));
            }
        });

        gui.getBoutton(5).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                log.setLogContent("Salon supp !",ServerLog.Level.INFO,ServerLog.Facility.SERVER);

                String salonSupp=gui.getListView(0).getSelectionModel().getSelectedItem().toString();

                Iterator itr=model.getClients().iterator();

                while(itr.hasNext()){

                    ServerClients st=(ServerClients)itr.next();

                    if (salonSupp==st.getSalon()) {
                        try {
                            st.getSocketChannel().close();
                        } catch (IOException e) {
                            log.setLogContent("Echec de l arret du socket channel !",ServerLog.Level.ERROR,ServerLog.Facility.SERVER);
                        }
                    }
                }

                model.deleteSalons(salonSupp);
                gui.getListView(0).setItems(FXCollections.observableList(model.getSalons()));
            }
        });

        gui.getBoutton(6).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                log.setLogContent("Supp client Blacklist !", ServerLog.Level.INFO, ServerLog.Facility.SERVER);
                model.deleteBlackListClient(gui.getListView(1).getSelectionModel().getSelectedItem().toString());
                gui.getListView(1).setItems(FXCollections.observableList(model.getBlackList()));
            }
        });

    }
}
