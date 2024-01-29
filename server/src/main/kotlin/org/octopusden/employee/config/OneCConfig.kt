package org.octopusden.employee.config

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Feign
import feign.Logger
import feign.Request
import feign.httpclient.ApacheHttpClient
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.octopusden.employee.service.onec.client.OneCBasicCredTokenRequestInterceptor
import org.octopusden.employee.service.onec.client.OneCClient
import org.octopusden.employee.service.onec.client.OneCRetry
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@EnableConfigurationProperties(OneCProperties::class)
class OneCConfig(
    private val oneCProperties: OneCProperties,
    private val objectMapper: ObjectMapper
) {

    @Bean
    fun oneCClient(): OneCClient {
        return Feign.builder()
            .client(ApacheHttpClient())
            .options(Request.Options(5, TimeUnit.MINUTES, 5, TimeUnit.MINUTES, true))
            .encoder(JacksonEncoder(objectMapper))
            .decoder(JacksonDecoder(objectMapper))
            .retryer(OneCRetry())
            .requestInterceptor(OneCBasicCredTokenRequestInterceptor(oneCProperties.username, oneCProperties.password))
            .logger(Slf4jLogger(OneCClient::class.java))
            .logLevel(Logger.Level.BASIC)
            .target(OneCClient::class.java, oneCProperties.host)
    }
}
