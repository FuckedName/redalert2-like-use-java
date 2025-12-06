package decoderealmap;

import java.io.IOException;
import java.util.List;

public class FSunPackLib {

    final static int DECODE_TIMES = 40;

    public static List DecodeBase64(String input)
    {
        return shp_decode.decode64(input);
    }

    public static Integer get2byte(List list, Integer index)
    {
        if (index >= list.size() - 2)
        {
            return null;
        }
        byte signedValue1 = (byte) list.get(index); // 有符号字节 (-1)
        int i = signedValue1 & 0xFF; // 转为无符号字节

        byte signedValue2 = (byte) list.get(index + 1); // 有符号字节 (-1)
        int j = signedValue2 & 0xFF; // 转为无符号字节
        return i + j * 256;
    }

    public static void DecodeIsoMapPack5(List listInput, byte[] m_mfd)
    {
        Integer inputIndex = 0;
        Integer ouputIndex = 0;
        int count = 1;
        while (inputIndex < listInput.size())
        {
            Integer wSrcSize;
            Integer wDestSize;


            wSrcSize = get2byte(listInput, inputIndex);
            if (wSrcSize == null)
            {
                break;
            }
            inputIndex += 2;

            wDestSize = get2byte(listInput, inputIndex);
            if  (wDestSize == null)
            {
                break;
            }
            inputIndex += 2;

            try
            {
                byte[] outBuffer = new byte[wDestSize];
                if (count > DECODE_TIMES)
                {
                    break;
                }
                shp_decode.decode5s(listInput, inputIndex, wSrcSize, outBuffer, wDestSize);
                count++;
                for (int k = 0; k < wDestSize; k++) {
                    m_mfd[ouputIndex+k] = outBuffer[k];
                }
            }
            catch (IOException e)
            {
                //e.printStackTrace();
            }

            //A.a.info(inputIndex.toString() + ": " + wSrcSize.toString());
            inputIndex += wSrcSize;
            ouputIndex += wDestSize;
        }

    }
}
