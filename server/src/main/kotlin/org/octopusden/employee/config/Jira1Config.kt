package org.octopusden.employee.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.octopusden.employee.service.jira.client.common.JiraBasicCredTokenRequestInterceptor
import org.octopusden.employee.service.jira.client.jira1.Jira1Client
import org.octopusden.employee.service.jira.client.common.JiraErrorDecoder
import feign.Feign.builder
import feign.Logger
import feign.Request
import feign.httpclient.ApacheHttpClient
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@EnableConfigurationProperties(Jira1Properties::class)
class Jira1Config(
    private val jira1Properties: Jira1Properties,
    private val objectMapper: ObjectMapper
) {

    @Bean
    fun jira1Client(): Jira1Client = builder()
        .client(ApacheHttpClient())
        .options(Request.Options(5, TimeUnit.MINUTES, 5, TimeUnit.MINUTES, true))
        .encoder(JacksonEncoder(objectMapper))
        .decoder(JacksonDecoder(objectMapper))
        .errorDecoder(JiraErrorDecoder(objectMapper))
        .requestInterceptor(JiraBasicCredTokenRequestInterceptor(jira1Properties.username, jira1Properties.password))
        .logger(Slf4jLogger(Jira1Client::class.java))
        .logLevel(Logger.Level.BASIC)
        .target(Jira1Client::class.java, jira1Properties.host)
}
