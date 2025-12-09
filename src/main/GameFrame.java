package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 游戏运行时前端界面类
 * 主类：
 * 1、鼠标、键盘输入的监听
 * 2、队伍的创建
 * 3、地图、菜单界面的绘制
 *
 //主界面类
 //完成地图，队伍、建筑、兵种的绘制，
 //完成鼠标点击菜单的响应
 //完成建筑的安装、兵种、坦克的生产
 //完成服务器或者客户端建筑、兵种、坦克的打包发送给对端
 */
public class GameFrame extends JFrame {
    
    //保存鼠标移动后的位置事件
    MouseEvent mouseEvent1 = null;
    String status = "";
    String title;
    
    //这是地图的大小
    private final int mapWidth;//画板的宽度
    private final int mapHeight;//画板的高度

    //整个全地图
    BufferedImage bufferedImage;
    Graphics bufferedImageGraphics;


    //电脑显示器的大小
    private static int monitorWidth;
    private static int monitorHeight;



    //这是显示窗口大小的信息，正常来说地图的大小比窗口大小大好几倍
    int xStart;
    int yStart;
    int displayWindowWidth;
    int displayWindowHeight;

    //左侧窗口地图（只显示地图的一部分）
    BufferedImage bufferedDisplayWindowImage;
    Graphics bufferedDisplayWindowImageGraphics;

    //右侧控制面板
    BufferedImage bufferedControlPanelWindowImage;
    Graphics bufferedControlPanelWindowImageGraphics;

    private final Graphics graphics; //画笔
    Logger logger = MyLogger.logger;
    static int cellSize = Global.cellSize;
    private SerializedVector serializedVector;
    //时间片
    long timer;

    Coord mouseEvent;
    //游戏里有很多个队伍联网，当前本地ID识别，其他的队伍也会有一个ID
    int selfTeamID;
    int enemyTeamID;

    //存放地图数组
    int[][] data;
    ArrayList<Coord> bridgeArrayList;

    //地图最小模型大小，有的格子占1*1，有的占1*2，1*2这样，尤其是在高地的边缘
    ModelSize[][] modelSizes;
    PanelUnit radio;

    //存放金币所在菜单数组的编号
    int goldBuildingID;

    int panelStartGap = 50;
    int panelLineGap = 10;

    //右侧雷达
    BufferedImage radioBufferedImage;
    Graphics radioBufferedImageGraphics;

    //右侧建造详细项
    BufferedImage buildItemsBufferedImage;
    Graphics buildItemsBufferedImageGraphics;

    PanelUnit buildingItems;

    //用于右侧面板显示，包含4个子菜单："建筑", "防御","兵营","重工"
    ArrayList<ArrayList<Unit>> buildingsForPanel;

    Point[] mouseSelectRectangle;
    Coord mouseFirstPoint = new Coord(-1, -1);
    Coord mouseSecondPoint = new Coord(-1, -1);
    Coord mouseThirdPoint;

    Color buildingTempColor = new Color(0, 255, 0);

    //所有坦克的对象都应该属于一个队伍，队伍也包含中立队伍
    LandUnit[] tank;
    AirUnit airUnit;

    final Object lock = new Object();
    Vector<BuildProcess> buildProcessArrayList;

    //存放地图每个格子图片的二维数组
    Image[][] mapImages;
    int buildingStep = 1;

    //展开动画的图片
    Image[][] buildImages;

    //存放所有队伍汇总，很重要
    ArrayList<Team> teamArrayList;

    //存放菜单的数组，比如：主菜单，设置这些
    ArrayList<Building> menuList;

    //当前显示的菜单："建筑", "防御","兵营","重工"
    int currentDisplayedMenu;

    //当前选中的建筑，这里有点问题，只能选择1个，如果有多个单位，比如同时选中20辆坦克，20个步兵等等
    //所以建设用数组存放
    Unit buildingSelected;
    ArrayList<Unit> unitArrayListSelected;
    ArrayList<Coord> pointList;

    //地图玩家视角本来是从侧上方看地图的，理论上来说纵坐标会扁一点，这里正常设置为0.8比较合适
    //但是这里改为0.8以后有很多地方需要适配的，有些不太容易改，所以又改为1了。。。。
    private double heightRate=1;

    /**
     * 构造方法
     */
    GameFrame()
    {
        title = "Red alert 2 ID: " + MainThread.thisDeviceType;
        selfTeamID = MainThread.thisDeviceType;
        enemyTeamID = 1;

        MapOblique map = new MapOblique();
        map.printHeight();
        data = MapOblique.data;
        bridgeArrayList = new ArrayList<>();
        for (int y = 0; y < MapOblique.height; y++) {
            for (int x = 0; x < MapOblique.width; x++) {
                if (data[y][x] / 100 % 10 == 5)
                {
                    bridgeArrayList.add(new Coord(x, y));
                };
            }
        }

        mapWidth = MapOblique.width * Global.cellSize;
        mapHeight = (int) (MapOblique.height * Global.cellSize * heightRate);
        setTitle(title);
        getScreenResolution();
        timer = 0;

        bufferedImage = new BufferedImage(mapWidth, mapHeight, BufferedImage.TYPE_INT_RGB);
        bufferedImageGraphics = bufferedImage.createGraphics();
        bufferedImageGraphics.setColor(Color.RED);
        bufferedImageGraphics.setFont(new Font("", Font.BOLD,15));

        bufferedDisplayWindowImage = new BufferedImage(monitorWidth * 80 / 100, displayWindowHeight,
                BufferedImage.TYPE_INT_RGB);
        bufferedDisplayWindowImageGraphics = bufferedDisplayWindowImage.createGraphics();

        bufferedControlPanelWindowImage = new BufferedImage(monitorWidth * 20 / 100, displayWindowHeight,
                BufferedImage.TYPE_INT_RGB);
        bufferedControlPanelWindowImageGraphics = bufferedControlPanelWindowImage.createGraphics();


        mouseEvent = new Coord(0, 0);
        mouseSelectRectangle = new Point[2];
        for (int i = 0; i < 2; i++) {
            mouseSelectRectangle[i] = new Point();
        }
        mapImages = new Image[10][10];
        setLayout(null);
        setSize(monitorWidth, monitorHeight);
        setLocationRelativeTo(null);
        serializedVector = new SerializedVector();

        //窗口没有标题
        //setUndecorated(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setVisible(true);
        //setTitle("红色有点警戒2");
        graphics = getContentPane().getGraphics();
        graphics.setFont(new Font("", Font.BOLD,15));
        logger.info("MainThread.thisDeviceType: " + MainThread.thisDeviceType);
        buildingSelected = null;
        currentDisplayedMenu = BUILD_MENU.MAX;
        pointList = new ArrayList<>();
        buildingsForPanel = new ArrayList<>();
        ArrayList<Unit> arrayList0 = new ArrayList<>();
        ArrayList<Unit> arrayList1 = new ArrayList<>();
        ArrayList<Unit> arrayList2 = new ArrayList<>();
        ArrayList<Unit> arrayList3 = new ArrayList<>();

        buildingsForPanel.add(arrayList0);
        buildingsForPanel.add(arrayList1);
        buildingsForPanel.add(arrayList2);
        buildingsForPanel.add(arrayList3);

        //mouseSelectRectangle = new Point[2];
        logger.info("height: " + data.length * cellSize + ", width " + data[0].length * cellSize);
        modelSizes = MapOblique.modelSize;
        initBuildBuildingImages();
        initMapImagePath();

        // mouseClicked   mousePressed    mouseReleased  mouseEntered
        addMouseListener();

        // mouseDragged  mouseMoved
        addMouseMotionListener();

        //
        addMouseWheelMotionListenerSelf();

        addKeyCapture();

        teamInit();

        unitArrayListSelected = new ArrayList<>();

        Coord teamInitialHomePosition = teamArrayList.get(selfTeamID).teamInitialHomePosition;
        xStart = teamInitialHomePosition.x * cellSize - 400;
        yStart = teamInitialHomePosition.y * cellSize - 400;
        initProcesses();

        initPanelMenu();

        initTankMoveTest();

        //UpdateWindow();
    }

    int getScreenResolution()
    {
        // 获取本地图形环境
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        // 获取默认屏幕设备
        GraphicsDevice gd = ge.getDefaultScreenDevice();

        // 获取显示模式
        DisplayMode dm = gd.getDisplayMode();

        // 获取屏幕宽度和高度
        monitorWidth = dm.getWidth();
        monitorHeight = dm.getHeight();

        displayWindowWidth = monitorWidth * 80/100 ;
        displayWindowHeight = monitorHeight;

        // 获取屏幕分辨率（像素/英寸）
        double dpi = gd.getDefaultConfiguration().getDefaultTransform().getScaleX();

        System.out.println("Screen Width: " + displayWindowWidth + " pixels");
        System.out.println("Screen Height: " + displayWindowHeight + " pixels");
        System.out.println("Screen Resolution (DPI): " + dpi + " dpi");

        return 0;
    }

    public void ifNotExistAdd(int menuID, Unit input)
    {
        ArrayList<Unit> buildings1 = buildingsForPanel.get(menuID);
        if (!buildings1.contains(input)) {
            buildingsForPanel.get(menuID).add(input);
        }
    }

    void drawButton2(Graphics graphics,
                    int x,
                    int y,
                    int width,
                    int height,
                    Color selectedStatus,
                    String name)
    {
        graphics.setColor(selectedStatus);
        graphics.setFont(new Font("", Font.BOLD,15));
        graphics.fillRect(x, y, width, height);

        graphics.setColor(Global.MENU_FONT_COLOR);
        graphics.drawString(name, x, y+15);
    }

    void initProcesses()
    {
        buildProcessArrayList = new Vector<>();
    }

    /**
     * 初始化队伍
     */
    void teamInit()
    {
        teamArrayList = new ArrayList<>();
        Team team0 = new Team(0); //server的本地队伍编号是0
        Team team1 = new Team(1); //
        Team team2 = new Team(2); //
        Team team3 = new Team(3); //
        teamArrayList.add(team0);
        teamArrayList.add(team1);
        teamArrayList.add(team2);
        teamArrayList.add(team3);
    }

