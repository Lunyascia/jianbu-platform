package org.dromara.walking.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.walking.domain.WalkingMember;
import org.dromara.walking.mapper.WalkingMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 会员鉴权服务（小程序会员独立 token，不走若依 sa-token）
 */
@Service
public class MemberAuthService {

    public static final String TOKEN_PREFIX = "walking:member_token:";
    public static final String SESSION_PREFIX = "walking:session:";

    @Autowired
    private WalkingMemberMapper memberMapper;

    /** 创建会员 token（Redis 缓存 7 天） */
    public String createToken(Long memberId) {
        String token = IdUtil.fastSimpleUUID();
        RedisUtils.setCacheObject(TOKEN_PREFIX + token, memberId, Duration.ofDays(7));
        return token;
    }

    /** 从请求头取 token */
    public String getToken() {
        HttpServletRequest request = ServletUtils.getRequest();
        String header = request.getHeader("Authorization");
        if (StrUtil.isBlank(header)) {
            throw new ServiceException("未登录");
        }
        return header.startsWith("Bearer ") ? header.substring(7) : header;
    }

    /** 从 token 取会员id */
    public Long getMemberIdByToken() {
        String token = getToken();
        Long memberId = RedisUtils.getCacheObject(TOKEN_PREFIX + token);
        if (memberId == null) {
            throw new ServiceException("登录已过期，请重新登录");
        }
        return memberId;
    }

    /** 获取当前登录会员 */
    public WalkingMember getCurrentMember() {
        Long memberId = getMemberIdByToken();
        WalkingMember member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new ServiceException("会员不存在");
        }
        return member;
    }

    /** 缓存会员微信 session_key（用于步数解密） */
    public void cacheSessionKey(Long memberId, String sessionKey) {
        RedisUtils.setCacheObject(SESSION_PREFIX + memberId, sessionKey, Duration.ofDays(7));
    }

    /** 取会员 session_key */
    public String getSessionKey(Long memberId) {
        return RedisUtils.getCacheObject(SESSION_PREFIX + memberId);
    }
}
