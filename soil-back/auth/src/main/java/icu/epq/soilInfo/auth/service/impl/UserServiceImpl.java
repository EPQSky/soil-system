package icu.epq.soilInfo.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.epq.soilInfo.auth.entity.User;
import icu.epq.soilInfo.auth.mapper.UserMapper;
import icu.epq.soilInfo.auth.service.IUserService;
import org.springframework.stereotype.Service;

/**
 * 服务实现类
 *
 * @author EPQ
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
}
