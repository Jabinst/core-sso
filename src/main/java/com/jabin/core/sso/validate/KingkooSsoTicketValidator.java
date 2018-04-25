package com.jabin.core.sso.validate;

import com.alibaba.fastjson.JSON;
import com.jabin.core.sso.SsoConstants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;

/**
 * 单点登录sso凭据验证器
 *
 * @author zhangbbj
 * @date 2017/12/06 23:40
 **/
public class KingkooSsoTicketValidator<T> {
    private String validateUrl;
    private Class<T> userClass;

    KingkooSsoTicketValidator(String validateUrl, Class<T> userClass) {
        this.validateUrl = validateUrl;
        this.userClass = userClass;
    }

    public KingkooSsoValidateResult validate(String ticket) {
        T user = null;
        try {
            if (StringUtils.hasText(ticket)) {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                //创建httppost
                HttpGet httpGet = new HttpGet(validateUrl + "?" + SsoConstants.SSO_TICKET + "=" + ticket);
                RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(3000).setSocketTimeout(10000).build();
                httpGet.setConfig(requestConfig);
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity entity = response.getEntity();
                String jsonResponse = EntityUtils.toString(entity, Charset.forName("utf-8"));
                KingkooSsoValidateResponse ssoValidateResponse = JSON.parseObject(jsonResponse, KingkooSsoValidateResponse.class);
                if (ssoValidateResponse.isSuccess()) {
                    user = JSON.parseObject(ssoValidateResponse.getUserInfo(), userClass);
                    if (user != null) {
                        return new KingkooSsoValidateResult<T>(user);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new KingkooSsoValidateResult<T>();
    }

    public void setUserClass(Class<T> userClass) {
        this.userClass = userClass;
    }

    public void setValidateUrl(String validateUrl) {
        this.validateUrl = validateUrl;
    }
}
