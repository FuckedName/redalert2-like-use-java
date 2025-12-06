package main;

import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

class ImagesOfUnit
{
    Image[][] images;

    //菜单显示的图片
    Image displayOnMenuImage;

    //地图显示的图片
    Image displayOnMapImage;

    public ImagesOfUnit(Image[][] images, Image displayOnMenuImage, Image displayOnMapImage)
    {
        this.images = images;
        this.displayOnMenuImage = displayOnMenuImage;
        this.displayOnMapImage = displayOnMapImage;
    }
}

class ImagesSaved
{
    transient Logger logger = MyLogger.logger;
    HashMap<Integer, ImagesOfUnit> imageSavedHashMap = new HashMap<>();

    public ImagesOfUnit get(int unitType)
    {
        /**
         * 先获取类型对应图片路径
         */
        ConfigItem configItem = Config.parameterHashMap.get(unitType);
        if (configItem == null)
        {
            logger.info("找不到 unitType: " + unitType);
        }

        if (unitType == UNIT_TYPE.MENU)
        {
            ImagesOfUnit imagesOfUnit2 = new ImagesOfUnit(null,
                    Global.getImage("resource\\images\\4-重工兵种\\8-基地车\\基地展开.png"),
                    Global.getImage("resource\\images\\4-重工兵种\\8-基地车\\基地展开.png"));

            return imagesOfUnit2;
        }

        //原来已经有了，直接返回
        ImagesOfUnit imagesOfUnit = imageSavedHashMap.get(unitType);
        if (imagesOfUnit != null)
        {
            return imagesOfUnit;
        }

        String path = Global.resourcePath + configItem.imagePath + configItem.name;


        //菜单显示的图片
        Image displayOnMenuImage = Global.getImage( path + "-菜单.png");

        //地图显示的图片
        Image displayOnMapImage  = Global.getImage(path + "-展开.png");

        Image[][] imagePath1 = null;
        if (unitType > UNIT_TYPE.SEA_UNIT_START) //如果是海陆空才会有8个移动方向的图片
        {
            //原来没有，那么添加
            imagePath1 = new Image[3][8];
            for (int i = 1; i < 9; i++) {
                String path1 = Global.resourcePath + configItem.imagePath +"1-平地\\" + i + ".png";

                File file = new File(path1);
                if (file.exists()) {
                    imagePath1[0][i - 1] = Global.getImage(path1);
                    //logger.info("路径存在：" + path1);
                } else {
                    logger.info("路径不存在：" + path1);
                    imagePath1[0][i - 1] = null;
                }
            }
        }

        ImagesOfUnit imagesOfUnit2 = new ImagesOfUnit(imagePath1, displayOnMenuImage, displayOnMapImage);
        imageSavedHashMap.put(unitType, imagesOfUnit2);

        return imagesOfUnit2;
    }
}

public class Unit  implements Serializable
{
    //很精髓，没有指针修改map里value的arraylist非常麻烦，有指针后就非常简单
    Unit pointer;
    String name;

    //请注意：这里边存放地图坐标，不能存放像素坐标，不然要来回转换，比较麻烦
    Coord position;
    Coord position2;

    int unitType;
    int teamID;
    static int currentID;
    int id;

    //菜单显示的图片
    transient Image displayOnMenuImage;

    //地图显示的图片
    transient Image displayOnMapImage;

    //占用格式个数，不是像素
    int width;
    int height;

    Color selectedStatus;

    BuildingState buildingState;

    String path;

    int price;

    //建筑在右侧菜单栏显示的位置

    /**
     * 存放最后一次移动的方向，因为移动到最后一个节点的时候计算移动方向的时候会因为没有下一个点会有问题
     * 所以使用上一次的移动方向是非常好的。
     */
    int lastDirection;

    //存放单位平路、上坡、下坡移动8个移动方向：东、东南、南、西南、西、西北、北、东北
    transient Image[][] imagePath;

    NextCell nextCellToMove;

    //计划移动到下一个位置的路径点
    public ArrayList<Coord> pathPlanToGo;
    transient ImagesSaved imagesSaved;
    int pathCurrentIndex;

    transient Logger logger = MyLogger.logger;

