import javafx.application.Platform;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class ClientCompute extends Thread {

    private ClientModel model;
    private ClientGui gui;
    private DateFormat df = new SimpleDateFormat("HH:mm:ss");
    private Date dateobj = new Date();

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

            ClientCompute.sendMsg("/setNickname " + gui.getTextField(2).getText(),model.getClientSocket());

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

                            model.deleteAllClients();

                            String outputPrefix = new String(output.replaceFirst("/listClient","")).trim();

                            String[] listClient=outputPrefix.split(",");

                            for (String s : listClient) {
                                model.setClients(s);
                            }

                            gui.majClient();
                        }

                        else if (output.startsWith("/listSalon",0)) {

                            System.out.println(output);

                            String outputPrefix = new String(output.replaceFirst("/listSalon","")).trim();

                            String[] listSalon=outputPrefix.split(",");

                            for (String s : listSalon) {
                                model.setSalons(s);
                            }

                            gui.majSalon();

                        } else if (output.startsWith("/addClient",0)) {

                            System.out.println(output);

                            String outputPrefix = new String(output.replaceFirst("/addClient","")).trim();

                            model.setClients(outputPrefix);

                            gui.majClient();

                        } else if (output.startsWith("/deleteClient",0)) {

                            System.out.println(output);

                            String outputPrefix = new String(output.replaceFirst("/deleteClient","")).trim();

                            model.deleteClients(outputPrefix);

                            gui.majClient();

                        } else if (output.startsWith("/addSalon",0)) {

                            System.out.println(output);

                            String outputPrefix = new String(output.replaceFirst("/addSalon","")).trim();

                            model.setSalons(outputPrefix);

                            gui.majSalon();

                        } else if (output.startsWith("/deleteSalon",0)) {

                            System.out.println(output);

                            String outputPrefix = new String(output.replaceFirst("/deleteSalon","")).trim();

                            model.deleteSalons(outputPrefix);

                            gui.majSalon();

                        } else if (output.startsWith("/quit",0)) {

                            System.out.println(output);

                            try {
                                model.getClientSocket().close();
                            } catch (IOException e) {
                            }

                            try {
                                model.getSelector().close();
                            } catch (IOException e) {
                            }

                            Platform.runLater(new Runnable() {
                                @Override public void run() {
                                    gui.clearClientSalon();
                                    gui.getAreaMsg().clear();
                                    gui.getBoutton(0).setDisable(false);
                                    gui.getBoutton(1).setDisable(true);
                                    gui.getBoutton(2).setDisable(true);
                                    gui.getBoutton(3).setDisable(true);
                                }
                            });


                        } else {

                            String outputPrefix = new String(output).trim();
                            gui.setTextMsg(df.format(dateobj) + " " + outputPrefix);
                            gui.setTextMsg("\n");
                        }

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
