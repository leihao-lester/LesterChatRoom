package Server.View;

import Common.Model.Entity.Request;
import Common.Model.Entity.User;
import Server.Buffer.DataBuffer;
import Server.Controller.RequestProcessor;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.ArrayList;


public class ServerInfoFrame extends Application {
    private static final long serialVersionUID = 6274443611957724780L;

    public TextField removeUserId;
    public TextField sendMessage;
    public static TableView<User> onlineTableView;

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("服务器");
        stage.setHeight(420);
        stage.setWidth(360);

        onlineTableView = new TableView<>();
        onlineTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        onlineTableView.setPrefWidth(345);
        onlineTableView.setPrefHeight(300);
        TableColumn<User, String> colId = new TableColumn<>("在线用户列表");
        colId.setStyle( "-fx-alignment: CENTER;");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        onlineTableView.getColumns().add(colId);
        System.out.println(DataBuffer.onlineUsersMap.values());
        // todo
//        if(!DataBuffer.onlineUsersMap.values().isEmpty()){
//            onlineTableView.setItems(FXCollections.observableArrayList(DataBuffer.onlineUsersMap.values()));
//        } else {
//            onlineTableView.setItems(FXCollections.observableArrayList(new ArrayList<User>()));
//        }
//        onlineTableView.getItems().add(new User("目前无人在线"));
        DataBuffer.onlineUsersMap.put("adminTest",new User("目前无人在线"));
        onlineTableView.setItems(FXCollections.observableArrayList(DataBuffer.onlineUsersMap.values()));

//        onlineTableView.getItems().addAll(DataBuffer.onlineUsersMap.values());
//        onlineTableView.getItems().add(new User("lester"));
//        onlineTableView.getItems().add(new User("sylvia"));
        onlineTableView.setFocusTraversable(false);
        root.getChildren().add(onlineTableView);

        Label removeUserLabel = new Label("输入用户名：");
        removeUserLabel.setLayoutX(10);
        removeUserLabel.setLayoutY(315);
        removeUserLabel.setPrefHeight(14);
        removeUserLabel.setFont(Font.font(14));
        removeUserLabel.setFocusTraversable(false);
        root.getChildren().add(removeUserLabel);

        removeUserId = new TextField();
        removeUserId.setLayoutX(95);
        removeUserId.setLayoutY(310);
        removeUserId.setPrefWidth(150);
        removeUserId.setFont(Font.font(14));
        removeUserId.setFocusTraversable(false);
        root.getChildren().add(removeUserId);

        Button removeButton = new Button("踢出群聊");
        removeButton.setLayoutX(250);
        removeButton.setLayoutY(310);
        removeButton.setPrefWidth(90);
        removeButton.setPrefHeight(10);
        removeButton.setFont(Font.font(14));
        removeButton.setFocusTraversable(false);
        removeButton.setOnAction(actionEvent -> {
            try {
                RequestProcessor.remove(DataBuffer.onlineUsersMap.get(removeUserId.getText()));
                removeUserId.setText("");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        root.getChildren().add(removeButton);

        Label sendMessageLabel = new Label("输入消息：");
        sendMessageLabel.setLayoutX(10);
        sendMessageLabel.setLayoutY(350);
        sendMessageLabel.setPrefHeight(14);
        sendMessageLabel.setFont(Font.font(14));
        sendMessageLabel.setFocusTraversable(false);
        root.getChildren().add(sendMessageLabel);

        sendMessage = new TextField();
        sendMessage.setLayoutX(95);
        sendMessage.setLayoutY(345);
        sendMessage.setPrefWidth(150);
        sendMessage.setFont(Font.font(14));
        sendMessage.setFocusTraversable(false);
        root.getChildren().add(sendMessage);

        Button sendButton = new Button("群发");
        sendButton.setLayoutX(250);
        sendButton.setLayoutY(345);
        sendButton.setPrefWidth(90);
        sendButton.setPrefHeight(10);
        sendButton.setFont(Font.font(14));
        sendButton.setFocusTraversable(false);
        sendButton.setOnAction(actionEvent -> {
            try {
                sendAllMsg(sendMessage.getText());
                sendMessage.setText("");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        root.getChildren().add(sendButton);

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                logout();
            }
        });

        stage.show();
    }


    private void sendAllMsg(String msg) throws IOException{
        RequestProcessor.board(msg);
    }

    private void logout(){
        System.out.println("服务器关闭");
        System.exit(0);
    }

    public static void refresh(){
        onlineTableView.getItems().clear();
//        for(var item: DataBuffer.onlineUsersMap.values()){
//            System.out.println("test refresh:" + item.getId());
//        }
        onlineTableView.getItems().addAll(DataBuffer.onlineUsersMap.values());
        onlineTableView.refresh();
    }
}
