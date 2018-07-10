import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ClientController {

    private ClientGui gui;
    private ClientModel model;
    private Stage stage;


    public ClientController(ClientGui a, ClientModel b,Stage c){
        this.gui=a;
        this.model=b;
        this.stage=c;
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
                    file=gui.showMsgSaver(stage);
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(gui.getTextMsg().replaceAll("\n", System.getProperty("line.separator")));
                    fileWriter.close();

                } catch (IOException |NullPointerException ex) {
                    return;
                }

            }
        });

        gui.getMenuItems(4).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                gui.setLicenceWindow();
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
                    return;
                }

                ClientCompute compute=new ClientCompute(model,gui);
                compute.start();

                gui.majClientSalon();

            }
        });

        gui.getBoutton(1).setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                try {
                    model.getClientSocket().close();
                } catch (IOException e) {
                }

                try {
                    model.getSelector().close();
                } catch (IOException e) {
                }

                ClientCompute.sendMsg("/quit",model.getClientSocket());
                gui.clearClientSalon();
            }
        });

        gui.getBoutton(2).setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                model.setSalons(gui.getTextField(0).getText());
                ClientCompute.sendMsg("/addSalon " + gui.getTextField(3).getText(),model.getClientSocket());
                gui.majClientSalon();
            }
        });

        gui.getBoutton(3).setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                ClientCompute.sendMsg(gui.getTextField(4).getText(),model.getClientSocket());
            }
        });
    }

}
