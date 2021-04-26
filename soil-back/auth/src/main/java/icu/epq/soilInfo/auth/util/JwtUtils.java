package icu.epq.soilInfo.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * jwt 工具类
 *
 * @author EPQ
 */
public class JwtUtils {

    private static final String KEY = "swept9527";

    /**
     * 创建 jwt token
     *
     * @param id
     * @param username
     * @return
     */
    public static String createToken(String id, String username) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("alg", "HS256");
        map.put("typ", "JWT");
        return JWT.create().withHeader(map).withClaim(id, username).sign(Algorithm.HMAC256(KEY));
    }

}
