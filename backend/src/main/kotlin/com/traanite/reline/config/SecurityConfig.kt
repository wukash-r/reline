package com.traanite.reline.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration


@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun userDetailsService(): ReactiveUserDetailsService {
        // todo auth
        val userDetails = User.withDefaultPasswordEncoder()
            .username("user")
            .password("user")
            .roles("USER")
            .build()
        return MapReactiveUserDetailsService(userDetails)
    }

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.authorizeExchange {
                it
                    .pathMatchers("/actuator/**").permitAll()
                    .pathMatchers(HttpMethod.GET, "/currencies/**").permitAll()
                    .pathMatchers(HttpMethod.GET, "/fuelprices/**").permitAll()
                    .pathMatchers(HttpMethod.GET, "/locks/**").permitAll()
//                    .pathMatchers(HttpMethod.POST, "/fuelprices/**", "/locks/**").permitAll() // todo
                    .anyExchange().denyAll()
            }
            .httpBasic { }
            .cors {
                it.configurationSource {
                    val corsConfiguration = CorsConfiguration()
                    corsConfiguration.allowedOrigins = listOf("*")
                    corsConfiguration.allowedMethods = listOf("*")
                    corsConfiguration.allowedHeaders = listOf("*")
                    corsConfiguration
                }

            }
            .headers { headerSpec ->
                headerSpec.frameOptions { it.disable() }
            }
            .csrf { it.disable() } // TODO: Enable CSRF https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html
            .build()
    }

}
