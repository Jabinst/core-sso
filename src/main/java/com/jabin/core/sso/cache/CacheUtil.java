package com.jabin.core.sso.cache;

import com.jabin.core.sso.tool.SerializeUtil;
import com.jabin.core.sso.tool.SpringTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import redis.clients.jedis.*;
import redis.clients.jedis.params.geo.GeoRadiusParam;

import java.util.ArrayList;
import java.util.List;

/**
 * 缓存工具类
 *
 * @author zhangbbj
 * @date 2016/1/27 14:35
 */

public class CacheUtil {

    private static Logger logger = LoggerFactory.getLogger(CacheUtil.class);

    /**
     * 获取ShardedJedis对象
     *
     * @return
     */
    private static ShardedJedis getShardedJedis() {
        ShardedJedisPool pool = null;
        ShardedJedis jedis = null;
        try {
            pool = SpringTool.getInstance().getBean("shardedJedisPool");
            if (pool == null) {
                logger.error("getShardedJedis", "未找到Bean shardedJedisPool");
            } else {
                jedis = pool.getResource();
            }
        } catch (Exception ex) {
            logger.error("getShardedJedis", ex);
        }
        return jedis;
    }

    /**
     * 添加缓存对象并指定有效期，指定key值，已存在则覆盖
     *
     * @param key
     * @param obj
     * @param time
     */
    public static void setObjectByKey(String key, Object obj, Integer time) {
        ShardedJedis jedis = getShardedJedis();
        try {
            if (jedis != null) {
                jedis.set(key.getBytes(), SerializeUtil.serialize(obj));
                if (time != null && time > 0) {
                    jedis.expire(key.getBytes(), time);
                }
            }
        } catch (Exception ex) {
            logger.error("setObject", ex);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    /**
     * 添加缓存对象，指定key值，已存在则覆盖
     *
     * @param key
     * @param obj
     */
    public static void setObjectByKey(String key, Object obj) {
        setObjectByKey(key, obj, 0);

    }

    /**
     * 添加缓存对象并指定有效期，key值为"objType:keyId"，已存在则覆盖
     *
     * @param objType
     * @param keyId
     * @param obj
     * @param time
     */
    public static void setObject(String objType, String keyId, Object obj, Integer time) {
        setObjectByKey(objType + ":" + keyId, obj, time);

    }

    /**
     * 添加缓存对象，key值为"objType:keyId"，已存在则覆盖
     *
     * @param objType
     * @param keyId
     * @param obj
     */
    public static void setObject(String objType, String keyId, Object obj) {
        setObject(objType, keyId, obj, 0);
    }

    /**
     * 根据key值获取缓存对象
     *
     * @param key
     * @param <T>
     * @return
     */
    public static <T> T getObject(String key) {
        ShardedJedis jedis = getShardedJedis();
        try {
            if (jedis != null) {
                byte[] bytes = jedis.get((key).getBytes());
                if (bytes != null) {
                    return (T) SerializeUtil.unserialize(bytes);
                }
            }
        } catch (Exception ex) {
            logger.error("getObject", ex);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    /**
     * 根据key值获取缓存对象，其中key值为 "objType:keyId"
     *
     * @param objType
     * @param keyId
     * @param <T>
     * @return
     */
    public static <T> T getObject(String objType, String keyId) {
        return getObject(objType + ":" + keyId);
    }

    /**
     * 删除一类缓存对象
     *
     * @param objType
     * @return
     */
    public static <T> List<T> getObjectsByObjType(String objType) {
        ShardedJedis jedis = getShardedJedis();
        if (!StringUtils.hasText(objType)) {
            return null;
        }
        List<T> resultList = new ArrayList<T>();
        try {
            if (jedis != null) {
                for (Jedis jedis1 : jedis.getAllShards()) {
                    for (String key : jedis1.keys((objType + "*"))) {
                        byte[] bytes = jedis1.get((key).getBytes());
                        resultList.add((T) SerializeUtil.unserialize(bytes));
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("getObjectsByObjType", ex);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return resultList;
    }

    /**
     * 删除缓存对象
     *
     * @param key
     * @return
     */
    public static long delObject(String key) {
        ShardedJedis jedis = getShardedJedis();
        try {
            if (jedis != null) {
                return jedis.del(key.getBytes());
            }
        } catch (Exception ex) {
            logger.error("delObject", ex);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return -1;
    }

    /**
     * 删除缓存对象
     *
     * @param objType
     * @param key
     * @return
     */
    public static long delObject(String objType, String key) {
        return delObject(objType + ":" + key);
    }

    /**
     * 删除一类缓存对象
     *
     * @param objTypes
     * @return
     */
    public static long delObjectByObjType(List<String> objTypes) {
        ShardedJedis jedis = getShardedJedis();
        if (objTypes == null || objTypes.size() < 1) {
            return -1;
        }

        try {
            if (jedis != null) {
                Long result = 0L;
                for (String objType : objTypes) {
                    for (Jedis jedis1 : jedis.getAllShards()) {
                        for (byte[] bytes : jedis1.keys((objType + "*").getBytes())) {
                            result += jedis1.del(bytes);
                        }
                    }
                }
                return result;
            }
        } catch (Exception ex) {
            logger.error("delObjectByObjType", ex);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return -1;
    }

    /**
     * 删除一类缓存对象
     *
     * @param objTypes
     * @return
     */
    public static long delObjectByObjType(String[] objTypes) {
        if (objTypes == null || objTypes.length < 1) {
            return -1;
        }
        List<String> objTypeList = new ArrayList<String>();
        for (String objType : objTypes) {
            objTypeList.add(objType);
        }
        return delObjectByObjType(objTypeList);
    }

    /**
     * 删除一类缓存对象
     *
     * @param objType
     * @return
     */
    public static long delObjectByObjType(String objType) {
        if (!StringUtils.hasText(objType)) {
            return -1;
        }
        return delObjectByObjType(new String[]{objType});
    }

    /**
     * 重置到期时间
     *
     * @param key
     * @param time
     */
    public static void setExpire(String key, Integer time) {
        ShardedJedis jedis = getShardedJedis();
        try {
            if (jedis != null) {
                if (time != null && time > 0) {
                    jedis.expire(key.getBytes(), time);
                }
            }
        } catch (Exception ex) {
            logger.error("setExpire", ex);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 重置到期时间，其中key值为 "objType:keyId"
     *
     * @param objType
     * @param keyId
     * @param time
     */
    public static void setExpire(String objType, String keyId, Integer time) {
        setExpire(objType + ":" + keyId, time);
    }

    public static long appendString(String key, String value) {
        ShardedJedis jedis = getShardedJedis();
        try {
            if (jedis != null) {
                return jedis.append(key, value);
            }
        } catch (Exception ex) {
            logger.error("appendString", ex);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return -1;
    }

    public static long geoAdd(String key, double longitude, double latitude, String member) {
        ShardedJedis jedis = getShardedJedis();
        try {
            if (jedis != null) {
                return jedis.geoadd(key, longitude, latitude, member);
            }
        } catch (Exception ex) {
            logger.error("geoAdd", ex);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return -1;
    }

    public static List<GeoRadiusResponse> geoRadius(String key, double longitude, double latitude, double radius, GeoUnit geoUnit, GeoRadiusParam geoRadiusParam) {
        ShardedJedis jedis = getShardedJedis();
        try {
            if (jedis != null) {
                return jedis.georadius(key, longitude, latitude, radius, geoUnit, geoRadiusParam);
            }
        } catch (Exception ex) {
            logger.error("geoRadius", ex);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public static List<GeoCoordinate> geoPos(String key, String... members) {
        ShardedJedis jedis = getShardedJedis();
        try {
            if (jedis != null) {
                return jedis.geopos(key, members);
            }
        } catch (Exception ex) {
            logger.error("geoPos", ex);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

}
