package org.dromara.walking.wx;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.redis.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 微信小程序接口服务
 */
@Slf4j
@Service
public class WxService {

    @Autowired
    private WxProperties wxProperties;

    /** code2session 结果 */
    @Data
    public static class Code2SessionResult {
        private String openid;
        private String sessionKey;
        private String unionid;
    }

    /**
     * 登录凭证校验 code2session
     */
    public Code2SessionResult code2Session(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session"
            + "?appid=" + wxProperties.getAppid()
            + "&secret=" + wxProperties.getSecret()
            + "&js_code=" + code
            + "&grant_type=authorization_code";
        String resp = HttpUtil.get(url, 10000);
        JSONObject obj = JSONUtil.parseObj(resp);
        if (obj.getInt("errcode", 0) != 0) {
            throw new ServiceException("微信登录失败：" + obj.getStr("errmsg"));
        }
        Code2SessionResult r = new Code2SessionResult();
        r.setOpenid(obj.getStr("openid"));
        r.setSessionKey(obj.getStr("session_key"));
        r.setUnionid(obj.getStr("unionid"));
        return r;
    }

    /**
     * 获取 access_token（Redis 缓存 7000 秒）
     */
    public String getAccessToken() {
        String cacheKey = "walking:wx:access_token";
        String token = RedisUtils.getCacheObject(cacheKey);
        if (StrUtil.isNotBlank(token)) {
            return token;
        }
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential"
            + "&appid=" + wxProperties.getAppid()
            + "&secret=" + wxProperties.getSecret();
        String resp = HttpUtil.get(url, 10000);
        JSONObject obj = JSONUtil.parseObj(resp);
        token = obj.getStr("access_token");
        if (StrUtil.isBlank(token)) {
            throw new ServiceException("获取微信 access_token 失败：" + resp);
        }
        RedisUtils.setCacheObject(cacheKey, token, Duration.ofSeconds(7000));
        return token;
    }

    /**
     * 文本内容安全检测（微信 msgSecCheck）
     * @return true=通过，false=有敏感内容
     */
    public boolean msgSecCheck(String content, String openid) {
        String url = "https://api.weixin.qq.com/wxa/msg_sec_check?access_token=" + getAccessToken();
        JSONObject body = JSONUtil.createObj()
            .set("version", 2)
            .set("openid", StrUtil.blankToDefault(openid, "unknown"))
            .set("scene", 1)
            .set("content", content);
        try {
            String resp = HttpRequest.post(url).body(body.toString(), "application/json").timeout(10000).execute().body();
            JSONObject obj = JSONUtil.parseObj(resp);
            // errcode 0=通过；87014=内容含有违法违规内容
            return obj.getInt("errcode", 0) == 0;
        } catch (Exception e) {
            log.warn("msgSecCheck 调用失败，默认通过：{}", e.getMessage());
            return true;
        }
    }

    /**
     * 本地敏感词兜底检测（content-check.enabled=false 时使用）
     */
    public boolean localSensitiveCheck(String content) {
        if (StrUtil.isBlank(content)) {
            return true;
        }
        String[] words = {"发票", "贷款", "刷单", "加微信", "博彩", "代开发票", "免费领取"};
        for (String w : words) {
            if (content.contains(w)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 解密微信运动步数数据（getWeRunData 的 encryptedData）
     * @return 最新步数
     */
    public int decryptWeRunData(String encryptedData, String sessionKey, String iv) {
        try {
            byte[] key = Base64.decode(sessionKey);
            byte[] ivBytes = Base64.decode(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] plain = cipher.doFinal(Base64.decode(encryptedData));
            String json = new String(plain, StandardCharsets.UTF_8);
            JSONObject obj = JSONUtil.parseObj(json);
            JSONArray list = obj.getJSONArray("stepInfoList");
            if (list == null || list.isEmpty()) {
                return 0;
            }
            // 取最后一条（最新）
            return list.getJSONObject(list.size() - 1).getInt("step", 0);
        } catch (Exception e) {
            log.error("微信运动数据解密失败", e);
            throw new ServiceException("步数数据解密失败");
        }
    }
}
