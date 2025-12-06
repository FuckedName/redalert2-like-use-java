package main;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class Data extends JFrame implements Runnable{
    private final int windowWidth;//画板的宽度
    private final int windowHeight;//画板的高度

    BufferedImage bufferedImage;
    Graphics bufferedImageGraphics;
    static Logger logger = MyLogger.logger;
    Graphics graphics;

    Data()
    {
        String title = "Red alert 2 data";
        setTitle(title);
        windowWidth = 1920;
        windowHeight = 1080;

        setLayout(null);
        setSize(windowWidth, windowHeight);
        setLocationRelativeTo(null);
        //setUndecorated(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        bufferedImage = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
        bufferedImageGraphics = bufferedImage.createGraphics();
        setVisible(true);
        graphics = getContentPane().getGraphics();
        view();
    }

    void view()
    {
        int[][] data = MapOblique.data;
        int cellSize = Global.cellSize;
        while (true)
        {
            graphics.clearRect(0,0,windowWidth,windowHeight);
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[0].length; j++) {
                    int cell = data[i][j];
                    bufferedImageGraphics.drawString(String.valueOf(cell), j * cellSize, i * cellSize);
                }
            }

            graphics.drawImage(bufferedImage,0,0, this);

            //logger.info("update data");

            try {
                sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void run() {
        logger.info("run");
    }
}
