package com.finding_a_partner.gateway_service.filters

//import com.finding_a_partner.gateway_service.security.PublicKeyProvider
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*

@Component
class JwtUserIdHeaderFilter(
//    private val publicKeyProvider: PublicKeyProvider
) : GlobalFilter {

    private val publicKeyPem = """-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhSt++iZtpNDVR5eiwWVy
jnhUMLmnX4M4gSntOZIHaTexEvp5GNI43wMxgOp8XsEuUrKVp4ZA6XAEh6RC/p/A
Q2TWZlqV3YvuE5UosV59PcAdWEu9VZOItIUB94Dc0r6UY/cztw5pgmc+3yh0DQdU
h+XPDmbplHkrBnb6j1cc2+84M9KBUvU1lS17FJ2EIK1BxlXZz/L2dn6r1kA1XcM7
HPOFHHtwz8ZabNoqvrxeuI3dbCFdKQwszxIqEieYwArewo7+OFfbdJFvPx78objR
qS49sQRV896ImuzQNphJBdAqbaQw3QlnHLZl5Ej5LPv0Yu9XTeqjwpwXwbcB0NzT
ZwIDAQAB
-----END PUBLIC KEY-----
    """.trimIndent()

    private val publicKey: PublicKey by lazy {
        val cleaned = publicKeyPem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")

        val keyBytes = Base64.getDecoder().decode(cleaned)
        val spec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        keyFactory.generatePublic(spec)
    }

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val path = exchange.request.uri.path
        val method = exchange.request.method?.name() ?: "UNKNOWN"

        if (path.startsWith("/ws")) {
            val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                val token = authHeader.substring(7)
                try {
                    val claims = Jwts.parserBuilder()
//                    .setSigningKey(publicKeyProvider.getKey())
                        .setSigningKey(publicKey)
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
                    println("[Gateway JwtFilter] JWT parsing error: ${ex.message}")
                    ex.printStackTrace()
                    exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                    return exchange.response.setComplete()
                }
            } else {
                println("[Gateway JwtFilter] No Bearer token found, forwarding without X-User-Id")
            }
        }
        
        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            try {
                val claims = Jwts.parserBuilder()
//                    .setSigningKey(publicKeyProvider.getKey())
                    .setSigningKey(publicKey)
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
                println("[Gateway JwtFilter] JWT parsing error: ${ex.message}")
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                return exchange.response.setComplete()
            }
        }

        return chain.filter(exchange)
    }
}
