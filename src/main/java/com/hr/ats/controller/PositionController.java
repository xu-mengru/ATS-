package com.hr.ats.controller;

import com.alibaba.excel.EasyExcel;
import com.hr.ats.common.Result;
import com.hr.ats.dto.PositionDTO;
import com.hr.ats.dto.PositionExcelDTO;
import com.hr.ats.entity.Position;
import com.hr.ats.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    /**
     * 分页查询岗位列表
     */
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Position> positionPage = positionService.listPositions(keyword, status, pageable);

        Map<String, Object> data = new HashMap<>();
        data.put("records", positionPage.getContent());
        data.put("total", positionPage.getTotalElements());
        data.put("page", page);
        data.put("size", size);
        return Result.success(data);
    }

    /**
     * 获取所有岗位（不分页，供下拉选择等场景使用）
     */
    @GetMapping("/all")
    public Result<List<Position>> all() {
        return Result.success(positionService.getAllPositions());
    }

    /**
     * 根据ID获取岗位详情
     */
    @GetMapping("/{id}")
    public Result<Position> getById(@PathVariable Long id) {
        return Result.success(positionService.getPositionById(id));
    }

    /**
     * 新增岗位
     */
    @PostMapping
    public Result<Position> create(@Valid @RequestBody PositionDTO dto) {
        return Result.success("岗位创建成功", positionService.createPosition(dto));
    }

    /**
     * 更新岗位信息
     */
    @PutMapping("/{id}")
    public Result<Position> update(@PathVariable Long id, @Valid @RequestBody PositionDTO dto) {
        return Result.success("岗位更新成功", positionService.updatePosition(id, dto));
    }

    /**
     * 删除岗位
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        positionService.deletePosition(id);
        return Result.success("岗位删除成功", null);
    }

    /**
     * Excel 批量导入岗位
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        int count = positionService.importExcel(file);
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return Result.success("成功导入 " + count + " 条岗位记录", data);
    }

    /**
     * 提交审核 DRAFT/REJECTED → REVIEWING
     */
    @PostMapping("/{id}/submit")
    public Result<Position> submitForReview(@PathVariable Long id) {
        return Result.success("岗位已提交审核", positionService.submitForReview(id));
    }

    /**
     * 审核通过 REVIEWING → PUBLISHED
     */
    @PostMapping("/{id}/approve")
    public Result<Position> approve(@PathVariable Long id) {
        return Result.success("岗位审核通过", positionService.approvePosition(id));
    }

    /**
     * 审核驳回 REVIEWING → REJECTED
     */
    @PostMapping("/{id}/reject")
    public Result<Position> reject(@PathVariable Long id) {
        return Result.success("岗位已驳回", positionService.rejectPosition(id));
    }

    /**
     * 岗位统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(positionService.getStatistics());
    }

    /**
     * 手动触发过期岗位自动关闭
     */
    @PostMapping("/auto-close")
    public Result<Map<String, Object>> autoCloseExpired() {
        int count = positionService.autoCloseExpiredPositions();
        Map<String, Object> data = new HashMap<>();
        data.put("closedCount", count);
        return Result.success("已自动关闭 " + count + " 个过期岗位", data);
    }

    /**
     * 下载 Excel 导入模板
     */
    @GetMapping("/template")
    public void downloadTemplate(jakarta.servlet.http.HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("岗位导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename*=UTF-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), PositionExcelDTO.class)
                .sheet("岗位信息")
                .doWrite(Collections.emptyList());
    }
}
