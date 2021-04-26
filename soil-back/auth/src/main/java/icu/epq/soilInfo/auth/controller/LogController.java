package icu.epq.soilInfo.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import icu.epq.soilInfo.auth.entity.User;
import icu.epq.soilInfo.auth.service.IUserService;
import icu.epq.soilInfo.auth.util.JwtUtils;
import icu.epq.soilInfo.common.tool.R;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 *
 * @author EPQ
 */
@RestController
@AllArgsConstructor
public class LogController {

    private StringRedisTemplate redisTemplate;

    private IUserService userService;

    /**
     * 登录
     *
     * @param user
     * @return
     */
    @PostMapping("/login")
    public R<Map<String, String>> login(@RequestBody User user) {
        if (user == null || StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())) {
            return R.fail("登录失败");
        }

        User myUser = userService.getOne(new QueryWrapper<User>().lambda().eq(User::getUsername, user.getUsername()));

        if (myUser == null || !user.getPassword().equals(myUser.getPassword())) {
            return R.fail("登录失败");
        }

        // token 保存到 redis
        String token = JwtUtils.createToken(String.valueOf(myUser.getId()), myUser.getUsername());
        redisTemplate.opsForHash().put("token_cache", myUser.getUsername(), token);

        Map<String, String> map = new HashMap<>(1);
        map.put("token", token);

        return R.data(map);
    }

    /**
     * 注册
     *
     * @param user
     * @return
     */
    @PostMapping("/logUp")
    public R<String> logUp(@RequestBody User user) {
        if (user == null || StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())) {
            return R.fail("注册失败");
        }

        if (userService.count(new QueryWrapper<User>().lambda().eq(User::getUsername, user.getUsername())) > 0) {
            return R.fail("用户已存在");
        }

        if (userService.save(user)) {
            return R.success("注册成功");
        }
        return R.fail("注册失败");
    }

}
