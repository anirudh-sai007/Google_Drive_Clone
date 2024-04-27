import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class UDPServerr {

    public static final int CHUNK_SIZE = 8192;

    public static void main(String args[]) throws Exception {
        Thread client1Thread = new Thread(new ClientHandler(9876));
        Thread client2Thread = new Thread(new ClientHandler(9877));

        client1Thread.start();
        client2Thread.start();

        client1Thread.join();
        client2Thread.join();
    }

    public static class ClientHandler implements Runnable {

        private int port;

        public ClientHandler(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            try {
                DatagramSocket serverSocket = new DatagramSocket(port);

                byte[] receiveData = new byte[CHUNK_SIZE];
                byte[] sendData = new byte[CHUNK_SIZE];

                while (true) {

DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            String command = new String(receivePacket.getData(), 0, receivePacket.getLength());
String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
System.out.println("Received from port " + port + ": " + receivedMessage);
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
if (command.startsWith("upload")) {
    // Receive file from client and save to disk
    System.out.println(command);
    String[] tokens = command.split(" ");
    String filename = tokens[1];
    String directoryName = filename + "_chunks";
    int numChunks = Integer.parseInt(tokens[2]);

    File chunksDir = new File("C:\\Users\\Aniruh\\OneDrive\\Desktop\\Project_Team10\\serverFolder\\chunks\\" + directoryName);
    if (!chunksDir.exists()) {
        chunksDir.mkdir();
    }

    String serverFilePath = "C:\\Users\\Aniruh\\OneDrive\\Desktop\\Project_Team10\\serverFolder\\" + filename;
    File file = new File(serverFilePath);
    FileOutputStream fos = new FileOutputStream(file);

    String clientPath = "C:\\Users\\Aniruh\\OneDrive\\Desktop\\Project_Team10\\client\\" + filename;
String clientPath2 = "C:\\Users\\Aniruh\\OneDrive\\Desktop\\Project_Team10\\client2\\" + filename;
String clientPathtemp1 = "C:\\Users\\Aniruh\\OneDrive\\Desktop\\Project_Team10\\client\\temp\\" + filename;
String clientPathtemp2 = "C:\\Users\\Aniruh\\OneDrive\\Desktop\\Project_Team10\\client2\\temp\\" + filename;
    FileOutputStream clientFos = new FileOutputStream(clientPath2);

    FileOutputStream clientFos2 = new FileOutputStream(clientPath);

File filet1 = new File(clientPathtemp1);
    FileOutputStream fost1 = new FileOutputStream(filet1);
File filet2 = new File(clientPathtemp2);
    FileOutputStream fost2 = new FileOutputStream(filet2);


    long bytesReceived = 0;
    byte[] buffer = new byte[8192];
    for (int i = 0; i < numChunks; i++) {
        DatagramPacket receivePacket2 = new DatagramPacket(buffer, buffer.length);
        serverSocket.receive(receivePacket2);
        byte[] chunk = Arrays.copyOf(receivePacket2.getData(), receivePacket2.getLength());
        fos.write(chunk, 0, chunk.length);
        
        clientFos.write(chunk, 0, chunk.length);
fost1.write(chunk, 0, chunk.length);

fost2.write(chunk, 0, chunk.length);

clientFos2.write(chunk, 0, chunk.length);
 sendData = ByteBuffer.allocate(4).putInt(i).array();
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                            serverSocket.send(sendPacket);
                            System.out.println("Acknowledgment sent for chunk " + i);
        File chunkFile = new File(chunksDir, "chunk" + i);
        FileOutputStream chunkFos = new FileOutputStream(chunkFile);
        chunkFos.write(chunk, 0, chunk.length);
        chunkFos.close();

        bytesReceived += receivePacket2.getLength();
        System.out.println("Received chunk " + i);
    sendData = ByteBuffer.allocate(Integer.BYTES).putInt(i).array();

    }

    fos.close();
    clientFos.close();
clientFos2.close();

    
    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
    serverSocket.send(sendPacket);
System.out.println("Finished receiving " + numChunks + " chunks");
}
else if (command.startsWith("sending")) {
    // Receive file from client and save to disk
    String[] tokens = command.split(" ");
    String filename = tokens[1];
    String directoryName = filename + "_chunks";
    int numChunks = Integer.parseInt(tokens[2]);

    File chunksDir = new File("C:\\Users\\Aniruh\\OneDrive\\Desktop\\Project_Team10\\serverFolder\\chunks\\" + directoryName);
    if (!chunksDir.exists()) {
        chunksDir.mkdir();
    }

    String serverFilePath = "C:\\Users\\Aniruh\\OneDrive\\Desktop\\Project_Team10\\serverFolder\\" + filename;
    File file = new File(serverFilePath);
    FileOutputStream fos = new FileOutputStream(file);

    String clientPath = "C:\\Users\\Aniruh\\OneDrive\\Desktop\\Project_Team10\\client2\\" + filename;
    FileOutputStream clientFos = new FileOutputStream(clientPath);

    long bytesReceived = 0;
    for (int i = 0; i < numChunks; i++) {
        DatagramPacket receivePacket2 = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket2);
        byte[] chunk = receivePacket2.getData();
        fos.write(chunk, 0, receivePacket2.getLength());
        clientFos.write(chunk, 0, receivePacket2.getLength());

        File chunkFile = new File(chunksDir, "chunk" + i);
        FileOutputStream chunkFos = new FileOutputStream(chunkFile);
        chunkFos.write(chunk, 0, receivePacket2.getLength());
        chunkFos.close();

        bytesReceived += receivePacket2.getLength();
        System.out.println("Sent chunk " + i);
    }

    fos.close();
    clientFos.close();

    sendData = ByteBuffer.allocate(Long.BYTES).putLong(bytesReceived).array();
    System.out.println("Finished sending " + numChunks + " chunks");
    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
    serverSocket.send(sendPacket);
} else if (command.startsWith("download")) {
                // Send file to client
                String filename = command.substring(9);
                File file = new File(filename);
                FileInputStream fis = new FileInputStream(file);
                long fileSize = file.length();
                ByteBuffer fileSizeBuffer = ByteBuffer.allocate(Long.BYTES);
                fileSizeBuffer.putLong(fileSize);
                sendData = fileSizeBuffer.array();
                DatagramPacket fileSizePacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                serverSocket.send(fileSizePacket);

                byte[] buffer = new byte[Client11.CHUNK_SIZE];
                while (fis.read(buffer) != -1) {
                    DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, IPAddress, port);
                    serverSocket.send(sendPacket);
                }
                fis.close();
            }






else {
                // Handle other commands
                String response = "Command not recognized.";
                sendData = response.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                serverSocket.send(sendPacket);
            }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}