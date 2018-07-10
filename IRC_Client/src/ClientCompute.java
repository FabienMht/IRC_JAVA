import javafx.collections.FXCollections;

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
            model.setSelector(selector);

            ClientCompute.sendMsg("/setNickname" + gui.getTextField(2).getText(),model.getClientSocket());
            ClientCompute.sendMsg("/getSalon",model.getClientSocket());
            ClientCompute.sendMsg("/getNickname Principal",model.getClientSocket());

            while (true) {

                int numberKeys = selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> itr = selectedKeys.iterator();


                while (itr.hasNext()) {

                    SelectionKey cle = itr.next();

                    if (cle.isReadable()) {

                        SocketChannel client = (SocketChannel) cle.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        client.read(buffer);

                        String output = new String(buffer.array()).trim();

                        if (output.startsWith("/listClient",0)) {

                            System.out.println(output);

                            String outputPrefix = new String(output.replaceFirst("/listClient","")).trim();

                            String[] listClient=outputPrefix.split(",");

                            for (String s : listClient) {
                                model.setClients(s);
                            }

                            gui.majClientSalon();
                        }

                        else if (output.startsWith("/listSalon",0)) {

                            System.out.println(output);

                            String outputPrefix = new String(output.replaceFirst("/listSalon","")).trim();

                            String[] listSalon=outputPrefix.split(",");

                            for (String s : listSalon) {
                                model.setSalons(s);
                            }

                            gui.majClientSalon();

                        }

                        /*else if (output.startsWith("/getSalon",0)) {

                            String salonFormat=model.getSalonsFormat();
                            byte[] message = salonFormat.getBytes();
                            ByteBuffer bufferClient = ByteBuffer.wrap(message);
                            client.write(bufferClient);

                            System.out.println(salonFormat);

                            bufferClient.clear();

                        }

                        else if (output.startsWith("/setSalon",0)) {

                            String outputPrefix = new String(output.replaceFirst("/setSalon","")).trim();
                            String ancienSalon=model.getClients(client).getSalon();

                            model.getClients(client).setSalon(outputPrefix);
                            gui.getTableView().setItems(FXCollections.observableList(model.getClients()));

                            log.setLogContent("Nouveau salon pour " + model.getClients(client).getNickname() + " -> Ancien : " + ancienSalon + " Nouveau : " + outputPrefix, ServerLog.Level.INFO, ServerLog.Facility.SERVER);
                        }

                        else if (output.startsWith("/addSalon",0)) {

                            String outputPrefix = new String(output.replaceFirst("/addSalon","")).trim();

                            model.setSalons(outputPrefix);
                            gui.getListView(0).setItems(FXCollections.observableList(model.getSalons()));
                            log.setLogContent("Ajout salon " + outputPrefix + " par " + model.getClients(client).getNickname(), ServerLog.Level.INFO, ServerLog.Facility.SERVER);
                        }

                        else if (output.startsWith("/quit",0)) {

                            log.setLogContent("Decconnection client : " + model.getClients(client).getNickname(), ServerLog.Level.WARNING, ServerLog.Facility.SERVER);
                            client.close();
                            model.deleteClients(model.getClients(client));
                            gui.getTableView().setItems(FXCollections.observableList(model.getClients()));
                        }

                        else {
                            String salonWrite=model.getSalons(client);

                            Iterator itrClientSalon=model.getClients(salonWrite).iterator();

                            byte [] message = output.getBytes();
                            ByteBuffer bufferBroadcast = ByteBuffer.wrap(message);

                            while(itrClientSalon.hasNext()){

                                ServerClients st=(ServerClients)itrClientSalon.next();

                                st.getSocketChannel().write(bufferBroadcast);
                            }

                            bufferBroadcast.clear();
                        }*/

                    }
                    itr.remove();
                }
            }

        } catch (Exception e) {

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
