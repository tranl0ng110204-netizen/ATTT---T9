package com.example.backend.repository;

import com.example.backend.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentRepository extends JpaRepository<Assessment,Long> {
}
