package com.traanite.reline.fuelprices.services

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import java.time.Duration
import java.util.Currency

@ConfigurationProperties(prefix = "fuel-prices.scraper")
data class FuelPricesScraperProperties(
    val currency: Currency,
    @NestedConfigurationProperty
    val rateLimit: RateLimitProperties,
    @NestedConfigurationProperty
    val site: SiteProperties
)

data class SiteProperties(
    val baseUrl: String,
    val dieselPricesEndpoint: String,
    val gasolinePricesEndpoint: String
)

data class RateLimitProperties(
    val refreshPeriod: Duration,
    val limitForPeriod: Int,
    val timeoutDuration: Duration,
)
