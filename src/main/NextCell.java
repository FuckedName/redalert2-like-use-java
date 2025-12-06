package main;

import java.io.Serializable;

public class NextCell implements Serializable
{
    private static final long serialVersionUID = 1L;
    int xIncrement;
    int yIncrement;
    int stepTotal;
    public int stepCurrent;
}
