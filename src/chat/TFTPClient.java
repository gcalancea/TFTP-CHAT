package chat;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class TFTPClient {


    private static final String TFTP_SERVER_IP = "127.0.0.1";
    private static final int TFTP_DEFAULT_PORT = 5678;

    private static final byte OPCODE_READ_REQ = 1;
    private static final byte OPCODE_WRITE_REQ = 2;
    private static final byte OPCODE_DATAPACKET = 3;
    private static final byte OPCODE_ACK_PACKET = 4;
    private static final byte OPCODE_ERROR_PACKET = 5;

    private final static int PACKET_SIZE = 516;
    private int LAST_TFTP_PACKET = 512;

    private String fileNameDownload;

    public String download_file(String fileName, String pathToWrite) throws IOException {

        fileNameDownload = fileName;

        InetAddress inetAddress = InetAddress.getByName(TFTP_SERVER_IP);
        DatagramSocket datagramSocket = new DatagramSocket();
        byte[] requestByteArray = createRequest(OPCODE_READ_REQ, fileName, "octet");
        DatagramPacket outBoundDatagramPacket = new DatagramPacket(requestByteArray, requestByteArray.length,
                inetAddress, TFTP_DEFAULT_PORT);

        datagramSocket.send(outBoundDatagramPacket);

        ByteArrayOutputStream byteOutOS = null;
        try {
            byteOutOS = receiveFile(datagramSocket, inetAddress);
        } catch (Exception e) {
            datagramSocket.close();
            return e.getMessage();
        }

        datagramSocket.close();

        try {
            writeFile(byteOutOS, pathToWrite);
        } catch (Exception e) {
            return e.getMessage();
        }

        return "File downloaded successfully!";
    }

    private ByteArrayOutputStream receiveFile(DatagramSocket datagramSocket, InetAddress inetAddress) throws Exception {
        ByteArrayOutputStream byteOutOS = new ByteArrayOutputStream();
        int block = 1;
        DatagramPacket inBoundDatagramPacket;
        do {
            System.out.println("TFTP Packet count: " + block);
            block++;
            byte[] bufferByteArray = new byte[PACKET_SIZE];
            inBoundDatagramPacket = new DatagramPacket(bufferByteArray, bufferByteArray.length, inetAddress,
                    datagramSocket.getLocalPort());

            datagramSocket.receive(inBoundDatagramPacket);

            byte[] opCode = {bufferByteArray[0], bufferByteArray[1]};

            if (opCode[1] == OPCODE_ERROR_PACKET) {
                throw new Exception(reportError(bufferByteArray));
            }
            if (opCode[1] == OPCODE_DATAPACKET) {
                byte[] blockNumber = {bufferByteArray[2], bufferByteArray[3]};

                DataOutputStream dos = new DataOutputStream(byteOutOS);
                dos.write(inBoundDatagramPacket.getData(), 4, inBoundDatagramPacket.getLength() - 4);


                sendAcknowledgment(datagramSocket, inetAddress, blockNumber);
            } else {
                throw new Exception("Unknown packet type!");
            }

        } while (!isLastPacket(inBoundDatagramPacket));
        return byteOutOS;
    }

    private void writeFile(ByteArrayOutputStream baoStream, String pathWrite) throws IOException {
        try {
            OutputStream outputStream;
            if (pathWrite.substring(pathWrite.length() - 1).equals("/")) {
                outputStream = new FileOutputStream(pathWrite + fileNameDownload);
            } else outputStream = new FileOutputStream(pathWrite + "/" + fileNameDownload);
            baoStream.writeTo(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }


    public String upload_file(String pathToFile) throws IOException {
        File f = new File(pathToFile);
        if (!f.exists()) {
            System.out.println("File not found!");
            return "File not found!";
        }

        FileInputStream fisI = null;

        try {
            fisI = new FileInputStream(f);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        int blockNumber = 0;

        byte[] ack = new byte[516];

        InetAddress inetAddress = InetAddress.getByName(TFTP_SERVER_IP);
        DatagramSocket datagramSocket = new DatagramSocket();

        DatagramPacket inBoundDatagramPacket = new DatagramPacket(ack, ack.length, inetAddress,
                datagramSocket.getLocalPort());

        String fileName = f.getName();

        byte[] requestByteArray = createRequest(OPCODE_WRITE_REQ, fileName, "octet");
        DatagramPacket outBoundDatagramPacket = new DatagramPacket(requestByteArray, requestByteArray.length,
                inetAddress, TFTP_DEFAULT_PORT);

        datagramSocket.send(outBoundDatagramPacket);

        Boolean uploadFinished = false;

        do {
            datagramSocket.receive(inBoundDatagramPacket);
            byte[] opCode = {ack[0], ack[1]};

            if (opCode[1] == OPCODE_ERROR_PACKET) {
                datagramSocket.close();
                return reportError(ack);
            }
            if (opCode[1] == OPCODE_ACK_PACKET) {
                if (handleAck(ack, blockNumber)) {
                    datagramSocket.close();
                    return "Error: Wrong packet number!";
                }
            } else {
                datagramSocket.close();
                return "Error: Unknown packet type!";
            }
            blockNumber++;
            try {
                uploadFinished = sendFilePacket(datagramSocket, inetAddress, fisI, blockNumber);
            } catch (Exception e) {
                datagramSocket.close();
                return e.getMessage();
            }

        } while (!uploadFinished);

        datagramSocket.close();
        return "File uploaded successfully!";
    }

    private Boolean sendFilePacket(DatagramSocket datagramSocket, InetAddress inetAddress, FileInputStream fisI,
                                   int blockNumber) throws IOException {

        int len = 0;
        byte[] buf = new byte[516];
        buf[0] = 0;
        buf[1] = 3;
        buf[2] = (byte) ((blockNumber >> 8) & 0xFF);
        buf[3] = (byte) (blockNumber & 0xFF);
        try {
            len = fisI.read(buf, 4, 512);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        if (len != LAST_TFTP_PACKET) {
            if (len == -1) {
                len = 0;
            }
        }

        DatagramPacket packet = new DatagramPacket(buf, len + 4, inetAddress, TFTP_DEFAULT_PORT);
        try {
            datagramSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        return len < LAST_TFTP_PACKET;

    }

    private Boolean handleAck(byte[] currentBuf, int blockNumber) {
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

    private void sendAcknowledgment(DatagramSocket datagramSocket, InetAddress inetAddress, byte[] blockNumber) {

        byte[] ACK = {0, OPCODE_ACK_PACKET, blockNumber[0], blockNumber[1]};


        DatagramPacket ack = new DatagramPacket(ACK, ACK.length, inetAddress, TFTP_DEFAULT_PORT);
        try {
            datagramSocket.send(ack);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String reportError(byte[] bufferByteArray) {
        String errorCode = new String(bufferByteArray, 3, 1);
        String errorText = new String(bufferByteArray, 4, bufferByteArray.length - 4);
        System.err.println("Error: " + errorCode + " " + errorText);
        return errorText;
    }


    private boolean isLastPacket(DatagramPacket datagramP) {
        if (datagramP.getLength() < LAST_TFTP_PACKET)
            return true;
        return false;
    }

    private byte[] createRequest(final byte opCode, final String fileName, final String mode) {

        List<Byte> req = new ArrayList<>();
        byte[] byteReq = new byte[4 + fileName.length() + mode.length()];
        int counter;

        req.add((byte) 0);
        req.add(opCode);
        for (counter = 0; counter < fileName.length(); counter++) {
            req.add((byte) fileName.charAt(counter));
        }
        req.add((byte) 0);
        for (counter = 0; counter < mode.length(); counter++) {
            req.add((byte) mode.charAt(counter));
        }
        req.add((byte) 0);
        for (counter = 0; counter < byteReq.length; counter++) {
            byteReq[counter] = req.get(counter);
        }

        return byteReq;
    }
}
