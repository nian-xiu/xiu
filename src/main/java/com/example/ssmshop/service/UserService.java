package com.example.ssmshop.service;

import com.example.ssmshop.domain.User;
import com.example.ssmshop.form.PasswordForm;
import com.example.ssmshop.form.ProfileForm;
import com.example.ssmshop.form.RegisterForm;
import com.example.ssmshop.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final SecurityService securityService;
    private final RewardCenterService rewardCenterService;

    public UserService(UserMapper userMapper, SecurityService securityService,
                       @org.springframework.context.annotation.Lazy RewardCenterService rewardCenterService) {
        this.userMapper = userMapper;
        this.securityService = securityService;
        this.rewardCenterService = rewardCenterService;
    }

    public User findById(Long id) {
        return userMapper.findById(id);
    }

    @Transactional
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            return null;
        }
        if (!securityService.matches(username, password, user.getPasswordHash())) {
            return null;
        }
        if (securityService.needsRehash(user.getPasswordHash())) {
            String upgradedHash = securityService.hashPassword(username, password);
            userMapper.updatePassword(user.getId(), upgradedHash);
            user.setPasswordHash(upgradedHash);
        }
        return withoutPasswordHash(user);
    }

    @Transactional
    public User register(RegisterForm form) {
        if (userMapper.findByUsername(form.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User();
        user.setUsername(form.getUsername());
        user.setPasswordHash(securityService.hashPassword(form.getUsername(), form.getPassword()));
        user.setNickname(form.getNickname());
        user.setPhone(form.getPhone());
        user.setEmail(form.getEmail());
        user.setRole("CUSTOMER");
        user.setStatus("ACTIVE");
        userMapper.insert(user);
        try {
            rewardCenterService.grantWelcomeCoupon(user.getId());
        } catch (RuntimeException ignored) {
            // welcome coupon failure must not block registration
        }
        return withoutPasswordHash(user);
    }

    public List<User> findAll() {
        return userMapper.findAll();
    }

    @Transactional
    public User updateProfile(Long userId, ProfileForm form) {
        User current = userMapper.findById(userId);
        boolean usernameChanged = !current.getUsername().equals(form.getUsername());
        User sameUsername = userMapper.findByUsername(form.getUsername());
        if (sameUsername != null && !sameUsername.getId().equals(userId)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (usernameChanged) {
            if (form.getCurrentPassword() == null || form.getCurrentPassword().isBlank()) {
                throw new IllegalArgumentException("修改用户名需要填写当前密码");
            }
            if (!securityService.matches(current.getUsername(), form.getCurrentPassword(), current.getPasswordHash())) {
                throw new IllegalArgumentException("当前密码不正确");
            }
            current.setPasswordHash(securityService.hashPassword(form.getUsername(), form.getCurrentPassword()));
            userMapper.updatePassword(userId, current.getPasswordHash());
        }
        current.setUsername(form.getUsername());
        current.setNickname(form.getNickname());
        current.setPhone(form.getPhone());
        current.setEmail(form.getEmail());
        userMapper.updateProfile(current);
        return withoutPasswordHash(userMapper.findById(userId));
    }

    @Transactional
    public User changePassword(Long userId, PasswordForm form) {
        User current = userMapper.findById(userId);
        if (!securityService.matches(current.getUsername(), form.getCurrentPassword(), current.getPasswordHash())) {
            throw new IllegalArgumentException("当前密码不正确");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("两次输入的新密码不一致");
        }
        userMapper.updatePassword(userId, securityService.hashPassword(current.getUsername(), form.getNewPassword()));
        return withoutPasswordHash(userMapper.findById(userId));
    }

    public void updateStatus(Long id, String status) {
        User target = userMapper.findById(id);
        if (target == null) {
            return;
        }
        if ("ADMIN".equals(target.getRole())) {
            throw new IllegalArgumentException("管理员账号不能被停用");
        }
        userMapper.updateStatus(id, status);
    }

    private User withoutPasswordHash(User user) {
        if (user != null) {
            user.setPasswordHash(null);
        }
        return user;
    }
}
