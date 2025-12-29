package com.finding_a_partner.gateway_service.config

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class WebSocketLoggingFilter : GlobalFilter, Ordered {

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val path = request.uri.path
        val method = request.method?.name() ?: "UNKNOWN"
        
        return chain.filter(exchange).then(Mono.fromRunnable {
            val response = exchange.response
            if (path.startsWith("/ws")) {
                println("[WebSocketLoggingFilter] WebSocket response status: ${response.statusCode}")
                println("[WebSocketLoggingFilter] Response headers:")
                response.headers.forEach { name, values ->
                    println("[WebSocketLoggingFilter]   $name: ${values.joinToString(", ")}")
                }
            }
        })
    }

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE + 1
    }
}

