package cc.wang1.frp.mapper.service;

import cc.wang1.frp.entity.User;
import cc.wang1.frp.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;


@Repository
public class UserMapperService extends ServiceImpl<UserMapper, User> {

    public User validateAccessToken(String accessToken) {
        LambdaQueryWrapper<User> condition = Wrappers.lambdaQuery(User.class).eq(User::getAccessToken, accessToken);
        return this.getOne(condition);
    }
}
