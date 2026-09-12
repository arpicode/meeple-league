package com.meepleleague.leagueapi.shared.error;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String CONFLICT_DETAIL = "The request conflicts with an existing resource.";
    // Map of postgreSQL default constraint names to error codes and details
    private static final Map<String, ConstraintError> CONSTRAINT_ERRORS = Map.of(
            "player_username_key", new ConstraintError("USERNAME_ALREADY_EXISTS", "Username already exists."),
            "player_email_key", new ConstraintError("EMAIL_ALREADY_EXISTS", "Email already exists."));

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException ex) {
        HttpStatus status = switch (ex.getCode()) {
            case PLAYER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
        };

        log.debug("Business error {} -> {}: {}", ex.getCode(), status.value(), ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problem.setProperty("code", ex.getCode().name());

        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {

        String codeValue = "DATA_INTEGRITY_VIOLATION";
        String detail = CONFLICT_DETAIL;

        if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException cve) {
            ConstraintError error = CONSTRAINT_ERRORS.get(cve.getConstraintName());
            if (error != null) {
                codeValue = error.code();
                detail = error.detail();
                log.debug("Data integrity violation: {}", detail);
            } else {
                log.warn("Unmapped constraint violation: {}", cve.getConstraintName(), ex);
            }
        } else {
            log.warn("Data integrity violation: {}", ex.getMessage(), ex);
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail);
        problem.setProperty("code", codeValue);

        return problem;
    }

}
