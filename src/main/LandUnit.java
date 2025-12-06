package main;

import java.awt.*;
import java.io.Serializable;

public class LandUnit extends Unit  implements Serializable
{
    public LandUnit(String name, int teamID, Coord position, int unitType, int width, int height, int price, Color selectedStatus, String path)
    {
        super(name, teamID, position, unitType, width, height, price, selectedStatus, path);
    }

    public LandUnit(LandUnit landUnit)
    {
        super(landUnit);
    }

    @Override
    public void move()
    {
        if (pathPlanToGo == null)
        {
            return;
        }

        if (pathCurrentIndex >= pathPlanToGo.size())
        {
            pathCurrentIndex = 0;
            pathPlanToGo = null;
            return;
        }

        //坦克在移动前需要先把地图上原来占用的位置清空，
        //移动后把新占用的位置设置
        MapOblique.mapRecovery(position, 1);

        position.x = pathPlanToGo.get(pathCurrentIndex).x;
        position.y = pathPlanToGo.get(pathCurrentIndex).y;

        MapOblique.mapUpdate(position, CellType.LAND_UNIT, 1);
        pathCurrentIndex++;
    }
}
