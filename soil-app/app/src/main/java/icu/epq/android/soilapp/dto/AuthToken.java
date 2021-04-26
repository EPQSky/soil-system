package icu.epq.android.soilapp.dto;

import java.io.Serializable;

/**
 * 用户 token 实体类
 *
 * @author EPQ
 */
public class AuthToken implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
