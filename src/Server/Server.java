package Server;

import Server.Buffer.DataBuffer;
import Server.Controller.RequestProcessor;
import Server.View.ServerInfoFrame;
import javafx.application.Application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    public static void main(String[] args) {
        int port =  Integer.parseInt(DataBuffer.configProp.getProperty("port"));
        try {
            DataBuffer.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true){
                        // 监听客户端的连接
                        Socket socket = DataBuffer.serverSocket.accept();
                        System.out.println("用户来了");
                        // 针对每个客户端启动一个线程，在线程中调用请求处理器来处理每个客户端的请求
                        new Thread(new RequestProcessor(socket)).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // 设置外观
        ServerInfoFrame mainUI = new ServerInfoFrame();
        Application.launch(mainUI.getClass());
    }
}
