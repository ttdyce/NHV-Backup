package com.ttdyce;

// Java implementation of Server side
// It contains two classes : Server and ClientHandler
// Save file as Server.java

import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.github.ttdyce.nhviewer.model.room.ComicCachedEntity;
import com.github.ttdyce.nhviewer.model.room.ComicCollectionEntity;

// Server class
public class RestoreServer {
    private static final String QR_CODE_IMAGE_PATH = "./ipQRCode.png";

    private static void generateQRCodeImage(String text, int width, int height, String filePath)
            throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }

    public static void main(final String[] args) throws IOException {
        // get local lan ip
        String localIp;
        
        // tested on windows ok, macos returns 0.0.0.0
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            localIp = socket.getLocalAddress().getHostAddress();
        }
        if (localIp.equals("0.0.0.0"))
            try (Socket socket = new Socket()) {
                // tested work on macos
                socket.connect(new InetSocketAddress("google.com", 80));
                localIp = socket.getLocalAddress().getHostAddress();
            }

        String port = "3333";
        String jsonText = String.format("{\"action\" : \"restore\", \"ip\": \"%s\", \"port\": \"%s\"}", localIp, port);

        // generate QRCode storing the ip
        try {
            generateQRCodeImage(jsonText, 300, 300, QR_CODE_IMAGE_PATH);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        // show the QRCode
        EventQueue.invokeLater(() -> {
            DisplayImage ex = new DisplayImage(QR_CODE_IMAGE_PATH);
            ex.setVisible(true);
        });

        final ServerSocket ss = new ServerSocket(3333);
        // running infinite loop for getting
        // client request
        // while (true) {
        Socket s = null;

        try {
            // socket object to receive incoming client requests
            s = ss.accept();

            System.out.println("A new client is connected : " + s);

            // obtaining input and out streams
            final ObjectInputStream dis = new ObjectInputStream(s.getInputStream());
            final ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());

            System.out.println("Assigning new thread for this client");

            // handle backup task to a thread
            final Thread t = new RestoreHandler(s, dis, dos);
            t.start();

        } catch (final Exception e) {
            // s.close();
            e.printStackTrace();
        } finally {
            ss.close();
        }
        // }
    }
}

// ClientHandler class
class RestoreHandler extends Thread {
    final ObjectInputStream objectInputStream;
    final ObjectOutputStream objectOutputStream;
    final Socket s;

    // Constructor
    public RestoreHandler(final Socket s, final ObjectInputStream objectInputStream, final ObjectOutputStream objectOutputStream) {
        this.s = s;
        // try {
        // this.s.setSoTimeout(5000);
        // } catch (SocketException e) {
        // e.printStackTrace();
        // }
        this.objectInputStream = objectInputStream;
        this.objectOutputStream = objectOutputStream;
    }

    @Override
    public void run() {

        final String path = "./NHServer_backups/";
        final File dir = new File(path);

        if (!dir.exists())
            dir.mkdir();

        try (
                FileInputStream finCollections = new FileInputStream(new File(path + "collections"));
                ObjectInputStream oisCollections = new ObjectInputStream(finCollections);

                FileInputStream finComics = new FileInputStream(new File(path + "comics"));
                ObjectInputStream oisComics = new ObjectInputStream(finComics);
        ) {
            System.out.println("Reading object from file...");
            List<ComicCollectionEntity> collectionEntities = (List<ComicCollectionEntity>)oisCollections.readObject();
            List<ComicCachedEntity> comicCachedEntities = (List<ComicCachedEntity>) oisComics.readObject();

            System.out.println("Writing collectionEntities to stream...");
            objectOutputStream.writeObject(collectionEntities);
            objectOutputStream.flush();
            String response = objectInputStream.readUTF();

            System.out.println("Writing comicCachedEntities to stream...");
            objectOutputStream.writeObject(comicCachedEntities);
            objectOutputStream.flush();
            response = objectInputStream.readUTF();

            System.out.println("ending...");
            objectInputStream.readUTF();
            objectOutputStream.writeUTF("ACK");
            objectOutputStream.flush();

            objectOutputStream.writeUTF("FIN");
            objectOutputStream.flush();
            objectInputStream.readUTF();
            System.out.println("ended");

            s.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
