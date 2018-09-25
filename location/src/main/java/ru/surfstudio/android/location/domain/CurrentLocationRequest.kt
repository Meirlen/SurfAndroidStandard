package ru.surfstudio.android.location.domain

import ru.surfstudio.android.location.location_errors_resolver.resolutions.impl.concrete.no_location_permission.LocationPermissionRequest

/**
 * Запрос текущего местоположения.
 *
 * @param priority Приоритет запроса (точность метостоположения/заряд батареи), который дает Google Play Services знать,
 * какие источники данных использовать.
 * @param relevanceTimeoutMillis Таймаут, при котором последнее полученное местоположение актуально. Если
 * местоположение ещё актуально - запрос не инициируется, а сразу возвращается закешированное значение.
 * @param resolveLocationErrors Нужно ли решать проблемы с получением местоположения.
 * @param locationPermissionRequest Запрос разрешения на доступ к местоположению, используемый в
 * [NoLocationPermissionResolution].
 */
class CurrentLocationRequest(
        val priority: LocationPriority = LocationPriority.BALANCED_POWER_ACCURACY,
        val relevanceTimeoutMillis: Long = 5_000L,
        val resolveLocationErrors: Boolean = false,
        val locationPermissionRequest: LocationPermissionRequest = LocationPermissionRequest()
)