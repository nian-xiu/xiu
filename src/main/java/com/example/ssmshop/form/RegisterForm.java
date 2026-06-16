package com.example.ssmshop.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterForm {
    @NotBlank(message = "请输入用户名")
    @Size(min = 3, max = 30, message = "用户名长度为 3-30 位")
    private String username;

    @NotBlank(message = "请输入密码")
    @Size(min = 6, max = 40, message = "密码至少 6 位")
    private String password;

    @NotBlank(message = "请输入昵称")
    private String nickname;

    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
