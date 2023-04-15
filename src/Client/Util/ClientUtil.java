package Client.Util;

import Client.Buffer.DataBuffer;
import Client.View.ChatFrame;
import Common.Model.Entity.Request;
import Common.Model.Entity.Response;

import java.io.IOException;

public class ClientUtil {
    /**
     * 发送请求对象，主动接收响应
     */
    public static Response sendTextRequest(Request request) throws IOException {
        Response response = null;
        // 发送请求
        DataBuffer.oos.writeObject(request);
        DataBuffer.oos.flush();
        System.out.println("客户端发送了请求对象：" + request.getAction());

        try {
            if(!"exit".equals(request.getAction())){
                // 获取响应
                response = (Response) DataBuffer.ois.readObject();
                System.out.println("客户端获取到了响应对象：" + response.getStatus());
            } else {
                System.out.println("客户端断开了连接");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * 发送请求对象，不主动接受响应
     */
    public static void sendTextRequest2(Request request) throws IOException{
        try {
            // 发送请求
            DataBuffer.oos.writeObject(request);
            DataBuffer.oos.flush();
            System.out.println("客户端发送了请求对象：" + request.getAction());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 把指定文本添加到消息列表文本域中
     */
    public static void appendTxt2MsgListArea(String txt){
        ChatFrame.allMsgArea.appendText(txt);
        // 把光标定位到文本域的最后一行

    }
}
