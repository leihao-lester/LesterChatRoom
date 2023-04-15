package Client.Controller;

import Client.Buffer.DataBuffer;
import Client.Util.ClientUtil;
import Client.View.ChatFrame;
import Common.Model.Entity.Message;
import Common.Model.Entity.Response;
import Common.Model.Entity.ResponseType;
import Common.Model.Entity.User;
import javafx.application.Application;

import java.io.IOException;

/**
 * �ͻ����̣߳����ϼ������Է��������͹�������Ϣ
 */
public class ClientThread extends Thread{
    private Application currentFrame; // ��ǰ����
    public ClientThread(Application ChatFrame){
        currentFrame = ChatFrame;
    }

    public void run(){
        try {
            while(DataBuffer.clientSocket.isConnected()){
                Response response = (Response) DataBuffer.ois.readObject();
                ResponseType type = response.getType();
                System.out.println("��ȡ����Ӧ���ݣ�" + type);
                if(type == ResponseType.LOGIN){
                    User newUser = (User) response.getData("loginUser");
                    DataBuffer.onlineUsers.add(newUser);
                    /// ˢ�������б�
                    ChatFrame.onlineTableView.getItems().clear();
                    ChatFrame.onlineTableView.getItems().addAll(DataBuffer.onlineUsers);
                    ClientUtil.appendTxt2MsgListArea("��ϵͳ��Ϣ���û�" + newUser.getId() + "������!\n");
                } else if (type == ResponseType.LOGOUT) {
                    User newUser = (User) response.getData("logoutUser");
                    System.out.println("test:"+newUser.getId());
                    DataBuffer.onlineUsers.remove(newUser);
                    // ˢ�������б�
                    ChatFrame.onlineTableView.getItems().clear();
                    ChatFrame.onlineTableView.getItems().addAll(DataBuffer.onlineUsers);
                    ChatFrame.onlineTableView.refresh();
                    ClientUtil.appendTxt2MsgListArea("��ϵͳ��Ϣ���û�" + newUser.getId() + "������!\n");
                } else if (type == ResponseType.CHAT) {
                    Message msg = (Message) response.getData("txtMsg");
                    if(msg.getFromUser().getId() != DataBuffer.currentUser.getId()){
                        ClientUtil.appendTxt2MsgListArea(msg.getMessage());
                    }
                } else if (type == ResponseType.BOARD) {
                    Message msg = (Message) response.getData("txtMsg");
                    ClientUtil.appendTxt2MsgListArea(msg.getMessage());
                } else if (type == ResponseType.REMOVE) {
                    Message msg = (Message) response.getData("txtMsg");
                    ClientUtil.appendTxt2MsgListArea(msg.getMessage());
                    ChatFrame.remove();
                }
            }
        } catch (IOException e) {
//            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
