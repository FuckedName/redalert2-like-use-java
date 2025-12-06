package main;

import java.awt.*;
import java.io.Serializable;
import java.util.List;

/**
 * 建筑用的这个类
 */
public class Building extends Unit implements Serializable
{
    public Building(String name,
                    int teamID,
                    Coord position,
                    int unitType,
                    int width,
                    int height,
                    int price,
                    Color selectedStatus,
                    List canProduceItems,
                    String path) {
        super(name, teamID, position, unitType, width, height, price, selectedStatus, path);
    }

    public Building(Building building)
    {
        super(building);
    }

    public Building(Unit building)
    {
        super(building);
    }
}
