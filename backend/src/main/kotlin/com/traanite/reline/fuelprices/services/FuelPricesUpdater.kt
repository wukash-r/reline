package com.traanite.reline.fuelprices.services

import com.traanite.reline.locking.OperationLockService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.PessimisticLockingFailureException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class FuelPricesUpdater(
    private val fuelPricesService: FuelPricesService,
    private val pricesScraper: GlobalPetrolPricesScraper,
    private val operationLockService: OperationLockService
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(FuelPricesUpdater::class.java)
        private const val FUEL_UPDATE_OPERATION_NAME = "FUEL_PRICES_UPDATE"
    }

    @Scheduled(cron = "\${fuel-prices.scheduled-update.cron}")
    private fun updateFuelPricesScheduledTask() {
        log.info("Updating fuel prices")
        updateFuelPrices().subscribe()
    }

    fun updateFuelPrices(): Mono<Void> {
        return operationLockService.lock(FUEL_UPDATE_OPERATION_NAME)
            .onErrorResume { error ->
                Mono.error { PessimisticLockingFailureException("Fuel prices update is already in progress") }
            }
            .flatMapMany {
                log.info("Start updating procedure")
                pricesScraper.getPrices()
            }
            .collectList()
            .flatMapMany { fuelPricesService.saveAll(it) }
            .collectList()
            .flatMap {
                log.info("Fuel prices update completed")
                operationLockService.unlock(FUEL_UPDATE_OPERATION_NAME)
            }
            .onErrorResume { error ->
                if (error is PessimisticLockingFailureException) {
                    log.info(error.message)
                    Mono.empty()
                } else {
                    log.error("Error during fuel prices update", error)
                    operationLockService.unlock(FUEL_UPDATE_OPERATION_NAME).then()
                }
            }
    }
}