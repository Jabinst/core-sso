# 单点登录SDK集成说明文档

## 前言
该sso-sdk是针对浏览器端，基于shiro封装的，本地会话由各应用内部管理
(按照shiro标准配置即可)，sdk实现的是本地会话校验失败自动重定向到
sso认证中心，认证成功后会自动跳转回原应用。
## 工作原理
1).用户对目标应用发起登录请求时，目标应用首先会校验是否存在该用户
的会话(这个步骤按照shiro的官方文档配置实现即可，这里不再阐述)，如
果本地会话不存在，则尝试从url中获取ticket信息，如果获取到ticket信
息则使用ticket校验器校验ticket的有效性(通过访问sso认证中心的接口实
现)。

2).如果本地会话不存在且ticket不存在或无效，会自动重定向到sso认证中心。

3).认证中心会尝试从用户的cookie信息中获取ticket信息，如果ticket存
在，则直接重定向回目标应用地址，并追加ticket参数，如果不存在，则重
定向到sso登录页面，用户输入登录信息进行登录，登录成功后则自动生成
ticket，并将ticket信息存储在用户的cookie中，同时重定向回目标应用
地址，追加ticket参数。

## 集成步骤
### 1.配置sso本地登录认证处理器kingkooSsoRelam
    <!-- sso登录认证Realm -->
    <bean id="kingkooSssoRealm" class="com.jabin.core.sso.realm.KingkooSsoRealm" >
        <constructor-arg name="ticketValidator" ref="ticketValidator"/>
    </bean>
### 2.配置sso凭据验证器ticketValidator
    <!-- sso凭据验证器 -->
    <bean id="ticketValidator" class="com.jabin.core.sso.validate.KingkooSsoTicketValidator">
        <!-- 指定用户信息实体类Class(必须参数) -->
        <constructor-arg name="userClass" value="com.jabin.api.dto.manage.user.UserDto"/>
        <!-- sso认证中心凭据验证服务地址 -->
        <constructor-arg name="validateUrl" value="${shiro.sso.validateUrl}"/>
    </bean>
### 3.配置sso用户授权过滤器
    <bean id="kingkooSsoFilter" class="com.jabin.core.sso.filter.KingkooSsoFilter">
        <!-- sso认证中心登录服务地址 -->
        <constructor-arg name="loginUrl" value="${shiro.sso.loginUrl}" />
        <!-- sso认证成功后默认回调地址 -->
        <property name="successUrl" value="${shiro.sso.successUrl}" />
    </bean>
    
    其中loginUrl为sso认证中心的认证服务地址，successUrl为登录成功后默认跳转的本地url(没有获取到用户原访问地址的前提下才会访问改地址)

### 4.配置sso登出过滤器
    <bean id="kingkooSsoLogoutFilter" class="com.jabin.core.sso.filter.KingkooSsoLogoutFilter">
        <!-- sso认证中心登出服务地址 -->
        <constructor-arg name="logoutUrl" value="${shiro.sso.logoutUrl}" />
    </bean>
    用户登出时，销毁本地会话并访问sso认证中心注销该用户的远程会话信息。

### 5.shiro过滤器配置参考
    <!-- Shiro Filter -->
    <bean id="shiroFilter" class="org.apache.shiro.spring.web.ShiroFilterFactoryBean">
        <property name="filters">
            <util:map>
                <!-- kingkooSsoFilter -->
                <entry key="user" value-ref="kingkooSsoFilter" />
                <entry key="logoutFilter" value-ref="kingkooSsoLogoutFilter" />
            </util:map>
        </property>
        <property name="securityManager" ref="securityManager"/>
        <property name="filterChainDefinitions">
            <value>
                /logout = logoutFilter
                /** = user
            </value>
        </property>
    </bean>