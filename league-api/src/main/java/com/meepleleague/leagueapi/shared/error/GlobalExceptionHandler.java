package com.meepleleague.leagueapi.shared.error;

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
        log.warn("Data integrity violation", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, CONFLICT_DETAIL);

        problem.setProperty("code", "DATA_INTEGRITY_VIOLATION");

        return problem;
    }

}
