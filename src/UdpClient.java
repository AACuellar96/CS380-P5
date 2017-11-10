import java.io.*;
import java.net.Socket;
import java.util.Random;
public final class UdpClient {
    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("18.221.102.182",38005)) {
            System.out.println("Connected to server.");
            InputStream is = socket.getInputStream();
            PrintStream out = new PrintStream((socket.getOutputStream()),true,"UTF-8");
            byte[] sequence = new byte[24];
            sequence[0] = 0x45;
            String hexSize;
            sequence[3] = 0x18;
            sequence[6] = 0x40;
            sequence[7] = 0;
            sequence[8] = 0x32;
            sequence[9] = 0x11;
            sequence[12] = 0x11;
            sequence[13] = 0x11;
            sequence[14] = 0x11;
            sequence[15] = 0x11;
            sequence[16] = 0x12;
            sequence[17] = (byte) 0xDD;
            sequence[18] = (byte) 0x66;
            sequence[19] = (byte) 0xB6;
            sequence[20] = (byte) 0xDE;
            sequence[21] = (byte) 0xAD;
            sequence[22] = (byte) 0xBE;
            sequence[23] = (byte) 0xEF;
            byte[] checkSumBytes=new byte[18];
            for(int i=0;i<18;i++){
                if(i<10){
                    checkSumBytes[i]=sequence[i];
            }
                else{
                    checkSumBytes[i]=sequence[i+2];
                }
            }
            int cSum = checksum(checkSumBytes);
            String hex = Integer.toHexString(cSum);
            if (hex.length() > 4)
                hex = hex.substring(hex.length() - 4);
            else {
                while (hex.length() < 4)
                    hex = "0" + hex;
            }
            sequence[10] = (byte) Integer.parseInt(hex.substring(0, 2).toUpperCase(), 16);
            sequence[11] = (byte) Integer.parseInt(hex.substring(2).toUpperCase(), 16);
            out.write(sequence);
            System.out.println("Handshake Response: 0x"+(Integer.toHexString(is.read())+Integer.toHexString(is.read())+Integer.toHexString(is.read())+Integer.toHexString(is.read())).toUpperCase());
            byte pNum1 = (byte) is.read();
            byte pNum2 = (byte) is.read();
            String portNum = ""+Byte.toUnsignedInt(pNum1)+Byte.toUnsignedInt(pNum2);
            System.out.println("Port number received: "+ Integer.parseInt(portNum,16));
            System.out.println("");
            long avgTime=0;
            for(int packetN=0; packetN<12; packetN++) {
                int byteAmt = (int) Math.pow(2,(packetN+1));
                System.out.println("Sending packet with " + byteAmt+" bytes of data");
                sequence = new byte[28+byteAmt];
                sequence[0] = 0x45;
                int size = (28 + byteAmt);
                hexSize = Integer.toHexString(size);
                if (hexSize.length() > 4)
                    hexSize = hexSize.substring(hexSize.length() - 4);
                else {
                    while (hexSize.length() < 4)
                        hexSize = "0" + hexSize;
                }
                sequence[2] = (byte) Integer.parseInt(hexSize.substring(0, 2).toUpperCase(), 16);
                sequence[3] = (byte) Integer.parseInt(hexSize.substring(2).toUpperCase(), 16);
                sequence[6] = 0x40;
                sequence[8] = 0x32;
                sequence[9] = 0x11;
                sequence[12] = 0x11;
                sequence[13] = 0x11;
                sequence[14] = 0x11;
                sequence[15] = 0x11;
                sequence[16] = 0x12;
                sequence[17] = (byte) 0xDD;
                sequence[18] = (byte) 0x66;
                sequence[19] = (byte) 0xB6;
               checkSumBytes=new byte[18];
                for(int i=0;i<18;i++){
                    if(i<10){
                        checkSumBytes[i]=sequence[i];
                    }
                    else{
                        checkSumBytes[i]=sequence[i+2];
                    }
                }
                cSum = checksum(checkSumBytes);
                hex = Integer.toHexString(cSum);
                if (hex.length() > 4)
                    hex = hex.substring(hex.length() - 4);
                else {
                    while (hex.length() < 4)
                        hex = "0" + hex;
                }
                sequence[10] = (byte) Integer.parseInt(hex.substring(0, 2).toUpperCase(), 16);
                sequence[11] = (byte) Integer.parseInt(hex.substring(2).toUpperCase(), 16);
                //UDP SOURCE PORT
                sequence[20]=0x11;
                sequence[21]=0x11;
                //UDP DEST PORT
                sequence[22] = pNum1;
                sequence[23] = pNum2;
                //UDP LENGTH
                hexSize=Integer.toHexString(size-20);
                if (hexSize.length() > 4)
                    hexSize = hexSize.substring(hexSize.length() - 4);
                else {
                    while (hexSize.length() < 4)
                        hexSize = "0" + hexSize;
                }
                sequence[24] = (byte) Integer.parseInt(hexSize.substring(0, 2).toUpperCase(), 16);
                sequence[25] = (byte) Integer.parseInt(hexSize.substring(2).toUpperCase(), 16);
                //FILLING UP DATA OF UDP
                Random rand = new Random();
                byte[] r = new byte[(int) Math.pow(2,(packetN+1))];
                rand.nextBytes(r);
                for(int i=0;i<Math.pow(2,(packetN+1));i++){
                        sequence[28+i]=r[i];
                }
                checkSumBytes=new byte[20+byteAmt];
                checkSumBytes[0]=0x11;
                checkSumBytes[1]=0x11;
                checkSumBytes[2]=0x11;
                checkSumBytes[3]=0x11;
                checkSumBytes[4]=0x12;
                checkSumBytes[5]=(byte) 0xDD;
                checkSumBytes[6]=(byte) 0x66;
                checkSumBytes[7]=(byte) 0xB6;
                checkSumBytes[9]=0x11;
                checkSumBytes[10]=sequence[24];
                checkSumBytes[11]=sequence[25];
                checkSumBytes[12]=0x11;
                checkSumBytes[13]=0x11;
                checkSumBytes[14] = pNum1;
                checkSumBytes[15] = pNum2;
                checkSumBytes[16]=sequence[24];
                checkSumBytes[17]=sequence[25];
                for(int i=0;i<byteAmt;i++){
                    checkSumBytes[20+i]=r[i];
                }
                cSum = checksum(checkSumBytes);
                hex = Integer.toHexString(cSum);
                if (hex.length() > 4)
                    hex = hex.substring(hex.length() - 4);
                else {
                    while (hex.length() < 4)
                        hex = "0" + hex;
                }
                sequence[26] = (byte) Integer.parseInt(hex.substring(0, 2).toUpperCase(), 16);
                sequence[27] = (byte) Integer.parseInt(hex.substring(2).toUpperCase(), 16);
                long sentTime = System.currentTimeMillis();
                out.write(sequence);
                System.out.println("Response: 0x"+(Integer.toHexString(is.read())+Integer.toHexString(is.read())+Integer.toHexString(is.read())+Integer.toHexString(is.read())).toUpperCase());
                long estimatedRTT =  System.currentTimeMillis() - sentTime;
                System.out.println("RTT: "+ estimatedRTT + " ms");
                System.out.println("");
                avgTime+=estimatedRTT;
            }
            System.out.println("Average RTT: " + avgTime/12);
            is.close();
            socket.close();
            System.out.println("Disconnected from server.");
        }
    }
    public static short checksum(byte[] b){
        int cSum = 0;
        for(int i=0;i<b.length;i+=2){
            short one = (short) (b[i] & 0xFF);
            try {
                short two = (short) (b[i + 1] & 0xFF);
                cSum += ((256 * one) + two);
                if (cSum >= 65535) {
                    cSum -= (65535);
                }
            }
            catch (ArrayIndexOutOfBoundsException e){
                cSum+=(256*one);
                if (cSum >= 65535) {
                    cSum -= (65535);
                }
            }
        }
        return (short) ((~(cSum))& 0xFFFF);
    }
}