    /**
     * 安装不同主建筑，在防御菜单、兵营、重工菜单可能可以建造新的兵种
     * 这里其实可以写到一个文件或者一个二维数组来维护
     */
    public void afterPlantBuilding(int menuID, int type)
    {
        if (BUILD_MENU.MAX == menuID)
        {
            return;
        }

        Team team = teamArrayList.get(selfTeamID);

        //这里其实有些是有问题的，有些新增菜单是需要好几个建筑同时有才可以的。
        switch (type)
        {
            //电厂安装完成后，可以建造矿厂和兵营，所以向右下的控制面板数组里添加
            case UNIT_TYPE.POWER_PLANT:
                ifNotExistAdd(BUILD_MENU.MAIN, team.refineryBuilding); //矿厂
                ifNotExistAdd(BUILD_MENU.MAIN, team.armoryBuilding);  //兵营
                break;

            //矿厂要有重工可以造矿车，这里后续再处理
            case UNIT_TYPE.REFINERY:
                ifNotExistAdd(BUILD_MENU.MAIN, team.dockyardBuilding); //船厂


                ifNotExistAdd(BUILD_MENU.WAR_FACTORY, team.minecarBuilding); //矿车
                break;

            //兵营比较特殊，兵营建造后可以建造防御类建筑和兵营兵种
            case UNIT_TYPE.ARMORY:
                ifNotExistAdd(BUILD_MENU.MAIN, team.warFactoryBuilding); //重工 这里其实需要判断基地是否存在，正常新建兵营的时候，基地是在的，不然没法建造


                ifNotExistAdd(BUILD_MENU.DEFENSE, team.wallBuilding); //围墙
                ifNotExistAdd(BUILD_MENU.DEFENSE, team.sentinelCannonBuilding); //哨戒炮
                ifNotExistAdd(BUILD_MENU.DEFENSE, team.antiAirGunBuilding); //防空炮

                ifNotExistAdd(BUILD_MENU.BARRACKS, team.conscriptBuilding); //动员兵
                ifNotExistAdd(BUILD_MENU.BARRACKS, team.engineerBuilding); //工程师
                ifNotExistAdd(BUILD_MENU.BARRACKS, team.policedogBuilding); //警犬
                break;

            //重工：WAR_FACTORY
            case UNIT_TYPE.WAR_FACTORY:
                ifNotExistAdd(BUILD_MENU.MAIN, team.airForceHeadquarters);

                ifNotExistAdd(BUILD_MENU.WAR_FACTORY, team.minecarBuilding); //矿车
                ifNotExistAdd(BUILD_MENU.WAR_FACTORY, team.grizzlyItankBuilding); //灰熊坦克
                ifNotExistAdd(BUILD_MENU.WAR_FACTORY, team.antiAirCarBuilding); //防空车
                ifNotExistAdd(BUILD_MENU.WAR_FACTORY, team.helicopterBuilding); //直升级
                break;

            //空军指挥部
            case UNIT_TYPE.AIR_FORCE_HEADQUARTERS:
                ifNotExistAdd(BUILD_MENU.MAIN, team.battleLab);

                ifNotExistAdd(BUILD_MENU.BARRACKS, team.Rocketeer); //火箭飞行兵
                break;

            //船厂
            case UNIT_TYPE.DOCKYARD:
                ifNotExistAdd(BUILD_MENU.WAR_FACTORY, team.landingPlatformDockShip); //两栖船坞运输舰
                ifNotExistAdd(BUILD_MENU.WAR_FACTORY, team.frigateBuilding); //驱逐舰
                break;

            default:
                logger.info("default branch: " + type);
        }
    }

    public void initPlantBuilding(int type, int menuID) 
	{
        Team team = teamArrayList.get(selfTeamID);
        switch (type)
        {
            case UNIT_TYPE.POWER_PLANT:
                if (!buildingsForPanel.get(menuID).contains(team.powerplantBuilding)) {
                    buildingsForPanel.get(menuID).add(team.powerplantBuilding);
                }

                break;

        }
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
                    //'-'
                    case 33:
                       // if (dispalyWindowWidth + 10 < windowWidth)
                        {
                            displayWindowWidth += 100;
                            displayWindowHeight += 100;
                        }
                        break;

                    //'+'
                    case 34:
                        //if (dispalyWindowWidth - 10 > windowWidth/3)
                        {
                            displayWindowWidth -= 100;
                            displayWindowHeight -= 100;
                        }
                        break;

                    default:
                        logger.info("default..." + e.toString());
                }

                switch (e.getKeyChar())
                {
                    case 'd':
                    case 'D':
                        if (buildingSelected != null)
                        {
                            //buildingStep = 1;
                            buildingSelected = null;
                        }
                        break;

                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
    }

    void buildingSelected(Coord mouse, Unit buildingSelected)
    {
        //logger.info(buildingSelected.toString());
        //logger.info(mouse.toString());

        if (buildingSelected == null)
        {
            return;
        }

        //当前被点击的是XXX菜单
        if (buildingSelected.buildingState == BuildingState.SELECTING_PLACE_POSITION)
        {
            buildingSelected.setPosition(mouse);
            bufferedImageGraphics.drawImage(buildingSelected.displayOnMapImage,
                    mouse.getX(),
                    mouse.getY(),
                    buildingSelected.width,
                    buildingSelected.height,
                    null);
        }
    }

    /**
     * 监听鼠标如下操作
     * mouseDragged  mouseMoved（鼠标移动事情，重要）
      */

    void addMouseMotionListener()
    {
        this.addMouseMotionListener(new MouseMotionListener()
        {

            /**
             * 处理鼠标移动时的事件
             * @param e 鼠标信息
             */
            @Override
            public void mouseMoved(MouseEvent e) {
                //logger.info("mouseMoved: " + e.getX() + " " + e.getY() + " buildingSelected: " + buildingSelected );
                mouseEvent1 = e;

                Coord coord1 = new Coord(xStart + e.getX(), yStart + e.getY());
                Coord pixelToMap = Global.pixelToMap(coord1);
                mouseEvent = coord1;
                if (teamArrayList.isEmpty())
                {
                    return;
                }

                //当前如果有多个单位被选中，则不能通过鼠标移动修改单位被选中的状态
                if (!unitArrayListSelected.isEmpty())
                {
                    return;
                }

                //这里想实现的功能是鼠标移动到单位（建筑，坦克、兵等等）时，单位显示绿色，鼠标离开单位时，单位显示为红色
                //因为鼠标移动只会有一个单位被选择到，如果把所有单位都放到一个数组，那么只需要遍历一遍数组就可以
                //知道当前鼠标光标对应的单位或者没有单位
                //当前没有建筑或者菜单被点击
                if (buildingSelected == null)
                {
                    if (Global.inRectangle(coord1, teamArrayList.get(selfTeamID).mainBuilding))
                    {
                        teamArrayList.get(selfTeamID).mainBuilding.selectedStatus = Global.UNIT_SELECTED;
                        return;
                    }
                    else
                    {
                        teamArrayList.get(selfTeamID).mainBuilding.selectedStatus = teamArrayList.get(selfTeamID).color;
                    }

                    if (e.getX() >  monitorWidth * 80 / 100)
                    {
                        Coord coord2 = new Coord(e.getX() - monitorWidth * 80 / 100, e.getY());
                        //这是菜单，例如：设置，售卖，主建筑这类
                        for (int i = 0; i < menuList.size(); i++)
                        {
                            Building building = menuList.get(i);
                            if (Global.inRectangle(Global.pixelToMap(coord2), building))
                            {
                                menuList.get(i).selectedStatus = Global.UNIT_SELECTED;
                                return;
                            }
                            else
                            {
                                menuList.get(i).selectedStatus = Global.MENU_DEFAULT;
                            }
                        }
                    }


                    teamArrayList.get(selfTeamID).inAirUnitRectangleSetColor(coord1, Global.UNIT_SELECTED, teamArrayList.get(selfTeamID).color);

                    Coord coord2 = Global.pixelToMap(new Coord(e.getX(), e.getY()));
                    teamArrayList.get(selfTeamID).inLandBuildingsSetColor(coord2, Global.UNIT_SELECTED, teamArrayList.get(selfTeamID).color);

                    Coord position = buildingItems.position;
                    int id = Global.getSelectedSubmenu(new Coord(e.getX(), e.getY()), position, buildingItems);

                    //感觉这里的算法不太好，效率有点低
                    //没有子菜单被鼠标选中
                    if (id == -1)
                    {
                        if (currentDisplayedMenu != BUILD_MENU.MAX)
                        {
                            ArrayList<Unit> buildings = buildingsForPanel.get(currentDisplayedMenu);
                            for (int i = 0; i < buildings.size(); i++) {
                                buildingsForPanel.get(currentDisplayedMenu).get(i).selectedStatus = Global.MENU_DEFAULT;
                            }
                        }
                    }
                    else //有菜单被选中
                    {
                        if (currentDisplayedMenu < buildingsForPanel.size())
                        {
                            ArrayList<Unit> buildings = buildingsForPanel.get(currentDisplayedMenu);
                            if (id < buildings.size())
                            {
                                //Building building = buildings.get(id);
                                //logger.info(building.selectedStatus.toString());
                                buildingsForPanel.get(currentDisplayedMenu).get(id).selectedStatus = Global.UNIT_SELECTED;
                                return;
                            }
                        }
                    }

                    if (tank != null)
                    {
                        //如果坦克被选中，显示绿色填充圆，未被选中则显示成红色
                        for (int i = 0; i < tank.length; i++)
                        {
                            if (tank[i] != null)
                            {
                                Coord coord = new Coord(e.getX() / cellSize, e.getY() / cellSize + 1);
                                //logger.info(tank[i].name + " x: " + tank[i].position.x + " y: " + tank[i].position.y);
                                if (Global.inRectangle2(coord, tank[i]))
                                {
                                    tank[i].selectedStatus = Global.UNIT_SELECTED;
                                    return;
                                }
                                else
                                {
                                    tank[i].selectedStatus = Global.NEUTRAL_UNIT;
                                }
                            }
                        }
                    }

                    if (airUnit != null)
                    {
                        if (Global.inRectangle(pixelToMap, airUnit))
                        {
                            airUnit.selectedStatus = Global.UNIT_SELECTED;
                            return;
                        }
                        else
                        {
                            airUnit.selectedStatus = Global.NEUTRAL_UNIT;
                        }
                    }

                    //建筑被鼠标移动是选中
                    //teamArrayList.get(selfTeamID).nearToBuildingsSetColor(pixelToMap,
                    //        Global.UNIT_SELECTED, teamArrayList.get(selfTeamID).color);

                    for (int j = 0; j < teamArrayList.size(); j++)
                    {
                        if (j == selfTeamID)
                        {
                            Team team = teamArrayList.get(j);
                            team.nearToBuildingsSetColor(pixelToMap,
                                    Global.UNIT_SELECTED, team.color);
                        }
                    }

                }

                buildingSelected(coord1, buildingSelected);
            }


            @Override
            public void mouseDragged(MouseEvent e) {
                if (mouseSelectRectangle[1] != null)
                {
                    mouseSelectRectangle[1].x = e.getX();
                    mouseSelectRectangle[1].y = e.getY();
                }
            }
        });


    }

