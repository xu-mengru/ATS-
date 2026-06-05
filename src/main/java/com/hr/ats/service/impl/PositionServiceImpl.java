package com.hr.ats.service.impl;

import com.alibaba.excel.EasyExcel;
import com.hr.ats.common.BusinessException;
import com.hr.ats.common.ResultCode;
import com.hr.ats.dto.PositionDTO;
import com.hr.ats.dto.PositionExcelDTO;
import com.hr.ats.entity.Position;
import com.hr.ats.repository.PositionRepository;
import com.hr.ats.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;

    @Override
    public Page<Position> listPositions(String keyword, String status, Pageable pageable) {
        Specification<Position> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                predicates.add(cb.like(root.get("title"), "%" + keyword + "%"));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return positionRepository.findAll(spec, pageable);
    }

    @Override
    public Position getPositionById(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "岗位不存在: id=" + id));
    }

    @Override
    @Transactional
    public Position createPosition(PositionDTO dto) {
        Position position = new Position();
        position.setTitle(dto.getTitle());
        position.setDescription(dto.getDescription());
        position.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : "DRAFT");
        if (StringUtils.hasText(dto.getExpireDate())) {
            position.setExpireDate(LocalDate.parse(dto.getExpireDate()));
        }
        Position saved = positionRepository.save(position);
        log.info("新增岗位成功: id={}, title={}", saved.getId(), saved.getTitle());
        return saved;
    }

    @Override
    @Transactional
    public Position updatePosition(Long id, PositionDTO dto) {
        Position position = getPositionById(id);
        position.setTitle(dto.getTitle());
        position.setDescription(dto.getDescription());
        if (StringUtils.hasText(dto.getStatus())) {
            position.setStatus(dto.getStatus());
        }
        if (StringUtils.hasText(dto.getExpireDate())) {
            position.setExpireDate(LocalDate.parse(dto.getExpireDate()));
        } else {
            position.setExpireDate(null);
        }
        Position saved = positionRepository.save(position);
        log.info("更新岗位成功: id={}, title={}", saved.getId(), saved.getTitle());
        return saved;
    }

    @Override
    @Transactional
    public void deletePosition(Long id) {
        Position position = getPositionById(id);
        if ("PUBLISHED".equals(position.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已发布的岗位不可直接删除，请先关闭该岗位");
        }
        positionRepository.delete(position);
        log.info("删除岗位成功: id={}, title={}", id, position.getTitle());
    }

    @Override
    @Transactional
    public Map<String, Object> importExcel(MultipartFile file) throws IOException {
        PositionExcelListener listener = new PositionExcelListener(positionRepository);
        EasyExcel.read(file.getInputStream(), PositionExcelDTO.class, listener).sheet().doRead();
        int count = listener.getSuccessCount();
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        if (listener.hasErrors()) {
            log.warn("Excel 批量导入完成: 成功 {} 条, 错误 {} 条", count, listener.getErrors().size());
            result.put("errors", listener.getErrors());
        } else {
            log.info("Excel 批量导入完成: 成功 {} 条", count);
        }
        return result;
    }

    @Override
    public List<Position> getAllPositions() {
        return positionRepository.findAll();
    }

    // ── Approval Flow ────────────────────────────────────────────

    @Override
    @Transactional
    public Position submitForReview(Long id) {
        Position position = getPositionById(id);
        if (!"DRAFT".equals(position.getStatus()) && !"REJECTED".equals(position.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅草稿或已驳回状态的岗位可提交审核，当前状态: " + position.getStatus());
        }
        position.setStatus("REVIEWING");
        log.info("岗位提交审核: id={}, title={}", id, position.getTitle());
        return positionRepository.save(position);
    }

    @Override
    @Transactional
    public Position approvePosition(Long id) {
        Position position = getPositionById(id);
        if (!"REVIEWING".equals(position.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅审核中的岗位可审批通过，当前状态: " + position.getStatus());
        }
        position.setStatus("PUBLISHED");
        log.info("岗位审核通过: id={}, title={}", id, position.getTitle());
        return positionRepository.save(position);
    }

    @Override
    @Transactional
    public Position rejectPosition(Long id) {
        Position position = getPositionById(id);
        if (!"REVIEWING".equals(position.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅审核中的岗位可驳回，当前状态: " + position.getStatus());
        }
        position.setStatus("REJECTED");
        log.info("岗位审核驳回: id={}, title={}", id, position.getTitle());
        return positionRepository.save(position);
    }

    // ── Statistics ───────────────────────────────────────────────

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Status counts
        stats.put("total", positionRepository.count());
        stats.put("draft", positionRepository.countByStatus("DRAFT"));
        stats.put("reviewing", positionRepository.countByStatus("REVIEWING"));
        stats.put("rejected", positionRepository.countByStatus("REJECTED"));
        stats.put("published", positionRepository.countByStatus("PUBLISHED"));
        stats.put("closed", positionRepository.countByStatus("CLOSED"));

        // Monthly trend (last 6 months)
        List<Map<String, Object>> monthlyTrend = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime start = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end = i == 0
                    ? LocalDateTime.now()
                    : start.plusMonths(1);
            long count = positionRepository.countByCreateTimeBetween(start, end);
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", start.getMonthValue() + "月");
            monthData.put("count", count);
            monthlyTrend.add(monthData);
        }
        stats.put("monthlyTrend", monthlyTrend);

        return stats;
    }

    @Override
    @Transactional
    public int autoCloseExpiredPositions() {
        List<Position> published = positionRepository.findByStatus("PUBLISHED");
        int closed = 0;
        for (Position p : published) {
            if (p.isExpired()) {
                p.setStatus("CLOSED");
                positionRepository.save(p);
                closed++;
                log.info("岗位已过期自动关闭: id={}, title={}", p.getId(), p.getTitle());
            }
        }
        return closed;
    }
}
