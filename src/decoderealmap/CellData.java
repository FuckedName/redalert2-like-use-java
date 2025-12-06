package decoderealmap;

import java.util.Objects;

public class CellData
{
    int x;
    int y;

    CellData(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        CellData cellData = (CellData) o;
        return x == cellData.x && y == cellData.y;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(x, y);
    }
}
