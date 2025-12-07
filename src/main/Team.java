package main;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

// 队伍：小黑、小蓝、小绿，队伍也包含中立队伍
// 队伍，比如开始自己就是一个队伍，队伍有很多建筑，
// 有很多兵种，
// 有很多坦克，
// 有很多防御建筑，
// 有很多船，
// 有很多飞机，
public class Team {
    static Logger logger = MyLogger.logger;

    //队伍的编号，游戏规划一共有8个队伍，当前只有2个队伍
    //id是非常重要的参数，唯一标识队伍
    public int id;

    public Coord teamInitialHomePosition;

    //队伍颜色
    public Color color;

    //而同一种建筑物在地图上可以是一个，也可以是多个，所以这个结构体是不妥的，要分开。
    //同一种类型的单位可能有多个比如多个重工，多个火箭飞行兵等等，所有建了一个Map，里边存放的是ArrayList
    //前面这个id是类型
    ConcurrentHashMap<Integer, ArrayList<Building>> buildingsMap;
    ConcurrentHashMap<Integer, ArrayList<SeaUnit>> seaUnitsMap;
    ConcurrentHashMap<Integer, ArrayList<LandUnit>> landUnitsMap;
    ConcurrentHashMap<Integer, ArrayList<AirUnit>> airUnitsMap;

    /**
     * 整个队伍所有子弹
     *    生成子弹场景：
     *    1、不同队伍单位在双方的攻击范围内
     *    2、攻击CD结束
     *    3、子弹初始位置是发出者位置附近
     *    4、子弹移动方向为发出者与被攻击者直线从发出者到被攻击者方向
     *
     *    子弹移动场景：
     *    1、随着画面更新移动
     *    2、发出后，子弹不受控制
     *
     *    子弹消失场景：
     *    1、击中目标
     *    2、超过射程（移动最大次数）
     *
     *    集合比较好
     */

    public Vector<Bullet> allBullets;

    static ArrayList<Unit> initedBuildings;

    int gold;

    /**
     * 主建筑类
     *
     */
    //主基地
    public Building mainBuilding;

    //发电厂
    public Building powerplantBuilding;

    //矿厂
    public Building refineryBuilding;

    //兵工厂
    public Building armoryBuilding;

    //重工
    public Building warFactoryBuilding;
    
    //船厂
    public Building dockyardBuilding;

    //空指部
    public Building airForceHeadquarters;

    //盟军作战实验室
    public Building battleLab;


    /**
     * 防御类工事
     *
     */
    //围墙
    public Building wallBuilding;

    //哨戒炮
    public Building sentinelCannonBuilding;

    //防空炮Anti-Air Gun
    public Building antiAirGunBuilding;

    /**
     * 兵种类
     */

    //动员兵
    public Building conscriptBuilding;

    //工程师
    public Building engineerBuilding;

    //警犬
    public Building policedogBuilding;

    //火箭飞行兵
    public AirUnit Rocketeer;


    /**
     * 重工类坦克
     *
     */

    //矿车
    public LandUnit minecarBuilding;

    //灰熊坦克（Grizzly I tank）
    public LandUnit grizzlyItankBuilding;

    //防空车
    public LandUnit antiAirCarBuilding;

    //直升机
    public AirUnit helicopterBuilding;


    /**
     * 船厂类
     *
     */

    //驱逐舰
    public SeaUnit frigateBuilding; //驱逐舰
    public SeaUnit landingPlatformDockShip; //两栖船坞运输舰LANDING_PLATFORM_DOCK


