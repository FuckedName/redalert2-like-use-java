package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

/**
 * TCP通信的服务器端
 */
public class TcpServer extends Thread
{
    private static ServerSocket serverSocket;
    static Logger logger = MyLogger.logger;

    /**
     * 远端客户端数组，因为可能有多个客户端
     */
    static private ArrayList<Socket> clients;

    /**
     * 从客户端收到的数据存放到这里，然后给前端界面处理
     */
    private static Vector<StateObject> stateObjects;


    TcpServer()
    {
        stateObjects = new Vector<>();
    }

    /**
     * 服务端初始化，启动监听
     */
    public void socketInitial()
    {

            try
            {
                /**
                 * 服务器启动监听端口
                 */
                logger.info("This is server, listen on ip: "+ "127.0.0.1" +", port: " + 8081);
                serverSocket = new ServerSocket(Global.tcpServerPort); // 监听Global.tcpServerPort端口

                /**
                 * 设置超时时间，异步通信，因为前端界面要不断刷新，不能一直等待客户端消息，那样画面会卡住
                 * 就变成，有消息来，服务器就处理，没消息来就不管
                 */
                serverSocket.setSoTimeout(500000);


                clients = new ArrayList<>();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
    }

    /**
     * 如果有客户端请求连接
     */
    public void Accept()
    {
        while (true)
        {
            logger.info("Accept");
            try {
                Socket accept = serverSocket.accept();
                accept.setSoTimeout(50000);

                //记录客户端
                clients.add(accept);
                logger.info("a new client connected, current client count: " + clients.size());

                sleep(100);
            } catch (InterruptedException e) {
                logger.info(e.toString());
                //throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*
    从多个客户端接收数据
     */
    public static Vector<StateObject> receive()
    {
        if (clients.isEmpty())
        {
            return null;
        }

        stateObjects.clear();

        //这里复制一份，不然会有clients并发修改异常，这样处理会增加点额外开销
        ArrayList<Socket> clients_copy = new ArrayList<>(clients);

        /**
         * 把客户端的数据打包
         */
        Iterator<Socket> iterator = clients_copy.iterator();
        while (iterator.hasNext())
        {
            Socket socket = iterator.next();
            if (socket.isClosed())
            {
                continue;
            }
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                //logger.info(objectInputStream.toString());
                socket.setSoTimeout(10000);

                //从客户端读取，所有读取都是异步的
                StateObject object = (StateObject)objectInputStream.readObject();
                //System.out.println(object);

                //logger.info(clients_copy.size() + ", " +object.toString());

                //读取到数据就先添加到stateObjects
                stateObjects.add(object); //因为有多个客户端，所以先放到缓冲区
            }
            catch (IOException e) {
                //iterator.remove();
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        return stateObjects;
    }

    /**
     * 这里是将打包好的状态信息从服务器端发送给客户端
     * @param stateObjects 打包好的状态信息
     */
    public static void send(ArrayList<StateObject>  stateObjects)
    {
        if (clients.isEmpty())
        {
            return ;
        }

        /**
         * 复制了份，防止并发修改异常
         */
        ArrayList<Socket> clients_copy = new ArrayList<>(clients);

        /**
         *  服务端数据会向所有客户端发送
         */
        Iterator<Socket> iterator = clients_copy.iterator();
        while (iterator.hasNext())
        {
            Socket socket = iterator.next();
            if (socket.isClosed()) {
                continue;
            }

            try
            {
                ObjectOutputStream objectOutputStream;
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                //logger.info(clients_copy.size() + " "+ stateObjects.toString());

                //这里是将打包好的状态信息从服务器端发送给客户端
                objectOutputStream.writeObject(stateObjects);

            } catch (IOException e) {
                logger.info(e.toString());
                //iterator.remove();
                //throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void run()
    {
        logger.info("This is Tcp Server Thread start");
        Accept();
    }
}
