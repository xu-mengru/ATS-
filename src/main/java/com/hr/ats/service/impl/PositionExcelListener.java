package com.hr.ats.service.impl;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.hr.ats.dto.PositionExcelDTO;
import com.hr.ats.entity.Position;
import com.hr.ats.repository.PositionRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class PositionExcelListener implements ReadListener<PositionExcelDTO> {

    private final PositionRepository positionRepository;
    private int successCount = 0;
    private final List<String> errors = new ArrayList<>();

    public PositionExcelListener(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    @Override
    public void invoke(PositionExcelDTO data, AnalysisContext context) {
        if (data.getTitle() == null || data.getTitle().isBlank()) {
            errors.add("第 " + (context.readRowHolder().getRowIndex() + 1) + " 行：岗位名称不能为空");
            return;
        }
        if (data.getDescription() == null || data.getDescription().isBlank()) {
            errors.add("第 " + (context.readRowHolder().getRowIndex() + 1) + " 行：岗位描述不能为空");
            return;
        }
        Position position = new Position();
        position.setTitle(data.getTitle().trim());
        position.setDescription(data.getDescription().trim());
        position.setStatus(normalizeStatus(data.getStatus()));
        positionRepository.save(position);
        successCount++;
        log.debug("Excel 导入岗位: {}", data.getTitle());
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        if (exception instanceof ExcelDataConvertException e) {
            errors.add("第 " + (e.getRowIndex() + 1) + " 行 " + e.getColumnIndex() + " 列：数据格式错误");
        } else {
            errors.add("第 " + (context.readRowHolder().getRowIndex() + 1) + " 行：解析异常 - " + exception.getMessage());
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("Excel 解析完成：成功 {} 条，错误 {} 条", successCount, errors.size());
    }

    public int getSuccessCount() {
        return successCount;
    }

    public List<String> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * 中文状态名 → 英文状态码
     */
    private static final Map<String, String> CN_STATUS_MAP = Map.of(
        "草稿", "DRAFT", "审核中", "REVIEWING", "已发布", "PUBLISHED",
        "已驳回", "REJECTED", "已关闭", "CLOSED"
    );

    private static final Set<String> VALID_STATUSES = Set.of(
        "DRAFT", "REVIEWING", "PUBLISHED", "REJECTED", "CLOSED"
    );

    /**
     * 规范化状态值：支持中文和英文（大小写不敏感）输入
     */
    private String normalizeStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return "DRAFT";
        }
        String input = raw.trim();
        // 先查中文映射
        String cn = CN_STATUS_MAP.get(input);
        if (cn != null) return cn;
        // 再尝试英文大写匹配
        String upper = input.toUpperCase();
        if (VALID_STATUSES.contains(upper)) return upper;
        // 无效值：记录警告，默认设为草稿
        errors.add("第 " + (successCount + errors.size() + 1) + " 行：状态值 \"" + input + "\" 无效，已默认设为「草稿」");
        return "DRAFT";
    }
}
