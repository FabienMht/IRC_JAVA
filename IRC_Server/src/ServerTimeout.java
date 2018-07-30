import java.io.IOException;
import java.util.ArrayList;

public class ServerTimeout extends Thread {

    private ServerLog log;
    private ServerModel model;
    private ServerGui gui;
    private ServerCompute compute;

    public ServerTimeout (ServerLog a,ServerModel b,ServerGui c,ServerCompute d) {
        this.log=a;
        this.model=b;
        this.gui=c;
        this.compute=d;
    }

    public void run() {

        while (model.getStop()) {

            System.out.println("TimeB");

            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("TimeA");

            ArrayList<ServerClients> clientsListTimeout = new ArrayList<ServerClients>();

            clientsListTimeout = model.changeTimeout();

            if (!clientsListTimeout.isEmpty()) {

                try {
                    System.out.println("Client");
                    //compute.sendMsg("/quit Timeout depasse",model.getClients());
                    //model.deleteClients(clientsListTimeout);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

    }
}
