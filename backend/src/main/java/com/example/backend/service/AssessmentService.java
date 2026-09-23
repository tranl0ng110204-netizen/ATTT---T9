package com.example.backend.service;

import com.example.backend.dto.AssessmentRequest;
import com.example.backend.dto.AssessmentResponse;
import com.example.backend.entity.Assessment;
import com.example.backend.entity.Enum.AssessmentType;
import com.example.backend.entity.Enum.ScopeType;
import com.example.backend.entity.Scope;
import com.example.backend.repository.AssessmentRepository;
import com.example.backend.validation.AssessmentValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentValidation assessmentValidation;

    @Transactional
    public AssessmentResponse create(AssessmentRequest request) {
        // 1. Validate từng target
        if (request.targets() == null || request.targets().isEmpty()) {
            throw new IllegalArgumentException("Cần ít nhất một mục tiêu (target)");
        }

        for (String target : request.targets()) {
            assessmentValidation.validate(request.type().name(), target);
        }

        // 2. Tạo Assessment
        Assessment assessment = Assessment.builder()
                .name(request.name())
                .type(request.type())
                .build();

        // 3. Tạo Scope cho mỗi target
        for (String target : request.targets()) {
            ScopeType scopeType = determineScopeType(request.type(), target);

            Scope scope = Scope.builder()
                    .assessment(assessment)
                    .target(target)
                    .targetType(scopeType)
                    .build();

            assessment.getScopes().add(scope);
        }

        // 4. Lưu (Cascade sẽ tự lưu Scope)
        Assessment saved = assessmentRepository.save(assessment);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> getAll() {
        return assessmentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssessmentResponse getById(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy Assessment với ID: " + id
                ));
        return toResponse(assessment);
    }

    /**
     * Tự động xác định ScopeType dựa trên AssessmentType và nội dung target
     */
    private ScopeType determineScopeType(AssessmentType type, String target) {
        if (type == AssessmentType.WEB) {
            return ScopeType.URL;
        }
        // SERVER: phân biệt IP đơn hay CIDR
        if (target.contains("/")) {
            return ScopeType.CIDR;
        }
        return ScopeType.IP;
    }

    private AssessmentResponse toResponse(Assessment entity) {
        return new AssessmentResponse(
                entity.getId(),
                entity.getName(),
                entity.getType(),
                entity.getCreatedAt()
        );
    }
}
