package com.meepleleague.leagueapi.shared.error;

/**
 * Vocabulaire d'erreur de l'API : la valeur de la propriété {@code code} de chaque
 * ProblemDetail. Stable et lisible par machine — c'est sur ce code, jamais sur
 * {@code detail}, qu'un client branche sa logique.
 * <p>
 * Ajouter une constante oblige à lui attribuer un statut dans
 * {@code GlobalExceptionHandler.statusOf} : le switch y est exhaustif, l'oubli ne
 * compile pas.
 */
public enum ErrorCode {

    // Règles métier
    PLAYER_NOT_FOUND,
    USERNAME_ALREADY_EXISTS,
    EMAIL_ALREADY_EXISTS,
    DATA_INTEGRITY_VIOLATION,

    // Requête mal formée ou mal adressée (statut choisi par le framework)
    VALIDATION_ERROR,
    BAD_REQUEST,
    NOT_FOUND,
    METHOD_NOT_ALLOWED,
    UNSUPPORTED_MEDIA_TYPE,

    // Défaut serveur
    INTERNAL_ERROR
}