    //当前正在攻击的对象，可能是建筑、坦克、兵种，空军、海军等等
    public long attacking;

    public long lastFiredTimer;

    //攻击其他单位距离，小于这个距离可以攻击
    public int fireDistance;

    //攻击力，有上下限比如：[40, 60]
    public int[] damage;

    //总血量
    public int bloodTotal;

    //剩余血量
    public int bloodCurrent;

    //攻击间隔
    public int fireCD;

    public Unit(String name,
                    int teamID,
                    Coord position,
                    int unitType,
                    int width,
                    int height,
                    int price,
                    Color selectedStatus,
                    String path) {
        this.pointer = this;
        this.id = currentID++;
        this.teamID = teamID;
        this.name = name;
        this.lastDirection = 1;

        if (unitType != UNIT_TYPE.MENU)
        {
            if (position.y > MapOblique.height || position.x > MapOblique.width)
            {
                try
                {
                    throw new SelfException("Exception position: " + position);
                }
                catch (SelfException e)
                {
                    e.printStackTrace();
                    logger.info("position error");
                    throw new RuntimeException(e);
                }
            }
        }

        this.position = position;

        this.unitType = unitType;
        this.width = width;
        this.height = height;
        this.price = price;
        this.fireCD = 10;
        this.fireDistance = 5;
        this.path = Global.resourcePath + path;

        //建筑物也是可以攻击的
        this.damage = new int[]{10, 20};
        this.bloodTotal = 200;
        this.bloodCurrent = 200;

        //默认是红色，表示未选中
        this.selectedStatus = selectedStatus;

        this.nextCellToMove = new NextCell();

        // 0：向上移动,
        // 1：移动
        // 2：向下移动
        // 每类移动有8移动方向，上，下，左，右，上左，上右，下左，下右
        // 当前只读入了平地8个方向的移动图片，正常的话应该要把上坡、下坡分别8个方向也读入，用入单位爬坡、下坡使用
        /**
         * 这里有个非常大的问题，新创建对象的时候，每次都要去加载图片，这个对性能影响估计比较大
         * 正常的话，只需要第一次创建的时候加载，后续直接使用原来加载过的就可以了。。。
         */
        imagesSaved = new ImagesSaved();
        ImagesOfUnit imagesOfUnit = imagesSaved.get(unitType);

        //菜单显示的图片
        this.displayOnMenuImage = imagesOfUnit.displayOnMenuImage;

        //地图显示的图片
        this.displayOnMapImage  = imagesOfUnit.displayOnMapImage;

        this.imagePath = imagesOfUnit.images;

        pathPlanToGo = null;
    }

    Unit(Unit building)
    {
        this.pointer = this;
        this.lastDirection = 1;
        this.id = currentID++;
        this.name = building.name;
        this.teamID = building.teamID;
        this.position = building.position;
        this.width = building.width;
        this.unitType = building.unitType;
        this.height = building.height;
        this.fireCD = building.fireCD;
        this.fireDistance = building.fireDistance;
        this.lastFiredTimer = building.lastFiredTimer;
        this.attacking = building.attacking;
        this.damage = building.damage;
        this.bloodTotal = building.bloodTotal;
        this.bloodCurrent = building.bloodCurrent;
        this.displayOnMenuImage = building.displayOnMenuImage;
        this.displayOnMapImage  = building.displayOnMapImage;
        this.path = building.path;
        this.price = building.price;
        this.buildingState = BuildingState.INIT;
        this.nextCellToMove = new NextCell();
        this.pathPlanToGo = new ArrayList<>();

        //默认是红色，表示未选中
        this.selectedStatus = building.selectedStatus;
    }

    void attackSet(int[] damage,
                   int bloodTotal,
                   int bloodCurrent,
                   int fireDistance,
                   int fireCD)
    {
        this.damage = damage;
        this.bloodTotal = bloodTotal;
        this.bloodCurrent = bloodCurrent;
        this.fireCD = fireCD;
        this.fireDistance = fireDistance;
    }

