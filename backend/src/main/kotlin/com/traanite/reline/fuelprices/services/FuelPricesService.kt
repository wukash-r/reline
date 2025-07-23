package com.traanite.reline.fuelprices.services

import com.traanite.reline.currency.CurrencyConverter
import com.traanite.reline.fuelprices.model.CountryFuelPriceData
import com.traanite.reline.fuelprices.model.LatestCountryFuelPriceData
import com.traanite.reline.fuelprices.repository.FuelAggregationRepository
import com.traanite.reline.fuelprices.repository.FuelPricesRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@Service
class FuelPricesService(
    private val pricesRepository: FuelPricesRepository,
    private val aggregationRepository: FuelAggregationRepository,
    private val currencyConverter: CurrencyConverter
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(FuelPricesService::class.java)
    }

    fun saveAll(fuelPriceData: List<CountryFuelPriceData>): Flux<CountryFuelPriceData> {
        log.debug ("Saving fuel prices" )
        return pricesRepository.saveAll(fuelPriceData)
    }

    fun findAllInWithCurrencyConversion(currency: Currency): Flux<CountryFuelPriceDataDto> {
        log.debug("Finding all fuel prices in currency: {}", currency)
        return aggregationRepository.findLatestFuelPriceData()
            .flatMap { toCurrencyConvertedDto(it, currency) }
    }

    private fun toCurrencyConvertedDto(
        fuelPriceData: LatestCountryFuelPriceData,
        currency: Currency
    ): Mono<CountryFuelPriceDataDto> {
        return currencyConverter.convertToCurrency(
            fuelPriceData.gasolineData.price,
            fuelPriceData.gasolineData.currency,
            currency
        ).zipWith(
            currencyConverter.convertToCurrency(
                fuelPriceData.dieselData.price,
                fuelPriceData.dieselData.currency,
                currency
            )
        ).map {
            CountryFuelPriceDataDto(
                fuelPriceData.id.value,
                currency.currencyCode,
                it.t1.setScale(2, RoundingMode.HALF_EVEN),
                it.t2.setScale(2, RoundingMode.HALF_EVEN)
            )
        }
    }

}

data class CountryFuelPriceDataDto(
    val country: String,
    val currencyCode: String,
    val gasolinePrice: BigDecimal,
    val dieselPrice: BigDecimal
)