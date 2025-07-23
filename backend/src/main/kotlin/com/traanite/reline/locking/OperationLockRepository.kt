package com.traanite.reline.locking

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface OperationLockRepository : ReactiveMongoRepository<OperationLock, String> {

    fun removeByOperationName(operationName: String): Mono<Void>

    fun removeByLockedAtBefore(before: LocalDateTime): Mono<Void>
}