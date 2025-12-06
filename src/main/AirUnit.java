package main;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

//空中单位
public class AirUnit extends Unit  implements Serializable {
    private static final long serialVersionUID = 1L;
    int moveStepTotal;
    int moveDerection = 1;
    int[][] heightData = MapOblique.heightData;
    int cellSize = Global.cellSize;

    public AirUnit(String name,
                   int teamID,
                   Coord position,
                   Color selectedStatus,
                   int  unitType,
                   String imageInPath
                   )
    {
        super(name, teamID, position, unitType,1,1,500,
                selectedStatus,imageInPath);
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


        position.x = pathPlanToGo.get(pathCurrentIndex).x;
        position.y = pathPlanToGo.get(pathCurrentIndex).y;
        if (position.y < heightData.length && position.x < heightData[0].length)
        {
            //logger.info("height: " + heightData[position.y / cellSize][position.x / cellSize]);
            position.y = position.y - heightData[position.y][position.x] % 10;
            pathCurrentIndex++;

        }
    }

    /**
     * 现在的空中单位寻路还会到处乱跑，有些问题
     * @param start 起始地地图坐标
     * @param end  终点地图坐标
     */
    void pathInit(Coord start, Coord end){
        Coord mapToPixelStart = Global.mapToPixel(start);
        Coord mapToPixelEnd = Global.mapToPixel(end);

        moveStepTotal = Global.getDistance(mapToPixelStart, mapToPixelEnd);
        moveStepTotal /= cellSize;
        if (moveStepTotal == 0)
        {
            return;
        }
        int xIncrease = (mapToPixelEnd.x - mapToPixelStart.x) / moveStepTotal;
        int yIncrease = (mapToPixelEnd.y - mapToPixelStart.y) / moveStepTotal;
        moveDerection = computeMoveDerection(xIncrease, yIncrease);
        ArrayList<Coord> pathTemp = new ArrayList<>();

        //每次移动一个地图上的格子是最好的，但是如何做到呢，原来因为使用的像素，比较容易，现在使用的地图坐标，计算除法的时候
        //误差比较大
        for (int i = 0; i < moveStepTotal; i++) {
            Coord coord = new Coord(mapToPixelStart.x + i * xIncrease, mapToPixelStart.y + i * yIncrease);
            pathTemp.add(Global.pixelToMap(coord));
        }
        pathCurrentIndex = 0;
        pathPlanToGo = pathTemp;
    }


    /**
     * 这是计算移动方向的，因为不同的移动方向，在界面显示的图片不一样，一共有8个方向
     * @param xIncrease
     * @param yIncrease
     * @return
     */
    public int computeMoveDerection(int xIncrease, int yIncrease)
    {
        Coord currentPosition = new Coord(20, 20);
        Coord nextPosition = new Coord(20 + xIncrease, 20 + yIncrease);

        if (currentPosition.x < nextPosition.x && currentPosition.y == nextPosition.y)
        {
            return 1;
        }
        else if (currentPosition.x < nextPosition.x && currentPosition.y < nextPosition.y)
        {
            return 2;
        }
        else if (currentPosition.x == nextPosition.x && currentPosition.y < nextPosition.y)
        {
            return 3;
        }
        else if (currentPosition.x > nextPosition.x && currentPosition.y < nextPosition.y)
        {
            return 4;
        }
        else if (currentPosition.x > nextPosition.x && currentPosition.y == nextPosition.y)
        {
            return 5;
        }
        else if (currentPosition.x > nextPosition.x && currentPosition.y > nextPosition.y)
        {
            return 6;
        }
        else if (currentPosition.x == nextPosition.x && currentPosition.y > nextPosition.y)
        {
            return 7;
        }
        else if (currentPosition.x < nextPosition.x && currentPosition.y > nextPosition.y)
        {
            return 8;
        }

        logger.info(currentPosition + " " + nextPosition);
        return 1;
    }

}
