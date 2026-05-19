package es.aviferdev.n3to.platform

import io.ktor.client.engine.HttpClientEngine

/**
 * Proporciona el engine de Ktor HttpClient específico de la plataforma.
 * Android: OkHttp. iOS: Darwin.
 */
expect fun httpClientEngine(): HttpClientEngine
