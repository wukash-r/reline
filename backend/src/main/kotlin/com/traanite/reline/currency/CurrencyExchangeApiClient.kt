package com.traanite.reline.currency

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Service
class CurrencyExchangeApiClient(
    private val webClient: WebClient,
    private val currencyApiProperties: CurrencyApiProperties
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(CurrencyExchangeApiClient::class.java)
    }

    @Cacheable("currencyExchangeRates")
    fun currencyExchangeRates(): Mono<CurrencyExchangeRatesResponse> {
        log.info("Retrieving currency exchange rates")
        return webClient.get()
            .uri { uriBuilder ->
                uriBuilder.path(currencyApiProperties.endpoints.exchangeRates)
                    .queryParam("app_id", currencyApiProperties.apiKey)
                    .build()
            }
            .retrieve()
            .bodyToMono(CurrencyExchangeRatesResponse::class.java)
            .doOnNext {
                log.info("CurrencyConversionResponse: $it")
            }
    }

    @Cacheable("availableCurrencies")
    fun availableCurrencies(): Mono<Map<String, String>> {
        log.info("Retrieving available currencies")
        return webClient.get()
            .uri { uriBuilder ->
                uriBuilder.path(currencyApiProperties.endpoints.currencies)
                    .queryParam("app_id", currencyApiProperties.apiKey)
                    .build()
            }
            .retrieve()
            .bodyToMono<Map<String, String>>()
            .doOnNext {
                log.info("Currencies: $it")
            }
    }
}

data class CurrencyExchangeRatesResponse(
    val timestamp: Int,
    val base: String,
    val rates: Map<String, BigDecimal>
)
