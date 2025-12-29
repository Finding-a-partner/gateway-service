package com.finding_a_partner.gateway_service.security

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*
import kotlinx.coroutines.runBlocking
import org.springframework.web.reactive.function.client.WebClient

//@Component
//class PublicKeyProvider(
//    @Value("\${auth-service.url}") private val authServiceUrl: String,
//    private val webClientBuilder: WebClient.Builder,
//) {
//    @Volatile
//    private var publicKey: PublicKey? = null
//
//    @PostConstruct
//    fun loadKey() {
//        runBlocking {
//            val webClient = webClientBuilder.build()
//
//            val pem = webClient.get()
//                .uri("$authServiceUrl/.well-known/public-key")
//                .retrieve()
//                .bodyToMono(String::class.java)
//                .block()
//
//            publicKey = pemToPublicKey(pem!!)
//        }
//    }
//
//    fun getKey(): PublicKey {
//        return publicKey ?: throw IllegalStateException("Public key not loaded")
//    }
//
//    private fun pemToPublicKey(pem: String): PublicKey {
//        val cleanPem = pem
//            .replace("-----BEGIN PUBLIC KEY-----", "")
//            .replace("-----END PUBLIC KEY-----", "")
//            .replace("\\s+".toRegex(), "")
//        val decoded = Base64.getDecoder().decode(cleanPem)
//        val spec = X509EncodedKeySpec(decoded)
//        val factory = KeyFactory.getInstance("RSA")
//        return factory.generatePublic(spec)
//    }
//}
