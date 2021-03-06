import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.net.*;
import java.io.*;

public class TCPMasterServer extends Thread {
    public static int masterServerPort = 7777;
    public static String serverHost = "localhost";
    public static ArrayList<FileInfo> listFileInfo = new ArrayList<FileInfo>();

    public static void main(String[] args) {
        ServerSocket server = null;

        try {

            // server is listening on port 1234
            server = new ServerSocket(masterServerPort);
            server.setReuseAddress(true);

            System.out.println("Master Server is starting");

            // running infinite loop for getting
            // client request
            while (true) {

                // socket object to receive incoming client
                // requests
                Socket client = server.accept();

                // Displaying that new client is connected
                // to server
                System.out.println("=================\nNew client connected " + client.getInetAddress().getHostAddress());

                // create a new thread object
                ClientHandler clientSock = new ClientHandler(client);

                // This thread will handle the client
                // separately
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void removeFileByServer(String host, int port) {
        FileInfo file = null;
        for (FileInfo fileInfo : listFileInfo) {
            if (fileInfo.getHost().equals(host) && fileInfo.getPort() == port)
                file = fileInfo;
        }

        listFileInfo.remove(file);
    }

    
    public static void storageInfo(String filesName, String connectionInfo) {
        String storageFileName = String.join("_", connectionInfo.split(":"));
        try {
            FileWriter myWriter = new FileWriter("TCPMasterServer/" + storageFileName + ".txt");
            myWriter.write(filesName);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    // ClientHandler class
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        // Constructor
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }   

        public void run() {
            String clientType = "";
            String filesName = "";
            String fileServerConnectionInfomation = "";

            try {
                InputStream type 
                = clientSocket.getInputStream();
                DataInputStream clientTypeInput = new DataInputStream(type);
                clientType = clientTypeInput.readUTF();

                switch (clientType) {
                    case "0":
                        System.out.println("Getting info from FileServer...");
                        InputStream inputFromFileServer = clientSocket.getInputStream();
                        DataInputStream dataInputStreamFromFileServer = new DataInputStream(inputFromFileServer);
                        filesName = dataInputStreamFromFileServer.readUTF();
                        System.out.println("- List of files name:\n" + filesName);
                        DataInputStream fileServerConnectionInfo = new DataInputStream(inputFromFileServer);
                        fileServerConnectionInfomation = fileServerConnectionInfo.readUTF();
                        System.out.println("- Host:Port - " + fileServerConnectionInfomation);
                        storageInfo(filesName, fileServerConnectionInfomation);
                        break;

                    case "1":
                        System.out.println("Sending FileServer info to Client...");
                        OutputStream outputStream = clientSocket.getOutputStream();
                        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                        dataOutputStream.writeUTF("DSFSDFD");
                        dataOutputStream.flush();
                        dataOutputStream.close();
                        
                        System.out.println("Terminating!");
                        break;

                    default:
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                System.out.println("Closed connection!\n=================");
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // tam thoi em stop o day:
            /*
            PrintWriter out = null;
            BufferedReader in = null;
            try {

                // get the outputstream of client
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                // get the inputstream of client
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // String line;
                // while ((line = in.readLine()) != null) {

                // // writing the received message from
                // // client
                // System.out.printf(
                // " Sent from the client: %s\n",
                // line);
                // out.println(line);
                // }


                // NEED TO REMOVE //////
                // sample data for client test
                for (int i = 0; i < 3; i++) {
                    FileInfo file = new FileInfo();
                    file.setFilename("abc"+i);
                    file.setInfo(clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());

                    listFileInfo.add(file);
                }
                //////////////////////

                int numOfFile = 0;

                String status;
                while ((status = in.readLine()) != null) {
                    // -1: null | 1: Server send File | 2: Server is down | 3: Client request file
                    // info
                    switch (status) {
                        case "1":
                            out.println("master server received services 1");
                            System.out.println("master server received services 1");
                            // receive number of size to initial loop
                            numOfFile = Integer.parseInt(in.readLine());
                            System.out.println("master server received files: " + numOfFile);
                            // cache every file to List
                            for (int i = 0; i < numOfFile; i++) {
                                FileInfo file = new FileInfo();
                                file.setFilename(in.readLine());
                                file.setInfo(clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());

                                listFileInfo.add(file);
                            }
                            break;

                        case "2":
                            out.println("master server received server down");
                            System.out.println("master server received server down");
                            System.out.println("master server remove files: " + numOfFile);
                            // find file by name and current client ip
                            for (int i = 0; i < numOfFile; i++) {
                                removeFileByServer(clientSocket.getInetAddress().getHostAddress(),clientSocket.getPort());
                            }
                            break;

                        case "3":
                            // out.println("master server received services 2");
                            System.out.println("master server received services 2");
                            // send the size of ArrayList for Client to Read
                            out.println(listFileInfo.size());
                            // then send file by file
                            for (int i = 0; i < listFileInfo.size(); i++) {
                                out.println(listFileInfo.get(i).getFilename());
                                out.println(listFileInfo.get(i).getHost());
                                out.println(listFileInfo.get(i).getPort());
                            }
                            break;

                        default:
                            out.println("master server received meanless command");
                            System.out.println("master server received meanless command");
                            break;
                    }

                    // for (FileInfo fileInfo : listFileInfo) {
                    //     System.out.println(fileInfo.getFilename());
                    //     System.out.println(fileInfo.getHost());
                    //     System.out.println(fileInfo.getPort());
                    // }
                    System.out.println(listFileInfo.size());

                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            */
        }
    }
}
