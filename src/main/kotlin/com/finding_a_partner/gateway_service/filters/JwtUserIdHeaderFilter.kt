package com.finding_a_partner.gateway_service.filters

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtUserIdHeaderFilter : GlobalFilter {
    private val secretKey = Keys.hmacShaKeyFor(
        "your-256-bit-secret-your-256-bit-secret-1234".toByteArray(),
    )

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            try {
                val claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .body

                val userId = claims["userId"] as? String ?: claims.subject

                val mutatedRequest = exchange.request.mutate()
                    .header("X-User-Id", userId)
                    .build()

                val mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build()

                return chain.filter(mutatedExchange)
            } catch (ex: Exception) {
                println("JWT parsing error: ${ex.message}")
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                return exchange.response.setComplete()
            }
        }

        return chain.filter(exchange)
    }
}
