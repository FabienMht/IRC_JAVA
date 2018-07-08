import javafx.stage.Stage;

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

    }

}
