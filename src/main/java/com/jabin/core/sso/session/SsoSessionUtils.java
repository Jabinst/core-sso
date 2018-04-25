package com.jabin.core.sso.session;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

/**
 * 单点登录模块，本地会话信息处理类
 *
 * @author zhangbbj
 * @date 2017/12/05 17:32
 **/
public class SsoSessionUtils {

    public static <T> T getUserDto() {
        T userDto = null;
        userDto = null;
        Subject user = SecurityUtils.getSubject();
        if (user != null && user.getSession() != null) {
            userDto = (T)user.getSession().getAttribute(SsoSessionKey.User.getSessionKey());
        }
        return userDto;
    }

}
