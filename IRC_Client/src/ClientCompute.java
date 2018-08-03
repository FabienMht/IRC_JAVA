import javafx.application.Platform;
import javafx.collections.FXCollections;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 Classe qui permet de gérer les socketchannel à l'aide d 'un thread
 */
public class ClientCompute extends Thread {

    private ClientModel model;
    private ClientGui gui;

    public ClientCompute (ClientModel a,ClientGui b) {
        this.model=a;
        this.gui=b;
    }

    /**
     Méthode run qui permet d'initialiser le selecteur et le socket channel
     */
    public void run() {

        try {

            Selector selector = Selector.open();

            InetSocketAddress hA = new InetSocketAddress(model.getIpAddress(), model.getPort());
            SocketChannel clientSocket = SocketChannel.open(hA);
            clientSocket.configureBlocking(false);

            clientSocket.register(selector, SelectionKey.OP_READ);

            model.setClientSocket(clientSocket);

            ClientCompute.sendMsg("/setNickname " + gui.getTextField(2).getText(),model.getClientSocket());

            while (model.getStop()) {

                if (!clientSocket.isConnected()){
                    model.setStop(false);
                }

                int numberKeys = selector.select(500);

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> itr = selectedKeys.iterator();


                while (itr.hasNext()) {

                    SelectionKey cle = itr.next();

                    if (cle.isReadable()) {

                        SocketChannel client = (SocketChannel) cle.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        client.read(buffer);

                        String output = new String(buffer.array()).trim();
                        String[] outputSplit = output.split("¨");

                        for (String commande : outputSplit) {

                            if (commande.startsWith("/listClient", 0)) {

                                model.deleteAllClients();

                                String outputPrefix = new String(commande.replaceFirst("/listClient", "")).trim();

                                String[] listClient = outputPrefix.split(",");

                                for (String s : listClient) {
                                    model.setClients(s);
                                }

                                majClient();

                            } else if (commande.startsWith("/listSalon", 0)) {

                                String outputPrefix = new String(commande.replaceFirst("/listSalon", "")).trim();

                                String[] listSalon = outputPrefix.split(",");

                                for (String s : listSalon) {
                                    model.setSalons(s);
                                }

                                majSalon();

                            } else if (commande.startsWith("/addClient", 0)) {

                                String outputPrefix = new String(commande.replaceFirst("/addClient", "")).trim();

                                model.setClients(outputPrefix);

                                setTextMsg("Client ajouté : " + outputPrefix + "\n");

                                majClient();

                            } else if (commande.startsWith("/updateClient", 0)) {

                                String outputPrefix = new String(commande.replaceFirst("/updateClient", "")).trim();

                                String[] split = outputPrefix.split(",", 2);

                                model.updateNickname(split[0], split[1]);

                                setTextMsg("Client ancien nom : " + split[0] + " nouveau nom : " + split[1] + "\n");

                                majClient();

                            } else if (commande.startsWith("/deleteClient", 0)) {

                                String outputPrefix = new String(commande.replaceFirst("/deleteClient", "")).trim();

                                model.deleteClients(outputPrefix);

                                setTextMsg("Client supp : " + outputPrefix + "\n");

                                majClient();

                            } else if (commande.startsWith("/addSalon", 0)) {

                                String outputPrefix = new String(commande.replaceFirst("/addSalon", "")).trim();

                                model.setSalons(outputPrefix);

                                setTextMsg("Salon ajouté : " + outputPrefix + "\n");

                                majSalon();

                            } else if (commande.startsWith("/deleteSalon", 0)) {

                                String outputPrefix = new String(commande.replaceFirst("/deleteSalon", "")).trim();

                                model.deleteSalons(outputPrefix);

                                setTextMsg("Salon supp : " + outputPrefix + "\n");

                                majSalon();

                            } else if (commande.startsWith("/erreur", 0)) {

                                String outputPrefix = new String(commande.replaceFirst("/erreur", "")).trim();

                                setStatus(ClientGui.Status.Error, outputPrefix);

                            } else if (commande.startsWith("/quit", 0)) {

                                String outputPrefix = new String(commande.replaceFirst("/quit", "")).trim();

                                setStatus(ClientGui.Status.Disconnected, outputPrefix);

                                model.setStop(false);

                                model.deleteAllClients();
                                model.deleteAllSalons();

                                disconnect();

                            } else {

                                String outputPrefix = new String(commande).trim();
                                setTextMsg(outputPrefix + "\n");
                            }
                        }

                    }

                    itr.remove();
                }
            }

            clientSocket.close();

            selector.close();

        } catch (Exception e) {

            setStatus(ClientGui.Status.Error,"Connection refused");

            model.deleteAllClients();
            model.deleteAllSalons();

            disconnect();

        }
    }

    /**
     Permet d'envoyer un msg à un client dont le socketchannel est donné en paramètre
     */
    public static void sendMsg (String msg,SocketChannel client){

        msg=msg+"¨";
        byte[] message = msg.getBytes();
        ByteBuffer bufferClient = ByteBuffer.wrap(message);

        try {
            client.write(bufferClient);
        } catch (IOException e) {
            e.printStackTrace();
        }

        bufferClient.clear();
    }

    /**
     Mets à jour la liste des clients.
     */
    public void majClient (){

        Platform.runLater(new Runnable() {
            public void run() {
                gui.majClient();
            }
        });

    }

    /**
     Mets à jour la liste des salons.
     */
    public void majSalon (){

        Platform.runLater(new Runnable() {
            public void run() {
                gui.majSalon();
            }
        });

    }

    /**
     Modifie le status de la GUI.
     */
    public void setStatus(ClientGui.Status status, String msg){

        Platform.runLater(new Runnable() {
            public void run() {
                gui.setStatus(status,msg);
            }
        });
    }

    /**
     Permet d'ajouter des msgs à l'écran.
     */
    public void setTextMsg(String a){

        Platform.runLater(new Runnable() {
            public void run() {
                gui.setTextMsg(a);
            }
        });

    }

    public void disconnect (){

        Platform.runLater(new Runnable() {
            public void run() {

                majClient();
                majSalon();

                gui.getAreaMsg().clear();

                gui.getBoutton(0).setDisable(false);
                gui.getBoutton(1).setDisable(true);
                gui.getBoutton(2).setDisable(true);
                gui.getBoutton(3).setDisable(true);
            }
        });


    }

}
