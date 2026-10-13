package com.example.backend.repository;

import com.example.backend.entity.Finding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FindingRepository  extends JpaRepository<Finding,Long> {
    List<Finding> findByAssessmentId(Long assessmentId);
}
