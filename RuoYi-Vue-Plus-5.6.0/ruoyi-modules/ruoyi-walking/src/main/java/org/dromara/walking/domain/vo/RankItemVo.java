package org.dromara.walking.domain.vo;

import lombok.Data;

/**
 * 排行条目
 */
@Data
public class RankItemVo {
    private Long memberId;
    private String name;
    private String dept;
    private Integer value;
}
