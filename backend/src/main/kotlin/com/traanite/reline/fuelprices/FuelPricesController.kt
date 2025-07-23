package com.traanite.reline.fuelprices

import com.traanite.reline.currency.CurrencyExchangeApiClient
import com.traanite.reline.fuelprices.services.CountryFuelPriceDataDto
import com.traanite.reline.fuelprices.services.FuelPricesService
import com.traanite.reline.fuelprices.services.FuelPricesUpdater
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.*

@RestController
@RequestMapping("/fuelprices")
class FuelPricesController(
    private val fuelPricesService: FuelPricesService,
    private val fuelPricesUpdater: FuelPricesUpdater
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(FuelPricesController::class.java)
    }

    @GetMapping
    fun getStoredGasolinePrices(
        @RequestParam(name = "currencyCode", required = true, defaultValue = "EUR")
        currencyCode: String
    ): Mono<FuelPricesResponse> {
        return fuelPricesService.findAllInWithCurrencyConversion(Currency.getInstance(currencyCode.uppercase()))
            .sort(compareBy { it.country })
            .collectList()
            .map {
                val response = FuelPricesResponse(it)
                log.info("Returning fuel prices: $response")
                response
            }
    }

    @PostMapping("/update")
    fun updateFuelPrices(): Mono<Void> {
        fuelPricesUpdater.updateFuelPrices()
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
        log.info("Fuel prices update initiated")
        return Mono.empty()
    }

    data class FuelPricesResponse(val values: List<CountryFuelPriceDataDto>)
}
