import java.io.*;
import java.net.Socket;
import java.util.Random;
public final class UdpClient {
    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("18.221.102.182",38005)) {

            System.out.println("Connected to server.");


            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            BufferedReader brIS = new BufferedReader(new InputStreamReader(System.in));
            PrintStream out = new PrintStream((socket.getOutputStream()),true,"UTF-8");


            byte[] sequence = new byte[24];
            //Version + HLen
            sequence[0] = 0x45;
            //TOTAL LENGTH in sequence[2] and sequence[3] - Should end up being 20 + 2^Packet Number+1
            int size = 24;
            String hexSize = Integer.toHexString(size);
            if (hexSize.length() > 4)
                hexSize = hexSize.substring(hexSize.length() - 4);
            else {
                while (hexSize.length() < 4)
                    hexSize = "0" + hexSize;
            }
            sequence[2] = (byte) Integer.parseInt(hexSize.substring(0, 2).toUpperCase(), 16);
            sequence[3] = (byte) Integer.parseInt(hexSize.substring(2).toUpperCase(), 16);
            //Flag assuming no fragmentation
            sequence[6] = 0x40;
            //TTL
            sequence[8] = 0x32;
            //Protocol
            sequence[9] = 0x06;
            //Source IP address
            sequence[12] = 0x11;
            sequence[13] = 0x11;
            sequence[14] = 0x11;
            sequence[15] = 0x11;
            //Server IP address
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
            int data = is.read();
            System.out.println("Port number received: "+ data);




            for(int packetN=0; packetN<12; packetN++) {
                System.out.println("Packet " + (packetN+1));
                // .5B + .5B + 1B+ 2B + 2B + 3/8B + 1 5/8 B + 1B + 1B + 2B + 4B + 4B + DATA
                sequence = new byte[28+(int) Math.pow(2,(packetN+1))];
                //Version + HLen
                sequence[0] = 0x45;
                //TOTAL LENGTH in sequence[2] and sequence[3] - Should end up being 28 + 2^Packet Number+1
                size = (int) (28 + Math.pow(2,(packetN+1)));
                hexSize = Integer.toHexString(size);
                if (hexSize.length() > 4)
                    hexSize = hexSize.substring(hexSize.length() - 4);
                else {
                    while (hexSize.length() < 4)
                        hexSize = "0" + hexSize;
                }
                sequence[2] = (byte) Integer.parseInt(hexSize.substring(0, 2).toUpperCase(), 16);
                sequence[3] = (byte) Integer.parseInt(hexSize.substring(2).toUpperCase(), 16);
                //Flag assuming no fragmentation
                sequence[6] = 0x40;
                //TTL
                sequence[8] = 0x32;
                //Protocol
                sequence[9] = 0x06;
                //Source IP address
                sequence[12] = 0x11;
                sequence[13] = 0x11;
                sequence[14] = 0x11;
                sequence[15] = 0x11;
                //Server IP address
                sequence[16] = 0x12;
                sequence[17] = (byte) 0xDD;
                sequence[18] = (byte) 0x66;
                sequence[19] = (byte) 0xB6;
                //Rest is DATA, assuming it will be default byte value 0.

                // Copies all of the bytes in the packet except for the checksum and data to calculate the checksum.
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

                //UDP DEST PORT
                hex=Integer.toHexString(data);
                sequence[24] = (byte) Integer.parseInt(hex.substring(0, 2).toUpperCase(), 16);
                sequence[25] = (byte) Integer.parseInt(hex.substring(2).toUpperCase(), 16);

                //UDP LENGTH
                hexSize=Integer.toHexString(size-20);
                if (hexSize.length() > 4)
                    hexSize = hexSize.substring(hexSize.length() - 4);
                else {
                    while (hexSize.length() < 4)
                        hexSize = "0" + hexSize;
                }
                sequence[26] = (byte) Integer.parseInt(hexSize.substring(0, 2).toUpperCase(), 16);
                sequence[27] = (byte) Integer.parseInt(hexSize.substring(2).toUpperCase(), 16);

                //FILLING UP DATA OF UDP
                Random rand = new Random();
                byte[] r = new byte[(int) Math.pow(2,(packetN+1))];
                rand.nextBytes(r);
                for(int i=0;i<Math.pow(2,(packetN+1));i++){
                        sequence[28+i]=r[i];
                }

                //TO DO CHECKSUM


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


                out.write(sequence);
                System.out.println("Response: 0x"+(Integer.toHexString(is.read())+Integer.toHexString(is.read())+Integer.toHexString(is.read())+Integer.toHexString(is.read())).toUpperCase());
            }

            is.close();
            isr.close();
            br.close();
            brIS.close();
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
