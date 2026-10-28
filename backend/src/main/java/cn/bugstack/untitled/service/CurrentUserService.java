package cn.bugstack.untitled.service;

import cn.bugstack.untitled.api.BusinessException;
import cn.bugstack.untitled.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserMapper userMapper;

    public CurrentUserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public long requireUserId(String externalKey) {
        String normalizedKey = externalKey == null || externalKey.isBlank() ? "demo-user" : externalKey.trim();
        Long userId = userMapper.findIdByExternalKey(normalizedKey);
        if (userId == null) {
            throw new BusinessException("USER_NOT_FOUND", "演示用户不存在", HttpStatus.UNAUTHORIZED);
        }
        return userId;
    }
}
