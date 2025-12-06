package main;

import java.awt.*;

public class BuildProcess {
    int x;
    int y;
    int width;
    int height;
    int stepTotal;
    long timerStart;
    Color colorDisplay;

    public BuildProcess(int x, int y, int width, int height, int stepTotal, long timerStart, Color colorDisplay) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.stepTotal = stepTotal;
        this.timerStart = timerStart;
        this.colorDisplay = colorDisplay;
    }

    @Override
    public String toString() {
        return "BuildProcess{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", stepTotal=" + stepTotal +
                ", timerStart=" + timerStart +
                ", colorDisplay=" + colorDisplay +
                '}';
    }

    void addToList(BuildProcess buildProcess, long timerCurrent)
    {

    }
}
