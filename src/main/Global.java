package main;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

/**
 * 全量单位类型统一管理（第1位和第2位因为飞行兵是兵营生产的，但是兵营也生产陆军的兵，并且寻路算法不一样，所以要区分开来）
 * 第1位：大类类别：建筑（1），海军单位（2），陆军单位（3），空军单位（4），因为不同类单位寻路算法不一样，其他单位（X）
 * 第2位：第几列建造栏建造：建筑（1），防御（2），兵营（3），重工（4），船厂（4），飞机场（4），其他（5 矿，箱子，等等）
 * 第3位：保留位
 * 第4，5，6位：子类类别：
 */
class UNIT_TYPE {
    /**
     * 主建筑列表
     */
    public final static int BUILDING_START = 110000; //主建筑开始

    public final static int POWER_PLANT = 110001; //发电厂

    public final static int REFINERY = 110002; //矿厂
    public final static int ARMORY = 110003; //兵营
    public final static int WAR_FACTORY = 110004; //重工
    public final static int RADIO = 110005; //雷达 盟军建造厂 + 发电厂 + 矿石精炼厂
    public final static int AIR_FORCE_HEADQUARTERS = 110006; //空军指挥部 盟军建造厂 + 发电厂 + 矿石精炼厂
    public final static int BATTLE_LAB = 110007; //空军指挥部 盟军建造厂 + 发电厂 + 矿石精炼厂
    public final static int DOCKYARD = 110008; //船厂 盟军建造厂 + 发电厂 + 矿石精炼厂
    public final static int NOT_THIS_TYPE = 110009; //



    /**
     * 防御建筑
     */

    //盟军：Allied forces
    public final static int DEFENSE_BUILDING_START = 120000; //防御建筑开始
    public final static int WALL_ALLIED_FORCES = 120001;  //盟军围墙
    public final static int SENTINEL_CANNON = 120002; //哨戒炮 sentinelCannon
    public final static int ANTI_AIR_GUN = 120003; //防空炮 antiAirGun



/**
 * 全量单位类型统一管理（第1位和第2位因为飞行兵是兵营生产的，但是兵营也生产陆军的兵，并且寻路算法不一样，所以要区分开来）
 * 第1位：大类类别：建筑（1），海军单位（2），陆军单位（3），空军单位（4），因为不同类单位寻路算法不一样，其他单位（X）
 * 第2位：第几列建造栏建造：建筑（1），防御（2），兵营（3），重工（4），船厂（4），飞机场（4），其他（5 矿，箱子，等等）
 * 第3位：保留位
 * 第4，5，6位：子类类别：
 */
    /**
     * 船厂
     *
     */
    public final static int SEA_UNIT_START = 240000; //防御建筑开始
    public final static int FRIGATE = 240006; //驱逐舰

    /**
     * 两栖船坞运输舰的英语是‌Amphibious Transport Dock Ship‌，缩写为‌LPD‌（Landing Platform Dock）。 ‌
     *     两栖船坞运输舰(LPD)是用于运载登陆兵员,登陆物资,登陆艇或两栖装甲车辆等登陆工具,实施由舰到岸登陆的大型登陆作战舰艇
     */
    public final static int LANDING_PLATFORM_DOCK = 240007;


    /**
     *
     */
    public final static int MENU = 300000; //因为兵种和建筑有很多不同的地方，所以区分开来



    /**
     * 全量单位类型统一管理（第1位和第2位因为飞行兵是兵营生产的，但是兵营也生产陆军的兵，并且寻路算法不一样，所以要区分开来）
     * 第1位：大类类别：建筑（1），海军单位（2），陆军单位（3），空军单位（4），因为不同类单位寻路算法不一样，其他单位（X）
     * 第2位：第几列建造栏建造：建筑（1），防御（2），兵营（3），重工（4），船厂（4），飞机场（4），其他（5 矿，箱子，等等）
     * 第3位：保留位
     * 第4，5，6位：子类类别：
     */
    //兵营ARMORY_UNIT_TYPE
    public final static int ARMORY_UNIT_START = 330000; //防御建筑开始
    public final static int CONSCRIPT = 330001; //动员兵
    public final static int ENGINEER = 330002; //engineer
    public final static int POLICE_DOG = 330003; //policedog


