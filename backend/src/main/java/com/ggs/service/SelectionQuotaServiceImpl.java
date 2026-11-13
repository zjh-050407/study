package com.ggs.service;

import com.ggs.mapper.SelectionQuotaMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;

/**
 * 选课容量配额：Redis 原子扣减（防超选/防重复），Redis 不可用时降级为 DB 条件扣减。
 *
 * Redis 键设计：
 *   sel:quota:{courseId}:{semesterId}   → 剩余名额（初始化时载入）
 *   sel:chosen:{sid}:{semesterId}       → 该学生已占名额的课程 Set
 */
@Service
public class SelectionQuotaServiceImpl implements SelectionQuotaService {

    private static final Logger log = LoggerFactory.getLogger(SelectionQuotaServiceImpl.class);

    /** Lua：名额>0 且未选过 → 名额-1、SADD 已选课程、返回 OK；chosen 集合 member 为 courseId */
    private static final String LUA_OCCUPY =
            "local quota = KEYS[1]; local chosen = KEYS[2]; local capacity = tonumber(ARGV[1]); local sid = ARGV[2]; local courseId = ARGV[3];" +
            "local cur = redis.call('GET', quota);" +
            "if not cur then redis.call('SET', quota, capacity); cur = capacity; end;" +
            "if redis.call('SISMEMBER', chosen, courseId) == 1 then return 'CHOSEN'; end;" +
            "if tonumber(cur) <= 0 then return 'FULL'; end;" +
            "redis.call('DECR', quota); redis.call('SADD', chosen, courseId); return 'OK';";

    private static final String LUA_RELEASE =
            "local quota = KEYS[1]; local chosen = KEYS[2]; local courseId = ARGV[1];" +
            "if redis.call('SREM', chosen, courseId) == 1 then redis.call('INCR', quota); end; return 1;";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SelectionQuotaMapper quotaMapper;

    @Value("${app.selection.redis-enabled:true}")
    private boolean redisEnabled;

    private final DefaultRedisScript<String> occupyScript = new DefaultRedisScript<>(LUA_OCCUPY, String.class);
    private final DefaultRedisScript<Long> releaseScript = new DefaultRedisScript<>(LUA_RELEASE, Long.class);

    @Override
    public QuotaResult tryOccupy(int sid, int courseId, Integer semesterId, int capacity) {
        if (redisEnabled) {
            try {
                return tryOccupyRedis(sid, courseId, semesterId, capacity);
            } catch (Exception e) {
                log.warn("Redis 不可用，选课配额降级为数据库模式: {}", e.getMessage());
            }
        }
        return tryOccupyDb(courseId, semesterId, capacity);
    }

    private QuotaResult tryOccupyRedis(int sid, int courseId, Integer semesterId, int capacity) {
        String sem = semesterId == null ? "0" : String.valueOf(semesterId);
        String quotaKey = "sel:quota:" + courseId + ":" + sem;
        String chosenKey = "sel:chosen:" + sid + ":" + sem;
        String result = redisTemplate.execute(occupyScript,
                Arrays.asList(quotaKey, chosenKey),
                String.valueOf(capacity), String.valueOf(sid), String.valueOf(courseId));
        if ("OK".equals(result)) {
            return QuotaResult.success();
        }
        if ("CHOSEN".equals(result)) {
            return QuotaResult.fail("该学生已选此课程，请勿重复选课");
        }
        return QuotaResult.fail("课程名额已满");
    }

    private QuotaResult tryOccupyDb(int courseId, Integer semesterId, int capacity) {
        // 名额表原子扣减：used < capacity 才成功，防超选（与 Redis 配额同构）；学期哨兵 0=未分学期
        int sem = semesterId == null ? 0 : semesterId;
        quotaMapper.ensure(courseId, sem, capacity);
        int rows = quotaMapper.occupy(courseId, sem);
        if (rows > 0) {
            return QuotaResult.success();
        }
        return QuotaResult.fail("课程名额已满");
    }

    @Override
    public void release(int sid, int courseId, Integer semesterId) {
        int sem = semesterId == null ? 0 : semesterId;
        if (redisEnabled) {
            try {
                String semStr = String.valueOf(sem);
                String quotaKey = "sel:quota:" + courseId + ":" + semStr;
                String chosenKey = "sel:chosen:" + sid + ":" + semStr;
                redisTemplate.execute(releaseScript,
                        Arrays.asList(quotaKey, chosenKey), String.valueOf(courseId));
                return;
            } catch (Exception e) {
                log.warn("Redis 不可用，退选回补降级为数据库模式: {}", e.getMessage());
            }
        }
        quotaMapper.release(courseId, sem);
    }
}
