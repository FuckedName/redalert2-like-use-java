package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * TCP通信的客户端
 */
public class TcpClient extends Thread
{
    private static Socket socketClient;
    static Logger logger = MyLogger.logger;

    /**
     * 请求连接服务端
     */
    public void socketInitial()
    {
        try {
            logger.info("try to connect server: " + "127.0.0.1" + ", at port: " + 8081);
            socketClient = new Socket(InetAddress.getByName(Global.tcpServerIP), Global.tcpServerPort); //需要服务端先开启

            /**
             * 异步，不然前端界面会卡住
             */
            socketClient.setSoTimeout(50000);
        } catch (IOException e) {
            logger.info("服务器端是否开启？？？");
            throw new RuntimeException(e);
        }
    }

    /**
     * 从服务器端读取数据
     * @return
     */
    public static ArrayList<StateObject> receive()
    {
        ArrayList<StateObject> object = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(socketClient.getInputStream());

            //异步
            socketClient.setSoTimeout(10000);

            //从服务器端读取状态信息
            object = (ArrayList<StateObject>)objectInputStream.readObject();

            //logger.info(object.toString());
            return object;
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return object;
    }

    /**
     * 因为从客户端的角度，只会有一个服务器
     * @param stateObject
     */
    public static void send(StateObject stateObject)
    {
        try
        {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socketClient.getOutputStream());
            //StateObject stateObject = new StateObject("Sent from client: ");
            stateObject.teamID = MainThread.thisDeviceType;
            //logger.info(stateObject.toString());

            /**
             * 发送数据到服务器端
             */
            objectOutputStream.writeObject(stateObject);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}