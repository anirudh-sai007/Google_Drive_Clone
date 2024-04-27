import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class Client22 {
    public static final int CHUNK_SIZE = 8192;
    public static final int MAX_PACKET_SIZE = 65507; // Maximum size of a UDP packet
//int port = 9877;

    static int c=0;

    public static void main(String args[]) throws Exception {
        InetAddress IPAddress = InetAddress.getByName("localhost");
       int  port = 9877;
        runClient(IPAddress, port);
    }

    public static void runClient(InetAddress IPAddress, int port) throws Exception {
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        DatagramSocket clientSocket = new DatagramSocket();
        clientSocket.setSoTimeout(5000);
        byte[] sendData = new byte[CHUNK_SIZE];
        byte[] receiveData = new byte[CHUNK_SIZE];

        IPAddress = InetAddress.getByName("localhost");

        while (true) {
            c=0;
            System.out.print("Enter command: ");
            String command = inFromUser.readLine();
            if (command.startsWith("upload")) {
                // Send file to server
                String filename = command.substring(7);
                File file = new File(filename);
                List<byte[]> chunks = splitFile(file, CHUNK_SIZE);
                int numChunks = (int) Math.ceil((double) file.length() / CHUNK_SIZE);
                sendData = String.format("upload %s %d", file.getName(), numChunks).getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                clientSocket.send(sendPacket);
                for (int i = 0; i < numChunks; i++) {
                    boolean acknowledged = false;
                    while (!acknowledged) {
                        byte[] chunk = chunks.get(i);
                        sendPacket = new DatagramPacket(chunk, chunk.length, IPAddress, port);
                        clientSocket.send(sendPacket);

                        byte[] ackData = new byte[4];
                        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length);
                        try {
                            clientSocket.receive(ackPacket);
                            int receivedChunkIndex = ByteBuffer.wrap(ackPacket.getData()).getInt();
                            if (receivedChunkIndex == i) {
                                acknowledged = true;
                            }
                        } catch (SocketTimeoutException e) {
                            System.out.println("sending chunk " + c);
                            c++;
                        }
                    }
                }
            } else if (command.startsWith("download")) {
                String filename = command.substring(9);
                download(clientSocket, IPAddress, filename,port);
            } else if (command.startsWith("update")) {
                // Send updated file to server
String filename = command.substring(7);
File originalFile = new File("C:\\Users\\Aniruh\\OneDrive\\Desktop\\Project_Team10\\client2\\" + filename);
File updatedFile = new File("C:\\Users\\Aniruh\\OneDrive\\Desktop\\Project_Team10\\client2\\temp\\" + filename);
List<byte[]> diffChunks = getDiffChunks(originalFile, updatedFile, CHUNK_SIZE);
int numChunks = diffChunks.size();
sendData = String.format("upload %s %d", filename, numChunks).getBytes();
DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
clientSocket.send(sendPacket);
for (int i = 0; i < diffChunks.size(); i++) {
    byte[] chunk = diffChunks.get(i);
    System.out.println("Sending chunk " + (i+1) + " of " + numChunks + " with size " + chunk.length);
    DatagramPacket sendPacket2 = new DatagramPacket(chunk, chunk.length, IPAddress, port);
    clientSocket.send(sendPacket2);
}

            }
else if (command.startsWith("delete")) {
    String filename = command.substring(7);
    File serverFile = new File("C:\\Users\\Aniruh\\OneDrive\\Desktop\\Project_Team10\\serverFolder\\" + filename);
    File clientFile = new File("C:\\Users\\Aniruh\\OneDrive\\Desktop\\Project_Team10\\client\\" + filename);
File clientFile2 = new File("C:\\Users\\Aniruh\\OneDrive\\Desktop\\Project_Team10\\client2\\" + filename);
File clientFiletemp2 = new File("C:\\Users\\Aniruh\\OneDrive\\Desktop\\Project_Team10\\client2\\temp\\" + filename);
File clientFiletemp1 = new File("C:\\Users\\Aniruh\\OneDrive\\Desktop\\Project_Team10\\client\\temp\\" + filename);
    File chunksDir = new File("C:\\Users\\Aniruh\\OneDrive\\Desktop\\Project_Team10\\serverFolder\\chunks\\" + filename + "_chunks");
    boolean serverFileDeleted = serverFile.delete();
    boolean clientFileDeleted = clientFile.delete();
boolean clientFiletemp11 = clientFiletemp1.delete();
boolean clientFiletemp22 = clientFiletemp2.delete();
boolean clientFileDeleted2 = clientFile2.delete();

    boolean chunksDirDeleted = deleteDirectory(chunksDir);
    if (serverFileDeleted && clientFileDeleted && clientFileDeleted2 && chunksDirDeleted  ) {
        System.out.println("File and chunks deleted successfully.");
    } else {
        System.out.println("Failed to delete file and chunks.");
    }
}

else {
                sendData = command.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                clientSocket.send(sendPacket);

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);

                String response = new String(receivePacket.getData());
                System.out.println("Response from server: " + response);
            }
        }
    }
public static List<byte[]> splitFile(File file, int chunkSize) throws IOException {
    List<byte[]> chunks = new ArrayList<>();
    try (FileInputStream fis = new FileInputStream(file)) {
        byte[] buffer = new byte[chunkSize];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            byte[] chunk = new byte[bytesRead];
            System.arraycopy(buffer, 0, chunk, 0, bytesRead);
            chunks.add(chunk);
        }
    }
    return chunks;
}

private static boolean deleteDirectory(File directory) {
    if(directory.exists()){
        File[] files = directory.listFiles();
        if(null!=files){
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
    }
    return(directory.delete());
}
private static List<byte[]> getDiffChunks(File originalFile, File updatedFile, int chunkSize) throws IOException {
    List<byte[]> diffChunks = new ArrayList<>();
    try (InputStream originalStream = new FileInputStream(originalFile);
         InputStream updatedStream = new FileInputStream(updatedFile)) {
        byte[] originalChunk = new byte[chunkSize];
        byte[] updatedChunk = new byte[chunkSize];
        int offset = 0;
        while (true) {
            int originalBytesRead = originalStream.read(originalChunk);
            int updatedBytesRead = updatedStream.read(updatedChunk);
            if (originalBytesRead == -1 && updatedBytesRead == -1) {
                break;
            }
            if (originalBytesRead != updatedBytesRead || !Arrays.equals(originalChunk, updatedChunk)) {
                int start = offset * chunkSize;
                int end = start + chunkSize;
                byte[] diffChunk = Arrays.copyOfRange(updatedChunk, 0, updatedBytesRead);
                diffChunks.add(diffChunk);
            }
            offset++;
        }
    }
    return diffChunks;
}

public static void download(DatagramSocket clientSocket, InetAddress IPAddress, String filename, int port) throws IOException {
    // Send command to server
    String command = "download " + filename;
    byte[] sendData = command.getBytes();
    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
    clientSocket.send(sendPacket);

    // Receive file from server and save to disk
    byte[] receiveData = new byte[MAX_PACKET_SIZE];
    DatagramPacket fileSizePacket = new DatagramPacket(receiveData, receiveData.length);
    clientSocket.receive(fileSizePacket);
    long fileSize = ByteBuffer.wrap(fileSizePacket.getData()).getLong();
    FileOutputStream fos = new FileOutputStream(filename);
    long bytesReceived = 0;
    while (bytesReceived < fileSize) {
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        byte[] buffer = receivePacket.getData();
        fos.write(buffer, 0, receivePacket.getLength());
        bytesReceived += receivePacket.getLength();
    }
    fos.close();
    System.out.println("File downloaded successfully.");
}
}