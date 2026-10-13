package com.example.backend.repository;

import com.example.backend.entity.ActionStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionStepRepository extends JpaRepository<ActionStep,Long> {
    List<ActionStep> findByAssessmentId(Long assessmentId);
}