    /**
     * 地图放大缩小
     */
    void addMouseWheelMotionListenerSelf()
    {
        this.addMouseWheelListener(new MouseWheelListener()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                logger.info(e.toString());
                if (e.getWheelRotation() == 1)
                {
                    cellSize -= 5;
                }
                else if (e.getWheelRotation() == -1)
                {
                    cellSize += 5;

                }

            }
        });
    }


    //右下角7行2列的子菜单被点击
    void submenuSelectedStatus(Coord mouse)
    {
        ArrayList<Unit> buildings = buildingsForPanel.get(currentDisplayedMenu);
        if (buildings.isEmpty())
        {
            return;
        }

        Coord position = buildingItems.position;

        //判断控制面板中是否某个子菜单被选中
        for (int j = 0; j < buildings.size(); j++)
        {
            //菜单里的子项
            Unit building = buildings.get(j);
            logger.info(building.name + " " + building);

            //有的建筑在菜单栏添加了，但是还未放置到地图上，所以位置为null
            if (building.position == null)
            {
                continue;
            }

            if (mouse.x > position.x
                    && mouse.x < position.x + Global.SUB_MENU_COLUMN_MAX * buildingItems.width
                    && mouse.y > position.y
                    && mouse.y < position.y + Global.SUB_MENU_LINE_MAX * buildingItems.height)
            {
                int id = Global.getSelectedSubmenu(mouse, position, buildingItems);
                if (id >= buildingsForPanel.get(currentDisplayedMenu).size())
                {
                    return;
                }
                Unit building1 = buildingsForPanel.get(currentDisplayedMenu).get(id);

                Unit temp = null;

                switch (currentDisplayedMenu)
                {
                    case BUILD_MENU.MAIN:
                    case BUILD_MENU.DEFENSE:
                        temp = Team.getBuildingByName(building1.name);
                        break;

                        //兵营生产兵
                    case BUILD_MENU.BARRACKS:
                        teamArrayList.get(selfTeamID).buildByBarracks(building1); //生产兵种
                        return;

                        //重工生产坦克和轮船
                    case BUILD_MENU.WAR_FACTORY:
                        int tenThousand = building1.unitType / 10000;
                        int first = building1.unitType / 100000;
                        int second = tenThousand % 10;
                        if (second == 4)
                        {
                            if (first == 1)
                            {

                            }
                            else if (first == 2) //240006
                            {
                                teamArrayList.get(selfTeamID).buildByShipyard(building1);

                            }
                            else if (first == 3) //340002
                            {
                                teamArrayList.get(selfTeamID).buildByWarFactory(building1); //生产坦克
                            }
                            else
                            {
                                logger.info("unrecognized type");
                            }
                        }
                        return;
                }

                if (temp != null) {
                    temp.selectedStatus = teamArrayList.get(selfTeamID).color;
                    temp.teamID = selfTeamID;
                    buildingSelected = new Unit(temp);
                    int[] damage = new int[]{50, 70};
                    int bloodTotal = 100;
                    int bloodCurrent = 20;
                    int fireDistance = 10;
                    int fireCD = 10;
                    buildingSelected.attackSet(damage,
                            bloodTotal,
                            bloodCurrent,
                            fireDistance,
                            fireCD);
                    buildingSelected.buildingState = BuildingState.SELECTING_PLACE_POSITION;
                    buildingSelected.setPosition(Global.pixelToMap(mouse));
                }
                else
                {
                    buildingSelected = null;
                }
                synchronized (lock) {
                    BuildProcess buildProcess = new BuildProcess(position.x + building1.position.x,
                            position.y + building1.position.y,
                            50,
                            50,
                            100,
                            timer,
                            Color.BLUE);

                    buildProcessArrayList.add(buildProcess);
                }
                logger.info("building selected: " + building.getPosition().x / building.width + " " +  building.getPosition().y / building.height);
            }
        }
    }

    //菜单被点击
    void handleMenuClicked(Building building)
    {
        logger.info(building.toString());
        int type = UNIT_TYPE.MAX;

        switch (building.name)
        {
            case "建筑":
                currentDisplayedMenu = BUILD_MENU.MAIN;
                type = UNIT_TYPE.ARMORY;
                break;

            case "兵营":
                currentDisplayedMenu = BUILD_MENU.BARRACKS;
                type = UNIT_TYPE.ARMORY;
                break;

            case "防御":
                currentDisplayedMenu = BUILD_MENU.DEFENSE;
                type = UNIT_TYPE.ARMORY;
                break;

            case "重工":
                currentDisplayedMenu = BUILD_MENU.WAR_FACTORY;
                type = UNIT_TYPE.WAR_FACTORY;
                break;

            default:
                logger.info("default branch: " + building);
        }

        //安装之前这里边会有待安装的建筑，安装建筑后，临时建筑要清空。。
        buildingSelected = null;

        //这里其实是有问题的，这个步骤正常是在安装后才会添加的
        afterPlantBuilding(currentDisplayedMenu, type);

        //因为有的子菜单比较多，比如原来显示3排，新菜单只有2排，这样只显示新菜单，那原来的第3排就不会清除，所以需要重置下
        resetSubMenu();

        //显示当前菜单下的子菜单
        displaySubMenu();
    }

    /**
     * 如果是点击的菜单，那表示要安装对应的建筑
     */
    void handleMainBuildingInstall(Unit building, Coord mouse)
    {
        int type = building.unitType;
        Unit temp = Team.getBuildingByName(building.name);
        Building building1 = new Building(temp);
        building1.position = new Coord(mouse);
        buildingSelected.setPosition(mouse);

        logger.info(String.valueOf(type));

        //把已经选择的建筑添加到buildingSelected
        teamArrayList.get(selfTeamID).buildingsMapAddUnit(building1);

        MapOblique.mapUpdate(mouse,
                CellType.BUILDING,
                2);
        buildingSelected = null;
        afterPlantBuilding(currentDisplayedMenu, type);
    }


    /**
     * 每个单位进行寻路
     *
     * unit 单位
     * mouseToMap 目的地
     *
     */

    void handleUnitArrayListSelected(
            Unit unit, Coord mouseToMap)
    {
        Team team0 = teamArrayList.get(selfTeamID);
        //船厂
        if (unit.unitType > 240000 && unit.unitType < 340000)
        {
            pointList.clear();
            pointList.add(unit.position);
            pointList.add(mouseToMap);

            boolean b = team0.seaUnitsGenerateToGoPath(unit, mouseToMap);
            if (b)
            {
                return;
            }
        }
        //原来坦克被选中，现在鼠标点了一个新的位置，那么坦克先生成一个路径列表，再移动到新的位置
        else if (unit.unitType > 340000 && unit.unitType < 430000)//坦克
        {
            pointList.clear();
            pointList.add(unit.position);
            pointList.add(mouseToMap);

            boolean b = team0.landUnitsGenerateToGoPath(unit, mouseToMap);
            if (b)
            {
                return;
            }
        }
        else

            //原来火箭飞行兵被选中，现在鼠标点了一个新的位置，那么火箭飞行兵先生成一个路径列表，再移动到新的位置
            if (unit.unitType > 430000) {
                teamArrayList.get(selfTeamID).airUnitsGenerateToGoPath(unit, mouseToMap);
                return;
            }

        //安装建筑时，需要在自己的建筑附近
        //这里还有问题，安装建筑时，建筑的图片应该在同一个高度平面
        //这里有点要注意，靠近友方建筑时，船厂的距离更远
        if (teamArrayList.get(selfTeamID).isCloseToFriendlyBuilding(unit.unitType, mouseToMap))
        {
            //地图有些高地不平的时方也不能安装建筑
            if (teamArrayList.get(selfTeamID).buildingInSameHeight(unit, mouseToMap)) {
                //安装的建筑正常只会是前两个菜单
                if (currentDisplayedMenu >= BUILD_MENU.BARRACKS) {
                    return;
                }

                //有个例外的建筑，船厂是在海里修建

                //是海面
                if (MapOblique.isMapCellSea(mouseToMap))
                {
                    logger.info("地图单元是海面，除了船厂外不能安装其他建筑！");

                    //是船厂
                    if (unit.unitType == UNIT_TYPE.DOCKYARD)
                    {
                        Unit buildingSelected1 = unit;
                        handleMainBuildingInstall(buildingSelected1, mouseToMap);
                    }
                }
                else //不是海面
                {
                    //不是船厂
                    if (unit.unitType != UNIT_TYPE.DOCKYARD)
                    {
                        Unit buildingSelected1 = unit;
                        handleMainBuildingInstall(buildingSelected1, mouseToMap);
                    }
                }
            }
            else
            {
                logger.info("地面不平，不能安装建筑");
            }
        }
        else
        {
            logger.info("距离友方建筑太远，不能安装建筑");
        }

        for (int i = 0; i < menuList.size(); i++) {
            Building building = menuList.get(i);
            if (Global.inRectangle(mouseToMap, building))
            {
                //这里只处理了展开菜单，实际还有很多菜单需要处理
                if (building.name.equals("展开"))
                {
                    if (unit != null && unit.name.equals("盟军基地")) {
                        //buildingStep = 1;
                        unit = null;
                    }
                }
            }
        }
    }

    void handleUnitSelected(Coord mouseToMap)
    {
        Team team0 = teamArrayList.get(selfTeamID);

        //船厂
        if (buildingSelected.unitType > 240000 && buildingSelected.unitType < 340000)
        {
            pointList.clear();
            pointList.add(buildingSelected.position);
            pointList.add(mouseToMap);

            boolean b = team0.seaUnitsGenerateToGoPath(buildingSelected, mouseToMap);
            if (b)
            {
                return;
            }
        }
        //原来坦克被选中，现在鼠标点了一个新的位置，那么坦克先生成一个路径列表，再移动到新的位置
        else if (buildingSelected.unitType > 340000 && buildingSelected.unitType < 430000)//坦克
        {
            pointList.clear();
            pointList.add(buildingSelected.position);
            pointList.add(mouseToMap);

            boolean b = team0.landUnitsGenerateToGoPath(buildingSelected, mouseToMap);
            if (b)
            {
                return;
            }
        }
        else

        //原来火箭飞行兵被选中，现在鼠标点了一个新的位置，那么火箭飞行兵先生成一个路径列表，再移动到新的位置
        if (buildingSelected.unitType > 430000) {
            teamArrayList.get(selfTeamID).airUnitsGenerateToGoPath(buildingSelected, mouseToMap);
            return;
        }

        //安装建筑时，需要在自己的建筑附近
        //这里还有问题，安装建筑时，建筑的图片应该在同一个高度平面
        //这里有点要注意，靠近友方建筑时，船厂的距离更远
        if (teamArrayList.get(selfTeamID).isCloseToFriendlyBuilding(buildingSelected.unitType, mouseToMap))
        {
            //地图有些高地不平的时方也不能安装建筑
            if (teamArrayList.get(selfTeamID).buildingInSameHeight(buildingSelected, mouseToMap)) {
                //安装的建筑正常只会是前两个菜单
                if (currentDisplayedMenu >= BUILD_MENU.BARRACKS) {
                    return;
                }

                //有个例外的建筑，船厂是在海里修建

                //是海面
                if (MapOblique.isMapCellSea(mouseToMap))
                {
                    logger.info("地图单元是海面，除了船厂外不能安装其他建筑！");

                    //是船厂
                    if (buildingSelected.unitType == UNIT_TYPE.DOCKYARD)
                    {
                        Unit buildingSelected1 = buildingSelected;
                        handleMainBuildingInstall(buildingSelected1, mouseToMap);
                    }
                }
                else //不是海面
                {
                    //不是船厂
                    if (buildingSelected.unitType != UNIT_TYPE.DOCKYARD)
                    {
                        Unit buildingSelected1 = buildingSelected;
                        handleMainBuildingInstall(buildingSelected1, mouseToMap);
                    }
                }
            }
            else
            {
                logger.info("地面不平，不能安装建筑");
            }
        }
        else
        {
            logger.info("距离友方建筑太远，不能安装建筑");
        }

        for (int i = 0; i < menuList.size(); i++) {
            Building building = menuList.get(i);
            if (Global.inRectangle(mouseToMap, building))
            {
                //这里只处理了展开菜单，实际还有很多菜单需要处理
                if (building.name.equals("展开"))
                {
                    if (buildingSelected != null && buildingSelected.name.equals("盟军基地")) {
                        //buildingStep = 1;
                        buildingSelected = null;
                    }
                }
            }
        }
    }

    /**
     * 监听鼠标如下几个方法
     * mouseClicked（鼠标点击，重要）   mousePressed    mouseReleased  mouseEntered
      */
    void addMouseListener()
    {
        this.addMouseListener(new MouseListener() {

            /**
             * 处理鼠标点击事件
             * @param e 鼠标
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                int width = 200;
                int height = 200;
                Coord mouseToMap = Global.pixelToMap(new Coord(e.getX(), e.getY()));
                Coord mouseWithDisplayWindowToMap = Global.pixelToMap(new Coord(xStart + e.getX(), yStart + e.getY()));
                Coord mouse = new Coord(e.getX(), e.getY());
                Coord mouse2 = new Coord(xStart + e.getX(), yStart + e.getY());

                Team team0 = teamArrayList.get(selfTeamID);

                logger.info("e: " + new Coord(xStart + e.getX(), yStart + e.getY()));
                //logger.info("e: " + e + ", building selected: " + buildingSelected);

                //鼠标右键点击
                if (e.getButton() == 3)
                {
                    buildingSelected = null;
                    unitArrayListSelected.clear();
                    return;
                }

                if (!unitArrayListSelected.isEmpty())
                {
                    Unit unit0 = unitArrayListSelected.get(0);
                    for (int i = 0; i < unitArrayListSelected.size(); i++) {
                        Unit unit = unitArrayListSelected.get(i);
                        int x1 = unit.position.x - unit0.position.x;
                        int y1 = unit.position.y - unit0.position.y;
                        int x2 = mouseWithDisplayWindowToMap.x + x1;
                        int y2 = mouseWithDisplayWindowToMap.y + y1;

                        //每个单位进行寻路，这里感觉有优化的空间，因为很多单位可以走相同的路
                        handleUnitArrayListSelected(unit, new Coord(x2, y2));
                    }

                    return;
                }

                //当前有建筑或者菜单被选中
                if (buildingSelected != null)
                {
                    handleUnitSelected(mouseWithDisplayWindowToMap);
                    return;
                }
                else if (Global.inRectangle(mouseWithDisplayWindowToMap, team0.mainBuilding)) //判断主建筑是否被选中,这里有点BUG，主基地也可能有很多个，这里只判断了一个
                {
                    bufferedImageGraphics.drawArc(xStart + e.getX() - width/2,
                            yStart + e.getY() - width/2,
                            width,
                            height,
                            0,
                            360);
                    logger.info("building selected: " + team0.mainBuilding.toString());
                    buildingSelected = team0.mainBuilding;
                    return;
                }
                else //这里是右侧菜单被选中
                {
                    if (e.getX() > monitorWidth * 80 / 100)
                    {
                        Coord mouseToMap2 = Global.pixelToMap(new Coord(e.getX() - monitorWidth * 80 / 100, e.getY()));

                        //这里是处理右侧菜单逻辑
                        for (int i = 0; i < menuList.size(); i++) {
                            Building building = menuList.get(i);
                            if (Global.inRectangle(mouseToMap2, building))
                            {
                                menuList.get(i).selectedStatus = Global.UNIT_SELECTED;
                                handleMenuClicked(menuList.get(i));
                                return;
                            }
                            else
                            {
                                menuList.get(i).selectedStatus = Global.MENU_DEFAULT;
                            }
                        }
                    }
                }

                //这里是判断所有队伍中是否有单位被选中
                for (int i = 0; i < teamArrayList.size(); i++) {
                    Team team = teamArrayList.get(i);
                    Unit building = team.unitSelected(mouse2);
                    if (building != null) {
                        buildingSelected = building;
                        return;
                    }
                }

                if (currentDisplayedMenu == BUILD_MENU.MAX)
                {
                    return;
                }

                //右下侧子菜单是否有菜单被选中
                submenuSelectedStatus(mouse);

                if (mouseFirstPoint.x == -1 && mouseFirstPoint.y == -1)
                {
                    //如果点击的是空地，则记录鼠标的位置，如果连续点击两个空白的地方，那表示要选择这两个点组成矩形里所有的单位
                    mouseFirstPoint = mouse2;
                    status = "mouseFirstPoint clicked: " + mouse2;
                    return;
                }

                if (mouseFirstPoint.x >= 0)
                {
                    mouseSecondPoint = mouse2;
                    status = "mouseSecondPoint clicked: " + mouse2;

                    Coord[] points = new Coord[2];
                    points[0] = mouseFirstPoint;
                    points[1] = mouseSecondPoint;
                    logger.info("mouseFirstPoint: " + mouseFirstPoint + " mouseSecondPoint: " + mouseSecondPoint);
                    unitArrayListSelected.clear();
                    Team team = teamArrayList.get(selfTeamID);

                    //把自己队伍里所有在鼠标点击两个点以内所有单位
                    team.unitsSelected(points, unitArrayListSelected);

                    logger.info("unit count: " + unitArrayListSelected.size() + " has been selected...");
                    logger.info(unitArrayListSelected.toString());

                    for (int i = 0; i < unitArrayListSelected.size(); i++)
                    {
                        unitArrayListSelected.get(i).selectedStatus = Global.UNIT_SELECTED;
                    }

                    //把记录鼠标已点击位置清空，以备下次选择
                    mouseFirstPoint = new Coord(-1, -1);
                    mouseSecondPoint = new Coord(-1, -1);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (mouseSelectRectangle[0] == null)
                {
                    mouseSelectRectangle[0] = new Point();
                }
                if (mouseSelectRectangle[1] == null)
                {
                    mouseSelectRectangle[1] = new Point();
                }

                mouseSelectRectangle[0].x = e.getX();
                mouseSelectRectangle[0].y = e.getY();
                mouseSelectRectangle[1].x = 0;
                mouseSelectRectangle[1].y = 0;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //logger.info(e.toString());

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                //logger.info(String.valueOf(MainThread.thisDeviceType));

            }

            @Override
            public void mouseExited(MouseEvent e) {
                //logger.info(e.toString());
            }
        });
    }

    void initBuildBuildingImages()
    {
        buildImages = new Image[2][95];
        for (int i = 1; i < 2; i++) {
            for (int j = 1; j <= 95; j++) {
                String cell = i + "\\" + j;
                buildImages[i][j - 1] = Global.getImage(Global.resourcePath + "展开动画\\" + cell + ".png");
            }
        }
    }

    /**
     * 将地图格子图片读入图片二维数组
     */
    void initMapImagePath()
    {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 10; j++) {
                String mapName = "0-地图\\" + i + "-" + j;
                mapImages[i][j] = Global.getImage(Global.resourcePath + mapName + ".png");
            }
        }
    }

    /**
     * 这是初始化测试用的坦克
     */
    void initTankMoveTest()
    {
        String name = "犀牛坦克";
        tank = new LandUnit[10];

        tank[0] = new LandUnit(name,4,
                new Coord(2,2),
                UNIT_TYPE.GRIZZLYI_TANK,
                2,
                2,
                700,
                Color.white,
                "4-重工兵种\\2-犀牛坦克\\"
                );

        MapOblique.mapUpdate(new Coord(2,2), CellType.LAND_UNIT, 1);
        int[] damage = new int[]{10, 20};
        int bloodTotal = 100;
        int bloodCurrent = 20;
        int fireDistance = 100;
        int fireCD = 30;

        tank[0].attackSet(damage,
                bloodTotal,
                bloodCurrent,
                fireDistance,
                fireCD
                );

        tank[0].pathInit(Global.pixelToMap(new Coord(880 ,256)),
                Global.pixelToMap(new Coord(285 ,848)));

        MapOblique.mapUpdate(Global.pixelToMap(new Coord(880 ,256)), 
                CellType.LAND_UNIT, 1);

        name = "灰熊坦克";
        tank[1] = new LandUnit(name,4,
                new Coord(6,6),
                UNIT_TYPE.GRIZZLYI_TANK,
                2,
                2,
                700,
                Color.white,
                "4-重工兵种\\2-灰熊坦克\\");

        MapOblique.mapUpdate(Global.pixelToMap(new Coord(574, 548)), CellType.LAND_UNIT, 1);
        tank[1].pathInit(Global.pixelToMap(new Coord(574, 548)),
                Global.pixelToMap(new Coord(1047 ,301 )));

        name = "基地车";
        tank[2] = new LandUnit(name,4,
                new Coord(2 ,
                        2 ),
                UNIT_TYPE.GRIZZLYI_TANK,
                2,
                2,
                700,
                Color.white,
                "4-重工兵种\\8-基地车\\"
        );

        MapOblique.mapUpdate(Global.pixelToMap(new Coord(297 ,451 )), CellType.LAND_UNIT, 1);
        tank[2].pathInit(Global.pixelToMap(new Coord(297 ,260)),
                Global.pixelToMap(new Coord(868 ,840)));


        name = "火箭飞行兵";
        airUnit = new AirUnit(name,4,
                new Coord(26,
                        22),
                Global.NEUTRAL_UNIT,
                UNIT_TYPE.ROCKETEER,
                "3-兵营兵种\\4-火箭飞行兵\\");
        airUnit.pathInit(new Coord(26, 22),
                new Coord(23, 13));
    }

    /**
     * 右侧菜单初始化
     */
    void initPanelMenu()
    {
        // 雷达
        radio = new PanelUnit();
        radio.position = new Coord(monitorWidth * 80/100, 70 );
        radio.width = monitorWidth*20 / 100;
        radio.height = 200;
        radioBufferedImage = new BufferedImage(radio.width, radio.height, BufferedImage.TYPE_INT_RGB);
        radioBufferedImageGraphics = radioBufferedImage.createGraphics();

        buildingItems = new PanelUnit();
        buildingItems.position = new Coord(monitorWidth * 80/100, 430 + 5 * panelLineGap);

        buildingItems.width = 100;
        buildingItems.height = 100;
        buildingItems.totalHeight = 520;
        buildingItems.totalWidth = 260;
        buildItemsBufferedImage = new BufferedImage(buildingItems.totalWidth,
                buildingItems.totalHeight,
                BufferedImage.TYPE_INT_RGB);
        buildItemsBufferedImageGraphics = buildItemsBufferedImage.createGraphics();
        resetSubMenu();
        menuList = new ArrayList<>();


        String[][] menuNames = new String[][]{
                {"设置", "菜单", "预留", "预留"},
                {"维修", "出售", "预留", "预留"},
                {"展开", "路径", "同类", "编号"},
                {"建筑", "防御","兵营","重工"},
        };

        int height1 = 1;
        int x1;
        int x0 = panelStartGap;
        int y1 = 0;
        y1 += cellSize;

        String[] names;
        names = new String[]{"Gold", "设备ID(0为服务端)： " + MainThread.thisDeviceType};

        int width1 = 6;
        x1 = x0;
        for (int i = 0; i < 2; i++) {
            Building building = new Building(names[i],
                    selfTeamID,
                    Global.pixelToMap(new Coord(x1, y1)),
                    UNIT_TYPE.MENU,
                    width1,
                    height1,
                    0,
                    Global.MENU_FONT_COLOR,
                    null,
                    "");
            menuList.add(building);
            x1 += width1 * cellSize;
            //
        }

        //让菜单位置更加合理些
        y1 += 3 * cellSize + radio.height;
        goldBuildingID = menuList.size() - 2;

        graphics.setColor(Global.MENU_FONT_COLOR);
        graphics.setFont(new Font("",Font.BOLD,14));
        width1 = 2;
        for (int i = 0; i < menuNames.length; i++)
        {
            x1 = x0;
            for (int j = 0; j < menuNames[0].length; j++)
            {
                Building building = new Building(menuNames[i][j], selfTeamID, Global.pixelToMap(new Coord(x1, y1)), UNIT_TYPE.MENU, width1, height1, 0, Global.MENU_FONT_COLOR, null, "");
                menuList.add(building);
                x1 += width1 * cellSize + cellSize;
            }
            y1 += 2 * height1 * cellSize;
        }

    }

    void drawMouseSelectRectangle()
    {
        if (mouseSelectRectangle[0] != null
                && mouseSelectRectangle[1] != null
                && mouseSelectRectangle[0].x != 0
                && mouseSelectRectangle[0].y != 0
                && mouseSelectRectangle[1].x != 0
                && mouseSelectRectangle[1].y != 0)
        {
            bufferedImageGraphics.drawRect(mouseSelectRectangle[0].x - 14,
                    mouseSelectRectangle[0].y - 26,
                    mouseSelectRectangle[1].x - mouseSelectRectangle[0].x,
                    mouseSelectRectangle[1].y - mouseSelectRectangle[0].y);
        }
    }

    void drawSelectedBuildings()
    {
        if (buildingSelected == null)
        {
            return;
        }

        if (buildingSelected.unitType > 200000)
        {
            return;
        }

        Image image;
        if (buildingSelected.buildingState == BuildingState.INIT)
        {
            image = buildingSelected.displayOnMenuImage;
        }
        else
        {
            image = buildingSelected.displayOnMapImage;
        }

        //logger.info(buildingSelected.toString());

        bufferedImageGraphics.drawImage(image,
                buildingSelected.position.x,
                buildingSelected.position.y,
                buildingSelected.width,
                buildingSelected.height,
                this
        );

        int width = 0;
        int height = 0;
        if (buildingSelected != null) {
            width = buildingSelected.width;
            height = buildingSelected.height;
        }

        if (mouseEvent.getX() == 0 && mouseEvent.getY() == 0)
        {
            return;
        }

        int x = mouseEvent.getX() / cellSize;
        int y = mouseEvent.getY() / cellSize;
        if (y >= MapOblique.height || x >= MapOblique.width)
        {
            logger.info("y > MapOblique.height || x > MapOblique.width,y= " + y
            + ", x= " + x + ", MapOblique.width= " + MapOblique.width + ", MapOblique.height= " + MapOblique.height);
            return;
        }
        int oldHeight = MapOblique.heightDataBack[y][x];

        Coord pixelToMap = Global.pixelToMap(buildingSelected.position);

        //这里如果有选中的建筑物，准备在地上安装，建筑物覆盖的地面，如果不在同一高度，就会显示不同的颜色
        //在游戏里，比如基地，一般需要安装在平地，如果有悬崖则不能安装
        for (int i = 0; i < height; i++)
        {
            for (int j = 0; j < width; j++)
            {
                if (y + i >= MapOblique.height || x + j >= MapOblique.width)
                {
                    logger.info("y + i >= MapOblique.height || x + j >= MapOblique.width " + y
                            + ", x= " + x + ", MapOblique.width= " + MapOblique.width + ", MapOblique.height= " + MapOblique.height);
                    return;
                }
                int cellHeight = MapOblique.heightDataBack[y + i][x + j];

                //这里存在高度差
                if (oldHeight != cellHeight)
                {
                    //logger.info("building is not in same height...");
                    bufferedImageGraphics.setColor(Color.CYAN);
                }
                else  //不存在高度差
                {
                    bufferedImageGraphics.setColor(Color.green);
                }

                //如果距离友方建筑太远，则显示为红色
                if (!teamArrayList.get(selfTeamID).isCloseToFriendlyBuilding(buildingSelected.unitType, pixelToMap))
                {
                    bufferedImageGraphics.setColor(Global.FAR_FROM_FRIEND_BUILDING);
                }
                else
                {
                    if (buildingSelected.unitType == UNIT_TYPE.DOCKYARD)
                    {
                        if (MapOblique.isMapCellSea(pixelToMap))
                        {
                            bufferedImageGraphics.setColor(Color.green);
                        }
                        else
                        {
                            bufferedImageGraphics.setColor(Global.FAR_FROM_FRIEND_BUILDING);
                        }
                    }

                    if (buildingSelected.unitType != UNIT_TYPE.DOCKYARD && MapOblique.isMapCellSea(pixelToMap))
                    {
                        bufferedImageGraphics.setColor(Global.FAR_FROM_FRIEND_BUILDING);
                    }
                }


                bufferedImageGraphics.fillRect(
                        (x + j) * cellSize,
                        (y + i) * cellSize,
                        cellSize,
                        cellSize
                );
            }
        }
    }

    //这里绘制的一般是格子的高度，因为地图有高度属性
    void drawCellHeight()
    {
        for (int y = 0; y < data.length; y++) {
            int[] line = data[y];
            for (int x = 0; x < line.length; x++) {
                int thousand = data[y][x] / 1000;
                int height2 = thousand * 10 + data[y][x] % 10;
                int color =  250-data[y][x] % 10 * 40;
                bufferedImageGraphics.setColor(new Color(0,color,0));
                bufferedImageGraphics.drawString(String.valueOf(height2),
                        x * cellSize,
                        (int) (y * cellSize * heightRate)
                );
            }
            // System.out.println();
        }
    }

    //这里绘制的一般是占1个格子的地图图片
    void drawCell11()
    {
        //画普通地图
        for (int y = 0; y < data.length; y++) {
            int[] line = data[y];
            for (int x = 0; x < line.length; x++) {
                int type = line[x] / 100;
                if (line[x] < 0)
                {
                    continue;
                }
                type %= 10;
                if (type == 0)
                {
                    continue;
                }
                int subtype = line[x] / 10 % 10;
                //logger.info("(" +type +" " +subtype + "), ");


                //这里地图子类型为6，7，8的时候，会缺一个角，所以先在地图上显示平地，然后在显示缺角的地图，这样就没有黑色三角形了。
                if (subtype == 6 || subtype == 7 || subtype == 8)
                {
                    bufferedImageGraphics.drawImage(mapImages[type][0],
                            x * cellSize,
                            (int) ((y - 1 )* cellSize * heightRate),
                            cellSize,
                            (int) (cellSize * heightRate),
                            null);

                }

                //这里代码不太好，双层for循环里有IO
                bufferedImageGraphics.drawImage(mapImages[type][subtype],
                        x * cellSize,
                        (int) ((y - 1 )* cellSize * heightRate),
                        cellSize,
                        (int) (cellSize * heightRate),
                        null);

            }
            // System.out.println();
        }
        // System.out.println();
    }

    void drawWidthModelSize(Image map, int x, int y, ModelSize modelSize)
    {
        bufferedImageGraphics.drawImage(map,
                x * cellSize,
                (int) ((y - 1 ) * cellSize * heightRate),
                modelSize.width * cellSize,
                (int) (modelSize.height * cellSize * heightRate),
                null);
    }

    /**
     * 绘制基地
     */
    void drawMainBuilding()
    {
        Team team = teamArrayList.get(selfTeamID);
        Image image = null;
        Building mainBuilding = team.mainBuilding;
        BuildingState state = mainBuilding.buildingState;
        switch (state)
        {
            case INIT:
                image = mainBuilding.displayOnMenuImage;
                break;

            case PUT_ON_TO_MAP:
                image = mainBuilding.displayOnMapImage;
                break;
        }

        bufferedImageGraphics.drawImage(image,
                mainBuilding.position.x * cellSize,
                (int) ((mainBuilding.position.y - 1 )* cellSize * heightRate),
                mainBuilding.width * cellSize,
                mainBuilding.height * cellSize,
                this);

        bufferedImageGraphics.setColor(mainBuilding.selectedStatus);
        bufferedImageGraphics.fillArc(mainBuilding.position.x * cellSize,
                (int) ((mainBuilding.position.y - 1 )* cellSize * heightRate),
                20,
                20,
                0,
                360);

        bufferedImageGraphics.setColor(teamArrayList.get(selfTeamID).color);
        bufferedImageGraphics.drawArc(mainBuilding.position.x * cellSize,
                (int) ((mainBuilding.position.y - 1 )* cellSize * heightRate),
                mainBuilding.fireDistance,
                mainBuilding.fireDistance,
                0,
                360
                );

        if (buildingStep > 0)
        {
            buildingStep = 0;
            logger.info("buildingStep: " + buildingStep);

            //如果主基地已经展开
            teamArrayList.get(selfTeamID).mainBuilding.buildingState = BuildingState.PUT_ON_TO_MAP;

            //建造的第一个主菜单显示可以建造发电厂
            currentDisplayedMenu = BUILD_MENU.MAIN;

            //这里先把发电厂添加到Building列表，方便右侧面板打印菜单图标
            //注意，此时发电厂在地图上还没有放置，需要鼠标点击右侧主建筑下的发电厂，再在地图上找个位置放置
            initPlantBuilding(UNIT_TYPE.POWER_PLANT, currentDisplayedMenu);

            buildingSelected = null;
        }

    }

    /**
     * 单位之间攻击就是新增子弹
     * @param selfTank
     * @param selfTeamID
     * @param enemyTank
     */
    void attack2(Unit selfTank,
                 int selfTeamID,
                 Unit enemyTank)
	{
        int damageMin = selfTank.damage[0];
        int damageMax = selfTank.damage[1];

        //伤害=【60，80】
        int damage = (int) (Math.random() * (damageMax - damageMin) + damageMin);

        Coord p1 = selfTank.position;
        Coord p2 = enemyTank.position;

        int xMoveStep = p2.x - p1.x;
        int yMoveStep = p2.y - p1.y;

        //设置坦克正在攻击的对象
        selfTank.pointer.attacking = enemyTank.id;

        //更新坦克正在的攻击的单位和最后一次攻击的时间戳
        //这里在计算上一次攻击时间时加了一个小的随机时间，不加的话，所有攻击时间都是一样的，不好看
        selfTank.pointer.lastFiredTimer = (long) (timer + (double) selfTank.fireCD + 4 * Math.random());

        Bullet bullet = selfTank.attack(selfTank.position,
                enemyTank.position,
                selfTeamID,
                selfTank.unitType,
                teamArrayList.get(selfTeamID).color,
                damage,
                xMoveStep,
                yMoveStep,
                cellSize,
                enemyTank);

        //if (!teamArrayList.get(selfTeamID).allBullets.contains(bullet)) {
            teamArrayList.get(selfTeamID).allBullets.add(bullet);
        //logger.info("team id: " + selfTeamID + ", bullet: " + bullet.toString());

            // 友方坦克完成攻击后，需要把新的状态信息更新到selfLandUnitsMap，
            // 1、而敌方坦克的数据是传过来的，所以没有办法更新到敌方坦克去
            // 2、并且攻击检测是在drawTeams()之后，所以也看不到远端结果
            // 这个问题需要检测，攻击计算统一会放到服务器端，计算完成后会返回给客户端
        //}
    }

    /**
     * 把每个队伍的海军、陆军、空军、建筑放到一个数组Vector<Building>
     */
    Vector<Unit> copyOneTeamAllUnitsToBuildingVector(Team team)
    {
        Vector<Unit> buildingVector = new Vector<>();

        ConcurrentHashMap<Integer, ArrayList<SeaUnit>> seaUnitsMap = team.seaUnitsMap;
        Iterator<Map.Entry<Integer, ArrayList<SeaUnit>>> iterator = seaUnitsMap.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<Integer, ArrayList<SeaUnit>> next = iterator.next();
            ArrayList<SeaUnit> seaUnitArrayList = next.getValue();
            buildingVector.addAll(seaUnitArrayList);
        }

        ConcurrentHashMap<Integer, ArrayList<LandUnit>> landUnitsMap = team.landUnitsMap;
        Iterator<Map.Entry<Integer, ArrayList<LandUnit>>> iterator2 = landUnitsMap.entrySet().iterator();
        while (iterator2.hasNext())
        {
            Map.Entry<Integer, ArrayList<LandUnit>> next = iterator2.next();
            ArrayList<LandUnit> seaUnitArrayList = next.getValue();
            buildingVector.addAll(seaUnitArrayList);
        }

        ConcurrentHashMap<Integer, ArrayList<Building>> buildingsMap = team.buildingsMap;
        Iterator<Map.Entry<Integer, ArrayList<Building>>> iterator3 = buildingsMap.entrySet().iterator();
        while (iterator3.hasNext())
        {
            Map.Entry<Integer, ArrayList<Building>> next = iterator3.next();
            ArrayList<Building> seaUnitArrayList = next.getValue();
            buildingVector.addAll(seaUnitArrayList);
        }

        ConcurrentHashMap<Integer, ArrayList<AirUnit>> airUnitsMap = team.airUnitsMap;
        Iterator<Map.Entry<Integer, ArrayList<AirUnit>>> iterator4 = airUnitsMap.entrySet().iterator();
        while (iterator4.hasNext())
        {
            Map.Entry<Integer, ArrayList<AirUnit>> next = iterator4.next();
            ArrayList<AirUnit> seaUnitArrayList = next.getValue();
            buildingVector.addAll(seaUnitArrayList);
        }

        return buildingVector;
    }

    String generateOutputString(long timer, int i, int j, Unit unit1, Unit unit2)
    {
        return  "timer: " + timer + ", team : " + i + " attack team : " + j + ", unit1: " + unit1
                + " ,unit2: " + unit2;
    }

    /**
     * 所有队伍之间进行攻击检测，有些精髓：
     * 1、所有队伍的所有海军、陆军、空军、建筑，要相互进行攻击检测非常麻烦
     *    本代码如何处理的呢？把每个队伍的海军、陆军、空军、建筑放到一个数组Vector<Building>
     *    然后再来个4层for循环搞定
     * 2、坏处就是会多些开销，好处就是非常的清晰，膜拜下自己，doge。
     */
    void attackCheck2()
    {
        //logger.info("");

        Vector<Vector<Unit>> vectors = new Vector<>();
        for (int i = 0; i < teamArrayList.size(); i++)
        {
            Team team1 = teamArrayList.get(i);

            /**
             * 把每个队伍的海军、陆军、空军、建筑放到一个数组Vector<Building>
             */
            Vector<Unit> buildingVector = copyOneTeamAllUnitsToBuildingVector(team1);
            vectors.add(buildingVector);
        }
        //logger.info(vectors.toString());

        /**
         * 4 层for循环
         */
        for (int i = 0; i < teamArrayList.size(); i++) {
            for (int j = 0; j < i; j++) {
                Vector<Unit> buildingVector1 = vectors.get(i);
                Vector<Unit> buildingVector2 = vectors.get(j);
                for (int k = 0; k < buildingVector1.size(); k++) {
                    for (int l = 0; l < buildingVector2.size(); l++) {
                        Unit unit1 = buildingVector1.get(k);
                        Unit unit2 = buildingVector2.get(l);

                        //在攻击范围内，这里注意，team1Tank和team2Tank的攻击距离不一定相同
                        int distance = Global.getDistance(unit1, unit2);
                        if (distance < unit1.fireDistance) {
                            //原来是没有攻击过别人，或者攻击过别人，时间超过攻击间隔了，那么在攻击范围内，则攻击
                            if (unit1.lastFiredTimer == 0 || timer > unit1.lastFiredTimer + unit1.fireCD) {
                                // team1 tank attack
                                logger.info(generateOutputString(timer, i, j, unit1, unit2));
                                /**
                                 * 这里碰到一个问题，攻击后需要更新原来攻击者和被攻击者单位的数据，
                                 * 比如：需要更新lastFiredTimer，血量等等信息，也即是需要修改这几个map里边某个ArrayList，某几个Building的值，没想到一个好的办法
                                 HashMap<Integer, ArrayList<Building>> buildingsMap;
                                 HashMap<Integer, ArrayList<SeaUnit>> seaUnitsMap;
                                 HashMap<Integer, ArrayList<Building>> landUnitsMap;
                                 HashMap<Integer, ArrayList<AirUnit>> airUnitsMap;
                                 */
                                if (unit1.pointer != null)
                                {
                                    unit1.pointer.lastFiredTimer = timer;
                                    attack2(unit1, i, unit2);
                                }
                            }
                        }
                        if (distance < unit2.fireDistance) {
                            //team2 tank attack
                            if (unit2.lastFiredTimer == 0 || timer > unit2.lastFiredTimer + unit2.fireCD) {
                                logger.info(generateOutputString(timer, j, i, unit2,  unit1));
                                if (unit2.pointer != null)
                                {
                                    unit2.pointer.lastFiredTimer = timer;
                                    attack2(unit2, j, unit1);
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    //画队伍
    void drawBullets()
    {
        for (int i = 0; i < teamArrayList.size(); i++)
        {
            Team team = teamArrayList.get(i);
            Iterator<Bullet> iterator1 = team.allBullets.iterator();
            while (iterator1.hasNext())
            {
                Bullet bullet = iterator1.next();

                /**
                 * 这里实际需要计算子弹每次移动后的位置
                 */

                bullet.t += 0.1;
                //horizontally moved distance
                /*
                double distance = Bullet.v0 * bullet.t * bullet.cos;
                double height = Bullet.v0 * bullet.t * bullet.sin - 1f / 2 * Bullet.g * bullet.t * bullet.t;

                distance
                bullet.setY(200-(int) height);

                */
                //显示子弹
                bufferedImageGraphics.setColor(teamArrayList.get(i).color);
                bufferedImageGraphics.fillRect(
                        (int) (bullet.positionSource.x * cellSize + bullet.currentMovedSteps * bullet.xMoveStep - 4 * Math.random()),
                        (int) (bullet.positionSource.y * cellSize + bullet.currentMovedSteps * bullet.yMoveStep - 4 * Math.random()),
                        10,
                        10
                );
                bullet.pointer.move();
            }

            Iterator<Bullet> iterator = team.allBullets.iterator();
            while (iterator.hasNext())
            {
                Bullet bullet = iterator.next();

                //这里是有问题的，子弹消失是击中目标才会消失
                if (bullet.currentMovedSteps >= bullet.needMoveTotalSteps)
                {
                    bullet.target.pointer.bloodCurrent -= bullet.damage;
                    iterator.remove();
                }
            }
        }


    }

    /**
     * 绘制队伍中陆地单位/空中单位
     */
    void drawLandAndAirUnitsOfTeam()
    {
        drawMainBuilding();

        //选中菜单的建筑物，但是未点击合适的建筑位置，鼠标移动中
        //这个临时的建筑物选中时，鼠标移动会跟着动，直到鼠标移动到一个合适的位置安装建筑
        Unit buildingTemp = buildingSelected;
        if (buildingTemp != null && buildingSelected.unitType < 200000)
        {
            bufferedImageGraphics.drawImage(buildingTemp.displayOnMapImage,
                    buildingTemp.position.x * cellSize,
                    (int) ((buildingTemp.position.y - 1 )* cellSize * heightRate),
                    buildingTemp.width * cellSize,
                    buildingTemp.height * cellSize,
                    this);

            bufferedImageGraphics.setColor(buildingTempColor);
            bufferedImageGraphics.fillArc(buildingTemp.position.x * cellSize,
                    (int) ((buildingTemp.position.y - 1 )* cellSize * heightRate),
                    20,
                    20,
                    0,
                    360);
        }

        for (int i = 0; i < teamArrayList.size(); i++) {
            Team team = teamArrayList.get(i);

            Set<Map.Entry<Integer, ArrayList<Building>>> entries = team.buildingsMap.entrySet();
            for (Map.Entry<Integer, ArrayList<Building>> next : entries)
            {
                ArrayList<Building> buildingArrayList = next.getValue();
                drawBuildingList(buildingArrayList, i);
            }

            Set<Map.Entry<Integer, ArrayList<LandUnit>>> entries2 = team.landUnitsMap.entrySet();
            for (Map.Entry<Integer, ArrayList<LandUnit>> next : entries2)
            {
                ArrayList<LandUnit> buildingArrayList = next.getValue();
                drawLandUnitList(buildingArrayList, i);
            }


            Set<Map.Entry<Integer, ArrayList<AirUnit>>> entries3 = team.airUnitsMap.entrySet();
            for (Map.Entry<Integer, ArrayList<AirUnit>> next : entries3)
            {
                ArrayList<AirUnit> buildingArrayList = next.getValue();
                drawAirUnitList(buildingArrayList, i);
            }
        }
    }

    /**
     * 绘制队伍中船
     */
    void drawSeaUnitsOfTeam()
    {
        for (int i = 0; i < teamArrayList.size(); i++) {
            Team team = teamArrayList.get(i);

            Set<Map.Entry<Integer, ArrayList<SeaUnit>>> entries4 = team.seaUnitsMap.entrySet();
            for (Map.Entry<Integer, ArrayList<SeaUnit>> next : entries4)
            {
                ArrayList<SeaUnit> buildingArrayList = next.getValue();
                drawSeaUnitList(buildingArrayList, i);
            }
        }
    }

    void drawBuildingList(ArrayList<Building> buildings, int teamID)
    {
        for (Building building : buildings) {
            bufferedImageGraphics.drawImage(building.displayOnMapImage,
                    building.getPosition().x * cellSize,
                    (int) ((building.getPosition().y - 1 )* cellSize * heightRate),
                    building.width * cellSize,
                    building.height  * cellSize,
                    this
            );
            bufferedImageGraphics.setColor(building.selectedStatus);
            bufferedImageGraphics.fillArc(
                    building.getPosition().x * cellSize,
                    (int) ((building.getPosition().y - 1 )* cellSize * heightRate),
                    20,
                    20,
                    0,
                    360);
            bufferedImageGraphics.setColor(teamArrayList.get(teamID).color);
            bufferedImageGraphics.drawArc(
                    building.getPosition().x * cellSize - building.fireDistance * cellSize / 2,
                    (int) ((building.getPosition().y - 1 - (double) building.fireDistance / 2) * cellSize * heightRate),
                    building.fireDistance * cellSize,
                    building.fireDistance * cellSize,
                    0,
                    360);
        }
    }

    void drawLandUnitList(ArrayList<LandUnit> buildings, int teamID)
    {
        Iterator<LandUnit> iterator = buildings.iterator();
        while (iterator.hasNext())
        {
            LandUnit next = iterator.next();
            if (next.bloodCurrent <= 0)
            {
                iterator.remove();
            }
        }

        for (int i = 0; i < buildings.size(); i++)
        {
            //logger.info("drawTankList i: " + i);
            LandUnit building = buildings.get(i);
            Coord position = building.getPosition();

            int moveDirection = building.computeMoveDirection();
            //绘制坦克
            Coord coord = new Coord(position.x * cellSize, position.y * cellSize);
            bufferedImageGraphics.drawImage(building.imagePath[0][moveDirection - 1],
                    coord.x,
                    (int) (coord.y * heightRate),
                    building.width * cellSize,
                    building.height  * cellSize,
                    this
            );

            //这里绘制坦克是否被选中
            bufferedImageGraphics.setColor(building.selectedStatus);
            bufferedImageGraphics.fillArc(
                    coord.x,
                    (int) (coord.y * heightRate),
                    20,
                    20,
                    0,
                    360);
            int fireDistance = building.fireDistance;

            //这里画的是攻击范围
            bufferedImageGraphics.setColor(teamArrayList.get(teamID).color);
            bufferedImageGraphics.drawArc(
                    coord.x - fireDistance * cellSize / 2,
                    (int) ((coord.y - fireDistance * cellSize / 2)*heightRate),
                    fireDistance * cellSize,
                    fireDistance * cellSize,
                    0,
                    360);
            bufferedImageGraphics.drawString(""+building.bloodCurrent+"/"+building.bloodTotal,
                    coord.x,
                    (int) (coord.y*heightRate));

            //自己队伍
            if (teamID == selfTeamID) {
                //坦克沿着原来计划移动的路径移动
                building.pointer.move();
            }
        }
    }

    void drawTankList(ArrayList<Building> buildings, int teamID)
    {
        for (int i = 0; i < buildings.size(); i++)
        {
            //logger.info("drawTankList i: " + i);
            Building building = buildings.get(i);
            Coord position = building.getPosition();

            int moveDirection = building.computeMoveDirection();
            //绘制坦克
            Coord coord = new Coord(position.x * cellSize, position.y * cellSize);
            bufferedImageGraphics.drawImage(building.imagePath[0][moveDirection - 1],
                    coord.x,
                    coord.y,
                    building.width * cellSize,
                    building.height  * cellSize,
                    this
            );

            //这里绘制坦克是否被选中
            bufferedImageGraphics.setColor(building.selectedStatus);
            bufferedImageGraphics.fillArc(
                    coord.x,
                    coord.y,
                    20,
                    20,
                    0,
                    360);
            int fireDistance = building.fireDistance;

            //这里画的是攻击范围
            bufferedImageGraphics.setColor(teamArrayList.get(teamID).color);
            bufferedImageGraphics.drawArc(
                    coord.x - fireDistance * cellSize / 2,
                    coord.y - fireDistance * cellSize / 2,
                    fireDistance * cellSize,
                    fireDistance * cellSize,
                    0,
                    360);

            //自己队伍
            if (teamID == selfTeamID) {
                //坦克沿着原来计划移动的路径移动
                building.pointer.move();
            }
        }
    }

    /**
     * 绘制海军单位
     * @param buildings
     * @param teamID
     */
    void drawSeaUnitList(ArrayList<SeaUnit> buildings, int teamID)
    {
        for (int i = 0; i < buildings.size(); i++)
        {
            //logger.info("drawTankList i: " + i);
            SeaUnit building = buildings.get(i);
            Coord position = building.getPosition();

            int moveDirection = building.computeMoveDirection();
            Coord coord = new Coord(position.x * cellSize, position.y * cellSize);

            bufferedImageGraphics.drawImage(building.imagePath[0][moveDirection - 1],
                    coord.x,
                    (int) (coord.y*heightRate),
                    building.width * cellSize,
                    building.height  * cellSize,
                    this
            );

            //这里绘制坦克是否被选中
            bufferedImageGraphics.setColor(building.selectedStatus);
            bufferedImageGraphics.fillArc(
                    coord.x,
                    (int) (coord.y*heightRate),
                    20,
                    20,
                    0,
                    360);

            int fireDistance = building.fireDistance;

            //这里画的是攻击范围
            bufferedImageGraphics.setColor(teamArrayList.get(teamID).color);
            bufferedImageGraphics.drawArc(
                    coord.x - fireDistance * cellSize / 2,
                    (int) ((coord.y - fireDistance * cellSize / 2)*heightRate),
                    fireDistance * cellSize,
                    fireDistance * cellSize,
                    0,
                    360);

            //非自己队伍
            if (teamID != selfTeamID)
            {
            }

            //自己队伍
            if (teamID == selfTeamID) {
                //坦克沿着原来计划移动的路径移动
                building.pointer.move();
            }
        }
    }

    /**
     * 画空中单位
     * @param buildings
     * @param teamID
     */
    void drawAirUnitList(ArrayList<AirUnit> buildings, int teamID)
    {
        //logger.info("timer: " + timer);
        for (int i = 0; i < buildings.size(); i++) {
            AirUnit unit = buildings.get(i);
            bufferedImageGraphics.drawImage(unit.imagePath[0][unit.moveDerection - 1],
                    unit.position.x * cellSize,
                    (int) (unit.position.y * cellSize*heightRate),
                    unit.width * cellSize,
                    unit.height * cellSize,
                    this);

            bufferedImageGraphics.setColor(unit.selectedStatus);
            bufferedImageGraphics.fillRect(unit.position.x * cellSize,
                    (int) (unit.position.y * cellSize*heightRate),
                    cellSize / 2,
                    cellSize / 2);

            //这里在飞行兵下边画的一个椭圆的影子
            bufferedImageGraphics.fillArc(unit.position.x * cellSize,
                    (int) (unit.position.y * cellSize*heightRate + 100),
                    cellSize / 2,
                    cellSize / 4,
                    0,
                    360);

            //自己的队伍成员才移动
            if (teamID == selfTeamID)
            {
                unit.pointer.move();
                //ArrayList<AirUnit> buildings1 = teamArrayList.get(teamID).airUnitsMap.get(key);
                //buildings1.get(i).move();
                //teamArrayList.get(selfTeamID).airUnitsMap.put(key, buildings1);
            }

        }
    }

    /**
     * 绘制测试的单位，主界面开始自动移动的单位：
     * 1、测试寻路算法是否生效
     * 2、测试坦克、飞行兵移动时移动的图片会不会根据移动的方向切换
     */
    void drawTestUnits()
    {
        for (int count = 0; count < tank.length; count++)
        {
            if (tank[count] == null)
            {
                continue;
            }

            int moveDirection = tank[count].computeMoveDirection();

            tank[count].move();

            Coord coord = tank[count].cellMove();
            bufferedImageGraphics.drawImage(tank[count].imagePath[0][moveDirection - 1],
                    coord.x,
                    coord.y - cellSize, //往上挪一个格子，坦克轨迹看起来容易些
                    null);

            bufferedImageGraphics.setColor(tank[count].selectedStatus);
            bufferedImageGraphics.fillArc(coord.x,
                    coord.y - cellSize,
                    20,
                    20,
                    0,
                    360);
            //logger.info("" + tank[count].name + " " + count + " move, x1: " + x1 + " , y1: " + y1 + ", pathCurrentIndex");
        }


        if (airUnit == null)
        {
            return;
        }

        bufferedImageGraphics.drawImage(airUnit.imagePath[0][airUnit.moveDerection - 1],
                airUnit.position.x * cellSize,
                airUnit.position.y * cellSize,
                null);

        airUnit.move();

        bufferedImageGraphics.setColor(airUnit.selectedStatus);
        bufferedImageGraphics.fillArc(airUnit.position.x * cellSize,
                airUnit.position.y * cellSize,
                20,
                20,
                0,
                360);

        if (pointList.size() == 2)
        {
            Coord start = pointList.get(0);
            Coord end = pointList.get(1);

            //这里是画的起始地点到目的地
            bufferedImageGraphics.setColor(Global.PLAN_MOVE_TO);
            bufferedImageGraphics.drawLine(start.x * cellSize,
                    start.y * cellSize,
                    end.x * cellSize,
                    end.y * cellSize);

        }
    }

    /**
     * 绘制右侧上方的菜单，很多按钮那种
     * @param graphics
     */
    void drawPanelMenu(Graphics graphics)
    {
        for (int i = 0; i < menuList.size(); i++) {
            Building building = menuList.get(i);
            drawButton2(graphics,
                    building.position.x * cellSize,
                    building.position.y * cellSize,
                    building.width * cellSize,
                    building.height * cellSize,
                    building.selectedStatus,
                    building.name);
        }

    }

    //画右边的控制面板
    void drawPanel(Graphics graphics)
    {
        //地图绘制完成后，把地图的信息绘制到雷达图层上
        radioBufferedImageGraphics.drawImage(bufferedImage,
                0,
                0,
                radio.width,
                radio.height,
                this);

        /*右手边菜单*/
        graphics.setColor(new Color(255,255,255));
        graphics.fillRect(0,
                0,
                monitorWidth * 20 / 100,
                monitorHeight);


        //绘制右侧的雷达
        graphics.drawImage(radioBufferedImage,
                panelStartGap,
                radio.position.y,
                monitorWidth * 20 / 100 - 100,
                radio.height,
                this);

        drawPanelMenu(graphics);

        if (currentDisplayedMenu == BUILD_MENU.MAX)
        {
            return;
        }

        displaySubMenu();

        graphics.drawImage(buildItemsBufferedImage,
                panelStartGap,
                buildingItems.position.y,
                buildingItems.totalWidth,
                buildingItems.totalHeight,
                this);
    }

    /**
     * //因为有的子菜单比较多，比如原来显示3排，新菜单只有2排，这样只显示新菜单，那原来的第3排就不会清除，所以需要重置下
     */
    private void resetSubMenu()
    {
        buildItemsBufferedImageGraphics.fillRect(0,0,buildingItems.totalWidth,
                buildingItems.totalHeight);
    }

    /**
     * 绘制右下边的子菜单，就是主建筑、防御、兵营、战车工厂下边的子菜单
     */
    private void displaySubMenu()
    {
        resetSubMenu();
        int buildingCount = buildingsForPanel.get(currentDisplayedMenu).size();

        if (buildingCount == 0)
        {
            return;
        }

        for (int i = 0; i < Global.SUB_MENU_LINE_MAX; i++) // 7 行
        {
            for (int j = 0; j < Global.SUB_MENU_COLUMN_MAX; j++) // 每行2个
            {
                //这里从当前队伍的建筑列表里开始显示可以安装的详细子菜单列表
                if (i * 2 + j >= buildingsForPanel.get(currentDisplayedMenu).size())
                {
                    break;
                }

                Unit building = buildingsForPanel.get(currentDisplayedMenu).get(i * 2 + j);
                buildItemsBufferedImageGraphics.drawImage(building.displayOnMenuImage,
                        130 * j,
                        130 * i,
                        buildingItems.width,
                        buildingItems.height,
                        this);

                if (i * 2 + j > buildingsForPanel.get(currentDisplayedMenu).size())
                {
                    logger.info("你点击的菜单位置太大： " + (i * 2 + j) + ", array size: " +buildingsForPanel.get(currentDisplayedMenu).size());
                    return;
                }
                //把绘制的位置记录下来，因为后续鼠标点击对应位置的时候才知道是哪个菜单
                buildingsForPanel.get(currentDisplayedMenu).get(i * 2 + j).position = new Coord(130 * j, 130 * i);

                buildItemsBufferedImageGraphics.setColor(building.selectedStatus);
                buildItemsBufferedImageGraphics.drawRect(130 * j,
                        130 * i,
                        buildingItems.width - 5,
                        buildingItems.height - 5);
/*
                buildItemsBufferedImageGraphics.drawString("" + 130 * j + " " + 130 * i,
                        130 * j,
                        130 * i);*/
            }
        }
    }

    //这里绘制的一般是占多个格子的地图图片，比如悬崖
    void drawCell24()
    {
        // 画高度高于2的地图
        for (int y = 0; y < data.length; y++)
        {
            int[] line = data[y];
            for (int x = 0; x < line.length; x++)
            {
                int cell = line[x];
                if (cell < 0)
                {
                    continue;
                }
                int type = cell / 100;
                type %= 10;
                int subtype = cell / 10 % 10;
                //这里代码不太好，双层for循环里有IO
                ModelSize modelSize = modelSizes[type][subtype];
                if (type == 2) {
                    if (subtype >= 1 && subtype <= 5)
                    {
                        drawWidthModelSize(mapImages[type][subtype], x, y, modelSize);
                    }
                }
                if (type == 4) {
                    if (subtype >= 1 && subtype <= 6)
                    {
                        drawWidthModelSize(mapImages[type][subtype], x, y, modelSize);
                    }
                }
            }
            //System.out.println();
        }
        //System.out.println();
    }

    //这里绘制的是桥
    void drawCellBridge()
    {
        for (int i = 0; i < bridgeArrayList.size(); i++) {
            Coord coord = bridgeArrayList.get(i);
            int y = coord.y;
            int x = coord.x;
            int cell = data[coord.y][coord.x];
            if (cell < 0)
            {
                continue;
            }
            int type = cell / 100;
            type %= 10;
            int subtype = cell / 10 % 10;
            //这里代码不太好，双层for循环里有IO
            ModelSize modelSize = modelSizes[type][subtype];
            if (type == 5) {
                if (subtype >= 1 && subtype <= 6)
                {
                    drawWidthModelSize(mapImages[type][subtype], x, y, modelSize);
                }
            }
        }
    }

    /**
     * 这里绘制那些有完成时间的任务，比如点击了某个菜单，这个当前没怎么生效，只有动画效果
     */
    void drawProcesses()
    {
        if (buildProcessArrayList.isEmpty())
        {
            return;
        }

        synchronized (lock) {
            Iterator<BuildProcess> iterator = buildProcessArrayList.iterator();
            while (iterator.hasNext())
            {
                BuildProcess buildProcess = iterator.next();
                long count = timer - buildProcess.timerStart;
                if (count > buildProcess.stepTotal) {
                    iterator.remove(); // 安全删除
                }

                teamArrayList.get(selfTeamID).goldUse();

                menuList.get(goldBuildingID).name = "Gold=" + teamArrayList.get(selfTeamID).getGold();

                bufferedImageGraphics.setColor(buildProcess.colorDisplay);

                bufferedImageGraphics.fillArc(buildProcess.x,
                        buildProcess.y,
                        buildProcess.width,
                        buildProcess.height,
                        0,
                        (int) ((float)count / (buildProcess.stepTotal) * 360));
            }
        }
    }

    /**
     * 窗口上下左右移动
     */
    void displayedWindowMove()
    {
        if (mouseEvent1 == null)
        {
            return;
        }
        MouseEvent e = mouseEvent1;
        int moveStep = 80;
        if (e.getX() < 40)
        {
            if (xStart - moveStep >= 0)
            {
                xStart -= moveStep;
            }

            return;
        }


        if (e.getY() < 60)
        {
            if (yStart - moveStep >= 0)
            {
                yStart -= moveStep;
            }

            return;
        }

        if (e.getY() > displayWindowHeight - 65)
        {
            if (yStart + displayWindowHeight + moveStep <= mapHeight && e.getX() < 300)
            {
                yStart += moveStep;
            }

            return;
        }

        if (e.getX() > displayWindowWidth  - 55 && e.getY() < 300)
        {

            if (xStart  + displayWindowWidth + 10 < mapWidth)
            {
                xStart += moveStep;
            }
            return;
        }

    }

    /**
     * 服务器端和客户端绘制前端界面：地图、攻击检测、队伍、子弹、被选中的单位等等
     */
    void updateWindow()
    {
        //logger.info(String.valueOf(mouseEvent1));
        setTitle(title + status);

        displayedWindowMove();

        drawCell11();

        drawCell24();

        //因为有时候有桥，所以需要先绘制海里单位
        drawSeaUnitsOfTeam();

        //绘制桥
        drawCellBridge();

        //drawCellHeight();

        //攻击检测，游戏里只有服务器才会进行攻击检测
        if (MainThread.thisDeviceType == DeviceType.SERVER)
        {
            attackCheck2(); //上一步有移动后，就有可能会进入单位的攻击范围，所以进行攻击检测
        }

        //原来的地图没有桥梁：先画地图的格子，再画超过1*1大小的格子，再画坦克/兵/船
        //地图上有了桥梁后：需要先画地图的格子，再画超过1*1大小的格子，画船，画桥，画坦克，画空中单位
        //画完坦克、兵种、空中单位后，会执行move动作
        //绘制陆地单位/空中单位
        drawLandAndAirUnitsOfTeam();

        drawBullets();

        drawSelectedBuildings();

        drawTestUnits();

        drawMouseSelectRectangle();

        drawProcesses();

        //因为游戏的地图很大，在玩家的屏幕上只会显示地图的一小部分区域
        //把全地图的部分绘制到窗口缓冲区
        bufferedDisplayWindowImageGraphics.drawImage(bufferedImage,
                0,0,displayWindowWidth, monitorHeight,
                 xStart, yStart,xStart + displayWindowWidth,yStart + displayWindowHeight,
                this);

        //在大地图上显示小地图范围的长方形
        bufferedImageGraphics.drawRect(xStart, yStart, displayWindowWidth, displayWindowHeight);
        bufferedImageGraphics.drawRect(xStart+2, yStart+2, displayWindowWidth - 4, displayWindowHeight-4);

        //右侧的控制面板
        drawPanel(bufferedControlPanelWindowImageGraphics);

        //把窗口显示出来
        graphics.drawImage(bufferedDisplayWindowImage,
                0,
                0,
                monitorWidth * 80 / 100,
                monitorHeight,
                this);

        graphics.drawImage(bufferedControlPanelWindowImage,
                monitorWidth * 80 / 100,
                0,
                monitorWidth * 20 / 100,
                monitorHeight,
                this);

        //logger.info("timer: " + timer);
        timer++;
    }

    /**
     * 主窗口刷新函数很重要
     * 这里边函数调用的先后顺序也很重要，基本上是先绘制最下面一层，然后绘制至下而上的第二层，等等
     * //完成服务器或者客户端建筑、兵种、坦克的打包发送给对端
     */
    void runClient()
    {
        //logger.info("runClient " + timer + ", Building.currentID: " + Building.currentID);
        //这是把本地的数据发送给远端服务器，注意这两行代码不能放到上边的if里，不然就变成收到数据才更新画面，因为自己也可以建造生产等等

        //if (timer % 10 == 0 || needPackageAndSendData)
        {
            StateObject stateObject2 = packageTeamData(MainThread.thisDeviceType);
            TcpClient.send(stateObject2);
        }

        ArrayList<StateObject> stateObjects = TcpClient.receive();

        //这下边代码有一个比较大的问题，就是会不断的new对象，这样对系统性能估计有相当的影响
        if (stateObjects != null) {
            clientGetTeamDataFromPackage(stateObjects);
        }

        updateWindow();
    }


    static class SerializedVector implements Serializable
    {
        private final ArrayList<StateObject> stateObjects2 = new ArrayList<>();

        public void add(StateObject stateObject)
        {
            stateObjects2.add(stateObject);
        }
    }

    /**
     * 完成服务器或者客户端建筑、兵种、坦克的打包发送给对端
     */
    void runServer()
    {
        //logger.info("runServer " + timer);
        //logger.info("runServer " + timer + ", Building.currentID: " + Building.currentID);

        // 1、收到客户端的单位位置
        // 2、根据位置距离计算是否攻击
        // 3、绘制子弹
        // 4、把计算后坦克的攻击时间戳和子弹数据发给客户端
        Vector<StateObject> stateObjectsReceive = TcpServer.receive();
        if (stateObjectsReceive != null) {
            serverGetTeamDataFromPackage(stateObjectsReceive);
        }

        //这是把本地的数据发送给远端客户端，注意这两行代码不能放到上边的if里，不然就变成收到数据才更新画面，因为自己也可以建造生产等等
        updateWindow();

        //if (timer % 10 == 0 || needPackageAndSendData)
        {
            serializedVector.stateObjects2.clear();
            for (int i = 0; i < 4; i++) {
                StateObject stateObject2 = packageTeamData(i);
                serializedVector.stateObjects2.add(stateObject2);
            }
            TcpServer.send(serializedVector.stateObjects2);

        }
    }

    StateObject packageTeamData(int teamID)
    {
        Team team = teamArrayList.get(teamID);
        StateObject stateObject = new StateObject(teamID);

        //所有建筑打包
        //所有空军单位打包
        team.copyBuildingsToItems(team.buildingsMap, stateObject.buildingsMap);
        team.copySeaUnitsToItems(team.seaUnitsMap, stateObject.seaUnitsMap);
        team.copyAirUnitsToItems(team.airUnitsMap, stateObject.airUnitsMap);
        team.copyLandUnitsToItems(team.landUnitsMap, stateObject.landUnitsMap);

        team.copyBullets(team.allBullets, stateObject.allBullets);

        return stateObject;
    }

    /**
     * 这里要注意，收到对方发来的数据，自己有可能是服务器，有可能是客户端
     * 对方有可能是服务器，有可能是客户端
     * @param stateObjects 从服务器发过来的单位数据，里边也有子弹的数据
     */
    void clientGetTeamDataFromPackage(ArrayList<StateObject> stateObjects)
    {
        for (int i = 0; i < stateObjects.size(); i++) {
            StateObject stateObject = stateObjects.get(i);
            Team team = teamArrayList.get(stateObject.teamID);

            //子弹生成是由服务器端生成的，客户端需要把从服务器端的子弹复制到对应的队伍
            //客户端生成子弹的话，子弹的攻击力可能不一致
            //因为有些单位攻击力不是定值，是范围比如10到20
            team.getBullets(stateObject.allBullets, team.allBullets);

            //客户端只会更新其他客户端的数据，自己客户端的数据不需要更新
            //如果后续开发带有血量减少功能这类，是需要更新的
            if (stateObject.teamID == selfTeamID)
            {
                continue;
            }
            //先把原来的清空，不然会非常的多
            //客户端接收从服务器端传输过来的信息，那么接收的信息是敌人（注意）
            team.buildingsMap.clear();
            Team.copyItemMapToBuildingMap(stateObject.buildingsMap, stateObject.teamID, team, team.buildingsMap);

            team.airUnitsMap.clear();
            team.copyItemMapToAirUnitMap2(stateObject.airUnitsMap, team);

            //team.landUnitsMap.clear();
            team.updateLandUnitMapFromStateObject(stateObject.landUnitsMap,  team.landUnitsMap);

            team.seaUnitsMap.clear();
            team.copyItemMapToSeaUnitMap2(stateObject.seaUnitsMap,  team);
        }
    }

    /**
     * 服务端从客户端更新数据，这里非常重要，尤其是这个：updateMapFromStateObject
     * @param stateObjects
     */
    void serverGetTeamDataFromPackage(Vector<StateObject> stateObjects)
    {
        //注意，服务端收到的数据，队伍里不会再有teamID为0(服务端ID)，因为只会启一台服务器
        for (int i = 0; i < stateObjects.size(); i++)
        {
            StateObject fromTeam = stateObjects.get(i);
            if (fromTeam.teamID == selfTeamID)
            {
                continue;
            }

            Team toTeam = teamArrayList.get(fromTeam.teamID);

            Team.updateBuildingMapFromStateObject(fromTeam.buildingsMap,    toTeam.buildingsMap);
            toTeam.updateAirUnitMapFromStateObject(fromTeam.airUnitsMap,    toTeam.airUnitsMap);
            toTeam.updateLandUnitMapFromStateObject(fromTeam.landUnitsMap,  toTeam.landUnitsMap);
            toTeam.updateSeaUnitMapFromStateObject(fromTeam.seaUnitsMap,    toTeam.seaUnitsMap);

            teamArrayList.set(fromTeam.teamID, toTeam);
        }
    }
}