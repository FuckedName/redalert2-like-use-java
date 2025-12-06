package main;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

class Base
{
    private int x;
    private int y;
    private int displayWidth;
    private int displayHeight;
    private final Image image;

    public Base(int x, int y, int displayWidth, int displayHeight, String fileName) {
        this.x = x;
        this.y = y;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.image = Toolkit.getDefaultToolkit().getImage( "resource\\images\\" + fileName);
    }

    Base(Image image) {
        this.image = image;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    public void setDisplayWidth(int displayWidth) {
        this.displayWidth = displayWidth;
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public void setDisplayHeight(int displayHeight) {
        this.displayHeight = displayHeight;
    }

    public Image getImage() {
        return image;
    }
}

class Bullet1 extends Base
{
    public Bullet1(int x, int y, int displayWidth, int displayHeight, String fileName)
    {
        super(x, y, displayWidth, displayHeight, fileName);
    }
}


public class GameFrame3 extends JFrame implements Runnable {

    Logger logger = MyLogger.logger;
    private long timerClick;
    private final Graphics graphics; //画笔
    private final Bullet1 bullet;

    private  double t ;
    private final double angle = Math.PI/6;
    private double v0 = 20;
    private final double g = 1;
    double cos = Math.cos(angle);
    double sin = Math.sin(angle);

    public GameFrame3() {
        //画板的宽度
        int windowWidth = 1629;
        //画板的高度
        int windowHeight = 990;
        setLayout(null);
        setSize(windowWidth, windowHeight);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        graphics = getContentPane().getGraphics();
        bullet = new Bullet1(0, 600, 64, 64, "2-pingdi.PNG");
    }

    /**
     * 速度-时间公式：v=gt
     * 位移-时间公式; h=gt^2/2
     * 速度-位移公式：v^2=2gh
     */
    @Override
    public void run() {
        while (true)
        {
            timerClick++;
            logger.info(timerClick + " x:" + bullet.getX() + ", y:" + bullet.getY());
            graphics.drawImage(bullet.getImage(), bullet.getX(), bullet.getY(), 15, 15, this);
            t += 0.1;
            double x = v0 * t * cos;
            double height = v0 * t * sin - 1f / 2 * g * t * t;
            bullet.setX((int) x);
            bullet.setY(200-(int) height);
            v0 -= 0.02;
            if (timerClick >= 200)
            {
                break;
            }

            try
            {
                Thread.sleep(10);
            } catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        GameFrame3 gameFrame3 = new GameFrame3();
        Thread thread = new Thread(gameFrame3);
        thread.start();
    }
}