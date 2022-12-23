package org.octopusden.employee.config

import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.LocalDate

@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addRedirectViewController("/", "swagger-ui/index.html")
    }

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(object : Converter<String, LocalDate> {
            override fun convert(source: String): LocalDate {
                return LocalDate.parse(source)
            }
        })
    }
}
