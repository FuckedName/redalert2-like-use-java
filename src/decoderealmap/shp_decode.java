package decoderealmap;

import main.MyLogger;
import org.anarres.lzo.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class shp_decode {
    static Logger logger = MyLogger.logger;

    static  int decode64_table[] = {-1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1,
            -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1,
            -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,62, -1,-1,-1,63,
            52,53,54,55, 56,57,58,59, 60,61,-1,-1, -1,-1,-1,-1,
            -1, 0, 1, 2,  3, 4, 5, 6,  7, 8, 9,10, 11,12,13,14,
            15,16,17,18, 19,20,21,22, 23,24,25,-1, -1,-1,-1,-1,
            -1,26,27,28, 29,30,31,32, 33,34,35,36, 37,38,39,40,
            41,42,43,44, 45,46,47,48, 49,50,51,-1, -1,-1,-1,-1,
            -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1,
            -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1,
            -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1,
            -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1,
            -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1,
            -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1,
            -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1,
            -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1};
    shp_decode()
    {


    };

    public static List decode64(String input)
    {
        byte[] writeList = new byte[329692];
        byte[] bytes = input.getBytes();
        logger.info("input bytes.length: " + bytes.length);

        Integer readIndex = 0;
        Integer writeIndex = 0;

        while (true)
        {
            if (readIndex > input.length() - 4)
                break;

            //logger.info(writeIndex.toString());
            int c1 = bytes[readIndex++];
            if (decode64_table[c1] == -1)
                return null;

            int c2 = bytes[readIndex++];
            if (decode64_table[c2] == -1)
                return null;

            int c3 = bytes[readIndex++];
            if (c3 != '=' && decode64_table[c3] == -1)
                return null;

            int c4 = bytes[readIndex++];
            if (c4 != '=' && decode64_table[c4] == -1)
                return null;

            byte value1 = (byte) ((decode64_table[c1] << 2) | (decode64_table[c2] >> 4));
            //logger.info(writeIndex.toString() + ": " + value.toString());
            writeList[writeIndex++] = value1;

            if (c3 == '=')
                break;

            byte value2 = (byte) (((decode64_table[c2] << 4) & 0xf0) | (decode64_table[c3] >> 2));
            //logger.info(writeIndex.toString() + ": " + value.toString());
            writeList[writeIndex++] = value2;

            if (c4 == '=')
                break;

            byte value3 = (byte) (((decode64_table[c3] << 6) & 0xc0) | decode64_table[c4]);
            //logger.info(writeIndex.toString() + ": " + value.toString());
            writeList[writeIndex++] = value3;
        }

        logger.info(writeList.toString());
        Integer length = writeList.length;
        logger.info(length.toString());
        List list = new ArrayList<>();
        for (int i = 0; i < writeList.length; i++) {
            list.add(writeList[i]);
        }

        //logger.info(list.toString());
        return list;
    }

    public static int decode5s(List inputList, int inputStart, int size, byte[] outBuffer, int outSize) throws IOException {
        byte[] bytes = new byte[size];
        int index = 0;
        for (int i = inputStart; i < inputStart + size;)
        {
            byte b = (byte) inputList.get(i++);
            bytes[index++] = b;
        }


        //byte[] bytesOutput = LzoDecompressionExample.decompressData(bytes);

        try
        {
            byte[] bytesOutput = LzoDecompressionExample.decompress1(bytes, outBuffer, outSize);
            String s = new String(bytesOutput);
            byte[] bytes1 = s.getBytes();
            int length = bytes1.length;
            logger.info("length: " + Integer.toString(length));

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //logger.info("data: " + Arrays.toString(bytes1));
        return 0;
    }
}



class LzoDecompressionExample {

    public static byte[] decompressData(byte[] compressedData) throws IOException {

        System.out.println("Successfully Decompressed:" );

        byte[] buffer = new byte[1024];

        //LzoDecompressor lzoDecompressor = LzoLibrary.getInstance().newDecompressor(LzoAlgorithm.LZO1X, LzoConstraint.SPEED);
        //lzoDecompressor.decompress(compressedData, 10, 10, buffer, 10, 1);

        //https://github.com/shevek/lzo-java
        return null;
    }

    public static byte[] decompress1(byte[] src, byte[] outBuffer, int outSize) throws Exception
    {
        lzo_uintp lzoUintp = new lzo_uintp();
        lzoUintp.value = outSize;
        LzoDecompressor decompressor = LzoLibrary.getInstance().newDecompressor(LzoAlgorithm.LZO1X, LzoConstraint.SPEED);
        decompressor.decompress(src, 0, src.length, outBuffer, 0, lzoUintp);
        return outBuffer;
    }

    public static byte[] decompress(byte[] src) {
        ByteArrayInputStream input = new ByteArrayInputStream(src);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        LzoDecompressor decompressor = LzoLibrary.getInstance().newDecompressor(LzoAlgorithm.LZO1X, null);
        LzoInputStream stream = new LzoInputStream(input, decompressor);

        try {
            int data = stream.read();
            while (data != -1) {
                out.write(data);
                data = stream.read();
            }
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return out.toByteArray();
    }

    public static void main(String[] args) throws IOException {
        //byte[] compressedData = ...; // 从数据源获取压缩后的数据
        //byte[] decompressedData = decompressData(compressedData);

        //System.out.println("Decompressed Data: " + new String(decompressedData));
    }
}