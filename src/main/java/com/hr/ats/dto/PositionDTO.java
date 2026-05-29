package com.hr.ats.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class PositionDTO {

    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 200, message = "岗位名称长度不能超过200个字符")
    private String title;

    @NotBlank(message = "岗位描述不能为空")
    private String description;

    @Pattern(regexp = "^(DRAFT|REVIEWING|REJECTED|PUBLISHED|CLOSED)?$", message = "岗位状态必须为 DRAFT、REVIEWING、REJECTED、PUBLISHED 或 CLOSED")
    private String status;

    private String expireDate;
}
