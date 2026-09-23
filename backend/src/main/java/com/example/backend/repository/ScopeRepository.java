package com.example.backend.repository;

import com.example.backend.entity.Scope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScopeRepository extends JpaRepository<Scope,Long> {
    List<Scope> findByAssessmentId(Long assessmentId);
}
