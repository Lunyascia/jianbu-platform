package org.dromara.walking.domain.vo.admin;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 排行统计（后台）
 */
@Data
@ExcelIgnoreUnannotated
public class RankingStatVo {

    /** 名次 */
    @ExcelProperty(value = "名次")
    private Integer rank;

    /** 会员id */
    private Long memberId;

    /** 单位id */
    private Long deptId;

    /** 姓名 */
    @ExcelProperty(value = "姓名")
    private String realName;

    /** 手机号 */
    @ExcelProperty(value = "手机号")
    private String phone;

    /** 单位 */
    @ExcelProperty(value = "单位")
    private String deptName;

    /** 累计步数 */
    @ExcelProperty(value = "累计步数")
    private Long totalSteps;

    /** 累计积分 */
    @ExcelProperty(value = "累计积分")
    private Long totalPoints;

    /** 达标天数 */
    @ExcelProperty(value = "达标天数")
    private Long qualifyDays;
}
