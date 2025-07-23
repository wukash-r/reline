package com.traanite.reline.locking

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime

@Service
class OperationLockService(
    private val operationLockRepository: OperationLockRepository
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(OperationLockService::class.java)
        val LOCK_THRESHOLD: Duration = Duration.ofHours(2)
    }

    fun findAll(): Flux<OperationLock> {
        log.info("Retrieving all operation locks")
        return operationLockRepository.findAll()
    }

    fun lock(operationName: String): Mono<OperationLock> {
        log.info("Locking operation: $operationName")
        val lock = OperationLock(operationName = operationName, lockedAt = LocalDateTime.now())
        return operationLockRepository.save(lock)
    }

    fun unlock(operationName: String): Mono<Void> {
        log.info("Unlocking operation: $operationName")
        return operationLockRepository.removeByOperationName(operationName)
    }

    @Scheduled(cron = "0 0 * * * *")
    private fun cleanExpiredLocks() {
        log.info("Cleaning expired locks")
        val expirationTime = LocalDateTime.now().minus(LOCK_THRESHOLD)
        operationLockRepository.removeByLockedAtBefore(expirationTime)
            .doOnSuccess {
                log.info("Expired locks cleaned successfully")
            }
            .doOnError { error ->
                log.error("Error cleaning expired locks", error)
            }
            .subscribe()
    }
}