package Common.Model.Entity;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 5942011574971970871L;
    private String id;
    public User(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean equals(Object obj){// 处理remove ArrayList对象
        if(obj == null) return false;
        if(obj == this) return true;
        if(!(obj instanceof User)) return false;
        User user = (User) obj;
        return this.id.equals(user.getId());
    }
}
