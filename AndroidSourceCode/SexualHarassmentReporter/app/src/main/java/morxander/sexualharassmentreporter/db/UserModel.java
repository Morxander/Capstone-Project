package morxander.sexualharassmentreporter.db;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

/**
 * Created by morxander on 2/28/16.
 */
@Table(name = "user")
public class UserModel extends Model {

    @Column(name = "user_id")
    int user_id;

    @Column(name = "api_token")
    String api_token;

    @Column(name = "email")
    String email;

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getApi_token() {
        return api_token;
    }

    public void setApi_token(String api_token) {
        this.api_token = api_token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public static UserModel getCurrentUser() {
        return new Select().from(UserModel.class).limit(1).executeSingle();
    }

    public static void logout(){
        new Delete().from(UserModel.class).execute();
    }

}
