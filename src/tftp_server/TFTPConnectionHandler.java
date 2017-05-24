package tftp_server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
//import java.nio.file.Files;
//import java.nio.file.Path;


public class TFTPConnectionHandler {
    private DatagramSocket socket;
    private FileInputStream fisI;
    private int blockNumber = 0;
    private ProgressBarTest myInterface;
    private DatagramPacket packet;
    private FileOutputStream fisO;
    private ProgressBarTest.dynamicProgressBar currentFile;
    private Boolean downloadFinished = false;
    private byte[] currentBuf;
    private String uploadFilePath;


    public TFTPConnectionHandler(DatagramSocket originalSocket, ProgressBarTest interfaceInstance) {
        socket = originalSocket;
        myInterface = interfaceInstance;
    }

    private DatagramPacket getErrorPacket(String operation) {
        byte[] buf = new byte[516];
        byte[] errorMessageBytes;
        buf[0] = 0;
        buf[1] = 5;
        switch (operation) {
            case "download":
                buf[2] = 0;
                buf[3] = 1;
                errorMessageBytes = "File not found.".getBytes(StandardCharsets.UTF_8);
                break;
            case "upload":
                buf[2] = 0;
                buf[3] = 6;
                errorMessageBytes = "File already exists.".getBytes(StandardCharsets.UTF_8);
                break;
            default:
                buf[2] = 0;
                buf[3] = 0;
                errorMessageBytes = "Not implemented.".getBytes(StandardCharsets.UTF_8);
        }

        System.arraycopy(errorMessageBytes, 0, buf, 4, errorMessageBytes.length);
        buf[4 + errorMessageBytes.length] = 0;
        return new DatagramPacket(buf, errorMessageBytes.length + 5, packet.getAddress(), packet.getPort());

    }

    private DatagramPacket getErrorPacket() {
        return getErrorPacket("");
    }

    private DatagramPacket getAckPacket() {
        byte[] buf = new byte[4];
        buf[0] = 0;
        buf[1] = 4;
        buf[2] = (byte) ((blockNumber >> 8) & 0xFF);
        buf[3] = (byte) (blockNumber & 0xFF);
        return new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
    }

    private Boolean initDownload() {
        StringBuilder myBuilder = new StringBuilder();
        int i;
        for (i = 2; currentBuf[i] != 0; i++) {
            myBuilder.append((char) currentBuf[i]);
        }
        String filename = myBuilder.toString();
        System.out.println(filename);
        System.out.println("Starting download");
        File f = new File(System.getProperty("user.dir") + "/src/user_files", filename);
        if (!f.exists()) {
            return false;
        }
        try {
            fisI = new FileInputStream(f);
            currentFile = myInterface.addProgressBar("Downloading: " + filename, (int) f.length());
            blockNumber = 0;
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Boolean initUpload() {
        StringBuilder myBuilder = new StringBuilder();
        int i;
        for (i = 2; currentBuf[i] != 0; i++) {
            myBuilder.append((char) currentBuf[i]);
        }
        uploadFilePath = myBuilder.toString();
        System.out.println(uploadFilePath);
        System.out.println("Starting upload");
        Path p = Paths.get(System.getProperty("user.dir") + "/src/user_files/" + uploadFilePath);
        try {
            Files.deleteIfExists(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
        File f = new File(System.getProperty("user.dir") + "/src/user_files", uploadFilePath);
        if (f.exists()) {
            return false;
        }
        try {
            fisO = new FileOutputStream(f);
            currentFile = myInterface.addProgressBar("Uploading: " + uploadFilePath);
            blockNumber = 0;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void handleDownload() {
        int len = 0;
        byte[] buf = new byte[516];
        buf[0] = 0;
        buf[1] = 3;
        ++blockNumber;
        buf[2] = (byte) ((blockNumber >> 8) & 0xFF);
        buf[3] = (byte) (blockNumber & 0xFF);
        try {
            len = fisI.read(buf, 4, 512);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (len != 512) {
            if (len == -1) {
                len = 0;
            }
            downloadFinished = true;
        }

        packet = new DatagramPacket(buf, len + 4, packet.getAddress(), packet.getPort());
        currentFile.incValue(len);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleUpload() {
        int lenReceived = 0;
        ++blockNumber;
        handleAck();
        lenReceived = currentBuf.length - 4;
        byte[] writeData = new byte[lenReceived];
        System.arraycopy(currentBuf, 4, writeData, 0, lenReceived);
        try {
            fisO.write(writeData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentFile.incValue(lenReceived);

        try {
            socket.send(getAckPacket());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (lenReceived != 512) {
            currentFile.done();
            System.out.println("Upload completed successfully");
            TreeIcon.addNodes(uploadFilePath);
            try {
                fisO.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            blockNumber = 0;
        }

    }

    private Boolean handleAck() {
        System.out.println("ACK handled");
        int blockNumberReceived = (currentBuf[2] << 8) | (currentBuf[3] & 0xFF);
        Boolean returnValue = blockNumberReceived != blockNumber
                && blockNumberReceived != -(32768 - blockNumber % 32768) && blockNumberReceived != blockNumber % 32768;
        if (returnValue) {
            System.out.println(
                    "Error: Block number should've been #" + blockNumber + ", but I got #" + blockNumberReceived);
        }
        return returnValue;
    }

    public void handlePacket(DatagramPacket receivedPacket) {
        packet = receivedPacket;
        currentBuf = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), 0, currentBuf, 0, packet.getLength());
        int opcode = (currentBuf[0] << 8) | currentBuf[1];
        System.out.println(opcode);
        switch (opcode) {
            case 1:
                if (initDownload()) {
                    handleDownload();
                } else {
                    blockNumber = 0;
                    try {
                        socket.send(getErrorPacket("download"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 2:
                if (initUpload()) {
                    try {
                        socket.send(getAckPacket());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    blockNumber = 0;
                    try {
                        socket.send(getErrorPacket("upload"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                break;
            case 3:
                handleUpload();
                break;
            case 4:
                handleAck();
                if (downloadFinished) {
                    downloadFinished = false;
                    blockNumber = 0;
                    System.out.println("Finished downloading file");
                    try {
                        fisI.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    handleDownload();
                }
                break;
            default:
                try {
                    socket.send(getErrorPacket());
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

}
