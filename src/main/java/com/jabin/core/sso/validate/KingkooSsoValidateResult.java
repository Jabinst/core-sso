package com.jabin.core.sso.validate;

import java.io.Serializable;

/**
 * 凭据验证结果
 *
 * @author zhangbbj
 * @date 2017/12/06 23:42
 **/
public class KingkooSsoValidateResult<T> implements Serializable {
    private boolean success = false;
    private T user;

    KingkooSsoValidateResult() {}

    KingkooSsoValidateResult(T user) {
        success = true;
        this.user = user;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getUser() {
        return user;
    }

    public void setUser(T user) {
        this.user = user;
    }
}
