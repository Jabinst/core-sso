package com.jabin.core.sso.session;

import com.jabin.core.sso.cache.CacheUtil;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * redis session集群共享
 *
 * @author zhangbbj
 * @date 2017/11/10 11:39
 **/
public class RedisSessionDAO extends EnterpriseCacheSessionDAO {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The Redis key prefix for the sessions
     */
    private final String KEY_PREFIX = "shiro_redis_local_session";
    private int expire = 12 * 60 * 60;

    public void setExpire(int expire) {
        this.expire = expire;
    }

//    @Override
//    public Collection<Session> getActiveSessions() {
//        return CacheUtil.getObjectsByObjType(KEY_PREFIX);
//    }

    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = super.doCreate(session);
        this.saveSession(session);
        return sessionId;
    }

    /**
     * 重写CachingSessionDao的readSession方法
     * @param sessionId
     * @return
     * @throws UnknownSessionException
     */
    @Override
    public Session readSession(Serializable sessionId) throws UnknownSessionException {
        Session session = this.getCachedSession(sessionId);
        if (session != null && session.getAttribute(SsoSessionKey.User.getSessionKey()) == null) {
            session = null;
        }
        if (session == null) {
            session = this.doReadSession(sessionId);
            if (session != null) {
                this.cache(session, sessionId);
            }
        }

        return session;
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        if (sessionId == null) {
            logger.error("session id is null");
            return null;
        }
        return CacheUtil.getObject(KEY_PREFIX, sessionId.toString());
    }

    @Override
    public void doUpdate(Session session) throws UnknownSessionException {
        if (!(Boolean) (session.getAttribute(SsoSessionKey.NeedUpdate.getSessionKey()))) {
            return;
        }
        //如果本地Session已经有效了就不需要更新session有效期了，减少访问redis的次数，提高响应速度
        if (session.getAttribute(SsoSessionKey.User.getSessionKey()) != null) {
            session.setAttribute(SsoSessionKey.NeedUpdate.getSessionKey(), false);
        }
        this.saveSession(session);
    }

    /**
     * 删除失效session
     */
    @Override
    protected void doDelete(Session session) {
        if (session == null || session.getId() == null) {
            logger.error("session or session id is null");
            return;
        }
        CacheUtil.delObject(KEY_PREFIX, session.getId().toString());
    }

    private void saveSession(Session session) throws UnknownSessionException {
        if (session == null || session.getId() == null) {
            logger.error("session or session id is null");
            return;
        }
        //设置过期时间(单位秒)
        CacheUtil.setObject(KEY_PREFIX, session.getId().toString(), session, expire);
    }
}