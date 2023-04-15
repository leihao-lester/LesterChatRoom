package Client;


import Client.Buffer.DataBuffer;
import Client.View.LoginFrame;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        connection();
        LoginFrame loginUI = new LoginFrame();
        Application.launch(loginUI.getClass());
    }

    /**
     * ���ӷ�����
     */
    public static void connection(){
        String ip = DataBuffer.configProp.getProperty("ip");
        int port = Integer.parseInt(DataBuffer.configProp.getProperty("port"));
        try {
            DataBuffer.clientSocket = new Socket(ip, port);
            DataBuffer.oos = new ObjectOutputStream(DataBuffer.clientSocket.getOutputStream());
            DataBuffer.ois = new ObjectInputStream(DataBuffer.clientSocket.getInputStream());
        } catch (IOException e){
            Alert error = new Alert(Alert.AlertType.ERROR, "����������ʧ�ܣ�����");
            error.setTitle("������δ����");
            Button err = new Button();
            error.showAndWait();
            System.exit(0);
        }
    }

}
