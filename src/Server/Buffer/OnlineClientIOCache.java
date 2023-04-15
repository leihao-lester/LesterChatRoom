package Server.Buffer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * ���߿ͻ��˵�IO��������
 */
public class OnlineClientIOCache {
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    public OnlineClientIOCache(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream){
        this.objectInputStream = objectInputStream;
        this.objectOutputStream = objectOutputStream;
    }

    public ObjectInputStream getObjectInputStream() {
        return objectInputStream;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }
}