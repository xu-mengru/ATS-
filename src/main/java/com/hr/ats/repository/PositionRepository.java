package com.hr.ats.repository;

import com.hr.ats.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long>, JpaSpecificationExecutor<Position> {

    List<Position> findByTitleContaining(String keyword);

    long countByStatus(String status);

    long countByCreateTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Position> findByStatus(String status);
}
