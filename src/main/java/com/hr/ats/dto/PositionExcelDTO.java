package com.hr.ats.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class PositionExcelDTO {

    @ExcelProperty("岗位名称")
    private String title;

    @ExcelProperty("岗位描述")
    private String description;

    @ExcelProperty("状态")
    private String status;
}
