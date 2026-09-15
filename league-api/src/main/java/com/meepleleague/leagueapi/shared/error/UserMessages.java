package com.meepleleague.leagueapi.shared.error;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Textes destinés à l'utilisateur final : ils deviennent le champ {@code detail}
 * de la réponse, tels quels.
 * <p>
 * Règle : affichables sans transformation, en anglais, sans aucune information
 * interne (nom de contrainte, SQL, valeur saisie, état du serveur). Le diagnostic
 * va dans les logs, retrouvable par le {@code traceId} de la réponse.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMessages {

    public static final String PLAYER_NOT_FOUND = "Player of ID %d not found.";
    public static final String USERNAME_ALREADY_EXISTS = "Username already exists.";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists.";
    public static final String CONFLICT = "The request conflicts with an existing resource.";
    public static final String VALIDATION_ERROR = "Request validation failed. See 'errors' for details.";
    public static final String INTERNAL_ERROR = "An unexpected error occurred. Please try again later.";
}
