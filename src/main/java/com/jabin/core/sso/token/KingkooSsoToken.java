package com.jabin.core.sso.token;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * 单点登录token处理类
 *
 * @author zhangbbj
 * @date 2017/12/05 16:57
 **/
public class KingkooSsoToken extends UsernamePasswordToken {
    private static final long serialVersionUID = 1L;
    private String ticket = null;
    private String userId = null;
    private boolean isRememberMe = false;

    public KingkooSsoToken(String ticket) {
        this.ticket = ticket;
    }

    @Override
    public Object getPrincipal() {
        return this.userId;
    }

    @Override
    public Object getCredentials() {
        return this.ticket;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean isRememberMe() {
        return this.isRememberMe;
    }

    @Override
    public void setRememberMe(boolean isRememberMe) {
        this.isRememberMe = isRememberMe;
    }
}