    //攻击就是在子弹链表里新增子弹
    public Bullet attack(Coord positionSource,
                         Coord positionTarget,
                         int teamID,
                         int unitType,
                         Color color,
                         int damage,
                         int xMoveStep,
                         int yMoveStep,
                         int needMoveTotalSteps,
                         Unit target)
    {

        return new Bullet(positionSource,positionTarget,
                teamID, unitType, color, damage, xMoveStep, yMoveStep, needMoveTotalSteps, target);
    }

    void pathInit(Coord start, Coord end){
        int type = CellType.SEA;

        //船厂
        if (unitType > 240000 && unitType < 340000)
        {
            type = CellType.SEA;
        }
        else if (unitType > 340000 && unitType < 430000){//坦克

            type = CellType.LAND;
        }

        //寻路算法
        ArrayList<Coord> pathTemp = MapOblique.pathFindInit(start, end, type);
        if (pathTemp != null) {
            Collections.reverse(pathTemp);
        }
        pathCurrentIndex = 0;
        pathPlanToGo = pathTemp;
        nextCellToMove = new NextCell();
    }

    //因为地图的格子比较大，每个格子移动时又分成15小步移动，这样比较平滑
    public Coord cellMove()
    {
        int x1 = position.x * Global.cellSize + nextCellToMove.stepCurrent * nextCellToMove.xIncrement * 2;
        int y1 = position.y * Global.cellSize + nextCellToMove.stepCurrent * nextCellToMove.yIncrement * 2;
        return new Coord(x1, y1);
    }

    public Coord getPosition() {
        return position;
    }

    public void setPosition(Coord position) {
        this.position = position;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public String toString()
    {
        return ", Unit{" +
                ", name='" + name + '\'' +
                ", position=" + position +
                ", unitType=" + unitType +
                ", teamID=" + teamID +
                ", id=" + id +
                ", width=" + width +
                ", height=" + height +
                ", selectedStatus=" + selectedStatus +
                ", buildingState=" + buildingState +
                ", path='" + path + '\'' +
                ", price=" + price +
                ", lastDirection=" + lastDirection +
                ", pathPlanToGo=" + pathPlanToGo +
                ", pathCurrentIndex=" + pathCurrentIndex +
                ", attacking=" + attacking +
                ", lastFiredTimer=" + lastFiredTimer +
                ", fireDistance=" + fireDistance +
                ", damage=" + Arrays.toString(damage) +
                ", bloodTotal=" + bloodTotal +
                ", bloodCurrent=" + bloodCurrent +
                ", fireCD=" + fireCD +
                '}';
    }

    public void move()
    {

    }

    /**
     *      *          （上）7
     *      *     （左上）6      8（右上）
     *      *  （左）5       0        1（右）
     *      *     （左下）4      2（右下）
     *      *          （下）3
     * @return
     */
    public int computeMoveDirection()
    {
        if (pathCurrentIndex <= 0 || pathCurrentIndex >= pathPlanToGo.size())
        {
            return lastDirection;
        }
        Coord currentPosition = pathPlanToGo.get(pathCurrentIndex - 1);
        Coord nextPosition = pathPlanToGo.get(pathCurrentIndex);

        if (currentPosition.x < nextPosition.x && currentPosition.y == nextPosition.y)
        {
            lastDirection = 1;
        }
        else if (currentPosition.x < nextPosition.x && currentPosition.y < nextPosition.y)
        {
            lastDirection = 2;
        }
        else if (currentPosition.x == nextPosition.x && currentPosition.y < nextPosition.y)
        {
            lastDirection = 3;
        }
        else if (currentPosition.x > nextPosition.x && currentPosition.y < nextPosition.y)
        {
            lastDirection = 4;
        }
        else if (currentPosition.x > nextPosition.x && currentPosition.y == nextPosition.y)
        {
            lastDirection = 5;
        }
        else if (currentPosition.x > nextPosition.x && currentPosition.y > nextPosition.y)
        {
            lastDirection = 6;
        }
        else if (currentPosition.x == nextPosition.x && currentPosition.y > nextPosition.y)
        {
            lastDirection = 7;
        }
        else if (currentPosition.x < nextPosition.x && currentPosition.y > nextPosition.y)
        {
            lastDirection = 8;
        }

        //logger.info("currentPosition: " + currentPosition + ", nextPosition: " + nextPosition);
        //lastDirection = 1;
        return lastDirection;
    }
}
