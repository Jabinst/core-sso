package com.jabin.core.sso.login;

import com.alibaba.fastjson.JSONObject;
import com.jabin.core.sso.SsoConstants;
import com.jabin.core.sso.cache.CacheUtil;
import com.jabin.core.sso.validate.KingkooSsoValidateResponse;
import org.apache.shiro.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * 单点登录，全局会话信息(SSO认证中心所管理的会话)处理模块
 *
 * @author zhangbbj
 * @date 2017/12/05 17:47
 **/
public class SsoLoginUtil {

    public static SsoLoginState validate(HttpServletRequest request) {
        SsoLoginState ssoLoginState = new SsoLoginState();
        String ticket = "";
        Object user = null;
        String requestURL = request.getParameter("requestURL");
        String queryString = request.getParameter("queryString");
        ssoLoginState.setRequestURL(requestURL);
        ssoLoginState.setQueryString(queryString);

        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (SsoConstants.SSO_TICKET.equals(cookie.getName())) {
                    ticket = cookie.getValue();
                }
            }
        }
        if (StringUtils.hasText(ticket)) {
            user = CacheUtil.getObject(ticket);
        }
        if (user != null) {
            ssoLoginState.setLogin(true);
            ssoLoginState.setRedirectURL(getRedirectURL(requestURL, queryString, ticket));
        }

        return ssoLoginState;
    }

    public static KingkooSsoValidateResponse validateTicket(HttpServletRequest request) {
        String ticket = request.getParameter(SsoConstants.SSO_TICKET);
        if (StringUtils.hasText(ticket)) {
            Object user = CacheUtil.getObject(ticket);
            if (user != null) {
                return new KingkooSsoValidateResponse(JSONObject.toJSONString(user));
            }
        }
        return new KingkooSsoValidateResponse();
    }

    public static SsoLoginState login(HttpServletRequest request, HttpServletResponse response, Object user) {
        SsoLoginState ssoLoginState = new SsoLoginState();
        String ticket = UUID.randomUUID().toString();

        Cookie cookie = new Cookie(SsoConstants.SSO_TICKET, ticket);
        cookie.setMaxAge(60 * 60 * 12);//保留0.5天
        cookie.setPath("/");
        response.addCookie(cookie);

        CacheUtil.setObjectByKey(ticket, user, 60 * 60 * 12);

        String requestURL = request.getParameter("requestURL");
        String queryString = request.getParameter("queryString");
        ssoLoginState.setRedirectURL(getRedirectURL(requestURL, queryString, ticket));
        ssoLoginState.setLogin(true);
        return ssoLoginState;
    }

    public static SsoLoginState logout(HttpServletRequest request, HttpServletResponse response) {
        SsoLoginState ssoLoginState = new SsoLoginState();
        String ticket = "";
        String callbackUrl = request.getParameter("callbackUrl");

        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (SsoConstants.SSO_TICKET.equals(cookie.getName())) {
                    ticket = cookie.getValue();
                    cookie.setMaxAge(0);//从cookie删除ticket凭据
                    cookie.setValue(null);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
            }
        }
        if (StringUtils.hasText(ticket)) {
            CacheUtil.delObject(ticket);
        }
        ssoLoginState.setRedirectURL(callbackUrl);
        return ssoLoginState;
    }

    public static String getRedirectURL (String requestURL, String queryString, String ticket) {
        String redirectURL = null;
        if (StringUtils.hasText(requestURL)) {
            redirectURL = requestURL;
            if (StringUtils.hasText(ticket)) {
                if (StringUtils.hasText(queryString)) {
                    redirectURL += "?" + queryString + "&" + SsoConstants.SSO_TICKET + "=" + ticket;
                } else {
                    redirectURL += "?" + SsoConstants.SSO_TICKET + "=" + ticket;
                }
            } else {
                if (StringUtils.hasText(queryString)) {
                    redirectURL += "?" + queryString;
                }
            }
        }
        return redirectURL;
    }
}