    /**
     * 初始化
     * @param id 队伍的编号
     */
    Team(int id)
    {
        Coord position = new Coord(0, 0);
        this.id = id;
        teamInitialHomePosition = Global.playerStartCoord[id];
        color = Global.colors[id];
        airUnitsMap = new ConcurrentHashMap<>();
        buildingsMap = new ConcurrentHashMap<>();
        landUnitsMap = new ConcurrentHashMap<>();
        seaUnitsMap = new ConcurrentHashMap<>();


        allBullets = new Vector<>();

        gold = 10000;

        initedBuildings = new ArrayList<>();
        
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("盟军发电厂");
        String path = "1-主建筑\\1-发电厂\\";
        powerplantBuilding = new Building("盟军发电厂", id,
                position,
                UNIT_TYPE.POWER_PLANT,
                2,
                2,
                500,
                color,
                arrayList,
                path);
        initedBuildings.add(powerplantBuilding);

        path = "1-主建筑\\2-矿厂\\";
        refineryBuilding = new Building("盟军矿厂",id,
                position,
                UNIT_TYPE.REFINERY,
                2,
                2,
                2000,
                color,
                arrayList,
                path);
        initedBuildings.add(refineryBuilding);

        path = "1-主建筑\\3-兵工厂\\";
        armoryBuilding = new Building("盟军兵工厂",id,
                position,
                UNIT_TYPE.ARMORY,
                2,
                2,
                500,
                color,
                arrayList,
                path);
        initedBuildings.add(armoryBuilding);

        path = "1-主建筑\\4-盟军重工\\";
        warFactoryBuilding = new Building("盟军重工",id,
                position,
                UNIT_TYPE.WAR_FACTORY,
                2,
                2,
                2000,
                color,
                arrayList,
                path);
        initedBuildings.add(warFactoryBuilding);


        path = "1-主建筑\\9-盟军船厂\\";
        dockyardBuilding = new Building("盟军船厂",id,
                position,
                UNIT_TYPE.DOCKYARD,
                3,
                3,
                2000,
                color,
                arrayList,
                path);
        initedBuildings.add(dockyardBuilding);

        path = "1-主建筑\\5-盟军空指部\\";
        airForceHeadquarters = new Building("盟军空指部",id,
                position,
                UNIT_TYPE.AIR_FORCE_HEADQUARTERS,
                2,
                2,
                2000,
                color,
                arrayList,
                path);
        initedBuildings.add(airForceHeadquarters);

        path = "1-主建筑\\7-盟军作战实验室\\";
        battleLab = new Building("盟军作战实验室",id,
                position,
                UNIT_TYPE.BATTLE_LAB,
                2,
                2,
                2000,
                color,
                arrayList,
                path);
        initedBuildings.add(battleLab);


        path = "3-兵营兵种\\1-动员兵\\";
        conscriptBuilding = new Building("动员兵",id,
                position,
                UNIT_TYPE.CONSCRIPT,
                1,
                1,
                100,
                color,
                arrayList,
                path);
        initedBuildings.add(conscriptBuilding);

        path = "3-兵营兵种\\2-工程师\\";
        engineerBuilding = new Building("工程师",id,
                position,
                UNIT_TYPE.ENGINEER,
                1,
                1,
                500,
                color,
                arrayList,
                path);
        initedBuildings.add(engineerBuilding);

        path = "3-兵营兵种\\3-警犬\\";
        policedogBuilding = new Building("警犬",id,
                position,
                UNIT_TYPE.POLICE_DOG,
                1,
                1,
                200,
                color,
                arrayList,
                path);
        initedBuildings.add(policedogBuilding);

        path = "3-兵营兵种\\4-火箭飞行兵\\";
        Rocketeer = new AirUnit("火箭飞行兵",id,
                position,
                color,
                UNIT_TYPE.ROCKETEER,
                path);
        initedBuildings.add(Rocketeer);

        path = "2-防御建筑\\1-盟军围墙\\";
        wallBuilding = new Building("盟军围墙",id,
                position,
                UNIT_TYPE.WALL_ALLIED_FORCES,
                2,
                2,
                100,
                color,
                arrayList,
                path);
        initedBuildings.add(wallBuilding);


        path = "2-防御建筑\\2-盟军机枪碉堡\\";
        sentinelCannonBuilding = new Building("盟军机枪碉堡",id,
                position,
                UNIT_TYPE.SENTINEL_CANNON,
                2,
                2,
                200,
                color,
                arrayList,
                path);
        initedBuildings.add(sentinelCannonBuilding);


        path = "2-防御建筑\\3-盟军防空炮\\";
        antiAirGunBuilding = new Building("盟军防空炮",id,
                position,
                UNIT_TYPE.ANTI_AIR_GUN,
                2,
                2,
                500,
                color,
                arrayList,
                path);
        initedBuildings.add(antiAirGunBuilding);


        path = "4-重工兵种\\1-武装矿车\\";
        minecarBuilding = new LandUnit("武装矿车",id,
                position,
                UNIT_TYPE.MINE_CAR,
                2,
                2,
                1400,
                color,
                path);
        initedBuildings.add(minecarBuilding);


        path = "4-重工兵种\\2-灰熊坦克\\";
        grizzlyItankBuilding = new LandUnit("灰熊坦克",id,
                position,
                UNIT_TYPE.GRIZZLYI_TANK,
                2,
                2,
                700,
                color,
                path);
        initedBuildings.add(grizzlyItankBuilding);


        path = "4-重工兵种\\3-防空车\\";
        antiAirCarBuilding = new LandUnit("盟军防空车",id,
                position,
                UNIT_TYPE.ANTI_AIR_CAR,
                2,
                2,
                600,
                color,
                path);
        initedBuildings.add(antiAirCarBuilding);


        path = "4-重工兵种\\4-直升机\\";
        helicopterBuilding = new AirUnit("直升机",id,
                position,
                color,
                UNIT_TYPE.HELICOPTER,
                path);
        initedBuildings.add(helicopterBuilding);

        path = "5-船厂兵种\\2-盟军-驱逐舰\\";
        frigateBuilding = new SeaUnit("驱逐舰",id,
                position,
                color,
                UNIT_TYPE.FRIGATE,
                path);
        initedBuildings.add(frigateBuilding);

        path = "5-船厂兵种\\1-盟军-两栖运输船\\";
        landingPlatformDockShip = new SeaUnit("两栖运输船",id,
                position,
                color,
                UNIT_TYPE.LANDING_PLATFORM_DOCK,
                path);
        initedBuildings.add(landingPlatformDockShip);

        initBuilding(teamInitialHomePosition);
    }


    /**
     * 假设现在需要在鼠标位置安装建筑物
     * 这个函数是判定鼠标在地图上附近矩形范围内的地面高度是否一致
     * 在地图数组的最后一位（请参考：MapOblique.data[][]）是高度。。。
     * @param building
     * @param mouse
     * @return
     */
    public boolean buildingInSameHeight(Unit building, Coord mouse)
    {
        int width = building.width;
        int height = building.height;
        int x = mouse.x ;
        int y = mouse.y ;
        int oldHeight = MapOblique.heightDataBack[y][x];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int cellHeight = MapOblique.heightDataBack[y + i][x + j];
                if (oldHeight != cellHeight)
                {
                    logger.info("building is not in same height...");
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 当前鼠标的位置，是否距离友方建筑比较近？
     * 一般是安装建筑物之前会判定在友方建筑物附近
     * @param unitType
     * @param mouse
     * @return
     */
    public boolean isCloseToFriendlyBuilding(int unitType, Coord mouse)
    {
        //这是距离乘积倍数，因为安装船厂的范围比普通建筑的范围大
        int times = 1;
        if (unitType == UNIT_TYPE.DOCKYARD)
        {
            times = 2;
        }

        int max_distance = times * Global.MAX_DISTANCE_INSTALL_BUILDING_COLSING_TO_FRIENDLY;
        max_distance /= Global.cellSize;

        //开始的时候还没有建筑添加到buildingsMap，因为只有主基地
        if (Global.getDistance(mouse, mainBuilding) < max_distance)
        {
            return true;
        }

        Set<Map.Entry<Integer, ArrayList<Building>>> entries = buildingsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<Building>> next : entries) {
            ArrayList<Building> buildingArrayList = next.getValue();
            for (int i = 0; i < buildingArrayList.size(); i++) {
                Building building2 = buildingArrayList.get(i);

                //只要有一个距离鼠标比较近就算数
                if (Global.getDistance(mouse, building2) < max_distance) {
                    return true;
                }
            }
        }

        //走到这里表示一个都没有找到距离近的。。。。
        return false;
    }

    public int getGold() {
        return gold;
    }


    public void goldUse()
    {
        gold--;

    }

    //这里边可以添加一些队伍其他类型的单位是否被选中
    Unit unitSelected(Coord mouse)
    {
        Coord mouseDivideCellsize = Global.pixelToMap(mouse);
        LandUnit Tank = landUnitsHasSelected(mouseDivideCellsize);
        if (null != Tank)
        {
            return Tank;
        }

        Unit unit= seaUnitsHasSelected(mouseDivideCellsize);
        if (null != unit)
        {
            return unit;
        }

        AirUnit building2 = airUnitsHasSelected2(mouseDivideCellsize);
        if (null != building2)
        {
            return building2;
        }

        return buildingHasSelected(mouseDivideCellsize);
    }


    //这里是一个队伍多个单位被选中
    void unitsSelected(Coord[] points, ArrayList<Unit> unitArrayList)
    {
        points[0] = Global.pixelToMap(points[0]);
        points[1] = Global.pixelToMap(points[1]);
        landUnitsHasSelected2(points, unitArrayList);

        seaUnitsHasSelected2(points, unitArrayList);

        airUnitsHasSelected3(points, unitArrayList);

        buildingHasSelected2(points, unitArrayList);
    }

    public LandUnit landUnitsHasSelected(Coord mouse)
    {
        //判断坦克被选中
        Set<Map.Entry<Integer, ArrayList<LandUnit>>> entries = landUnitsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<LandUnit>> next : entries) {
            ArrayList<LandUnit> buildingArrayList = next.getValue();
            for (int i = 0; i < buildingArrayList.size(); i++) {
                LandUnit building = buildingArrayList.get(i);
                if (Global.inRectangle2(mouse, building)) {
                    //logger.info("坦克被选中building.name= " + building.name);
                    return building;
                }
            }
        }

        return null;
    }


