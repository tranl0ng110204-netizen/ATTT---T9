package com.example.backend.repository;

import com.example.backend.entity.ScanResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScanResultRepository extends JpaRepository<ScanResult,Long> {
    List<ScanResult> findByAssessmentId(Long assessmentId);
}
