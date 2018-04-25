package com.jabin.core.sso.filter;

import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.net.URLEncoder;

/**
 * @author zhangbbj
 * @date 2017/12/06 20:43
 **/
public class KingkooSsoLogoutFilter extends LogoutFilter {
    private static final Logger log = LoggerFactory.getLogger(KingkooSsoLogoutFilter.class);
    private String callbackUrl = "/";

    KingkooSsoLogoutFilter(String logoutUrl) {
        this.setRedirectUrl(logoutUrl);
    }

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        Subject subject = this.getSubject(request, response);
        String redirectUrl = this.getRedirectUrl(request, response, subject);
        try {
            subject.logout();
        } catch (SessionException var6) {
            log.debug("Encountered session exception during logout.  This can generally safely be ignored.", var6);
        }
        String requestUrl = WebUtils.toHttp(request).getRequestURL().toString();
        requestUrl = requestUrl.substring(0, requestUrl.lastIndexOf("/logout"));
        if (!StringUtils.hasText(callbackUrl)) {
            callbackUrl = "/";
        }
        this.issueRedirect(request, response, redirectUrl + "?callbackUrl=" + URLEncoder.encode(requestUrl + callbackUrl, "UTF-8"));
        return false;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
}
