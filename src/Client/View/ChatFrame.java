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
        stage.setTitle("������ (" + DataBuffer.currentUser.getId() + ")");
        stage.setHeight(500);
        stage.setWidth(600);

        allMsgArea = new TextArea();
        allMsgArea.setLayoutX(20);
        allMsgArea.setLayoutY(10);
        allMsgArea.setPrefHeight(400);
        allMsgArea.setPrefWidth(400);
        allMsgArea.setFocusTraversable(false);
        allMsgArea.setFont(Font.font(14));
        allMsgArea.setText("��ӭ����������\n");
        allMsgArea.setEditable(false);
        root.getChildren().add(allMsgArea);

        onlineTableView = new TableView<>();
        onlineTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        onlineTableView.setLayoutX(430);
        onlineTableView.setLayoutY(10);
        onlineTableView.setPrefHeight(400);
        onlineTableView.setPrefWidth(140);
//        onlineTableView.setEditable(false);
        TableColumn<User, String> colId = new TableColumn<>("�����û��б�");
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
        myMsgText.setOnKeyPressed(keyEvent -> { // ���»س�����
            if(keyEvent.getCode() == KeyCode.ENTER){
                sendTxtMsg();
            }
        });
        root.getChildren().add(myMsgText);

        sendButton = new Button("����");
        sendButton.setLayoutX(430);
        sendButton.setLayoutY(420);
        sendButton.setFont(Font.font(14));
        sendButton.setPrefWidth(65);
        sendButton.setOnAction(actionEvent -> {
            sendTxtMsg();
        });
        root.getChildren().add(sendButton);

        chat21Button = new RadioButton("˽��");
        chat21Button.setLayoutX(505);
        chat21Button.setLayoutY(425);
        chat21Button.setFont(Font.font(14));
        chat21Button.setOnAction(actionEvent -> {
            if(chat21Button.isSelected()){
                User selectedUser = (User) onlineTableView.getSelectionModel().getSelectedItem();
                if(null == selectedUser){
                    Alert error = new Alert(Alert.AlertType.ERROR, "ѡ�������û��б���ĳ���û�����˽��");
                    error.setTitle("˽��ʧ��");
                    error.showAndWait();
                } else if (DataBuffer.currentUser.getId() == selectedUser.getId()) {
                    Alert error = new Alert(Alert.AlertType.ERROR, "���������...ϵͳ������");
                    error.setTitle("˽��ʧ��");
                    error.showAndWait();
                } else {
                    // todo
                }
            }
        });
        root.getChildren().add(chat21Button);

//        send2OneButton = new Button("˽��");
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
        // ���������û��б� todo
        // ����������������Ϣ���߳�
        new ClientThread(this).start();
    }

    public void sendTxtMsg(){
        String content = myMsgText.getText();
        if("".equals(content)){// ������
            Alert error = new Alert(Alert.AlertType.ERROR, "���ܷ���");
            error.setTitle("���ܷ��Ϳ���Ϣ");
            error.showAndWait();
        } else { // ����
            User selectedUser = (User) onlineTableView.getSelectionModel().getSelectedItem();
            Message msg = new Message();
            if(chat21Button.isSelected()){
                if(null == selectedUser) {
                    Alert error = new Alert(Alert.AlertType.ERROR, "û��ѡ��˽�Ķ���");
                    error.setTitle("˽��ʧ��");
                    error.showAndWait();
                    return;
                } else if (DataBuffer.currentUser.getId() == selectedUser.getId()){
                    Alert error = new Alert(Alert.AlertType.ERROR, "���ܸ��Լ�����Ϣ");
                    error.setTitle("˽��ʧ��");
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
                stringBuffer.append("��" + selectedUser.getId() +"˵:\n");
            }else {
                stringBuffer.append(" �Դ��˵:\n ");
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
     * ����
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
