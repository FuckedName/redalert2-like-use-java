package main;

import java.awt.*;
import java.io.Serializable;
import java.util.Objects;

//子弹
public class Bullet implements Serializable {
    Bullet pointer;

    /**
     *
     * 抛射体运动，这里需要注意，教科书上的夹角是二维坐标轴的，红警是三维坐标轴的，
     * 这个夹角的是炮弹发射与水平方向上的夹角，这个有点难计算，受距离和所处高度差坐标影响
     * 单位之间的直线距离是两个单位之间的坐标计算形成的: ∛（x1-x2）^2+(y1-y2)^2+(h1-h2)^2  (3次根号下三个坐标差平方和)
     * 假设坦克1的坐标是x1,y1；坦克2的坐标是x2，y2，这里要注意红警游戏里坦克还有高度坐标，
     * 1、高度相同的坐标场景：两个坦克的高度坐标都是3的话，一般子弹路径可以采用抛物线实现
     * 2、高度不同的坐标场景：如果一个坦克的高度坐标是3，另一个坦克的高度坐标是5的话，感觉要用到抛射体运动曲线
     * 3、距离比较近的场景：往上抛射炮弹的角度可以小些（现实中投石块打中目标一个原理）；
     * 4、距离比较远的场景：往上抛射炮弹的角度可以大些；
     * 初始夹角θ，初始速度v0
     * x=v0 * t * cosθ
     * y=v0 * t * sinθ - 1/2 * g * t *　t + h
     */
    private final double angle = Math.PI / 6;
    double cos = Math.cos(angle);
    double sin = Math.sin(angle);

    final static double v0 = 40;

    final static double g = 1;
    double t = 0;

    //子弹的源头
    Coord positionSource;

    //子弹的目标
    Coord positionTarget;

    //子弹所属队伍
    int teamID;

    //子弹是什么兵种发出的
    int unitType;

    //子弹显示的颜色
    Color color;

    //子弹的伤害
    int damage;

    //子弹移动多少步会击中目标，这里先给的20步，实际上因为敌方坦克会移动，所以可能步数会有变化
    public int needMoveTotalSteps;
    public int currentMovedSteps;

    int xMoveStep;
    int yMoveStep;

    Unit target;

    public Bullet(Coord positionSource,Coord positionTarget,
                  int teamID,
                  int unitType,
                  Color color,
                  int damage,
                  int xMoveStep,
                  int yMoveStep,
                  int needMoveTotalSteps,
                  Unit target)
    {
        this.pointer = this;

        //后边实际上是随机生成一个[-5,5]的随机数，这样两个坦克的子弹弹道不容易重合
        this.positionSource = new Coord(positionSource);
        this.positionTarget = new Coord(positionTarget);
        this.teamID = teamID;
        this.unitType = unitType;
        this.damage = damage;
        this.color = color;
        this.xMoveStep = xMoveStep;
        this.yMoveStep = yMoveStep;
        this.target = target;
        this.currentMovedSteps = 0;
        this.needMoveTotalSteps = needMoveTotalSteps;
    }

    public void move()
    {
        //position = new Coord(position.x + xMoveStep, position.y + yMoveStep);
        currentMovedSteps++;
    }

    @Override
    public String toString()
    {
        return ", Bullet{" +
                "pointer=" + pointer +
                ", positionSource=" + positionSource +
                ", positionTarget=" + positionTarget +
                ", teamID=" + teamID +
                ", unitType=" + unitType +
                ", color=" + color +
                ", damage=" + damage +
                ", needMoveTotalSteps=" + needMoveTotalSteps +
                ", currentMovedSteps=" + currentMovedSteps +
                ", xMoveStep=" + xMoveStep +
                ", yMoveStep=" + yMoveStep +
                ", target=" + target +
                '}';
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(positionSource.x, positionSource.y);
    }

    /**
     * 这里的比较是基于位置比较，而子弹在move的时候位置是不变的，只是step变化了，所以添加不了子弹
     * @param obj
     * @return
     */
}