    public void landUnitsHasSelected2(Coord[] points, ArrayList<Unit> unitArrayList)
    {
        //判断坦克被选中
        Set<Map.Entry<Integer, ArrayList<LandUnit>>> entries = landUnitsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<LandUnit>> next : entries) {
            ArrayList<LandUnit> buildingArrayList = next.getValue();
            for (int i = 0; i < buildingArrayList.size(); i++) {
                LandUnit building = buildingArrayList.get(i);

                if (Global.inRectangle2(points, building)) {
                    unitArrayList.add(building);
                }
            }
        }
    }


    public Unit seaUnitsHasSelected(Coord mouse)
    {
        //判断海军被选中
        Set<Map.Entry<Integer, ArrayList<SeaUnit>>> entries = seaUnitsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<SeaUnit>> next : entries) {
            ArrayList<SeaUnit> buildingArrayList = next.getValue();
            for (int i = 0; i < buildingArrayList.size(); i++) {
                SeaUnit building = buildingArrayList.get(i);
                if (Global.inRectangle2(mouse, building)) {
                    logger.info("海军被选中building.name= " + building.name);
                    return building;
                }
            }
        }

        return null;
    }


    public void seaUnitsHasSelected2(Coord[] points, ArrayList<Unit> unitArrayList)
    {
        //判断海军被选中
        Set<Map.Entry<Integer, ArrayList<SeaUnit>>> entries = seaUnitsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<SeaUnit>> next : entries) {
            ArrayList<SeaUnit> buildingArrayList = next.getValue();
            for (int i = 0; i < buildingArrayList.size(); i++) {
                SeaUnit building = buildingArrayList.get(i);
                if (Global.inRectangle2(points, building)) {
                    unitArrayList.add(building);
                }
            }
        }
    }

    public AirUnit airUnitsHasSelected2(Coord mouse)
    {
        Set<Map.Entry<Integer, ArrayList<AirUnit>>> entries = airUnitsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<AirUnit>> next : entries) {
            ArrayList<AirUnit> buildingArrayList = next.getValue();
            for (int i = 0; i < buildingArrayList.size(); i++) {
                AirUnit building = buildingArrayList.get(i);
                if (Global.inRectangle(mouse, building)) {
                    logger.info("空中单位被选中building.name= " + building.name);
                    return building;
                }
            }
        }

        return null;
    }

    public void airUnitsHasSelected3(Coord[] points,  ArrayList<Unit> unitArrayList)
    {
        Set<Map.Entry<Integer, ArrayList<AirUnit>>> entries = airUnitsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<AirUnit>> next : entries) {
            ArrayList<AirUnit> buildingArrayList = next.getValue();
            for (int i = 0; i < buildingArrayList.size(); i++) {
                AirUnit building = buildingArrayList.get(i);
                if (Global.inRectangle2(points, building)) {
                    unitArrayList.add(building);
                }
            }
        }
    }

    public Building buildingHasSelected(Coord mouse)
    {
        //判断建筑是否被选中
        Set<Map.Entry<Integer, ArrayList<Building>>> entries = buildingsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<Building>> next : entries) {
            ArrayList<Building> buildingArrayList = next.getValue();
            for (int i = 0; i < buildingArrayList.size(); i++) {
                Building building = buildingArrayList.get(i);
                if (Global.inRectangle(mouse, building)) {
                    logger.info("building.name= " + building.name);
                    return building;
                }
            }
        }

        return null;
    }


    public void buildingHasSelected2(Coord[] points, ArrayList<Unit> buildingArrayList2)
    {
        //判断建筑是否被选中
        Set<Map.Entry<Integer, ArrayList<Building>>> entries = buildingsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<Building>> next : entries) {
            ArrayList<Building> buildingArrayList = next.getValue();
            for (int i = 0; i < buildingArrayList.size(); i++) {
                Building building = buildingArrayList.get(i);
                if (Global.inRectangle2(points, building)) {
                    buildingArrayList2.add(building);
                }
            }
        }
    }

    //这个方法也有点问题，正常的话应该是通过建筑物ID来获取位置，
    //因为通过建筑物名称来获取的话，比如你有两个重工，两个兵营或者多个相同类型建筑就会有问题
    public Coord getBuildingPositionByName2(String name)
    {
        Building buildingByName = getBuildingByName2(name);
        if (buildingByName != null)
        {
            return buildingByName.position;
        }

        return null;
    }

    /**
     * 安装建筑时会把建筑添加到buildingsMap
     * @param building
     */
    public void buildingsMapAddUnit(Building building)
    {
        ArrayList<Building> buildings = buildingsMap.get(building.unitType);

        //添加建筑时，原来没有添加过这种类型的建筑，那需要新建
        if (buildings == null)
        {
            buildings = new ArrayList<>();
        }

        //如果已经包含过了，则不要再添加，这个不知道比较器有没有生效，有点怀疑XXXXX
        if (!buildings.contains(building))
        {
            buildings.add(building);

            buildingsMap.put(building.unitType, buildings);
        }
    }



    public void landUnitsMapAddUnit(LandUnit building)
    {
        ArrayList<LandUnit> buildings = landUnitsMap.get(building.unitType);

        //添加建筑时，原来没有添加过这种类型的建筑，那需要新建
        if (buildings == null)
        {
            buildings = new ArrayList<>();
        }

        //如果已经包含过了，则不要再添加，这个不知道比较器有没有生效，有点怀疑XXXXX
        if (!Global.contains(buildings, building))
        {
            buildings.add(building);
            landUnitsMap.put(building.unitType, buildings);
        }
    }


    public void seaUnitsMapAddUnit(SeaUnit seaUnit)
    {
        ArrayList<SeaUnit> buildings = seaUnitsMap.get(seaUnit.unitType);

        //添加建筑时，原来没有添加过这种类型的建筑，那需要新建
        if (buildings == null)
        {
            buildings = new ArrayList<>();
        }

        //如果已经包含过了，则不要再添加，这个不知道比较器有没有生效，有点怀疑XXXXX
        if (!buildings.contains(seaUnit))
        {
            buildings.add(seaUnit);
            seaUnitsMap.put(seaUnit.unitType, buildings);
        }
    }

    //这个方法也有点问题，正常的话应该是通过建筑物ID来获取位置，
    //因为通过建筑物名称来获取的话，比如你有两个重工，两个兵营或者多个相同类型建筑就会有问题
    public static Unit getBuildingByName(String name)
    {
        for (int i = 0; i < initedBuildings.size(); i++) {
            Unit building = initedBuildings.get(i);
            if (building.name.equals(name))
            {
                return building;
            }
        }
        return null;
    }

    //这个方法也有点问题，正常的话应该是通过建筑物ID来获取位置，
    //因为通过建筑物名称来获取的话，比如你有两个重工，两个兵营或者多个相同类型建筑就会有问题
    public Building getBuildingByName2(String name)
    {
        Set<Map.Entry<Integer, ArrayList<Building>>> entries = buildingsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<Building>> next : entries) {
            ArrayList<Building> buildingArrayList = next.getValue();
            for (int i = 0; i < buildingArrayList.size(); i++) {
                Building building = buildingArrayList.get(i);
                if (building.name.equals(name))
                {
                    return building;
                }
            }
        }

        return null;
    }

    /**
     * 兵工厂生产兵
     */

    public void buildByBarracks(Unit selectedMenu)
    {
        Coord position = getBuildingPositionByName2("盟军兵工厂");
        Coord position2 = new Coord(position.x  +  2,
                position.y );
        int unitType = selectedMenu.unitType;
        AirUnit airUnit = null;


        //因为飞行兵是在兵营附近，兵营是在陆地上
        Coord cell = MapOblique.findCell(position2, 2, CellType.LAND);
        if (cell != null)
        {
            switch (unitType) {
                case UNIT_TYPE.ROCKETEER:
                    logger.info("火箭飞行兵");
                    airUnit = new AirUnit(selectedMenu.name,id,
                            new Coord(cell.x, cell.y),
                            color,
                            unitType,
                            "3-兵营兵种\\4-火箭飞行兵\\");
                    break;
            }

            if (airUnit != null)
            {
                airUnitAdd(airUnit);
                MapOblique.mapUpdate(cell,
                        CellType.LAND, 1);
            }

        }
        else
        {
            logger.info("生产的兵种没有地方可以放置");
        }
    }

    public void airUnitAdd(AirUnit airUnit)
    {
        ArrayList<AirUnit> buildings1 = airUnitsMap.get(airUnit.unitType);
        if (buildings1 == null) //原来还没有这个链表
        {
            ArrayList<AirUnit> buildings2 = new ArrayList<>();
            buildings2.add(airUnit);
            airUnitsMap.put(airUnit.unitType, buildings2);

            return;
        }

        //有这个类型的链接，但是已经包含这个单位了，就不需要再添加了，因为会重复添加
        if (!buildings1.contains(airUnit))
        {
            buildings1.add(airUnit);
            airUnitsMap.put(airUnit.unitType, buildings1);
        }
    }

    /**
     * 船厂生产船支
     */

    public void buildByShipyard(Unit selectedMenu)
    {
        //首先获取重工位置，因为新生产的坦克会移动到重工旁边
        Coord position = getBuildingPositionByName2("盟军船厂");

        //这里注意生成的坦克应该在重工附近，找一个没有放建筑，没有方坦克的地方
        //其实挺难的
        Coord position2 = new Coord(position.x + 2, position.y + 2);

        Coord cell = MapOblique.findCell(position2, 2, CellType.SEA);
        if (cell != null)
        {
            position2 = cell;
        }

        SeaUnit seaUnit = null;

        switch (selectedMenu.unitType)
        {
            case UNIT_TYPE.FRIGATE:
                seaUnit = new SeaUnit("驱逐舰",id,
                        position2,
                        color,
                        UNIT_TYPE.FRIGATE,
                        "5-船厂兵种\\2-盟军-驱逐舰\\");

                int[] damage = new int[]{10, 20};
                int bloodTotal = 100;
                int bloodCurrent = 20;
                int fireDistance = 10;
                int fireCD = 10;

                seaUnit.attackSet(damage,
                        bloodTotal,
                        bloodCurrent,
                        fireDistance,
                        fireCD);
                break;
        }

        MapOblique.mapUpdate(position2,
                CellType.SEA, 1);

        if (seaUnit != null) {
            seaUnitsMapAddUnit(seaUnit);
        }
    }

    /**
     * 因为可能有多个重工，所以需要传重工ID
     */
    //重工生产坦克
    public void buildByWarFactory(Unit selectedMenu)
    {
        //首先获取重工位置，因为新生产的坦克会移动到重工旁边
        Coord position = getBuildingPositionByName2("盟军重工");

        //这里注意生成的坦克应该在重工附近，找一个没有放建筑，没有方坦克的地方
        //其实挺难的
        Coord position2 = new Coord(position.x + 2, position.y + 2);

        Coord cell = MapOblique.findCell(position2, 2, CellType.LAND);
        if (cell != null)
        {
            position2 = cell;
        }

        String name = selectedMenu.name;
        LandUnit tank = null;

        switch (selectedMenu.name)
        {
            case "灰熊坦克":
                tank = new LandUnit(name,
                        id,
                        position2,
                        UNIT_TYPE.GRIZZLYI_TANK,
                        2,
                        2,
                        1400,
                        color,
                        "4-重工兵种\\2-灰熊坦克\\");
                int[] damage = new int[]{10, 20};
                int bloodTotal = 100;
                int bloodCurrent = 100;
                int fireDistance = 5;
                int fireCD = 10;
                MapOblique.mapUpdate(position2,
                        CellType.LAND_UNIT, 1);

                tank.attackSet(damage,
                        bloodTotal,
                        bloodCurrent,
                        fireDistance,
                        fireCD);
                break;
        }

        if (tank != null) {
            landUnitsMapAddUnit(tank);
        }
    }

    //
    void initBuilding(Coord position)
    {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("盟军发电厂");

        //开始显示主基地是收起的状态
        String path = "1-主建筑\\" + "盟军基地" + "\\";
        mainBuilding = new Building("盟军基地车", id,
                new Coord(position.x, position.y),
                UNIT_TYPE.MAIN_BASE_CAR,
                3,
                3,
                3000,
                color,
                arrayList,
                path);
        mainBuilding.buildingState = BuildingState.INIT;
        initedBuildings.add(mainBuilding);
    }

    void nearToBuildingsSetColor(Coord position, Color nearColor, Color farColor)
    {
        Set<Map.Entry<Integer, ArrayList<Building>>> entries = buildingsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<Building>> next : entries) {
            ArrayList<Building> buildingArrayList = next.getValue();
            for (int i = 0; i < buildingArrayList.size(); i++) {
                Building building = buildingArrayList.get(i);
                Color color;
                if (Global.inRectangle(position, building)) {
                    color = nearColor;
                }
                else
                {
                    color = farColor;
                }
                building.pointer.selectedStatus = color;
            }
        }

        Set<Map.Entry<Integer, ArrayList<LandUnit>>> entries1 = landUnitsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<LandUnit>> next : entries1) {
            ArrayList<LandUnit> buildingArrayList = next.getValue();
            for (int i = 0; i < buildingArrayList.size(); i++) {
                LandUnit building = buildingArrayList.get(i);
                Color color;
                if (Global.inRectangle(position, building)) {
                    color = nearColor;
                }
                else
                {
                    color = farColor;
                }
                building.pointer.selectedStatus = color;
            }
        }

        Set<Map.Entry<Integer, ArrayList<SeaUnit>>> entries2 = seaUnitsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<SeaUnit>> next : entries2) {
            ArrayList<SeaUnit> buildingArrayList = next.getValue();
            for (int i = 0; i < buildingArrayList.size(); i++) {
                SeaUnit building = buildingArrayList.get(i);
                Color color;
                if (Global.inRectangle(position, building)) {
                    color = nearColor;
                }
                else
                {
                    color = farColor;
                }
                building.pointer.selectedStatus = color;
            }
        }



        Set<Map.Entry<Integer, ArrayList<AirUnit>>> entries3 = airUnitsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<AirUnit>> next : entries3) {
            ArrayList<AirUnit> buildingArrayList = next.getValue();
            for (int i = 0; i < buildingArrayList.size(); i++) {
                AirUnit building = buildingArrayList.get(i);
                Color color;
                if (Global.inRectangle(position, building)) {
                    color = nearColor;
                }
                else
                {
                    color = farColor;
                }
                building.pointer.selectedStatus = color;
            }
        }
    }

    boolean landUnitsGenerateToGoPath(Unit target, Coord end)
    {
        Set<Map.Entry<Integer, ArrayList<LandUnit>>> entries = landUnitsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<LandUnit>> next : entries) {
            ArrayList<LandUnit> buildingArrayList = next.getValue();
            Integer key = next.getKey();
            for (int i = 0; i < buildingArrayList.size(); i++) {
                LandUnit building = buildingArrayList.get(i);
                if (building == target)
                {
                    building.pathInit(target.position, end);

                    buildingArrayList.set(i, building);
                    ArrayList<LandUnit> put = landUnitsMap.put(key, buildingArrayList);
                    return put != null;
                }
            }
        }

        return false;
    }

    boolean seaUnitsGenerateToGoPath(Unit target, Coord end)
    {
        Set<Map.Entry<Integer, ArrayList<SeaUnit>>> entries = seaUnitsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<SeaUnit>> next : entries) {
            ArrayList<SeaUnit> buildingArrayList = next.getValue();
            Integer key = next.getKey();
            for (int i = 0; i < buildingArrayList.size(); i++) {
                SeaUnit building = buildingArrayList.get(i);
                if (building == target)
                {
                    building.pathInit(target.position, end);

                    buildingArrayList.set(i, building);
                    ArrayList<SeaUnit> put = seaUnitsMap.put(key, buildingArrayList);
                    return put != null;
                }
            }
        }

        return false;
    }


    void airUnitsGenerateToGoPath(Unit target, Coord end)
    {
        Set<Map.Entry<Integer, ArrayList<AirUnit>>> entries = airUnitsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<AirUnit>> next : entries) {
            ArrayList<AirUnit> buildingArrayList = next.getValue();
            for (int i = 0; i < buildingArrayList.size(); i++) {
                AirUnit building = buildingArrayList.get(i);
                if (building == target)
                {
                    building.pointer.pathInit(target.position, end);
                    return;
                }
            }
        }
    }

    void inAirUnitRectangleSetColor(Coord position, Color innerColor, Color outerColor)
    {
        Set<Map.Entry<Integer, ArrayList<AirUnit>>> entries = airUnitsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<AirUnit>> next : entries) {
            ArrayList<AirUnit> buildingArrayList = next.getValue();
            Integer key = next.getKey();
            for (int i = 0; i < buildingArrayList.size(); i++) {
                AirUnit building = buildingArrayList.get(i);
                Color color;
                if (Global.inRectangle2(position, building)) {
                    color = innerColor;
                }
                else
                {
                    color = outerColor;
                }
                building.selectedStatus = color;
                buildingArrayList.set(i, building);
                airUnitsMap.put(key, buildingArrayList);
            }
        }
    }

    void inLandBuildingsSetColor(Coord position, Color innerColor, Color outerColor)
    {
        Set<Map.Entry<Integer, ArrayList<LandUnit>>> entries = landUnitsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<LandUnit>> next : entries) {
            ArrayList<LandUnit> buildingArrayList = next.getValue();
            Integer key = next.getKey();
            for (int i = 0; i < buildingArrayList.size(); i++) {
                LandUnit building = buildingArrayList.get(i);
                Color color;
                if (Global.inRectangle2(position, building)) {
                    color = innerColor;
                }
                else
                {
                    color = outerColor;
                }
                building.selectedStatus = color;
                buildingArrayList.set(i, building);
                landUnitsMap.put(key, buildingArrayList);
            }
        }
    }

    static void copyLandUnitListToItemList(ArrayList<Item> itemArrayList, ArrayList<LandUnit> buildingArrayList)
    {
        for (int i = 0; i < buildingArrayList.size(); i++) {
            LandUnit building = buildingArrayList.get(i);
            Item item = new Item();
            StateObject.copyBuildingToItem(item, building);
            itemArrayList.add(item);
        }
    }


    static void copyBuildingListToItemList(ArrayList<Item> itemArrayList, ArrayList<Building> buildingArrayList)
    {
        for (int i = 0; i < buildingArrayList.size(); i++) {
            Building building = buildingArrayList.get(i);
            Item item = new Item();
            StateObject.copyBuildingToItem(item, building);
            itemArrayList.add(item);
        }
    }

    static void copySeaUnitListToItemList(ArrayList<Item> itemArrayList, ArrayList<SeaUnit> buildingArrayList)
    {
        for (int i = 0; i < buildingArrayList.size(); i++) {
            SeaUnit building = buildingArrayList.get(i);
            Item item = new Item();
            StateObject.copyBuildingToItem(item, building);
            itemArrayList.add(item);
        }
    }

    static void copyAirUnitListToItemList(ArrayList<Item> itemArrayList, ArrayList<AirUnit> buildingArrayList)
    {
        for (int i = 0; i < buildingArrayList.size(); i++) {
            AirUnit building = buildingArrayList.get(i);
            Item item = new Item();
            StateObject.copyBuildingToItem(item, building);
            itemArrayList.add(item);
        }
    }

    void copyBullets(Vector<Bullet> allBulletsFrom,
                     Vector<Bullet> allBulletsTo)
    {
        allBulletsTo.addAll(allBulletsFrom);
    }


    void getBullets(Vector<Bullet> allBulletsFrom,
                    Vector<Bullet> allBulletsTo)
    {
        allBulletsTo.addAll(allBulletsFrom);
    }

    void copyLandUnitsToItems(ConcurrentHashMap<Integer, ArrayList<LandUnit>> sourceBuildingsMap,
                              ConcurrentHashMap<Integer, ArrayList<Item>> outputBuildingsMap)
    {
        Set<Map.Entry<Integer, ArrayList<LandUnit>>> entries = sourceBuildingsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<LandUnit>> next : entries) {
            ArrayList<LandUnit> buildingArrayList = next.getValue();
            Integer key = next.getKey();
            ArrayList<Item> itemArrayList = new ArrayList<>();
            copyLandUnitListToItemList(itemArrayList, buildingArrayList);

            outputBuildingsMap.put(key, itemArrayList);
        }
    }

    void copyBuildingsToItems(ConcurrentHashMap<Integer, ArrayList<Building>> sourceBuildingsMap,
                              ConcurrentHashMap<Integer, ArrayList<Item>> outputBuildingsMap)
    {
        Set<Map.Entry<Integer, ArrayList<Building>>> entries = sourceBuildingsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<Building>> next : entries) {
            ArrayList<Building> buildingArrayList = next.getValue();
            Integer key = next.getKey();
            ArrayList<Item> itemArrayList = new ArrayList<>();
            copyBuildingListToItemList(itemArrayList, buildingArrayList);

            outputBuildingsMap.put(key, itemArrayList);
        }
    }

    void copySeaUnitsToItems(ConcurrentHashMap<Integer, ArrayList<SeaUnit>> sourceBuildingsMap,
                             ConcurrentHashMap<Integer, ArrayList<Item>> outputBuildingsMap)
    {
        Set<Map.Entry<Integer, ArrayList<SeaUnit>>> entries = sourceBuildingsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<SeaUnit>> next : entries) {
            ArrayList<SeaUnit> buildingArrayList = next.getValue();
            Integer key = next.getKey();
            ArrayList<Item> itemArrayList = new ArrayList<>();
            copySeaUnitListToItemList(itemArrayList, buildingArrayList);

            outputBuildingsMap.put(key, itemArrayList);
        }
    }

    void copyAirUnitsToItems(ConcurrentHashMap<Integer, ArrayList<AirUnit>> sourceBuildingsMap,
                             ConcurrentHashMap<Integer, ArrayList<Item>> outputBuildingsMap)
    {
        Set<Map.Entry<Integer, ArrayList<AirUnit>>> entries = sourceBuildingsMap.entrySet();
        for (Map.Entry<Integer, ArrayList<AirUnit>> next : entries) {
            ArrayList<AirUnit> buildingArrayList = next.getValue();
            Integer key = next.getKey();
            ArrayList<Item> itemArrayList = new ArrayList<>();
            copyAirUnitListToItemList(itemArrayList, buildingArrayList);

            outputBuildingsMap.put(key, itemArrayList);
        }
    }

    public static void copyItemMapToBuildingMap(ConcurrentHashMap<Integer,
                                                ArrayList<Item>> from,
                                                int fromTeamID, Team to,
                                                ConcurrentHashMap<Integer, ArrayList<Building>> map)
    {
        //客户端接收从服务器端传输过来的信息，那么接收的信息是敌人（注意）
        Iterator<Map.Entry<Integer, ArrayList<Item>>> iterator = from.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<Integer, ArrayList<Item>> next = iterator.next();
            ArrayList<Item> itemArrayList = next.getValue();
            ArrayList<Building> buildingArrayList = new ArrayList<>();
            Integer key = next.getKey();
            for (int i = 0; i < itemArrayList.size(); i++)
            {
                Item item = itemArrayList.get(i);

                //这里因为敌方没有初始化，所以取自己的
                Unit temp = Team.getBuildingByName(item.name);
                if (temp != null) {
                    temp.selectedStatus = to.color;
                    Building building1 = new Building(temp);
                    building1.fireDistance = item.fireDistance;
                    building1.teamID = fromTeamID;
                    building1.lastFiredTimer = item.lastFiredTimer;
                    building1.damage = new int[]{10, 20};
                    building1.position = new Coord(item.x, item.y);

                    buildingArrayList.add(building1);
                }
            }

            map.put(key, buildingArrayList);
        }
    }

    static int findLandUnitByID(ArrayList<LandUnit> ArrayList1, int id)
    {
        if (ArrayList1 == null)
        {
            return -1;
        }
        for (int j = 0; j < ArrayList1.size(); j++)
        {
            LandUnit building = ArrayList1.get(j);
            if (id == building.id)
            {
                return j;
            }
        }

        return -1;
    }


    static int findAirUnitByID(ArrayList<AirUnit> ArrayList1, int id)
    {
        for (int j = 0; j < ArrayList1.size(); j++)
        {
            AirUnit building = ArrayList1.get(j);
            if (id == building.id)
            {
                return j;
            }
        }

        return -1;
    }



    static int findSeaUnitByID(ArrayList<SeaUnit> ArrayList1, int id)
    {
        if (ArrayList1 == null)
        {
            return -1;
        }
        for (int j = 0; j < ArrayList1.size(); j++)
        {
            SeaUnit building = ArrayList1.get(j);
            if (id == building.id)
            {
                return j;
            }
        }

        return -1;
    }

    static int findBuildingUnitByID(ArrayList<Building> ArrayList1, int id)
    {
        if (ArrayList1 == null)
        {
            return -1;
        }

        for (int j = 0; j < ArrayList1.size(); j++)
        {
            Building building = ArrayList1.get(j);
            if (id == building.id)
            {
                return j;
            }
        }

        return -1;
    }

    /**
     * 这个方法如果是对的，那么另外几个更新数据的方法就会有问题。。。。
     * @param from stateobject from client
     * @param toMap client data at server endian
     */
    public void updateLandUnitMapFromStateObject(ConcurrentHashMap<Integer, ArrayList<Item>> from,
                                                 ConcurrentHashMap<Integer, ArrayList<LandUnit>> toMap)
    {
        //客户端接收从服务器端传输过来的信息，那么接收的信息是敌人（注意）
        Iterator<Map.Entry<Integer, ArrayList<Item>>> fromIterator = from.entrySet().iterator();

        while (fromIterator.hasNext())
        {
            Map.Entry<Integer, ArrayList<Item>> fromNext = fromIterator.next();

            //比如：取出来的是灰熊坦克列表
            ArrayList<Item> fromItemArrayList = fromNext.getValue();
            Integer fromKey = fromNext.getKey();

            ArrayList<LandUnit> toBuildingArrayList = toMap.get(fromKey);
            if (toBuildingArrayList == null)
            {
                toBuildingArrayList = new ArrayList<>();
            }

            /**
             * 比如：取出来每个灰熊坦克
             * 这里感觉遍历会不会影响性能。。。。
             */
            for (int i = 0; i < fromItemArrayList.size(); i++)
            {
                //从对端发送过来的数据，一个一个取出来
                Item fromItem = fromItemArrayList.get(i);

                //在本地的链表先查找下，看看原来有没有保存
                int byID = findLandUnitByID(toBuildingArrayList, fromItem.id);

                /**
                 *比如：原来客户端已经发送过某个ID的坦克，已经有的，更新位置
                 * 因为客户端可能移动过这个坦克，已经不再攻击范围等等
                 * 这里为什么不更新最后一次攻击时间呢，因为最后攻击时间只有服务端的攻击检测才会修改，其他地方不会修改
                 */
                if (-1 != byID)
                {
                    //logger.info("this building almost exist in server's map...");
                    Unit toBuilding = toBuildingArrayList.get(byID);
                    toBuilding.pointer.position.x = fromItem.x;
                    toBuilding.pointer.position.y = fromItem.y;
                    toBuilding.pointer.bloodCurrent = fromItem.bloodCurrent;
                    toBuilding.pointer.bloodTotal = fromItem.bloodTotal;
                }
                /**
                 * 原来客户端未发送给服务端过的坦克，那表示本地需要新增一辆坦克
                 */
                else
                {
                    logger.info("this building have not found in server's map...: " + fromItem);
                    Unit temp = Team.getBuildingByName(fromItem.name);
                    //if (temp != null)
                    {
                        switch (fromItem.unitType)
                        {
                            case UNIT_TYPE.GRIZZLYI_TANK:
                                LandUnit unit = newLandUnit(fromItem);
                                unit.fireCD = temp.fireCD;
                                unit.id = fromItem.id;
                                unit.fireDistance = temp.fireDistance;
                                unit.damage = new int[]{10, 20};
                                toBuildingArrayList.add(unit);
                                toMap.put(fromKey, toBuildingArrayList);
                                break;
                        }
                    }
                }
            }
        }
    }

    private static LandUnit newLandUnit(Item fromItem)
    {
        String name = "灰熊坦克";
        LandUnit unit = new LandUnit(name,
                fromItem.teamID,
                new Coord(fromItem.x, fromItem.y),
                UNIT_TYPE.GRIZZLYI_TANK,
                2,
                2,
                700,
                Global.colors[fromItem.teamID],
                "4-重工兵种\\2-灰熊坦克\\");
        return unit;
    }

    public void updateAirUnitMapFromStateObject(ConcurrentHashMap<Integer, ArrayList<Item>> from,
                                                 ConcurrentHashMap<Integer, ArrayList<AirUnit>> toMap)
    {
        //客户端接收从服务器端传输过来的信息，那么接收的信息是敌人（注意）
        Iterator<Map.Entry<Integer, ArrayList<Item>>> fromIterator = from.entrySet().iterator();

        while (fromIterator.hasNext())
        {
            Map.Entry<Integer, ArrayList<Item>> fromNext = fromIterator.next();

            //比如：取出来的是火箭飞行兵列表
            ArrayList<Item> fromItemArrayList = fromNext.getValue();
            Integer fromKey = fromNext.getKey();

            /**
             * 比如：取出来每个灰熊坦克
             * 这里感觉遍历会不会影响性能。。。。
             */
            for (int i = 0; i < fromItemArrayList.size(); i++)
            {
                //从对端发送过来的数据，一个一个取出来
                Item fromItem = fromItemArrayList.get(i);

                ArrayList<AirUnit> toBuildingArrayList = toMap.get(fromKey);
                /**
                 * 原来客户端未发送给服务端过的坦克，那表示本地需要新增一辆坦克
                 */
                if (toBuildingArrayList == null)
                {
                    toBuildingArrayList = new ArrayList<>();
                    logger.info("this building have not found in server's map...: " + fromItem);
                    switch (fromItem.unitType)
                    {
                        case UNIT_TYPE.ROCKETEER:
                            String path = "3-兵营兵种\\4-火箭飞行兵\\";
                            Unit unit = new AirUnit("火箭飞行兵",id,
                                    new Coord(fromItem.x,fromItem.y),
                                    Global.colors[fromItem.teamID],
                                    UNIT_TYPE.ROCKETEER,
                                    path);

                            unit.id = fromItem.id;
                            unit.teamID = fromItem.teamID;
                            unit.damage = new int[]{10, 20};
                            toBuildingArrayList.add((AirUnit) unit);
                            toMap.put(fromKey, toBuildingArrayList);
                            break;
                    }
                }
                else
                {
                    //在本地的链表先查找下，看看原来有没有保存
                    int byID = findAirUnitByID(toBuildingArrayList, fromItem.id);
                    if (-1 != byID)
                    {
                        AirUnit toBuilding = toBuildingArrayList.get(byID);
                        toBuilding.pointer.position.x = fromItem.x;
                        toBuilding.pointer.position.y = fromItem.y;
                        toBuilding.pointer.bloodCurrent = fromItem.bloodCurrent;
                        toBuilding.pointer.bloodTotal = fromItem.bloodTotal;
                    }
                    else
                    {
                        logger.info("未找到");
                    }
                }
            }
        }
    }

    public void updateSeaUnitMapFromStateObject(ConcurrentHashMap<Integer, ArrayList<Item>> from,
                                                ConcurrentHashMap<Integer, ArrayList<SeaUnit>> toMap)
    {
        //客户端接收从服务器端传输过来的信息，那么接收的信息是敌人（注意）
        Iterator<Map.Entry<Integer, ArrayList<Item>>> fromIterator = from.entrySet().iterator();

        while (fromIterator.hasNext())
        {
            Map.Entry<Integer, ArrayList<Item>> fromNext = fromIterator.next();

            //比如：取出来的是火箭飞行兵列表
            ArrayList<Item> fromItemArrayList = fromNext.getValue();
            Integer fromKey = fromNext.getKey();

            ArrayList<SeaUnit> toBuildingArrayList = toMap.get(fromKey);
            if (toBuildingArrayList == null)
            {
                toBuildingArrayList = new ArrayList<>();
            }

            /**
             * 比如：取出来每个灰熊坦克
             * 这里感觉遍历会不会影响性能。。。。
             */
            for (int i = 0; i < fromItemArrayList.size(); i++)
            {
                //从对端发送过来的数据，一个一个取出来
                Item fromItem = fromItemArrayList.get(i);

                //在本地的链表先查找下，看看原来有没有保存
                int byID = findSeaUnitByID(toBuildingArrayList, fromItem.id);

                /**
                 *比如：原来客户端已经发送过某个ID的坦克，已经有的，更新位置
                 * 因为客户端可能移动过这个坦克，已经不再攻击范围等等
                 * 这里为什么不更新最后一次攻击时间呢，因为最后攻击时间只有服务端的攻击检测才会修改，其他地方不会修改
                 */
                if (-1 != byID)
                {
                    SeaUnit toBuilding = toBuildingArrayList.get(byID);
                    toBuilding.pointer.position.x = fromItem.x;
                    toBuilding.pointer.position.y = fromItem.y;
                    toBuilding.pointer.bloodCurrent = fromItem.bloodCurrent;
                    toBuilding.pointer.bloodTotal = fromItem.bloodTotal;
                }
                /**
                 * 原来客户端未发送给服务端过的坦克，那表示本地需要新增一辆坦克
                 */
                else
                {
                    logger.info("this building have not found in server's map...: " + fromItem);
                    Unit temp = Team.getBuildingByName(fromItem.name);
                    //if (temp != null)
                    {
                        switch (fromItem.unitType)
                        {
                            case UNIT_TYPE.FRIGATE:
                                SeaUnit unit = newSeaUnit(fromItem);
                                unit.fireCD = temp.fireCD;
                                unit.id = fromItem.id;
                                unit.fireDistance = temp.fireDistance;
                                unit.damage = new int[]{10, 20};
                                toBuildingArrayList.add(unit);
                                toMap.put(fromKey, toBuildingArrayList);
                                break;
                        }
                    }
                }
            }
        }
    }


    private static SeaUnit newSeaUnit(Item fromItem)
    {
        return new SeaUnit("驱逐舰", fromItem.teamID,
                new Coord(fromItem.x,fromItem.y),
                Global.colors[fromItem.teamID],
                UNIT_TYPE.FRIGATE,
                "5-船厂兵种\\2-盟军-驱逐舰\\");
    }

    public static void updateBuildingMapFromStateObject(ConcurrentHashMap<Integer, ArrayList<Item>> from,
                                                ConcurrentHashMap<Integer, ArrayList<Building>> toMap)
    {
        //客户端接收从服务器端传输过来的信息，那么接收的信息是敌人（注意）
        Iterator<Map.Entry<Integer, ArrayList<Item>>> fromIterator = from.entrySet().iterator();

        while (fromIterator.hasNext())
        {
            Map.Entry<Integer, ArrayList<Item>> fromNext = fromIterator.next();

            //比如：取出来的是火箭飞行兵列表
            ArrayList<Item> fromItemArrayList = fromNext.getValue();
            Integer fromKey = fromNext.getKey();

            /**
             * 比如：取出来每个灰熊坦克
             * 这里感觉遍历会不会影响性能。。。。
             */
            for (int i = 0; i < fromItemArrayList.size(); i++)
            {
                //从对端发送过来的数据，一个一个取出来
                Item fromItem = fromItemArrayList.get(i);

                update(fromItem, fromKey, toMap);
            }
        }
    }

    static void update(Item fromItem, Integer fromKey, ConcurrentHashMap<Integer, ArrayList<Building>> toMap)
    {
        ArrayList<Building> toBuildingArrayList = toMap.get(fromKey);
        /**
         * 原来客户端未发送给服务端过的坦克，那表示本地需要新增一辆坦克
         */
        if (toBuildingArrayList == null)
        {
            toBuildingArrayList = new ArrayList<>();
            logger.info("this building have not found in server's map...: " + fromItem);
            Unit temp = getBuildingByName(fromItem.name);
            if (temp == null)
            {
                return;
            }

            Unit unit = new Building(temp);
            unit.position = new Coord(fromItem.x, fromItem.y);
            unit.fireCD = temp.fireCD;
            unit.id = fromItem.id;
            unit.teamID = fromItem.teamID;
            unit.fireDistance = temp.fireDistance;
            unit.damage = new int[]{10, 20};
            toBuildingArrayList.add((Building) unit);
            toMap.put(fromKey, toBuildingArrayList);
        }
        /**
         *比如：原来客户端已经发送过某个ID的坦克，已经有的，更新位置，因为客户端可能移动过这个坦克，已经不再攻击范围等等
         * 这里为什么不更新最后一次攻击时间呢，因为最后攻击时间只有服务端的攻击检测才会修改，其他地方不会修改
         */
        else
        {
            //在本地的链表先查找下，看看原来有没有保存
            int byID = findBuildingUnitByID(toBuildingArrayList, fromItem.id);
            if (-1 != byID)
            {
                //logger.info("this building almost exist in server's map...");
                Building toBuilding = toBuildingArrayList.get(byID);
                toBuilding.pointer.position.x = fromItem.x;
                toBuilding.pointer.position.y = fromItem.y;
                toBuilding.pointer.bloodCurrent = fromItem.bloodCurrent;
                toBuilding.pointer.bloodTotal = fromItem.bloodTotal;
            }
        }
    }

    void copyItemMapToAirUnitMap2(ConcurrentHashMap<Integer, ArrayList<Item>> from, Team to)
    {
        Iterator<Map.Entry<Integer, ArrayList<Item>>> iterator2 = from.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry<Integer, ArrayList<Item>> next = iterator2.next();
            ArrayList<Item> itemArrayList = next.getValue();
            ArrayList<AirUnit> buildingArrayList = new ArrayList<>();
            Integer key = next.getKey();
            for (int i = 0; i < itemArrayList.size(); i++) {
                Item item = itemArrayList.get(i);
                String path = "3-兵营兵种\\4-火箭飞行兵\\";
                AirUnit building = new AirUnit(item.name, item.teamID,
                        new Coord(item.x, item.y),
                        to.color,
                        item.unitType,
                        path);

                StateObject.copyItemToBuilding(item, building);
                buildingArrayList.add(building);
            }

            to.airUnitsMap.put(key, buildingArrayList);
        }
    }

    void copyItemMapToSeaUnitMap2(ConcurrentHashMap<Integer, ArrayList<Item>> from, Team to)
    {
        Iterator<Map.Entry<Integer, ArrayList<Item>>> iterator2 = from.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry<Integer, ArrayList<Item>> next = iterator2.next();
            ArrayList<Item> itemArrayList = next.getValue();
            ArrayList<SeaUnit> buildingArrayList = new ArrayList<>();
            Integer key = next.getKey();
            for (int i = 0; i < itemArrayList.size(); i++) {
                Item item = itemArrayList.get(i);
                SeaUnit building = new SeaUnit("驱逐舰", id,
                        new Coord(item.x, item.y),
                        to.color,
                        UNIT_TYPE.FRIGATE,
                        "5-船厂兵种\\2-盟军-驱逐舰\\");

                StateObject.copyItemToBuilding(item, building);
                buildingArrayList.add(building);
            }

            to.seaUnitsMap.put(key, buildingArrayList);
        }
    }
}

