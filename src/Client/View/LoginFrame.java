package Client.View;

import Client.Buffer.DataBuffer;
import Client.Util.ClientUtil;
import Common.Model.Entity.Request;
import Common.Model.Entity.Response;
import Common.Model.Entity.ResponseStatus;
import Common.Model.Entity.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.List;


public class LoginFrame extends Application {
    private TextField id;

    @Override
    public void start(Stage stage)  {
        Group root = new Group();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("������");
        stage.setHeight(200);
        stage.setWidth(360);

        Image userImg = new Image("file:img/user.png");
        ImageView imageView = new ImageView();
        imageView.setImage(userImg);
        imageView.setLayoutX(20);
        imageView.setLayoutY(20);
        imageView.setFitHeight(130);
        imageView.setFitWidth(130);
        root.getChildren().add(imageView);

        Label loginLabel = new Label("�������û���");
        loginLabel.setLayoutX(150);
        loginLabel.setLayoutY(30);
        loginLabel.setFont(Font.font(15));
        root.getChildren().add(loginLabel);

        id = new TextField();
        id.setLayoutX(150);
        id.setLayoutY(60);
        id.setFont(Font.font(15));
        id.setPrefWidth(165);
        root.getChildren().add(id);

        Button loginButton = new Button("����������");
        loginButton.setLayoutX(220);
        loginButton.setLayoutY(100);
        loginButton.setFont(Font.font(15));
        loginButton.setOnAction(actionEvent -> {
            login(stage);
        });
        root.getChildren().add(loginButton);

        stage.show();
    }

    private void login(Stage stage){
        if(id.getText().length() == 0){
            Alert error = new Alert(Alert.AlertType.ERROR, "�������û���");
            error.setTitle("�û����Ƿ�");
            error.showAndWait();
        }

        Request request = new Request();
        request.setAction("login");
        request.setAttribute("id", id.getText());

        Response response = null;
        try{
            response = ClientUtil.sendTextRequest(request);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        if(response.getStatus() == ResponseStatus.OK){
            // ��ȡ��ǰ�û�
            User user = (User) response.getData("user");
            if(user != null){ // ��¼�ɹ�
                DataBuffer.currentUser = user;
                // ��ȡ��ǰ�����û��б�
                DataBuffer.onlineUsers = (List<User>) response.getData("onlineUsers");
                Platform.runLater(() ->{
                    // �رյ�ǰ����
                    stage.hide();
                    // �����촰��
                    ChatFrame chatFrame = new ChatFrame();
                    try {
                        chatFrame.start(stage);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

            } else{ // ��¼ʧ��
                String str = (String) response.getData("msg");
                Alert error = new Alert(Alert.AlertType.ERROR, str);
                error.setTitle("��¼ʧ��");
                error.showAndWait();
            }
        } else{
            Alert error = new Alert(Alert.AlertType.ERROR, "�������ڲ��������Ժ�����");
            error.setTitle("��¼ʧ��");
            error.showAndWait();
        }
    }
}
