package loadbalancer;

import java.io.Serializable;

/**
 * Created by lobous on 18-10-31.
 */
public class LoadBalancerInfo implements Serializable{
    public String username;
    public String password;

    public LoadBalancerInfo(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
