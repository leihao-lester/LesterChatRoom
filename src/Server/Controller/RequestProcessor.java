package Server.Controller;

import Common.Model.Entity.*;
import Server.Buffer.DataBuffer;
import Server.Buffer.OnlineClientIOCache;
import Server.View.ServerInfoFrame;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

public class RequestProcessor implements Runnable{
    // ��������������Ŀͻ���Socket
    private Socket currentClientSocket;


    public RequestProcessor(Socket currentClientSocket){
        this.currentClientSocket = currentClientSocket;
    }


    @Override
    public void run() {
        // �Ƿ񲻼�ϼ���
        boolean flag = true;
        try {
            OnlineClientIOCache currentClientIOCache = new OnlineClientIOCache(
                    new ObjectInputStream(currentClientSocket.getInputStream()),
                    new ObjectOutputStream(currentClientSocket.getOutputStream()));
            // ���ϵض�ȡ�ͻ��˷�����������
            while (flag){
                Request request = (Request)currentClientIOCache.getObjectInputStream().readObject();
                System.out.println("Server��ȡ�˿ͻ��˵�����:" + request.getAction());
                String actionName = request.getAction();
                if(actionName.equals("login")){
                    login(currentClientIOCache, request);
//                } else if(actionName.equals("chatAll")){
//                    chatAll(request);
                } else if(actionName.equals("chat")){
                    chat(request);
                } else if(actionName.equals("exit")){
                    flag = logout(currentClientIOCache, request);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * ��¼
     */
    public void login(OnlineClientIOCache currentClientIO, Request request) throws IOException{
        String id = (String)request.getAttribute("id");
        User user = new User(id);
        boolean idUsed = false;
        for(String idtmp: DataBuffer.onlineUsersMap.keySet()){
            if(id.equals(idtmp)){
                idUsed = true;
                break;
            }
        }
        Response response = new Response();
        if(!idUsed){
            System.out.println(id + "����������");
            // ��ӵ������û�
            DataBuffer.onlineUsersMap.put(id, user);
            DataBuffer.onlineUsersMap.remove("adminTest");
//            if(currentWindow.onlineTableView != null){
//                currentWindow.onlineTableView.setItems(FXCollections.observableArrayList(DataBuffer.onlineUsersMap.values()));
//                currentWindow.onlineTableView.getItems().clear();
//                currentWindow.onlineTableView.getItems().addAll(DataBuffer.onlineUsersMap.values());
//                currentWindow.onlineTableView.refresh();
                ServerInfoFrame.refresh();
//            }

            // ���������û�
            response.setData("onlineUsers",
                    new CopyOnWriteArrayList<User>(DataBuffer.onlineUsersMap.values()));
            response.setStatus(ResponseStatus.OK);
            response.setData("user", user);
            currentClientIO.getObjectOutputStream().writeObject(response);
            currentClientIO.getObjectOutputStream().flush();
            // ֪ͨ�����û�����������
            Response response2 = new Response();
            response2.setType(ResponseType.LOGIN);
            response2.setData("loginUser", user);
            iteratorResponse(response2);

            // �ѵ�ǰ���ߵ��û���IO��ӵ�����map��
            DataBuffer.onlineUserIOCacheMap.put(id, currentClientIO);
        }else {
            // ��¼ʧ��
            response.setStatus(ResponseStatus.OK);
            response.setData("msg", "id�ѱ�ռ��");
            currentClientIO.getObjectOutputStream().writeObject(response);
            currentClientIO.getObjectOutputStream().flush();
        }
    }

    /**
     * �������û���������Ӧ
     */
    public void iteratorResponse(Response response) throws IOException{
        for(OnlineClientIOCache onlineUserIO : DataBuffer.onlineUserIOCacheMap.values()){
            ObjectOutputStream objectOutputStream = onlineUserIO.getObjectOutputStream();
            objectOutputStream.writeObject(response);
            objectOutputStream.flush();
        }
    }


    public boolean logout(OnlineClientIOCache oio, Request request) throws IOException{
        System.out.println(currentClientSocket.getInetAddress().getHostAddress()
                + ":" + currentClientSocket.getPort() + "�뿪������");
        User user = (User) request.getAttribute("user");
        // �ѵ�ǰ���߿ͻ��˵�IO��Map��ɾ��
        DataBuffer.onlineUserIOCacheMap.remove(user.getId());
        // �������û�����Map��ɾ����ǰ�û�
        DataBuffer.onlineUsersMap.remove(user.getId());
        if(DataBuffer.onlineUsersMap.isEmpty()){
            DataBuffer.onlineUsersMap.put("adminTest", new User("Ŀǰ��������"));
        }
        ServerInfoFrame.refresh();

        // ����һ����Ӧ����
        Response response = new Response();
        response.setType(ResponseType.LOGOUT);
//        System.out.println("test:" + user.getId() + "\n");
        response.setData("logoutUser", user);
        // ����Ӧ�������ͻ���д
        oio.getObjectOutputStream().writeObject(response);
        oio.getObjectOutputStream().flush();
        // �ر�����ͻ���Socket
        currentClientSocket.close();
        // ֪ͨ�������߿ͻ���
        iteratorResponse(response);
        // �Ͽ�����
        return false;
    }

    public void chat(Request request) throws IOException{
        Message msg = (Message)request.getAttribute("msg");
        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.CHAT);
        response.setData("txtMsg", msg);

        if(msg.getToUser() != null){
            // ˽�ģ�ֻ��˽�ĵĶ��󷵻���Ӧ
            OnlineClientIOCache io = DataBuffer.onlineUserIOCacheMap.get(msg.getToUser().getId());
            sendResponse(io, response);
        }else {
            // Ⱥ�ģ������˷���Ϣ�����пͻ��˷�����Ӧ
            for(String id : DataBuffer.onlineUserIOCacheMap.keySet()){
                if(msg.getFromUser().getId() == id){
                    continue;
                }
                sendResponse(DataBuffer.onlineUserIOCacheMap.get(id), response);
            }
        }
    }

    /**
     * ����
     */
    public static void remove(User user) throws IOException{
        User admin = new User("admin");
        Message msg = new Message();
        msg.setFromUser(admin);
        msg.setSendTime(new Date());
        msg.setToUser(user);

        StringBuffer stringBuffer = new StringBuffer();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        stringBuffer.append(" ").append(dateFormat.format(msg.getSendTime()))
                .append(" ").append("ϵͳ֪ͨ��\n " + "����ǿ������\n");
        msg.setMessage(stringBuffer.toString());

        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.REMOVE);
        response.setData("txtMsg", msg);

        DataBuffer.onlineUsersMap.remove(msg.getToUser().getId());
        if(DataBuffer.onlineUsersMap.isEmpty()){
            DataBuffer.onlineUsersMap.put("adminTest", new User("Ŀǰ��������"));
        }
        ServerInfoFrame.refresh();

        OnlineClientIOCache io = DataBuffer.onlineUserIOCacheMap.get(msg.getToUser().getId());
        sendResponse_sys(io, response);
    }

    /**
     * �㲥
     */
    public static void board(String str) throws IOException {
        User user = new User("admin");
        Message msg = new Message();
        msg.setFromUser(user);
        msg.setSendTime(new Date());

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(" ").append(dateFormat.format(msg.getSendTime()))
                .append(" ").append("ϵͳ֪ͨ\n " + str + "\n");
        msg.setMessage(stringBuffer.toString());

        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.BOARD);
        response.setData("txtMsg", msg);

        for (String id : DataBuffer.onlineUserIOCacheMap.keySet()){
            sendResponse_sys(DataBuffer.onlineUserIOCacheMap.get(id), response);
        }
    }

    private void sendResponse(OnlineClientIOCache onlineUserIO, Response response) throws IOException{
        ObjectOutputStream oos = onlineUserIO.getObjectOutputStream();
        oos.writeObject(response);
        oos.flush();
    }

    /**
     * ��ָ���ͻ���IO������������ָ����Ӧ
     */
    private static void sendResponse_sys(OnlineClientIOCache onlineUserIO, Response response)throws IOException {
        ObjectOutputStream oos = onlineUserIO.getObjectOutputStream();
        oos.writeObject(response);
        oos.flush();
    }
}