    /**
     * 重工
     */
    //
    public final static int WAR_FACTORY_UNIT_START = 340000; //防御建筑开始
    public final static int MINE_CAR          = 340001;
    public final static int GRIZZLYI_TANK     = 340002; //灰熊坦克grizzlyItank
    public final static int RHINOCEROS_TANK   = 340003; //犀牛坦克grizzlyItank
    public final static int ANTI_AIR_CAR      = 340004; //antiAirCar
    public final static int MAIN_BASE_CAR     = 340006; //盟军基地车

    /**
     * 空军单位
     */
    public final static int AIR_UNIT_START = 430000; //防御建筑开始
    public final static int ROCKETEER = 430004; //飞行兵

    public final static int HELICOPTER = 440005; //直升机


    public final static int MAX = 999999; //直升机
}


enum BuildingState{
    INIT, //初始化状态
    SELECTED, //菜单被鼠标选中，注意还未点击
    CLICKED, //菜单被鼠标点击
    BUILDING, //正在花钱建造中
    BUILDING_FINISHED, //菜单中建造完成
    SELECTING_PLACE_POSITION, //选择合适的位置放到地图上去
    PUT_ON_TO_MAP //放到地图上去
}

//这里的值为什么不能是0，因为这是修改map的cell的最高位，如果是0，修改后不知道有没有修改
class CellType {
    public static final int SEA = 1; //海
    public static final int LAND = 2; //海
    public static final int BRIDGE = 3; //海
    public static final int WALL = 4; //海
    public static final int BUILDING = 5; //海
    public static final int LAND_UNIT = 6; //海
}

class BUILD_MENU {
    public static final int MAIN = 0; //主菜单
    public static final int DEFENSE = 1; //防御类
    public static final int BARRACKS = 2; //兵营菜单
    public static final int WAR_FACTORY = 3; //重工菜单
    public static final int MAX = 4; //默认时没有菜单可以显示
}


enum MapUnit {
    IDLE, // 可以能行
    BAR, // 障碍
    MINION,  // 小兵
    FOUND_PATH,  // 小兵
    PLAYER, // 玩家
}

public class Global
{
    static Logger logger = MyLogger.logger;
    // public static String resourcePath = "resource\\images\\";
    // public static String srcPath = "src\\";
    // 用java.io.File.separator可以让代码在macOS上运行
    // 否则会报错 Exception in thread "main" java.lang.RuntimeException: java.io.FileNotFoundException: src\config.txt
    public static String resourcePath = "resource" + java.io.File.separator + "1-images" + java.io.File.separator;
    public static String resourceRoot = "resource" + java.io.File.separator ;
    public static String srcPath = "src" + java.io.File.separator;
    //每个地图格子大小
    public static int cellSize = 24;
    public final static int SUB_MENU_LINE_MAX = 7;
    public final static int SUB_MENU_COLUMN_MAX = 2;
    public static final int MAX_DISTANCE_INSTALL_BUILDING_COLSING_TO_FRIENDLY = 300;
    public static final int tcpServerPort = 8001;

    public static final String tcpServerIP = "127.0.0.1";

    public Data data;

    public static boolean inRectangle(Coord point, Unit building)
    {
        return building.position.x <= point.x && building.position.x + building.width >= point.x
                && building.position.y <= point.y && building.position.y + building.height >= point.y;
    }

    public static final Color UNIT_SELECTED = Color.green;   //单位被选中
    public static final Color NEUTRAL_UNIT = Color.white;    //中立单位
    public static final Color MENU_DEFAULT = Color.white;      //
    public static final Color MENU_FONT_COLOR = Color.black;      //
    public static final Color MENU_BACKGROUND_COLOR = Color.red;      //
    public static final Color FIRE_AREA = Color.red;         //
    public static final Color PLAN_MOVE_TO = Color.red;         //
    public static final Color FAR_FROM_FRIEND_BUILDING = Color.red;         //

