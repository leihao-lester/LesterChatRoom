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
    // 正在请求服务器的客户端Socket
    private Socket currentClientSocket;


    public RequestProcessor(Socket currentClientSocket){
        this.currentClientSocket = currentClientSocket;
    }


    @Override
    public void run() {
        // 是否不间断监听
        boolean flag = true;
        try {
            OnlineClientIOCache currentClientIOCache = new OnlineClientIOCache(
                    new ObjectInputStream(currentClientSocket.getInputStream()),
                    new ObjectOutputStream(currentClientSocket.getOutputStream()));
            // 不断地读取客户端发过来的请求
            while (flag){
                Request request = (Request)currentClientIOCache.getObjectInputStream().readObject();
                System.out.println("Server读取了客户端的请求:" + request.getAction());
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
     * 登录
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
            System.out.println(id + "进入聊天室");
            // 添加到在线用户
            DataBuffer.onlineUsersMap.put(id, user);
            DataBuffer.onlineUsersMap.remove("adminTest");
//            if(currentWindow.onlineTableView != null){
//                currentWindow.onlineTableView.setItems(FXCollections.observableArrayList(DataBuffer.onlineUsersMap.values()));
//                currentWindow.onlineTableView.getItems().clear();
//                currentWindow.onlineTableView.getItems().addAll(DataBuffer.onlineUsersMap.values());
//                currentWindow.onlineTableView.refresh();
                ServerInfoFrame.refresh();
//            }

            // 设置在线用户
            response.setData("onlineUsers",
                    new CopyOnWriteArrayList<User>(DataBuffer.onlineUsersMap.values()));
            response.setStatus(ResponseStatus.OK);
            response.setData("user", user);
            currentClientIO.getObjectOutputStream().writeObject(response);
            currentClientIO.getObjectOutputStream().flush();
            // 通知其他用户有人上线了
            Response response2 = new Response();
            response2.setType(ResponseType.LOGIN);
            response2.setData("loginUser", user);
            iteratorResponse(response2);

            // 把当前上线的用户的IO添加到缓存map中
            DataBuffer.onlineUserIOCacheMap.put(id, currentClientIO);
        }else {
            // 登录失败
            response.setStatus(ResponseStatus.OK);
            response.setData("msg", "id已被占用");
            currentClientIO.getObjectOutputStream().writeObject(response);
            currentClientIO.getObjectOutputStream().flush();
        }
    }

    /**
     * 给所有用户都发送响应
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
                + ":" + currentClientSocket.getPort() + "离开聊天室");
        User user = (User) request.getAttribute("user");
        // 把当前上线客户端的IO从Map中删除
        DataBuffer.onlineUserIOCacheMap.remove(user.getId());
        // 从在线用户缓存Map中删除当前用户
        DataBuffer.onlineUsersMap.remove(user.getId());
        if(DataBuffer.onlineUsersMap.isEmpty()){
            DataBuffer.onlineUsersMap.put("adminTest", new User("目前无人在线"));
        }
        ServerInfoFrame.refresh();

        // 创建一个响应对象
        Response response = new Response();
        response.setType(ResponseType.LOGOUT);
//        System.out.println("test:" + user.getId() + "\n");
        response.setData("logoutUser", user);
        // 把响应对象往客户端写
        oio.getObjectOutputStream().writeObject(response);
        oio.getObjectOutputStream().flush();
        // 关闭这个客户端Socket
        currentClientSocket.close();
        // 通知其他在线客户端
        iteratorResponse(response);
        // 断开监听
        return false;
    }

    public void chat(Request request) throws IOException{
        Message msg = (Message)request.getAttribute("msg");
        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.CHAT);
        response.setData("txtMsg", msg);

        if(msg.getToUser() != null){
            // 私聊：只给私聊的对象返回响应
            OnlineClientIOCache io = DataBuffer.onlineUserIOCacheMap.get(msg.getToUser().getId());
            sendResponse(io, response);
        }else {
            // 群聊：给除了发消息的所有客户端返回响应
            for(String id : DataBuffer.onlineUserIOCacheMap.keySet()){
                if(msg.getFromUser().getId() == id){
                    continue;
                }
                sendResponse(DataBuffer.onlineUserIOCacheMap.get(id), response);
            }
        }
    }

    /**
     * 踢人
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
                .append(" ").append("系统通知您\n " + "您被强制下线\n");
        msg.setMessage(stringBuffer.toString());

        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.REMOVE);
        response.setData("txtMsg", msg);

        DataBuffer.onlineUsersMap.remove(msg.getToUser().getId());
        if(DataBuffer.onlineUsersMap.isEmpty()){
            DataBuffer.onlineUsersMap.put("adminTest", new User("目前无人在线"));
        }
        ServerInfoFrame.refresh();

        OnlineClientIOCache io = DataBuffer.onlineUserIOCacheMap.get(msg.getToUser().getId());
        sendResponse_sys(io, response);
    }

    /**
     * 广播
     */
    public static void board(String str) throws IOException {
        User user = new User("admin");
        Message msg = new Message();
        msg.setFromUser(user);
        msg.setSendTime(new Date());

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(" ").append(dateFormat.format(msg.getSendTime()))
                .append(" ").append("系统通知\n " + str + "\n");
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
     * 向指定客户端IO的输出流中输出指定响应
     */
    private static void sendResponse_sys(OnlineClientIOCache onlineUserIO, Response response)throws IOException {
        ObjectOutputStream oos = onlineUserIO.getObjectOutputStream();
        oos.writeObject(response);
        oos.flush();
    }
}

