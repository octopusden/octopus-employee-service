package org.octopusden.employee.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.octopusden.employee.service.jira.client.common.JiraBasicCredTokenRequestInterceptor
import org.octopusden.employee.service.jira.client.common.JiraErrorDecoder
import org.octopusden.employee.service.jira.client.jira2.Jira2Client
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
@EnableConfigurationProperties(Jira2Properties::class)
class Jira2Config(private val jira2Properties: Jira2Properties,
                  private val objectMapper: ObjectMapper) {
    @Bean
    fun jira2Client(): Jira2Client = builder()
        .client(ApacheHttpClient())
        .options(Request.Options(5, TimeUnit.MINUTES, 5, TimeUnit.MINUTES, true))
        .encoder(JacksonEncoder(objectMapper))
        .decoder(JacksonDecoder(objectMapper))
        .errorDecoder(JiraErrorDecoder(objectMapper))
        .requestInterceptor(JiraBasicCredTokenRequestInterceptor(jira2Properties.username, jira2Properties.password))
        .logger(Slf4jLogger(Jira2Client::class.java))
        .logLevel(Logger.Level.BASIC)
        .target(Jira2Client::class.java, jira2Properties.host)
}
