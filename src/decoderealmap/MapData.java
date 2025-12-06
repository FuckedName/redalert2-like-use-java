package decoderealmap;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

import main.Global;
import main.MainThread;
import main.MyLogger;

import javax.swing.*;

import static java.lang.Thread.sleep;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;


public class MapData extends JFrame
{
    private final int windowWidth;//画板的宽度
    private final int windowHeight;//画板的高度
    int xStart;
    int dispalyWindowWidth;
    int yStart;
    int dispalyWindowHeight;
    //整个全地图
    BufferedImage bufferedImage;
    Graphics bufferedImageGraphics;
    Graphics graphics;
    private InitFile initFile;
    public static final int MAPFIELDDATA_SIZE = 11;
    final static int HEIGHT_BYTE_INDEX = 9;
    final static int GROUND_BYTE_INDEX = 4;
    final static int X_BYTE_INDEX = 0;
    final static int Y_BYTE_INDEX = 2;
    HashMap<CellData, Integer> cellDataIntegerHashMap;
    byte[] m_mfd;
    Vector<MAPFIELDDATA> mapfielddataVector;
    int dwIsoMapSize;

    //这是取出来数据为凌形的高度数据
    Vector<StringBuilder> stringBuilderVector;

    //这是转换为矩形后的高度数据
    Vector<StringBuilder> stringBuilderVector1 = new Vector<>();

    Logger logger = MyLogger.logger;

    MapData()
    {
        String title = "draw map ";

        setTitle(title);
        setLayout(null);

        windowWidth = 1920;
        windowHeight = 1080;
        bufferedImage = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
        bufferedImageGraphics = bufferedImage.createGraphics();
        setSize(windowWidth, windowHeight);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //setVisible(true);

        graphics = getContentPane().getGraphics();
        initFile = new InitFile();
        cellDataIntegerHashMap = new HashMap<>();
        mapfielddataVector = new Vector<>();
        stringBuilderVector = new Vector<>();
        logger.info("a");
        dispalyWindowWidth = 500;
        dispalyWindowHeight = 500;
        addKeyCapture();
    }


