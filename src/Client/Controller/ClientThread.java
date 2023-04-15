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
 * 客户端线程，不断监听来自服务器发送过来的消息
 */
public class ClientThread extends Thread{
    private Application currentFrame; // 当前窗体
    public ClientThread(Application ChatFrame){
        currentFrame = ChatFrame;
    }

    public void run(){
        try {
            while(DataBuffer.clientSocket.isConnected()){
                Response response = (Response) DataBuffer.ois.readObject();
                ResponseType type = response.getType();
                System.out.println("获取了响应内容：" + type);
                if(type == ResponseType.LOGIN){
                    User newUser = (User) response.getData("loginUser");
                    DataBuffer.onlineUsers.add(newUser);
                    /// 刷新在线列表
                    ChatFrame.onlineTableView.getItems().clear();
                    ChatFrame.onlineTableView.getItems().addAll(DataBuffer.onlineUsers);
                    ClientUtil.appendTxt2MsgListArea("【系统消息】用户" + newUser.getId() + "上线了!\n");
                } else if (type == ResponseType.LOGOUT) {
                    User newUser = (User) response.getData("logoutUser");
                    System.out.println("test:"+newUser.getId());
                    DataBuffer.onlineUsers.remove(newUser);
                    // 刷新在线列表
                    ChatFrame.onlineTableView.getItems().clear();
                    ChatFrame.onlineTableView.getItems().addAll(DataBuffer.onlineUsers);
                    ChatFrame.onlineTableView.refresh();
                    ClientUtil.appendTxt2MsgListArea("【系统消息】用户" + newUser.getId() + "下线了!\n");
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
