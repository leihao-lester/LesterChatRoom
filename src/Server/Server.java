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
                        // �����ͻ��˵�����
                        Socket socket = DataBuffer.serverSocket.accept();
                        System.out.println("�û�����");
                        // ���ÿ���ͻ�������һ���̣߳����߳��е�����������������ÿ���ͻ��˵�����
                        new Thread(new RequestProcessor(socket)).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // �������
        ServerInfoFrame mainUI = new ServerInfoFrame();
        Application.launch(mainUI.getClass());
    }
}