    public static boolean inRectangle2(Coord point, Unit building)
    {
        Coord position = building.position;
        return position.x <= point.x && position.x + building.width >= point.x
                && position.y <= point.y && position.y + building.height >= point.y;
    }


    public static boolean inRectangle2(Coord[] points, Unit building)
    {
        int xMax = Math.max(points[0].x, points[1].x);
        int xMin = Math.min(points[0].x, points[1].x);

        int yMax = Math.max(points[0].y, points[1].y);
        int yMin = Math.min(points[0].y, points[1].y);

        return xMin <= building.position.x && building.position.x <= xMax
        && yMin <= building.position.y && building.position.y <= yMax;
    }

    public static int getDistance(Coord p1, Coord p2)
    {
        if (p2 == null)
        {
            return -1;
        }

        int absx = abs(p1.x - p2.x);
        int absy = abs(p1.y - p2.y);
        return (int) sqrt(absx * absx + absy * absy);

    }

    public static int getSelectedSubmenu(Coord mouse, Coord position, PanelUnit buildingItems)
    {
        /**
         * 右侧下方的菜单，当前是：2行7列
         */
        if (mouse.x > position.x
                && mouse.x < position.x + SUB_MENU_COLUMN_MAX * buildingItems.width
                && mouse.y > position.y
                && mouse.y < position.y + SUB_MENU_LINE_MAX * buildingItems.height)
        {
            int column = mouse.x - position.x;
            int line = mouse.y - position.y;
            int i = column / buildingItems.width;
            int k = line / buildingItems.height;
            int id = k * 2 + i;
            //logger.info("id: " + id + ",  column: " + i + ", line: " + k);
            return id;
        }

        return -1;
    }


    public static int getDistance(Coord p1, Building building)
    {
        Coord p2 = building.position;
        int absx = abs(p1.x - p2.x);
        int absy = abs(p1.y - p2.y);
        return (int) sqrt(absx * absx + absy * absy);
    }

    //像素坐标转为地图坐标
    public static Coord pixelToMap(Coord input)
    {
        return new Coord(input.x / cellSize, input.y / cellSize);
    }

    public static Coord mapToPixel(Coord input)
    {
        return new Coord(input.x * cellSize, input.y * cellSize);
    }

    public static int getDistance(Unit building1, Unit building2)
    {
        Coord p1 = building1.position;
        Coord p2 = building2.position;
        int absx = abs(p1.x - p2.x);
        int absy = abs(p1.y - p2.y);
        return (int) sqrt(absx * absx + absy * absy);
    }

    public static Image getImage(String path)
    {
        if (Files.exists(Paths.get(path)))
        {
            //logger.info(path + " exists");
            return Toolkit.getDefaultToolkit().getImage(path);
        }
        else
        {
            logger.info(path + " does not exists");
            return null;
        }
    }

    public static final String ACCEPT_FROM_SERVER = "Accept request, send from server: ";

    //8个队伍颜色初始化
    public static final Color[] colors = new Color[]{Color.red,
            Color.yellow,
            Color.cyan,
            Color.orange,
            Color.pink,
            Color.blue,
            Color.green};

    //队伍基地位置,当前的地图还比较小，只支持两个基地点
    public static final Coord[] playerStartCoord = new Coord[]{
            Global.pixelToMap(new Coord(745,712)),
            Global.pixelToMap(new Coord(2304,712)),
            Global.pixelToMap(new Coord(745,2304)),
            Global.pixelToMap(new Coord(2304,2304))};

    public static boolean contains(ArrayList<LandUnit> buildings, LandUnit building)
    {
        for (int i = 0; i < buildings.size(); i++)
        {
            LandUnit building1 = buildings.get(i);
            if (building1.id == building.id)
            {
                return true;
            }
        }

        return false;
    }
}
