package com.traanite.reline.currency

import jakarta.annotation.PostConstruct
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.MathContext
import java.util.*

@Service
class CurrencyConverter(
    private val currencyExchangeApiClient: CurrencyExchangeApiClient,
    private val currencyExchangeRatesRepository: CurrencyExchangeRatesRepository
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(CurrencyConverter::class.java)
    }

    @PostConstruct
    private fun init() {
        updateCurrencyPrices()
    }

    @Scheduled(cron = "0 0 4 * * *")
    private fun updateCurrencyPrices() {
        log.info("CurrencyConverter initialized")
        currencyExchangeApiClient.currencyExchangeRates()
            .flatMap {
                val currencyExchangeRates =
                    CurrencyExchangeRates(ObjectId(), it.timestamp, Currency.getInstance(it.base), it.rates)
                currencyExchangeRatesRepository.save(currencyExchangeRates)
            }
            .subscribe()
    }

    fun convertToCurrency(amount: BigDecimal, fromCurrency: Currency, toCurrency: Currency): Mono<BigDecimal> {
        if (fromCurrency == toCurrency) {
            return Mono.just(amount)
        }
        return Mono.defer {
            currencyExchangeRatesRepository.findFirstByOrderByIdDesc()
                .map {
                    if (it.baseCurrency == fromCurrency) {
                        it.rates.getOrDefault(toCurrency.currencyCode, BigDecimal.ZERO)
                            .multiply(amount)
                    } else {
                        val amountInBaseCurrency = convertToBaseCurrency(it, fromCurrency, amount)
                        it.rates.getOrDefault(toCurrency.currencyCode, BigDecimal.ZERO)
                            .multiply(amountInBaseCurrency)
                    }
                }
                .doOnError {
                    log.error(
                        "Error converting currency. amount=${amount}, fromCurrency=${fromCurrency}, toCurrency=${toCurrency}",
                        it
                    )
                }
        }
    }

    fun availableCurrencies(): Flux<CurrencyData> {
        return currencyExchangeApiClient.availableCurrencies()
            .flatMapIterable { currenciesResponse ->
                currenciesResponse.entries.map { mapEntry ->
                    CurrencyData(mapEntry.key, mapEntry.value)
                }
            }
    }

    private fun convertToBaseCurrency(
        currencyExchangeRates: CurrencyExchangeRates,
        fromCurrency: Currency,
        amount: BigDecimal
    ): BigDecimal {
        return currencyExchangeRates.rates[fromCurrency.currencyCode]
            .let { rate ->
                if (rate == null) {
                    log.error("No rate found for currency: ${fromCurrency.currencyCode}")
                    BigDecimal.ZERO
                } else {
                    amount.divide(rate, MathContext.DECIMAL32)
                }
            }
    }

}

data class CurrencyData(
    val code: String,
    val name: String
)