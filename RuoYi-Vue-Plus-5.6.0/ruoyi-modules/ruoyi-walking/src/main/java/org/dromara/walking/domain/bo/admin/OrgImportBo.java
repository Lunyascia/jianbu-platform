package org.dromara.walking.domain.bo.admin;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 组织机构批量导入（后台）Excel 行
 */
@Data
@ExcelIgnoreUnannotated
public class OrgImportBo {

    @ExcelProperty("机构名称")
    private String deptName;

    /** 上级机构名称，空为顶级 */
    @ExcelProperty("上级机构名称")
    private String parentName;

    @ExcelProperty("显示顺序")
    private Integer orderNum;

    @ExcelProperty("负责人")
    private String leader;

    @ExcelProperty("联系电话")
    private String phone;
}
