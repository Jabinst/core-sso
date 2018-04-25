package com.jabin.core.sso.login;

/**
 * 单点登录状态信息
 *
 * @author zhangbbj
 * @date 2017/12/05 17:57
 **/
public class SsoLoginState {
    private boolean isLogin = false;
    private Object object = null;
    private String redirectURL;
    private String requestURL;
    private String queryString;

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }
}
