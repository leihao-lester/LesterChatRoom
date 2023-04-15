package Client.View;

import Client.Buffer.DataBuffer;
import Client.Controller.ClientThread;
import Client.Util.ClientUtil;
import Common.Model.Entity.Message;
import Common.Model.Entity.Request;
import Common.Model.Entity.User;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatFrame extends Application {
    private static final long serialVersionUID = -2310785591507878535L;
    public static TextArea allMsgArea;
    public static TableView< User> onlineTableView;
    public static TextField myMsgText;
    public Button sendButton;
//    public Button send2OneButton;
    public RadioButton chat21Button;

    @Override
    public void start(Stage stage) throws Exception {
        Group root = new Group();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("聊天室 (" + DataBuffer.currentUser.getId() + ")");
        stage.setHeight(500);
        stage.setWidth(600);

        allMsgArea = new TextArea();
        allMsgArea.setLayoutX(20);
        allMsgArea.setLayoutY(10);
        allMsgArea.setPrefHeight(400);
        allMsgArea.setPrefWidth(400);
        allMsgArea.setFocusTraversable(false);
        allMsgArea.setFont(Font.font(14));
        allMsgArea.setText("欢迎来到聊天室\n");
        allMsgArea.setEditable(false);
        root.getChildren().add(allMsgArea);

        onlineTableView = new TableView<>();
        onlineTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        onlineTableView.setLayoutX(430);
        onlineTableView.setLayoutY(10);
        onlineTableView.setPrefHeight(400);
        onlineTableView.setPrefWidth(140);
//        onlineTableView.setEditable(false);
        TableColumn<User, String> colId = new TableColumn<>("在线用户列表");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        onlineTableView.getColumns().add(colId);
        onlineTableView.setItems(FXCollections.observableArrayList(DataBuffer.onlineUsers));
        onlineTableView.setFocusTraversable(false);
        root.getChildren().add(onlineTableView);

        myMsgText = new TextField();
        myMsgText.setLayoutX(20);
        myMsgText.setLayoutY(420);
        myMsgText.setPrefWidth(400);
        myMsgText.setFont(Font.font(14));
        myMsgText.setOnKeyPressed(keyEvent -> { // 按下回车发送
            if(keyEvent.getCode() == KeyCode.ENTER){
                sendTxtMsg();
            }
        });
        root.getChildren().add(myMsgText);

        sendButton = new Button("发送");
        sendButton.setLayoutX(430);
        sendButton.setLayoutY(420);
        sendButton.setFont(Font.font(14));
        sendButton.setPrefWidth(65);
        sendButton.setOnAction(actionEvent -> {
            sendTxtMsg();
        });
        root.getChildren().add(sendButton);

        chat21Button = new RadioButton("私聊");
        chat21Button.setLayoutX(505);
        chat21Button.setLayoutY(425);
        chat21Button.setFont(Font.font(14));
        chat21Button.setOnAction(actionEvent -> {
            if(chat21Button.isSelected()){
                User selectedUser = (User) onlineTableView.getSelectionModel().getSelectedItem();
                if(null == selectedUser){
                    Alert error = new Alert(Alert.AlertType.ERROR, "选中在线用户列表中某个用户进行私聊");
                    error.setTitle("私聊失败");
                    error.showAndWait();
                } else if (DataBuffer.currentUser.getId() == selectedUser.getId()) {
                    Alert error = new Alert(Alert.AlertType.ERROR, "想自言自语？...系统不允许");
                    error.setTitle("私聊失败");
                    error.showAndWait();
                } else {
                    // todo
                }
            }
        });
        root.getChildren().add(chat21Button);

//        send2OneButton = new Button("私聊");
//        send2OneButton.setLayoutX(505);
//        send2OneButton.setLayoutY(420);
//        send2OneButton.setFont(Font.font(14));
//        send2OneButton.setPrefWidth(65);
//        send2OneButton.setOnAction(actionEvent -> {
//            sendTxtMsg();
//        });
//        root.getChildren().add(send2OneButton);

        stage.setOnCloseRequest(windowEvent -> {
            logout();
        });
        stage.show();
        loadData();
    }

    public void loadData(){
        // 设置在线用户列表 todo
        // 启动监听服务器消息的线程
        new ClientThread(this).start();
    }

    public void sendTxtMsg(){
        String content = myMsgText.getText();
        if("".equals(content)){// 无内容
            Alert error = new Alert(Alert.AlertType.ERROR, "不能发送");
            error.setTitle("不能发送空消息");
            error.showAndWait();
        } else { // 发送
            User selectedUser = (User) onlineTableView.getSelectionModel().getSelectedItem();
            Message msg = new Message();
            if(chat21Button.isSelected()){
                if(null == selectedUser) {
                    Alert error = new Alert(Alert.AlertType.ERROR, "没有选择私聊对象");
                    error.setTitle("私聊失败");
                    error.showAndWait();
                    return;
                } else if (DataBuffer.currentUser.getId() == selectedUser.getId()){
                    Alert error = new Alert(Alert.AlertType.ERROR, "不能给自己发消息");
                    error.setTitle("私聊失败");
                    error.showAndWait();
                    return;
                } else {
                    msg.setToUser(selectedUser);
                }
            }
            msg.setFromUser(DataBuffer.currentUser);
            msg.setSendTime(new Date());
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(" ").append(dateFormat.format(msg.getSendTime())).append(" ")
                    .append(msg.getFromUser().getId());
            if(chat21Button.isSelected()){
                stringBuffer.append("对" + selectedUser.getId() +"说:\n");
            }else {
                stringBuffer.append(" 对大家说:\n ");
            }
            stringBuffer.append(content).append("\n");
            msg.setMessage(stringBuffer.toString());

            Request request = new Request();
            request.setAction("chat");
            request.setAttribute("msg", msg);
            try {
                ClientUtil.sendTextRequest2(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
            myMsgText.setText("");
            if(chat21Button.isSelected()){
                ClientUtil.appendTxt2MsgListArea(msg.getMessage());
            }
        }
    }

    public void logout(){
        Request request = new Request();
        request.setAction("exit");
        request.setAttribute("user", DataBuffer.currentUser);
        try {
            ClientUtil.sendTextRequest(request);
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    /**
     * 踢人
     */
    public static void remove(){
        Request request = new Request();
        request.setAction("exit");
        request.setAttribute("user", DataBuffer.currentUser);
        try {
            ClientUtil.sendTextRequest(request);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}
