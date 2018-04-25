package com.jabin.core.sso.session;

/**
 * 单点登录shiro认证处理类
 *
 * @author zhangbbj
 * @date 2017/12/05 16:57
 **/
public  enum SsoSessionKey {
    User("User", "用户信息"),
    NeedUpdate("NeedUpdate", "是否需要更新Session"),
    ;

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getSessionDesc() {
        return sessionDesc;
    }

    public void setSessionDesc(String sessionDesc) {
        this.sessionDesc = sessionDesc;
    }

    private String sessionKey;
    private String sessionDesc;

    SsoSessionKey(String sessionKey, String sessionDesc){
        this.sessionDesc=sessionDesc;
        this.sessionKey=sessionKey;
    }




}
