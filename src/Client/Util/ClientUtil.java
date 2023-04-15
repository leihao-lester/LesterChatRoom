package Client.Util;

import Client.Buffer.DataBuffer;
import Client.View.ChatFrame;
import Common.Model.Entity.Request;
import Common.Model.Entity.Response;

import java.io.IOException;

public class ClientUtil {
    /**
     * ���������������������Ӧ
     */
    public static Response sendTextRequest(Request request) throws IOException {
        Response response = null;
        // ��������
        DataBuffer.oos.writeObject(request);
        DataBuffer.oos.flush();
        System.out.println("�ͻ��˷������������" + request.getAction());

        try {
            if(!"exit".equals(request.getAction())){
                // ��ȡ��Ӧ
                response = (Response) DataBuffer.ois.readObject();
                System.out.println("�ͻ��˻�ȡ������Ӧ����" + response.getStatus());
            } else {
                System.out.println("�ͻ��˶Ͽ�������");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * ����������󣬲�����������Ӧ
     */
    public static void sendTextRequest2(Request request) throws IOException{
        try {
            // ��������
            DataBuffer.oos.writeObject(request);
            DataBuffer.oos.flush();
            System.out.println("�ͻ��˷������������" + request.getAction());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ��ָ���ı���ӵ���Ϣ�б��ı�����
     */
    public static void appendTxt2MsgListArea(String txt){
        ChatFrame.allMsgArea.appendText(txt);
        // �ѹ�궨λ���ı�������һ��

    }
}