    void writeObjectIntoFile(MapObjectData mapObjectData, String file) {
        try (FileOutputStream fileOut = new FileOutputStream(file);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(mapObjectData); // 写入对象到文件
            System.out.println("对象已序列化并保存到文件");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    void readFile(String path)
    {
        //String filePath = Global.resourceRoot + "6-真实地图" + java.io.File.separator + "amazon.mmx";
        //String filePath = Global.resourceRoot + "6-真实地图" + java.io.File.separator + "冰天平衡版.mpr";
        String filePath = Global.resourceRoot + "6-真实地图" + java.io.File.separator + path;
        initFile.LoadFile(filePath);
    }

    /**
     * 键盘的事件，没怎么用到。。。。
     */
    public void addKeyCapture()
    {
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                logger.info(e.toString());
                int keyCode = e.getKeyCode();
                switch (keyCode)
                {
                    //up
                    case 38:
                        //if (yStart - 10 >= 0)
                        {
                            yStart -= 10;
                        }
                        break;

                    //down
                    case 40:
                        //if (yStart + dispalyWindowHeight + 10 <= windowHeight)
                        {
                            yStart += 10;
                        }
                        break;

                    //left
                    case 37:
                        //if (xStart - 10 >= 0)
                        {
                            xStart -= 10;
                        }
                        break;

                    //right
                    case 39:
                        //if (xStart  + dispalyWindowWidth + 10 < windowWidth)
                        {
                            xStart += 10;
                        }
                        break;

                    //'-'
                    case 33:
                        // if (dispalyWindowWidth + 10 < windowWidth)
                    {
                        dispalyWindowWidth += 100;
                        dispalyWindowHeight += 100;
                    }
                    break;

                    //'+'
                    case 34:
                        //if (dispalyWindowWidth - 10 > windowWidth/3)
                    {
                        dispalyWindowWidth -= 100;
                        dispalyWindowHeight -= 100;
                    }
                    break;

                    default:
                        logger.info("default..." + e.toString());
                }

                switch (e.getKeyChar())
                {
                    case 'd':
                    case 'D':
                        break;

                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
    }

    void Unpack()
    {
        StringBuilder sectionData = initFile.getSectionData("[IsoMapPack5]", "=");
        int length = sectionData.toString().length();

        logger.info(Integer.toString(length));
        List list = FSunPackLib.DecodeBase64(sectionData.toString());
        logger.info(Integer.toString(length));
        int size = list.size();
        logger.info(String.valueOf(size));

        int index = 0;
        int MapSizeBytes = 0;
        int sec = 0;
        while (index < list.size())
        {
            Integer wSrcSize;
            Integer wDestSize;
            wSrcSize = FSunPackLib.get2byte(list, index);
            if  (wSrcSize == null)
            {
                break;
            }
            index += 2;

            wDestSize = FSunPackLib.get2byte(list, index);
            if  (wDestSize == null)
            {
                break;
            }
            index += 2;

            MapSizeBytes += wDestSize;
            index += wSrcSize;

            sec++;
        }

        logger.info(Integer.toString(MapSizeBytes));
        logger.info(Integer.toString(sec));
        logger.info(Integer.toString(index));
        m_mfd = new byte[MapSizeBytes];
        dwIsoMapSize = MapSizeBytes / MAPFIELDDATA_SIZE;

        FSunPackLib.DecodeIsoMapPack5(list, m_mfd);
        logger.info(String.valueOf(m_mfd[0]));
    }

    /**
     * m_mfd -> Vector<MAPFIELDDATA> mapfielddataVector
     * save data into HashMap<CellData, Integer> cellDataIntegerHashMap;
     */
    void transform()
    {
        for (int i = 0; i < m_mfd.length / MAPFIELDDATA_SIZE; i++)
        {
            MAPFIELDDATA mapfielddata1 = new MAPFIELDDATA();
            int index = i * MAPFIELDDATA_SIZE;

            mapfielddata1.wX = getTwoByte(index);
            mapfielddata1.wY = getTwoByte(index + 2);
            mapfielddata1.wGround = getTwoByte(index + 4);
            mapfielddata1.bData = new byte[3];
            mapfielddata1.bData[0] = m_mfd[index + 6];
            mapfielddata1.bData[1] = m_mfd[index + 7];
            mapfielddata1.bData[2] = m_mfd[index + 8];
            mapfielddata1.bHeight = m_mfd[index + 9];
            mapfielddata1.bData2 = m_mfd[index + 10];

            mapfielddataVector.add(mapfielddata1);

            //这里为什么要减1呢，因为地图上有些高度是10的，打印出来会占用两位，-1后变为9只占用一位，看起来美观些
            cellDataIntegerHashMap.put(new CellData((int) mapfielddata1.wX, (int) mapfielddata1.wY),
                    (int) mapfielddata1.bHeight);
        }
    };

    int getTwoByte(int startIndex)
    {
        byte x1 = m_mfd[startIndex];
        byte x2 = m_mfd[startIndex + 1];
        int x3 = x1 & 0xFF; // 将byte提升到int，然后与0xFF进行按位与操作
        int x4 = x2 & 0xFF; // 将byte提升到int，然后与0xFF进行按位与操作
        return x4 * 256 + x3;
    }

    /**
     // Draw map with rectangle.
     * Width=83
     * Height=90
     */
    void print()
    {
        logger.info("");

        int height = stringBuilderVector1.size();
        int width = stringBuilderVector1.get(0).length();
        MapObjectData obj = new MapObjectData(height, width);
        for (int y = 0; y < stringBuilderVector1.size(); y++)
        {
            System.out.print("{");
            for (int x = 0; x < stringBuilderVector1.get(0).length(); x++)
            {
                char s = '0';
                char cyx = '0';
                if(y + 1 < stringBuilderVector1.size() - 1 && x+1 < stringBuilderVector1.get(0).length()
                        && y - 1 >0 && x-1 >0
                )
                {
                    cyx = stringBuilderVector1.get(y).charAt(x);
                    char cy1x = stringBuilderVector1.get(y+1).charAt(x);
                    char cyx1 = stringBuilderVector1.get(y).charAt(x+1);
                    char cy1x1 = stringBuilderVector1.get(y+1).charAt(x+1);
                    char c1y1x = stringBuilderVector1.get(y+1).charAt(x-1);
                    /**
                     * 5,<----
                     * 1
                     */
                    if (cyx - cy1x >= 4)
                    {
                        s = '3';
                    }
                    /**
                     * 1
                     * 5,<----
                     */
                    if (cy1x - cyx>= 4)
                    {
                        s = '7';
                    }

                    /**
                     * 1，5<----
                     */
                    if (cyx1 - cyx>= 4)
                    {
                        s = '5';
                    }
                    /**
                     * ---->5,1
                     */
                    if (cyx - cyx1 >= 4)
                    {
                        s = '1';
                    }


                    /**
                     * 5,<----
                     *   1
                     */
                    if (cyx - cy1x1 >= 4)
                    {
                        s = '2';
                    }

                    /**
                     * 1
                     *   5,<----
                     */
                    if (cy1x1 - cyx>= 4)
                    {
                        s = '6';
                    }

                    /**
                     *   1,<----
                     * 5
                     */
                    if (c1y1x - cyx>= 4)
                    {
                        s = '8';
                    }

                    /**
                     *   5,<----
                     * 1
                     */
                    if (cyx - c1y1x >= 4)
                    {
                        s = '4';
                    }
                }

                System.out.print("2" + s + cyx);

                //地图的205
                int temp = 200 + (s - '0') * 10 + cyx - '0';
                obj.data[y][x] = temp;
                if (x < stringBuilderVector1.get(0).length()-1)
                {
                    System.out.print(',');
                }
            }
            System.out.print("},");
            System.out.println();
        }

        writeObjectIntoFile(obj, "map.objectdata");
    }

    /**
     // Draw map with rectangle.
     * Width=83
     * Height=90
     */
    void print2()
    {
        logger.info("");

        for (int y = 0; y < stringBuilderVector1.size(); y++)
        {
            for (int x = 0; x < stringBuilderVector1.get(0).length(); x++)
            {
                char cyx = '0';
                if(y + 1 < stringBuilderVector1.size() - 1 && x+1 < stringBuilderVector1.get(0).length()
                        && y - 1 >0 && x-1 >0
                )
                {
                    cyx = stringBuilderVector1.get(y).charAt(x);
                }

                int temp = cyx - '0';
                System.out.print(temp);
            }
            System.out.println();
        }
    }


    void printMap2()
    {
        logger.info("");
        int count = 0;
        int mapwidth = 0, mapheight = 0;

        while (true)
        {
            if (stringBuilderVector.get(0).charAt(count) != ' ')
            {
                mapwidth = count;
                break;
            }
            count++;
        }
        count = 0;

        while (true)
        {
            if (stringBuilderVector.get(count).charAt(1) != ' ')
            {
                mapheight = count;
                break;
            }
            count++;
        }
        count = 0;

        for (int x = mapwidth; x < mapwidth + mapheight + 1; x++, count++)
        {
            int k = x;
            StringBuilder stringBuilder = new StringBuilder();
            //stringBuilder.append('{');
            for (int y = 1 + count; y < mapwidth + count + 1; y++)
            {
                if (y >= stringBuilderVector.size() || k >= stringBuilderVector.get(0).length())
                {
                    continue;
                }
                char height = stringBuilderVector.get(y).charAt(k);
                if (height != ' ')
                {
                    stringBuilder.append(height);
                }
                k--;
            }

            //stringBuilder.append('}');
            //System.out.print(stringBuilder);
            stringBuilderVector1.add(stringBuilder);
            //System.out.println();
        }

        /*
        //其实上边的是正确的
        System.out.println("行列位置调换了下:");
        logger.info("");

        Vector<StringBuilder> stringBuilderVector2 = new Vector<>();
        for (int column = 0; column < stringBuilderVector1.get(0).length(); column++)
        {
            StringBuilder stringBuilder = new StringBuilder();
            for (int line = 0; line < stringBuilderVector1.size(); line++)
            {
                char c = stringBuilderVector1.get(line).charAt(column);
                stringBuilder.append(c);
            }
            stringBuilderVector2.add(stringBuilder);
            System.out.println(stringBuilder);
        }*/
    }

    /**
     * 能通行
     * 高度差不能超过1
     * @param vectorMap
     * @param x
     * @param y
     * @return
     */
    boolean mapCanGo(Vector<StringBuilder> vectorMap, int x, int y)
    {
        char heightxy = vectorMap.get(y).charAt(x);
        for (int y1 = -1; y1 <= 1; y1++)
        {
            for (int x1 = -1; x1 <= 1; x1++)
            {
                if (y1 == 0 && x1 == 0)
                {
                    continue;
                }

                //防止索引越界
                if (y + y1 >= 0 && y + y1 < vectorMap.size()
                && x + x1 >= 0 && x + x1 < vectorMap.get(0).length())
                {
                    StringBuilder stringBuilder = vectorMap.get(y + y1);
                    char height = stringBuilder.charAt(x + x1);

                    //高度差超过1，表示是不能通行的
                    if (Math.abs(heightxy - height) > 1)
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * 悬崖
     * @param vectorMap
     * @param x
     * @param y
     * @return
     */
    boolean mapIsCliff(Vector<StringBuilder> vectorMap, int x, int y)
    {
        ArrayList<Integer> arrayList = new ArrayList<>();
        int max = -1;
        int min = 8;
        for (int y1 = -2; y1 <= 2; y1++)
        {
            for (int x1 = -2; x1 <= 2; x1++)
            {
                int y2 = y + y1;
                int x2 = x + x1;
                if (y1 != 0 || x1 != 0)
                {
                    if (y2 >= 0 && y2 < vectorMap.size()
                            && x2 >= 0 && x2 < vectorMap.get(0).length())
                    {
                        int temp = vectorMap.get(y2).charAt(x2) - '0';
                        if (temp > max)
                        {
                            max = temp;
                        }

                        if (temp < min)
                        {
                            min = temp;
                        }
                        arrayList.add(temp);

                    }
                }
            }
        }

        //logger.info("max: " + max + ", min: " + min);
        if (max - min < 3)
        {
            return false;
        }
        for (int i = min; i < max; i++)
        {
            if (!arrayList.contains(i))
            {
                return false;
            }
        }

        return true;
    }

    //平地
    boolean mapIsPlain(Vector<StringBuilder> vectorMap, int x, int y)
    {
        char heightxy = vectorMap.get(y).charAt(x);
        for (int y1 = -1; y1 <= 1; y1++)
        {
            for (int x1 = -1; x1 <= 1; x1++)
            {
                if (y1 != 0 && x1 != 0)
                {
                    if (y + y1 >= 0 && y + y1 < vectorMap.size()
                            && x + x1 >= 0 && x + x1 < vectorMap.get(0).length())
                    {
                        StringBuilder stringBuilder = vectorMap.get(y + y1);
                        char height = stringBuilder.charAt(x + x1);

                        //高度不相等，表示不是平地
                        if (heightxy != height)
                        {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    void drawMap()
    {
        int cellSize = 10;
        Image image = Global.getImage("resource\\1-images\\0-地图\\2-0.png");
        //graphics.drawImage(image,200, 200,10,10, null);
        //logger.info("yStart: " + yStart +" xStart: "+ xStart);
        for (int y = yStart; y < yStart + dispalyWindowHeight; y++)
        {
            for (int x = xStart; x < xStart + dispalyWindowWidth; x++)
            {
                if (y >= stringBuilderVector1.size() - 1 || x >= stringBuilderVector1.get(0).length() - 1
                || y < 0 || x < 0)
                {
                    continue;
                }
                char height = stringBuilderVector1.get(y).charAt(x);
                if (mapCanGo(stringBuilderVector1, x, y))
                {
                    //bufferedImageGraphics.drawImage(image, (x-xStart) * cellSize, (y-yStart) * cellSize,cellSize,cellSize, this);
                }
                else if (mapIsPlain(stringBuilderVector1, x, y))
                {
                    //bufferedImageGraphics.drawImage(image, x * 10, y * 10,10,10, this);
                }

                ;
                if (mapIsCliff(stringBuilderVector1, x, y))
                {
                    bufferedImageGraphics.setColor(Color.blue);
                    bufferedImageGraphics.fillRect((x - 1) * cellSize, (y - 1) * cellSize,3*cellSize,3*cellSize);
                }

                bufferedImageGraphics.setColor(new Color(0,(height - '0') * 25,0));
                bufferedImageGraphics.fillRect((x-xStart) * cellSize, (y -1-yStart) * cellSize, cellSize,cellSize);
                bufferedImageGraphics.setColor(Color.white);
                bufferedImageGraphics.drawString(String.valueOf(height),(x-xStart) * cellSize, (y- yStart) * cellSize);

            }
        }
        /**/
        //graphics.drawImage(bufferedImage,200, 200,10,10, null);
        graphics.drawImage(bufferedImage, 0, 0, windowWidth, windowHeight,this);
    }

    void printMap()
    {
        logger.info("print map:");
        int hasData;
        for (int y = 1; y < 400; y++) //第一行没有数据
        {
            hasData = 0;
            StringBuilder stringBuilder = new StringBuilder();
            for (int x = 0; x < 400; x++)
            {
                Integer b = cellDataIntegerHashMap.get(new CellData(x, y));
                char c = ' ';
                if (b != null)
                {
                    hasData += b;
                    c = (char) (b + '0');
                }
                stringBuilder.append(c);
            }
            System.out.println(stringBuilder);
            stringBuilderVector.add(stringBuilder);

            if (hasData == 0)
            {
                break;
            }
        }
    }

    public static void main(String[] args) {
        MapData mapData = new MapData();
        mapData.readFile("Grinder.mmx");
        mapData.Unpack();
        mapData.transform();

        mapData.printMap();

        mapData.printMap2();

        mapData.print2();

        mapData.print();

        while (true)
        {
            //mapData.drawMap();

            try
            {
                sleep(100);
            } catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }

    }

}



/*

// mapfielddata is the data of every field in an extracted isomappack!
struct MAPFIELDDATA
{
	unsigned short wX;
	unsigned short wY;
	WORD wGround; //typedef unsigned short WORD;
	BYTE bData[3];
	BYTE bHeight;
	BYTE bData2[1];
};
#define MAPFIELDDATA_SIZE 11

* */

// mapfielddata is the data of every field in an extracted isomappack!
class MAPFIELDDATA
{
    long wX;//2 0原来用c++写的，是无符号的int占用两个字节，Java没有无符号，所以用long来表示unsigned int
    long wY;//2 2原来用c++写的，是无符号的int占用两个字节，Java没有无符号，所以用long来表示unsigned int
    long wGround;//2 4原来用c++写的，是无符号的int占用两个字节，Java没有无符号，所以用long来表示unsigned int
    byte bData[] = new byte[3];//3 6
    byte bHeight;//1 9
    byte bData2; //1 10
};