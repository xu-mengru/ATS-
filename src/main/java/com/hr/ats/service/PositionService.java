package com.hr.ats.service;

import com.hr.ats.dto.PositionDTO;
import com.hr.ats.entity.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface PositionService {

    Page<Position> listPositions(String keyword, String status, Pageable pageable);

    Position getPositionById(Long id);

    Position createPosition(PositionDTO dto);

    Position updatePosition(Long id, PositionDTO dto);

    void deletePosition(Long id);

    Map<String, Object> importExcel(MultipartFile file) throws IOException;

    List<Position> getAllPositions();

    /** 提交审核 DRAFT → REVIEWING */
    Position submitForReview(Long id);

    /** 审核通过 REVIEWING → PUBLISHED */
    Position approvePosition(Long id);

    /** 审核驳回 REVIEWING → REJECTED */
    Position rejectPosition(Long id);

    /** 岗位统计 */
    Map<String, Object> getStatistics();

    /** 自动关闭过期岗位 */
    int autoCloseExpiredPositions();
}
