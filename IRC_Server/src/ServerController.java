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
    private ServerCompute compute;


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

                String Ip;
                Integer Port;

                gui.getTextField(0).setText("127.0.0.1");
                gui.getTextField(1).setText("27001");

                if(checkIPAddress(gui.getTextField(0).getText())){
                    Ip=gui.getTextField(0).getText();
                } else {
                    log.setLogContent("Format de l'adresse IP incorrect 0-255.0-255.0-255.0-255",ServerLog.Level.ERROR,ServerLog.Facility.SERVER);
                    return;
                }

                try {
                    if (Integer.parseInt(gui.getTextField(1).getText()) < 65635 && Integer.parseInt(gui.getTextField(1).getText()) > 1024) {
                        Port = Integer.parseInt(gui.getTextField(1).getText());
                        model.setIpPort(Ip, Port);
                    } else {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex){
                    log.setLogContent("Le numéro de port est incorrect : 1024 < port < 65534",ServerLog.Level.ERROR,ServerLog.Facility.SERVER);
                    return;
                }

                log.setLogContent("Demarrage du serveur IP : " + model.getIpAddress().getHostAddress() + " Port : " + model.getPort(), ServerLog.Level.INFO, ServerLog.Facility.SERVER);

                compute=new ServerCompute(log,model,gui);
                compute.start();

                model.setSalons("Principal");

                gui.getBoutton(0).setDisable(true);
                gui.getBoutton(1).setDisable(false);
                gui.getBoutton(2).setDisable(false);
                gui.getBoutton(3).setDisable(false);
                gui.getBoutton(4).setDisable(false);
                gui.getBoutton(5).setDisable(false);
                gui.getBoutton(6).setDisable(false);

                gui.majClientSalon();

            }
        });

        gui.getBoutton(1).setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                log.setLogContent("Arret du serveur",ServerLog.Level.WARNING,ServerLog.Facility.SERVER);

                ServerCompute.sendMsg("/quit",model.getClients());

                try {
                    model.deleteClients(model.getClients());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    model.getServerSocket().close();
                } catch (IOException e) {
                    log.setLogContent("Echec de l arret du server socket channel",ServerLog.Level.ERROR,ServerLog.Facility.SERVER);
                }

                try {
                    System.out.println(model.getSelector().selectedKeys().isEmpty());
                    model.getSelector().close();
                } catch (IOException e) {
                    log.setLogContent("Echec de l arret du selecteur",ServerLog.Level.ERROR,ServerLog.Facility.SERVER);
                }

                gui.clearClientSalon();

                gui.getBoutton(0).setDisable(false);
                gui.getBoutton(1).setDisable(true);
                gui.getBoutton(2).setDisable(true);
                gui.getBoutton(3).setDisable(true);
                gui.getBoutton(4).setDisable(true);
                gui.getBoutton(5).setDisable(true);
                gui.getBoutton(6).setDisable(true);
            }
        });

        gui.getBoutton(2).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                log.setLogContent("Deconnection client !",ServerLog.Level.INFO,ServerLog.Facility.SERVER);

                ServerClients client=(ServerClients) gui.getTableView().getSelectionModel().getSelectedItem();

                ServerCompute.sendMsg("/quit",client.getSocketChannel());

                ServerCompute.sendMsg("/deleteClient " + client.getNickname(),model.getClients(client.getSalon()),client.getSocketChannel());

                try {
                    model.deleteClients(client);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                gui.majClientSalon();
            }
        });

        gui.getBoutton(3).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                log.setLogContent("Deconnection tous les client!",ServerLog.Level.INFO,ServerLog.Facility.SERVER);

                Iterator itr=model.getClients().iterator();

                while(itr.hasNext()){

                    ServerClients st=(ServerClients)itr.next();

                    ServerCompute.sendMsg("/quit",st.getSocketChannel());

                    try {
                        st.getSocketChannel().close();
                        itr.remove();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                gui.majClientSalon();
            }
        });

        gui.getBoutton(4).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                ServerClients client=(ServerClients) gui.getTableView().getSelectionModel().getSelectedItem();

                log.setLogContent("Client est banni : " + client.getNickname(),ServerLog.Level.INFO,ServerLog.Facility.SERVER);

                ServerCompute.sendMsg("/quit",client.getSocketChannel());
                ServerCompute.sendMsg("/deleteClient " + client.getNickname(),model.getClients(client.getSalon()),client.getSocketChannel());

                model.setBlackList(client.getIpAddress());

                try {
                    model.deleteClients(client);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                gui.majClientSalon();
            }
        });

        gui.getBoutton(5).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                String salonSupp=gui.getListView(0).getSelectionModel().getSelectedItem().toString();

                if (salonSupp.equals("Principal")) {
                    return;
                }

                log.setLogContent("Salon supp : " + salonSupp,ServerLog.Level.INFO,ServerLog.Facility.SERVER);

                ServerCompute.sendMsg("/quit",model.getClients(salonSupp));

                try {
                    model.deleteClients(model.getClients(salonSupp));
                } catch (IOException e) {
                    log.setLogContent("Echec de deco des clients !",ServerLog.Level.ERROR,ServerLog.Facility.SERVER);
                    return;
                }

                model.deleteSalons(salonSupp);
                gui.majClientSalon();
            }
        });

        gui.getBoutton(6).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                String clientBlacklistSupp=gui.getListView(1).getSelectionModel().getSelectedItem().toString();

                log.setLogContent("Supp client Blacklist : " + clientBlacklistSupp, ServerLog.Level.INFO, ServerLog.Facility.SERVER);

                model.deleteBlackListClient(clientBlacklistSupp);

                gui.getListView(1).setItems(FXCollections.observableList(model.getBlackList()));
            }
        });

    }

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
