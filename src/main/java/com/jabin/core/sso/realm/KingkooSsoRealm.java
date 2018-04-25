package com.jabin.core.sso.realm;

import com.jabin.core.sso.session.SsoSessionKey;
import com.jabin.core.sso.token.KingkooSsoToken;
import com.jabin.core.sso.validate.KingkooSsoTicketValidator;
import com.jabin.core.sso.validate.KingkooSsoValidateResult;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * 单点登录shiro认证处理类
 *
 * @author zhangbbj
 * @date 2017/12/05 16:57
 **/
public class KingkooSsoRealm extends AuthorizingRealm {

    private static Logger logger = LoggerFactory.getLogger(KingkooSsoRealm.class);

    public final static String REALM_NAME = "KingkooSsoRealm";

    KingkooSsoRealm(KingkooSsoTicketValidator ticketValidator) {
        this.ticketValidator = ticketValidator;
    }

    @Override
    public String getName(){
        return REALM_NAME;
    }

    private KingkooSsoTicketValidator ticketValidator;

    /**
     * 建立当前应用本地会话
     * @param token
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        KingkooSsoToken ssoKingkooToken = (KingkooSsoToken)token;
        if (token == null) {
            return null;
        } else {
            String ticket = (String)ssoKingkooToken.getCredentials();
            if (!StringUtils.hasText(ticket)) {
                return null;
            } else {
                //访问SSO服务进行凭据验证
                KingkooSsoValidateResult kingkooSsoValidateResult = ticketValidator.validate(ticket);
                if (!kingkooSsoValidateResult.isSuccess()) {
                    return null;
                }
                Object userDto = kingkooSsoValidateResult.getUser();
                if (userDto == null) {
                    return null;
                }
                Subject user= SecurityUtils.getSubject();
                if(user!=null && user.getSession()!=null){
                    user.getSession().setAttribute(SsoSessionKey.User.getSessionKey(), userDto);
                }
                ssoKingkooToken.setRememberMe(true);
                String salt2 = new SecureRandomNumberGenerator().nextBytes().toHex();
                return new SimpleAuthenticationInfo(userDto, token.getCredentials(), ByteSource.Util.bytes(salt2), getName());
            }
        }
    }

    /**
     * 当前用户授权
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return new SimpleAuthorizationInfo();
    }

    public void setTicketValidator(KingkooSsoTicketValidator ticketValidator) {
        this.ticketValidator = ticketValidator;
    }
}
