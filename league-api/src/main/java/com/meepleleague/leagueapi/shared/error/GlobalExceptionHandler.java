package com.meepleleague.leagueapi.shared.error;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Traduit toute exception en {@link ProblemDetail} (RFC 9457) portant deux
 * propriétés d'extension stables pour le front :
 * <ul>
 * <li>{@code code} — un {@link ErrorCode} sur lequel le client branche sa logique ;</li>
 * <li>{@code traceId} — identifiant repris dans la ligne de log correspondante.</li>
 * </ul>
 * Les échecs de validation ajoutent {@code errors}, la liste des champs en faute.
 * <p>
 * Étendre {@link ResponseEntityExceptionHandler} désactive le handler par défaut
 * de Spring Boot ({@code @ConditionalOnMissingBean}) : les exceptions du framework
 * (400, 404, 405, 415…) transitent par {@link #handleExceptionInternal} et
 * reçoivent le même contrat que les erreurs métier.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String CODE = "code";
    private static final String TRACE_ID = "traceId";
    private static final String ERRORS = "errors";

    // Noms générés par PostgreSQL pour les contraintes UNIQUE de V002__create_table_player.sql
    private static final Map<String, ConstraintMapping> CONSTRAINT_MAPPINGS = Map.of(
            "player_username_key", new ConstraintMapping(ErrorCode.USERNAME_ALREADY_EXISTS, UserMessages.USERNAME_ALREADY_EXISTS),
            "player_email_key", new ConstraintMapping(ErrorCode.EMAIL_ALREADY_EXISTS, UserMessages.EMAIL_ALREADY_EXISTS));

    /** Un champ en faute, tel qu'exposé au client : jamais la valeur rejetée. */
    public record FieldViolation(String field, String message) {
    }

    private record ConstraintMapping(ErrorCode code, String message) {
    }

    // ---- Règles métier ---------------------------------------------------------

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex) {
        ProblemDetail problem = problem(ex.getCode(), ex.getMessage());
        log.debug("[{}] business error {}: {}", traceIdOf(problem), ex.getCode(), ex.getMessage());
        return problem;
    }

    // ---- Contraintes de base ---------------------------------------------------

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        String constraintName = ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException cve
                ? cve.getConstraintName()
                : null;
        ConstraintMapping mapping = constraintName == null ? null : CONSTRAINT_MAPPINGS.get(constraintName);

        if (mapping != null) {
            ProblemDetail problem = problem(mapping.code(), mapping.message());
            log.debug("[{}] constraint {} violated", traceIdOf(problem), constraintName);
            return problem;
        }

        // Contrainte inconnue de la table ci-dessus : le client reçoit un 409 générique,
        // le détail technique reste ici.
        ProblemDetail problem = problem(ErrorCode.DATA_INTEGRITY_VIOLATION, UserMessages.CONFLICT);
        log.warn("[{}] unmapped data integrity violation (constraint={})", traceIdOf(problem), constraintName, ex);
        return problem;
    }

    // ---- Validation des corps de requête (@Valid) ------------------------------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
                .sorted(Comparator.comparing((FieldViolation v) -> v.field())
                        .thenComparing(v -> v.message(), Comparator.nullsLast(Comparator.<String>naturalOrder())))
                .toList();

        ProblemDetail problem = problem(ErrorCode.VALIDATION_ERROR, UserMessages.VALIDATION_ERROR);
        problem.setProperty(ERRORS, violations);
        log.debug("[{}] validation failed: {}", traceIdOf(problem), violations);
        return ResponseEntity.status(status).headers(headers).body(problem);
    }

    // ---- Exceptions du framework (JSON illisible, route inconnue, 405, 415…) ---

    @Override
    @Nullable
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response != null && response.getBody() instanceof ProblemDetail problem) {
            tag(problem, codeFor(statusCode));
            if (statusCode.is5xxServerError()) {
                log.error("[{}] {}", traceIdOf(problem), ex.getMessage(), ex);
            } else {
                log.debug("[{}] {} -> {}", traceIdOf(problem), ex.getClass().getSimpleName(), statusCode.value());
            }
        }
        return response;
    }

    // ---- Tout le reste : un 500 au format du contrat, la stacktrace en log -----

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        ProblemDetail problem = problem(ErrorCode.INTERNAL_ERROR, UserMessages.INTERNAL_ERROR);
        log.error("[{}] unexpected error", traceIdOf(problem), ex);
        return problem;
    }

    // ---- Construction ----------------------------------------------------------

    private static ProblemDetail problem(ErrorCode code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(statusOf(code), detail);
        tag(problem, code);
        return problem;
    }

    private static void tag(ProblemDetail problem, ErrorCode code) {
        problem.setProperty(CODE, code.name());
        problem.setProperty(TRACE_ID, UUID.randomUUID().toString());
    }

    private static String traceIdOf(ProblemDetail problem) {
        Map<String, Object> properties = problem.getProperties();
        return properties == null ? "-" : String.valueOf(properties.get(TRACE_ID));
    }

    // Un code, un statut. Pas de default : ajouter un ErrorCode sans le traiter ne compile pas.
    private static HttpStatus statusOf(ErrorCode code) {
        return switch (code) {
            case VALIDATION_ERROR, BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case PLAYER_NOT_FOUND, NOT_FOUND -> HttpStatus.NOT_FOUND;
            case METHOD_NOT_ALLOWED -> HttpStatus.METHOD_NOT_ALLOWED;
            case USERNAME_ALREADY_EXISTS, EMAIL_ALREADY_EXISTS, DATA_INTEGRITY_VIOLATION -> HttpStatus.CONFLICT;
            case UNSUPPORTED_MEDIA_TYPE -> HttpStatus.UNSUPPORTED_MEDIA_TYPE;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    // Sens inverse, pour les exceptions dont le framework a déjà fixé le statut.
    private static ErrorCode codeFor(HttpStatusCode status) {
        return switch (status.value()) {
            case 404 -> ErrorCode.NOT_FOUND;
            case 405 -> ErrorCode.METHOD_NOT_ALLOWED;
            case 415 -> ErrorCode.UNSUPPORTED_MEDIA_TYPE;
            default -> status.is4xxClientError() ? ErrorCode.BAD_REQUEST : ErrorCode.INTERNAL_ERROR;
        };
    }
}
