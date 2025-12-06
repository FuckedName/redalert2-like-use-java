package decoderealmap;
import main.Coord;

import java.io.Serializable;
import java.util.ArrayList;

public class MapObjectData implements Serializable
{
    private static final long serialVersionUID = 1L; // 版本控制
    public int[][] data;
    ArrayList<Coord> baseCarStartPositionList;

    public MapObjectData(int height, int width)
    {
        data = new int[height][width];
        baseCarStartPositionList = new ArrayList<>();
    }
}