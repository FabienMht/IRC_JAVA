import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class ServerController {

    private ServerGui gui;
    private ServerModel model;
    private ServerLog log;


    public ServerController(ServerGui a, ServerModel b,ServerLog c){
        this.gui=a;
        this.model=b;
        this.log=c;
        initListenners();
    }

    private void initListenners () {

        // Action event.

        gui.getChoiceBox().valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String oldValue, String newValue) {
                log.setLogLevel(ServerLog.Level.valueOf(newValue));

            }
        });

        gui.getBoutton(0).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                log.setLogContent("Demarrage du serveur !", ServerLog.Level.INFO, ServerLog.Facility.SERVER);
                model.setClients("Test","Test0","Test1");
                System.out.println(model.getClients());
                gui.getTableView().setItems(FXCollections.observableList(model.getClients()));
            }
        });

        gui.getBoutton(1).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                log.setLogContent("Arret du serveur !",ServerLog.Level.INFO,ServerLog.Facility.SERVER);
            }
        });

        gui.getBoutton(2).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                log.setLogContent("Deconnection client !",ServerLog.Level.INFO,ServerLog.Facility.SERVER);
                model.deleteClients((ServerClients) gui.getTableView().getSelectionModel().getSelectedItem());
            }
        });

        gui.getBoutton(3).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                log.setLogContent("Deconnection tous les client!",ServerLog.Level.INFO,ServerLog.Facility.SERVER);
            }
        });

        gui.getBoutton(4).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                log.setLogContent("Client est banni!",ServerLog.Level.INFO,ServerLog.Facility.SERVER);
            }
        });

        gui.getBoutton(5).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                log.setLogContent("Salon supp !",ServerLog.Level.INFO,ServerLog.Facility.SERVER);
            }
        });

    }
}
