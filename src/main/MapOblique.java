package main;

import decoderealmap.MapObjectData;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Logger;

import static java.lang.Math.abs;

/**
 * 地图类
 * 1、地图数组的初始化定义
 * 2、地图上寻路算法的实现
 * 3、地图上格子的搜索
 * 4、地图上格子的占用和解除占用
 */

public class MapOblique {
    static Logger logger = MyLogger.logger;
    final static int DIRECT_COST = 10;  // 横竖移动代价
    final static int OBLIQUE_COST = 14;  // 斜移动代价

    static int step = 1;
    public static Node start; // 起始结点
    public static Node end; // 最终结点
    static int addNeighborCount;
    static public int width;
    static public int height;
    static Queue<Node> openList = new PriorityQueue<>(); // 优先队列(升序)
    static List<Node> closeList = new ArrayList<>();

    //搜索路径的时候，坦克可以走的路是陆地，而驱逐舰可能走的路是海
    static int findCellType = CellType.LAND;

    /**
     * data中每个元素包含3位，第1位是地图主类型，陆地、海面、斜坡等等type，第2位是subtype，第3位是高度height
     * 主类型中:
     *     1是海
     *     2是高地
     *     4是斜坡
     *
     * 因为高地，斜坡坡会有8个方向，每个方法如下详细如下subtype：
     *          （上）7
     *     （左上）6      8（右上）
     *     （左）5    0      1（右）
     *     （左下）4      2（右下）
     *          （下）3
     *          这就是数组里为什么会有213，223，233，243，253，263，273，283围成的高地
     *          因为高地的边缘会有8种不同类型的小图片
     */
    /**/
    static int[][] data;


    public static int[][] dataBack;

    static int[][] heightData;
    public static int[][] heightDataBack;

