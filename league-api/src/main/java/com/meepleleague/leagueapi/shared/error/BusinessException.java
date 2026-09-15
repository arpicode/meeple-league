package com.meepleleague.leagueapi.shared.error;

import lombok.Getter;

/**
 * Violation d'une règle métier, levée par les services.
 * <p>
 * Le {@code message} est le texte destiné à l'utilisateur (voir
 * {@link UserMessages}) : il devient le {@code detail} de la réponse sans
 * transformation. Ne jamais y mettre de diagnostic — pour ça, les logs.
 * <p>
 * Le statut HTTP n'est pas porté ici : il est dérivé du {@link ErrorCode} par le
 * {@code GlobalExceptionHandler}, ce qui garde la couche métier ignorante de HTTP.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode code;

    public BusinessException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }
}
