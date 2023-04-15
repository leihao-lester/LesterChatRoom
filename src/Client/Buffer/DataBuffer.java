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
     * ��ǰ�ͻ��˵��û���Ϣ
     */
    public static User currentUser;
    /**
     * �����û��б�
     */
    public static List<User> onlineUsers;

    /**
     * ��ǰ�ͻ������ӵ����������׽���
     */
    public static Socket clientSocket;
    /**
     * ��ǰ�ͻ������ӵ��������������
     */
    public static ObjectOutputStream oos;
    /**
     * ��ǰ�ͻ������ӵ���������������
     */
    public static ObjectInputStream ois;
    /**
     * ���������ò������Լ�
     */
    public static Properties configProp;
    /**
     * ���ͻ��˵�IP��ַ
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
