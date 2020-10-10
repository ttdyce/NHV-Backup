package com.ttdyce;

// Java implementation of Server side
// It contains two classes : Server and ClientHandler
// Save file as Server.java
import java.awt.EventQueue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
import com.ttdyce.nhviewer.model.room.ComicCachedEntity;
import com.ttdyce.nhviewer.model.room.ComicCollectionEntity;

// Server class
public class BackupServer {
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
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            localIp = socket.getLocalAddress().getHostAddress();
        }
        String port = "3333";
        String jsonText = String.format("{\"action\" : \"backup\", \"ip\": \"%s\", \"port\": \"%s\"}", localIp, port);

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
            final Thread t = new BackupHandler(s, dis, dos);
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
class BackupHandler extends Thread {
    final ObjectInputStream objectInputStream;
    final ObjectOutputStream objectOutputStream;
    final Socket s;

    // Constructor
    public BackupHandler(final Socket s, final ObjectInputStream objectInputStream, final ObjectOutputStream objectOutputStream) {
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

        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HHmm");
        final Date date = new Date();

        String tableName;

        final String path = ".\\NHServer_backups\\";
        final String fileName = String.format("NHCollections %s.zip", dateFormat.format(date));
        final File dir = new File(path);
        final File file = new File(path + fileName);

        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        if (!dir.exists())
            dir.mkdir();

        try {
            System.out.println("reading numOfTables...");
            final int numOfTables = objectInputStream.readInt();
            System.out.println("read numOfTables = " + numOfTables);
            objectOutputStream.writeUTF("ACK");
            objectOutputStream.flush();

            // table data
            System.out.println("reading tableName...");
            tableName = objectInputStream.readUTF();
            System.out.println("read tableName = " + tableName);
            objectOutputStream.writeUTF("ACK");
            objectOutputStream.flush();

            final List<ComicCollectionEntity> collectionEntities = (List<ComicCollectionEntity>) objectInputStream
                    .readObject();
            System.out.println("read entities ");
            objectOutputStream.writeUTF("ACK");
            objectOutputStream.flush();
            // table data
            System.out.println("reading tableName...");
            tableName = objectInputStream.readUTF();
            System.out.println("read tableName = " + tableName);
            objectOutputStream.writeUTF("ACK");
            objectOutputStream.flush();

            final List<ComicCachedEntity> comicCachedEntities = (List<ComicCachedEntity>) objectInputStream.readObject();
            System.out.println("read entities ");
            objectOutputStream.writeUTF("ACK");
            objectOutputStream.flush();

            System.out.println("ending...");
            objectInputStream.readUTF();
            objectOutputStream.writeUTF("ACK");
            objectOutputStream.flush();
            objectOutputStream.writeUTF("FIN");
            objectOutputStream.flush();
            objectInputStream.readUTF();
            System.out.println("ended");

            s.close();

            System.out.println("");
            System.out.println("Collection---");
            for (final ComicCollectionEntity e : collectionEntities) {
                System.out.println(e.getName());
                System.out.println(e.getDateCreated());
            }

            System.out.println("");
            System.out.println("Comic---");
            for (final ComicCachedEntity e : comicCachedEntities) {
                System.out.println(e.getTitle());
            }

            fout = new FileOutputStream(new File(path + "collections"));
            oos = new ObjectOutputStream(fout);
            oos.writeObject(collectionEntities);

            fout = new FileOutputStream(new File(path + "comics"));
            oos = new ObjectOutputStream(fout);
            oos.writeObject(comicCachedEntities);

        } catch (IOException | ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

            try {
                fout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
