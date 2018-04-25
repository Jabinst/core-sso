package com.jabin.core.sso.filter;

import com.jabin.core.sso.SsoConstants;
import com.jabin.core.sso.session.SsoSessionKey;
import com.jabin.core.sso.token.KingkooSsoToken;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 单点登录过滤器
 *
 * @author zhangbbj
 * @date 2017/12/05 16:57
 **/
public class KingkooSsoFilter extends FormAuthenticationFilter {
    private static Logger logger = LoggerFactory.getLogger(KingkooSsoFilter.class);

    public KingkooSsoFilter(String loginUrl) {
        this.setLoginUrl(loginUrl);
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String ticket = httpRequest.getParameter(SsoConstants.SSO_TICKET);
        return new KingkooSsoToken(ticket);
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        Subject subject = this.getSubject(request, response);
        if (subject != null && subject.getSession(true).getAttribute(SsoSessionKey.User.getSessionKey()) != null) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        saveRequest(request);
        return this.executeLogin(request, response);
    }

    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
        String successUrl = null;
        boolean contextRelative = true;
        SavedRequest savedRequest = WebUtils.getAndClearSavedRequest(request);
        if (savedRequest != null && savedRequest.getMethod().equalsIgnoreCase("GET")) {
            successUrl = savedRequest.getRequestUrl();
            contextRelative = false;
        }

        if (successUrl == null) {
            successUrl = getSuccessUrl();
        }

        if (successUrl == null) {
            throw new IllegalStateException("Success URL not available via saved request or via the successUrlFallback method parameter. One of these must be non-null for issueSuccessRedirect() to work.");
        } else {
            if (successUrl.lastIndexOf("?" + SsoConstants.SSO_TICKET + "=") > -1) {
                successUrl = successUrl.substring(0, successUrl.lastIndexOf("?" + SsoConstants.SSO_TICKET + "="));
            }
            if (successUrl.lastIndexOf("&" + SsoConstants.SSO_TICKET + "=") > -1) {
                successUrl = successUrl.substring(0, successUrl.lastIndexOf("&" + SsoConstants.SSO_TICKET + "="));
            }
            WebUtils.issueRedirect(request, response, successUrl, null, contextRelative);
        }
        return false;
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException ae, ServletRequest request, ServletResponse response) {
        String requestURL = WebUtils.toHttp(request).getRequestURL().toString();
        String queryString = WebUtils.toHttp(request).getQueryString();
        if (StringUtils.hasText(requestURL)) {
            try {
                requestURL = URLEncoder.encode(requestURL, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                requestURL = "";
            }
        }
        if (StringUtils.hasText(queryString)) {
            try {
                queryString = URLEncoder.encode(queryString, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                queryString = "";
            }
        }

        String loginUrl = this.getLoginUrl() + "?requestURL=" + requestURL;
        if (StringUtils.hasText(queryString)) {
            loginUrl += "&queryString=" + queryString;
        }
        try {
            WebUtils.issueRedirect(request, response, loginUrl);
        } catch (IOException var7) {
            logger.error("Cannot redirect to failure url : {}", loginUrl, var7);
        }
        return false;
    }

}
