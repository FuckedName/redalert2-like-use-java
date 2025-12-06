package main;

import java.awt.*;

public class SeaUnit extends Unit{

    public SeaUnit(String name,
                int teamID,
                Coord position,
                Color selectedStatus,
                int unitType,
                String imagePath) {
        super(name, teamID, position, unitType,2,2,500,
                selectedStatus, imagePath);
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

        MapOblique.mapUpdate(position, CellType.SEA, 1);
        pathCurrentIndex++;
    }
}
