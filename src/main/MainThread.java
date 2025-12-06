package main;

import java.util.logging.Logger;

class DeviceType
{
    public static final int SERVER = 0;
}

/**
 * 系统主线程，如果当前是服务端，则启动tcp 服务器，界面也会调用runServer
 *           如果当前是客户端，则启动tcp 客户端，界面也会调用runClient
 */
public class MainThread implements Runnable
{
    /**
     * TCP通信的服务器端
     */
    public static TcpServer tcpServer;

    /**
     * TCP通信的客户端
     */
    public static TcpClient tcpClient;

    Logger logger = MyLogger.logger;
    int timer;

    /**
     * 当前本地设备标识：服务器还是客户端，ID是多少
     */
    public static int thisDeviceType;

    /**
     * 游戏运行时前端界面类
     */
    GameFrame gameFrame;

    /**
     * 构建方法，初始化
     * @param thisDeviceType
     */
    public MainThread(int thisDeviceType)
    {
        MainThread.thisDeviceType = thisDeviceType;
        if (thisDeviceType == DeviceType.SERVER)
        {
            tcpServer = new TcpServer();
            tcpServer.socketInitial();
            tcpServer.start();
        }
        else
        {
            tcpClient = new TcpClient();
            tcpClient.socketInitial();
        }
        gameFrame = new GameFrame();
    }

    public void run()
    {
        logger.info("run timer: " + timer++);
        while (true)
        {
            //logger.info("while");
            if (thisDeviceType == DeviceType.SERVER)
            {
                gameFrame.runServer();
            }
            else
            {
                gameFrame.runClient();
            }

            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * 总入口
     */
    public static void main(String[] args)
    {
        /**
         * 为什么改为配置文件：修改ID，不需要重新编译代码
         *src目录中config.txt内容为0，表示当前是服务器端；为1、2、3...表示为客户端

        Config config = new Config("config.txt");
        thisDeviceType = config.getResult();*/
        new Config("0-config.yml");

        thisDeviceType = Config.thisDeviceType;

        MainThread mainThread = new MainThread(thisDeviceType);
        mainThread.run();
		
		//这里类似后台显示一些测试数据的，比如地图上因为放了建筑或者坦克后，地图上的位置会被占用，可以通过这个窗口来查看
        /*
        Global global = new Global();
        global.data = new Data();
        global.data.run();

         */
    }
}
