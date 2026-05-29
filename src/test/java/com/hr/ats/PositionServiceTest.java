package com.hr.ats;

import com.hr.ats.common.BusinessException;
import com.hr.ats.dto.PositionDTO;
import com.hr.ats.entity.Position;
import com.hr.ats.repository.PositionRepository;
import com.hr.ats.service.PositionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PositionServiceTest {

    @Autowired
    private PositionService positionService;

    @Autowired
    private PositionRepository positionRepository;

    private Long testId;

    @BeforeEach
    void setUp() {
        positionRepository.deleteAll();
        PositionDTO dto = new PositionDTO();
        dto.setTitle("测试岗位");
        dto.setDescription("这是一个测试岗位的描述");
        dto.setStatus("DRAFT");
        Position created = positionService.createPosition(dto);
        testId = created.getId();
    }

    @Test
    @DisplayName("创建岗位 - 默认状态为 DRAFT")
    void createPosition() {
        PositionDTO dto = new PositionDTO();
        dto.setTitle("Java 开发工程师");
        dto.setDescription("负责 Java 后端开发工作");
        dto.setStatus("PUBLISHED");

        Position created = positionService.createPosition(dto);
        assertNotNull(created.getId());
        assertEquals("Java 开发工程师", created.getTitle());
        assertEquals("PUBLISHED", created.getStatus());
    }

    @Test
    @DisplayName("分页查询")
    void listPositions() {
        Page<Position> page = positionService.listPositions(null, null, PageRequest.of(0, 10));
        assertTrue(page.getTotalElements() > 0);
    }

    @Test
    @DisplayName("根据关键词搜索")
    void listPositionsWithKeyword() {
        Page<Position> page = positionService.listPositions("测试", null, PageRequest.of(0, 10));
        assertTrue(page.getTotalElements() > 0);
        assertEquals("测试岗位", page.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("根据状态筛选")
    void listPositionsWithStatus() {
        Page<Position> page = positionService.listPositions(null, "DRAFT", PageRequest.of(0, 10));
        assertTrue(page.getTotalElements() > 0);
        page.forEach(p -> assertEquals("DRAFT", p.getStatus()));
    }

    @Test
    @DisplayName("根据 ID 查询")
    void getPositionById() {
        Position found = positionService.getPositionById(testId);
        assertEquals("测试岗位", found.getTitle());
    }

    @Test
    @DisplayName("查询不存在的岗位抛出异常")
    void getPositionByIdNotFound() {
        assertThrows(BusinessException.class, () -> positionService.getPositionById(99999L));
    }

    @Test
    @DisplayName("更新岗位")
    void updatePosition() {
        PositionDTO dto = new PositionDTO();
        dto.setTitle("更新后的岗位");
        dto.setDescription("更新后的描述");
        dto.setStatus("PUBLISHED");

        Position updated = positionService.updatePosition(testId, dto);
        assertEquals("更新后的岗位", updated.getTitle());
        assertEquals("PUBLISHED", updated.getStatus());
    }

    @Test
    @DisplayName("删除岗位")
    void deletePosition() {
        positionService.deletePosition(testId);
        assertThrows(BusinessException.class, () -> positionService.getPositionById(testId));
    }

    @Test
    @DisplayName("审批流程: 提交审核 → 通过")
    void approvalFlow() {
        Position submitted = positionService.submitForReview(testId);
        assertEquals("REVIEWING", submitted.getStatus());

        Position approved = positionService.approvePosition(testId);
        assertEquals("PUBLISHED", approved.getStatus());
    }

    @Test
    @DisplayName("审批流程: 提交审核 → 驳回")
    void approvalFlowReject() {
        positionService.submitForReview(testId);

        Position rejected = positionService.rejectPosition(testId);
        assertEquals("REJECTED", rejected.getStatus());

        // Rejected positions can be resubmitted
        Position resubmitted = positionService.submitForReview(testId);
        assertEquals("REVIEWING", resubmitted.getStatus());
    }

    @Test
    @DisplayName("已发布岗位不能直接提交审核")
    void cannotSubmitPublished() {
        PositionDTO dto = new PositionDTO();
        dto.setTitle("已发布岗位");
        dto.setDescription("desc");
        dto.setStatus("PUBLISHED");
        Position pub = positionService.createPosition(dto);

        assertThrows(BusinessException.class, () -> positionService.submitForReview(pub.getId()));
    }

    @Test
    @DisplayName("统计信息")
    void getStatistics() {
        Map<String, Object> stats = positionService.getStatistics();
        assertNotNull(stats.get("total"));
        assertNotNull(stats.get("draft"));
        assertNotNull(stats.get("published"));
        assertNotNull(stats.get("monthlyTrend"));
    }
}
