import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ClientCompute extends Thread {

    private ClientModel model;
    private ClientGui gui;

    public ClientCompute (ClientModel a,ClientGui b) {
        this.model=a;
        this.gui=b;
    }

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
                        System.out.println(output);

                        if (output.startsWith("/listClient",0)) {

                            model.deleteAllClients();

                            String outputPrefix = new String(output.replaceFirst("/listClient","")).trim();

                            String[] listClient=outputPrefix.split(",");

                            for (String s : listClient) {
                                model.setClients(s);
                            }

                            gui.majClient();
                        }

                        else if (output.startsWith("/listSalon",0)) {

                            String outputPrefix = new String(output.replaceFirst("/listSalon","")).trim();

                            String[] listSalon=outputPrefix.split(",");

                            for (String s : listSalon) {
                                model.setSalons(s);
                            }

                            gui.majSalon();

                        } else if (output.startsWith("/addClient",0)) {

                            String outputPrefix = new String(output.replaceFirst("/addClient","")).trim();

                            model.setClients(outputPrefix);

                            gui.majClient();

                        } else if (output.startsWith("/deleteClient",0)) {

                            String outputPrefix = new String(output.replaceFirst("/deleteClient","")).trim();

                            model.deleteClients(outputPrefix);

                            gui.majClient();

                        } else if (output.startsWith("/addSalon",0)) {

                            String outputPrefix = new String(output.replaceFirst("/addSalon","")).trim();

                            model.setSalons(outputPrefix);

                            gui.majSalon();

                        } else if (output.startsWith("/deleteSalon",0)) {

                            String outputPrefix = new String(output.replaceFirst("/deleteSalon","")).trim();

                            model.deleteSalons(outputPrefix);

                            gui.majSalon();

                        } else if (output.startsWith("/erreur",0)) {

                            String outputPrefix = new String(output.replaceFirst("/erreur","")).trim();

                            gui.setStatus(ClientGui.Status.Error,outputPrefix);

                        } else if (output.startsWith("/quit",0)) {

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

                        } else {

                            String outputPrefix = new String(output).trim();
                            gui.setTextMsg(outputPrefix);
                            gui.setTextMsg("\n");
                        }

                    }

                    itr.remove();
                }
            }

            clientSocket.close();

            selector.close();

        } catch (Exception e) {

            gui.setStatus(ClientGui.Status.Error,"Connection refused");

            gui.getBoutton(0).setDisable(false);
            gui.getBoutton(1).setDisable(true);
            gui.getBoutton(2).setDisable(true);
            gui.getBoutton(3).setDisable(true);

            model.deleteAllClients();
            model.deleteAllSalons();

            gui.majClient();
            gui.majSalon();

        }
    }

    public static void sendMsg (String msg,SocketChannel client){

        byte[] message = msg.getBytes();
        ByteBuffer bufferClient = ByteBuffer.wrap(message);

        try {
            client.write(bufferClient);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(msg);

        bufferClient.clear();
    }

}
