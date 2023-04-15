package Server.Buffer;

import Common.Model.Entity.User;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentSkipListMap;

public class DataBuffer {
    public static ServerSocket serverSocket;
    public static Map<String, OnlineClientIOCache> onlineUserIOCacheMap;
    public static Map<String, User> onlineUsersMap;
    public static Properties configProp;
//    public static OnlineUserTableModel onlineUserTableModel;

    static {
        onlineUserIOCacheMap = new ConcurrentSkipListMap<String, OnlineClientIOCache>();
        onlineUsersMap = new ConcurrentSkipListMap<String, User>();
        configProp = new Properties();
//        onlineUserTableModel = new OnlineUserTableModel();
        try {
            configProp.load(Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("serverConfig.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