    public static void mapRecovery(Coord position, int sideLength)
    {
        if (sideLength < 1 || sideLength > 3)
        {
            return;
        }

        int x = position.x;
        int y = position.y;
        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                data[y + i][x + j] = dataBack[y + i][x + j];
                heightData[y + i][x + j] += heightDataBack[y + i][x + j];
            }
        }
    }

    //地图上位置被占用了，这样坦克就会有碰撞体积了。
    //这里的position是地图坐标，不是像素坐标
    public static void mapUpdate(Coord position, int value, int sideLength)
    {
        if (sideLength < 1 || sideLength > 3)
        {
            return;
        }

        int x = position.x ;
        int y = position.y ;
        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                data[y + i][x + j] += value * 1000;
                heightData[y + i][x + j] += value * 1000;
                heightDataBack[y + i][x + j] += value * 1000;
            }
        }
    }

    /** 在兵营或者战车工厂或者船厂生产的时候，首先要在工厂附近的地图上找一个空的位置才能把生产的单位放到这个位置
     * 不然所有单位放在一个位置，单位是没有碰撞体积，对游戏效果影响比较大
     * 第1位不能有数（有数就是已经摆放建筑了，参考mapUpdate方法）
     *
     * 第2位是1：表示是CellType.SEA，表示海军建造，海军在海里找位置），
     *     是2： 如果是CellType.LAND，表示陆军建造，陆军在陆地上找位置，参考data数组的第1位
     *
     * 第3位是0（表示是平地，参考data数组的第2位），
     * 第4位是高度，不影响
     */
    public static Coord findCell(Coord center, int radius, int type)
    {
        int x = center.x;
        int y = center.y;
        for (int i = -radius; i < radius; i++) {
            for (int j = -radius; j < radius; j++) {
                int cell = data[y + i][x + j];
                int FirstBit = cell / 1000;
                int SeondBit = cell / 100;
                SeondBit %= 10;
                int ThreeBit = cell / 10;
                ThreeBit %= 10;

                if (FirstBit == 0 && ThreeBit == 0)
                {
                    logger.info("center: " + center + ", radius: " + radius + ", result: " + (x + j) + "," + (y + i));

                    //传入的要求从陆地或者海面寻找合适的位置跟地图上取出的坐标类型要能对应上
                    if (SeondBit == type)
                    {
                        return new Coord(x + j, y + i);
                    }
                }
            }
        }

        return null;
    }

    /**
     *地图图片素材占用的单位格子大小：
     * 有的占用1*1：resource\images\0-地图\1-0.png、
     *             resource\images\0-地图\2-0.png
     *             resource\images\0-地图\2-6.png
     *             resource\images\0-地图\2-7.png
     *             resource\images\0-地图\2-8.png
     *
     * 有的占用1*2：resource\images\0-地图\2-1.png、
     *             resource\images\0-地图\2-2.png
     *             resource\images\0-地图\2-3.png
     *             resource\images\0-地图\2-4.png
     *             resource\images\0-地图\2-5.png
     *
     * 有的占用2*2：
     *             resource\images\0-地图\4-6.png
     */
    static final ModelSize[][] modelSize = {
            //[0][0]                    [0][1]
            {new ModelSize(1,1), new ModelSize(1,1)},

            //[1][0]                    [1][1]
            {new ModelSize(1,1), new ModelSize(1,1)},

            //[2][0]                         [2][1]                          [2][2]                           [2][3]                           [2][4]                           [2][5]
            {new ModelSize(1,1),
                    new ModelSize(1,2),
                    new ModelSize(1,2),
                    new ModelSize(1,2),
                    new ModelSize(1,2),
                    new ModelSize(1,2),
                    new ModelSize(1,1),
                    new ModelSize(1,1),
                    new ModelSize(1,1)},

            //[3][0]
            {},

            //[4][0]
            {new ModelSize(1,1),
                    new ModelSize(1,1),
                    new ModelSize(1,1),
                    new ModelSize(1,1),
                    new ModelSize(1,1),
                    new ModelSize(1,1),
                    new ModelSize(3,3),
                    new ModelSize(1,1),},

            //[5][0]
            {new ModelSize(1,1),
                    new ModelSize(10,10),
                    new ModelSize(10,10),},
    };


    public ArrayList<String>  readFromFile2(String path)
    {
        File file = new File(path);
        ArrayList<String> stringArrayList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                stringArrayList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringArrayList;
    }

    public MapObjectData readFromFile(String path)
    {
        MapObjectData mapObjectData1 = null;
        try (FileInputStream fileIn = new FileInputStream(path);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            mapObjectData1 = (MapObjectData) in.readObject(); // 读取对象从文件
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            System.out.println("MyObject类未找到");
            c.printStackTrace();
        }
        System.out.println("反序列化后的对象信息：");
        return mapObjectData1;
    }


    /**
     * 构造方法，初始化
     */
    MapOblique()
    {
        //MapObjectData mapObjectData1 = readFromFile2("map.objectdata");
        //data = mapObjectData1.data;
        ArrayList<String> stringArrayList = readFromFile2(Global.resourceRoot + "7-map" + File.separator +
                "4players.map");

        String string1 = stringArrayList.get(0);
        String[] split1 = string1.split(",");
        width = split1.length;
        height = stringArrayList.size();
        data = new int[height][width];

        for (int i = 0; i < stringArrayList.size(); i++)
        {
            String string = stringArrayList.get(i);
            String[] split = string.split(",");
            for (int j = 0; j < split.length; j++)
            {
                String str = split[j].replaceAll(" ", "");
                data[i][j] = Integer.parseInt(str);
            }
        }

        initHeightData();
    }

    /**
     * 当前是不是海面
     * @param mouse
     * @return
     */
    public static boolean isMapCellSea(Coord mouse)
    {
        int x = mouse.x;
        int y = mouse.y;
        int cell = MapOblique.data[y][x];
        cell %= 1000;
        return cell / 100 == 1;
    }

    /**
     * 地图的高度数据
     */
    void initHeightData()
    {
        heightData = new int[height][width];
        heightDataBack = new int[height][width];
        dataBack = new int[height][width];

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                dataBack[y][x] = data[y][x];
                heightData[y][x] = data[y][x] % 10;
                heightDataBack[y][x] = data[y][x] % 10;
            }
        }
    }

    /**
     * 地图高度数据恢复，注意在陆地上寻路需要用到高度数据
     */
    static void dataRecovery()
    {
        start = null;
        end = null;

        //因为搜索路径会修改地图数组，搜索完成后要恢复数组的内容
        for (int i = 0; i < height; i++)
        {
            //如果不是小兵或者玩家才恢复
            if (width >= 0) System.arraycopy(heightDataBack[i], 0, heightData[i], 0, width);
        }
    }

    /**
     * 判断结点能否放入Open列表
     */
    private static boolean canAddNodeToOpen(Coord current, Coord coord)
    {
        // 超出地图像素大小
        if (coord.x < 0 || coord.x >= width || coord.y < 0 || coord.y >= height)
            return false;

        //高度为1的是海面
        if (findCellType == CellType.LAND && heightData[current.y][current.x] <= 1)
        {
            return false;
        }

        int currentCellHeight = heightData[current.y][current.x];
        currentCellHeight %= 10;
        int newCellHeight = heightData[coord.y][coord.x];
        newCellHeight %= 10;

        // 判断是否是不可通过的结点
        // 这里实际上地图上的上下坡
        if (findCellType == CellType.LAND && abs(currentCellHeight - newCellHeight) > 1)
            return false;

        // 1是海面
        if (findCellType == CellType.SEA && data[coord.y][coord.x] / 100 % 10 != 1)
            return false;

        // 判断结点是否存在close表
        return !isInCloseList(coord);
    }

    private static Node findNodeInOpen(Coord coord)
    {
        //logger.info("");
        if (coord == null || openList.isEmpty()) return null;

        for (Node node : openList)
        {
            if (node.coord.equals(coord))
            {
                return node;
            }
        }
        return null;
    }


    /**
     * 添加一个邻结点到open表
     */
    private static void addNeighborNodeInOpen(Node current, int x, int y, int value)
    {
        if (canAddNodeToOpen(current.coord, new Coord(x, y)))
        {
            Node end1=end;
            Coord coord = new Coord(x, y);
            int G = current.G + value; // 计算邻结点的G值
            Node child = findNodeInOpen(coord);
            if (child == null)
            {
                int H=calcH(end1.coord,coord); // 计算H值
                if(isEndNode(end1.coord,coord))
                {
                    child=end1;
                    child.parent=current;
                    child.G=G;
                    child.H=H;
                }
                else
                {
                    child = new Node(coord, current, G, H);
                }
                openList.add(child);
            }
            else if (child.G > G)
            {
                child.G = G;
                child.parent = current;

                // 重新调整堆
                openList.add(child);
            }
        }
    }

    /**
     * 添加所有邻结点到open表
     */
    private static void addNeighborNodeInOpen(Node current)
    {
        int x = current.coord.x;
        int y = current.coord.y;

        //bufferGraphics.setColor(drawFindingColor);
        //bufferGraphics.fillRect(x, y, 3, 3); //这步比较消耗资源，注释掉会快很多
        addNeighborCount++;

        // 左
        addNeighborNodeInOpen(current, x - step, y, DIRECT_COST);

        // 上
        addNeighborNodeInOpen(current, x, y - step, DIRECT_COST);

        // 右
        addNeighborNodeInOpen(current, x + step, y, DIRECT_COST);

        // 下
        addNeighborNodeInOpen(current, x, y + step, DIRECT_COST);

        // 左上
        addNeighborNodeInOpen(current, x - step, y - step, OBLIQUE_COST);

        // 右上
        addNeighborNodeInOpen(current, x + step, y - step, OBLIQUE_COST);

        // 右下
        addNeighborNodeInOpen(current, x + step, y + step, OBLIQUE_COST);

        // 左下
        addNeighborNodeInOpen(current, x - step, y + step, OBLIQUE_COST);
    }

    private static int calcH(Coord end,Coord coord)
    {
        return abs(end.x - coord.x) + abs(end.y - coord.y);
    }

    /**
     * 判断坐标是否在close表中
     */
    private static boolean isInCloseList(Coord end)
    {
        if (end == null)
            return false;

        if (closeList.isEmpty())
            return false;

        for (Node node : closeList)
        {
            if (node.coord.x == end.x && node.coord.y == end.y)
            {
                return true;
            }
        }
        return false;
    }
    /**
     * 判断结点是否是最终结点
     */
    private static boolean isEndNode(Coord end, Coord coord)
    {
        return end.equals(coord);
    }

    void printHeight()
    {
        int height = data.length;
        int width = data[0].length;
        heightData = new int[height][width];
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                heightData[y][x] = data[y][x] % 10;
                System.out.print("(" + y + "," + x + "=" + data[y][x] % 10 + ") ");
            }
            System.out.println();
        }
    }

    private static void drawFoundPath(Node end, ArrayList<Coord> coordArrayList)
    {
        if(end == null || heightData == null)
            return;

        while (end != null)
        {
            Coord c = end.coord;
            heightData[c.y][c.x] = -2;
            //bufferGraphics.setColor(drawFoundPathColor);
            //bufferGraphics.fillRect(c.x, c.y, 3, 3);
            coordArrayList.add(c);
            end = end.parent;
        }
    }

    /**
     *寻路算法详见：
     *    resource\游戏介绍\2 A星寻路算法在MapOblique.java的pathFindInit方法.docx
     *
     * start,end已经被成员变量占用了，所以加了个1
     */
    public static ArrayList<Coord> pathFindInit(Coord start1, Coord end1, int findCellType)
    {
        //不能超过地图宽度和高度
        if (start1.x >= width
                || start1.y >= height
                || end1.x >= width
                || end1.y >= height )
        {
            logger.info("start or end outof index, can not find path...");
            return null;
        }

        if (heightData[start1.y][start1.x] == -2
                || heightData[end1.y][end1.x] == -2)
        {
            logger.info("start or end must bar, can not find path..." + heightData[start1.y][start1.x] + " " + heightData[end1.y][end1.x]);
            dataRecovery();
            return null;
        }

        //这里除step再乘以step为了加速搜索，建议使用5这种比较容易搜索到的路坐标++++
        start = new Node(start1.x / step * step, start1.y / step * step);
        end = new Node(end1.x / step * step, end1.y / step * step);

        MapOblique.findCellType = findCellType;

        // clean
        openList.clear();
        closeList.clear();
        ArrayList<Coord> coordArrayList = new ArrayList<>();
        pathFindStart(coordArrayList);
        return coordArrayList;
    }

    /**
     * 移动当前结点
     */
    private static void pathFindStart(ArrayList<Coord> coordArrayList)
    {
        //添加第一个结点
        openList.add(start);

        // 开始搜索
        while (!openList.isEmpty())
        {
            Node current = openList.poll();
            closeList.add(current);

            //关键步骤
            if (current != null) {
                addNeighborNodeInOpen(current);
            }

            if (isInCloseList(end.coord)) // 如果终点坐标在closeList里已经有，表示找到路径
            {
                drawFoundPath(end, coordArrayList);
                logger.info(" addNeighborCount " + addNeighborCount);

                addNeighborCount = 0;
                dataRecovery();

                break;
            }
        }
    }

    /**
     * 导路算法单独测试入口
     * @param args
     */
    public static void main(String[] args) {
        Logger logger = MyLogger.logger;
        MapOblique map = new MapOblique();
        map.printHeight();

        ArrayList<Coord> coords = MapOblique.pathFindInit(new Coord(1, 1),
                new Coord(12, 16), CellType.LAND);

        if (coords != null)
        {
            for (int i = 0; i < coords.size(); i++) {
                Coord coord = coords.get(i);
                int x = coord.getX();
                int y = coord.getY();
                logger.info(coord + " " + MapOblique.heightData[y][x]);
            }
        }
    }
}