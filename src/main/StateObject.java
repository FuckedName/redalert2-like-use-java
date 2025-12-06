package main;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


class Item implements Serializable
{
    int id;
    int teamID;
    int x;
    int y;
    int unitType;
    int fireDistance;
    int bloodTotal;
    int bloodCurrent;
    String name;
    public long lastFiredTimer;

    Item()
    {

    }

    @Override
    public String toString() {
        return ", Item{" +
                "id=" + id +
                ", teamID=" + teamID +
                ", x=" + x +
                ", y=" + y +
                ", unitType=" + unitType +
                ", fireDistance=" + fireDistance +
                ", name='" + name + '\'' +
                ", lastFiredTimer=" + lastFiredTimer +
                '}';
    }
}

public class StateObject implements Serializable {
    private static final long serialVersionUID = 1L;

    int teamID;

    ConcurrentHashMap<Integer, ArrayList<Item>> buildingsMap;
    ConcurrentHashMap<Integer, ArrayList<Item>> seaUnitsMap;
    ConcurrentHashMap<Integer, ArrayList<Item>> landUnitsMap;
    ConcurrentHashMap<Integer, ArrayList<Item>> airUnitsMap;

    public Vector<Bullet> allBullets;

    public StateObject(int teamID) {
        super();
        this.teamID = teamID;
        buildingsMap = new ConcurrentHashMap<>();
        seaUnitsMap = new ConcurrentHashMap<>();
        landUnitsMap = new ConcurrentHashMap<>();
        airUnitsMap = new ConcurrentHashMap<>();
        allBullets = new Vector<>();
    }

    public static void copyBuildingToItem(Item to, Unit from)
    {
        to.x = from.position.x;
        to.y = from.position.y;
        to.unitType = from.unitType;
        to.id = from.id;
        to.teamID = from.teamID;
        to.lastFiredTimer = from.lastFiredTimer;
        to.fireDistance = from.fireDistance;
        to.bloodCurrent = from.bloodCurrent;
        to.bloodTotal = from.bloodTotal;
        to.name = from.name;

    }

    public static void copyItemToBuilding(Item from, Unit to)
    {
        to.position.x = from.x;
        to.position.y = from.y;
        to.teamID = from.teamID;
        to.fireDistance = from.fireDistance;
        to.lastFiredTimer = from.lastFiredTimer;
        to.unitType = from.unitType;
        to.name = from.name;

    }



    String mapToString(ConcurrentHashMap<Integer, ArrayList<Item>> integerArrayListHashMap)
    {
        StringBuilder s = new StringBuilder();
        s.append(",  map start===>  ");
        Set<Map.Entry<Integer, ArrayList<Item>>> entries = integerArrayListHashMap.entrySet();
        for (Map.Entry<Integer, ArrayList<Item>> next : entries) {
            ArrayList<Item> itemArrayList = next.getValue();
            for (int i = 0; i < itemArrayList.size(); i++) {
                Item item = itemArrayList.get(i);
                s.append(", item start===>  item.name: ").append(item.name);
                s.append(" id: ").append(item.id);
                s.append(" x: ").append(item.x);
                s.append(" y: ").append(item.y);
                s.append(" unitType: ").append(item.unitType);
                s.append(" fireDistance: ").append(item.fireDistance);
                s.append(" lastFiredTimer: ").append(item.lastFiredTimer);

            }
        }

        return s.toString();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(mapToString(buildingsMap));
        s.append(mapToString(seaUnitsMap));
        s.append(mapToString(landUnitsMap));
        s.append(mapToString(airUnitsMap));

        Iterator<Bullet> iterator = allBullets.iterator();
        while (iterator.hasNext())
        {
            Bullet next = iterator.next();
            s.append(next);
        }

        return "StateObject{" +
                "teamID='" + teamID + '\'' +
                ", all Item List=" + s +
                '}';
    }

}