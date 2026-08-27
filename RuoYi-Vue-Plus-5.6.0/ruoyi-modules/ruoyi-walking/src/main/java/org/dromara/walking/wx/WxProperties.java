package org.dromara.walking.wx;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置（application-dev.yml 的 wx.*）
 */
@Data
@Component
@ConfigurationProperties(prefix = "wx")
public class WxProperties {

    /** 小程序 AppID */
    private String appid;

    /** 小程序 AppSecret */
    private String secret;

    /** 内容安全开关（true=调微信 msgSecCheck，false=本地敏感词） */
    private ContentCheck contentCheck = new ContentCheck();

    /** 每日步数上限（默认15000） */
    private Integer dailyStepLimit = 15000;

    @Data
    public static class ContentCheck {
        private Boolean enabled = false;
    }
}
