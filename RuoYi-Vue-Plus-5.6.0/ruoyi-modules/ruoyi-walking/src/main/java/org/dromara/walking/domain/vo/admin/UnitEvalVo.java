package org.dromara.walking.domain.vo.admin;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 先进组织单位评选参考（后台）—— 量化计分排名，供管理员参考
 *
 * 计分公式（透明可解释，最终评选由管理员综合判断）：
 *  参与率得分 = 参与率 × 40
 *  打卡率得分 = 打卡率 × 30
 *  获奖得分   = min(30, 获奖加权分 / 参与人数 × 30)，加权：一等奖3 二等奖2 三等奖1 优秀奖0.5
 *  总分       = 参与率得分 + 打卡率得分 + 获奖得分（0~100）
 */
@Data
@ExcelIgnoreUnannotated
public class UnitEvalVo {

    /** 名次 */
    @ExcelProperty(value = "名次")
    private Integer rank;

    /** 单位id */
    private Long deptId;

    /** 单位名称 */
    @ExcelProperty(value = "单位")
    private String deptName;

    /** 会员总数 */
    @ExcelProperty(value = "会员总数")
    private Long memberTotal;

    /** 参与人数 */
    @ExcelProperty(value = "参与人数")
    private Long approvedCount;

    /** 参与率 */
    @ExcelProperty(value = "参与率")
    private String participationRateText;

    /** 打卡达标人数 */
    @ExcelProperty(value = "打卡达标人数")
    private Long checkinCount;

    /** 打卡率 */
    @ExcelProperty(value = "打卡率")
    private String checkinRateText;

    /** 一等奖人数 */
    @ExcelProperty(value = "一等奖人数")
    private Long award1Count;

    /** 二等奖人数 */
    @ExcelProperty(value = "二等奖人数")
    private Long award2Count;

    /** 三等奖人数 */
    @ExcelProperty(value = "三等奖人数")
    private Long award3Count;

    /** 优秀奖人数 */
    @ExcelProperty(value = "优秀奖人数")
    private Long awardExcellentCount;

    /** 参与率得分(0-40) */
    @ExcelProperty(value = "参与率得分")
    private Double participationScore;

    /** 打卡率得分(0-30) */
    @ExcelProperty(value = "打卡率得分")
    private Double checkinScore;

    /** 获奖得分(0-30) */
    @ExcelProperty(value = "获奖得分")
    private Double awardScore;

    /** 总分(0-100) */
    @ExcelProperty(value = "总分")
    private Double totalScore;
}
