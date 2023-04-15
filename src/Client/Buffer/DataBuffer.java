package Client.Buffer;

import Common.Model.Entity.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Properties;

public class DataBuffer {
    /**
     * 当前客户端的用户信息
     */
    public static User currentUser;
    /**
     * 在线用户列表
     */
    public static List<User> onlineUsers;

    /**
     * 当前客户端连接到服务器的套接字
     */
    public static Socket clientSocket;
    /**
     * 当前客户端连接到服务器的输出流
     */
    public static ObjectOutputStream oos;
    /**
     * 当前客户端连接到服务器的输入流
     */
    public static ObjectInputStream ois;
    /**
     * 服务器配置参数属性集
     */
    public static Properties configProp;
    /**
     * 本客户端的IP地址
     */
    public static String ip;

    static {
        configProp = new Properties();
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
            configProp.load(Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("serverConfig.properties"));
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
