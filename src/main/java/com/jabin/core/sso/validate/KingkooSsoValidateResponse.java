package com.jabin.core.sso.validate;

import java.io.Serializable;

/**
 * 凭据验证结果
 *
 * @author zhangbbj
 * @date 2017/12/06 23:42
 **/
public class KingkooSsoValidateResponse implements Serializable {
    private boolean success = false;
    private String userInfo;

    public KingkooSsoValidateResponse() {}

    public KingkooSsoValidateResponse(String userInfo) {
        this.success = true;
        this.userInfo = userInfo;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }
}
