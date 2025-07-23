package com.traanite.reline.locking

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/locks")
class OperationLockController(private val operationLockService: OperationLockService) {


    @GetMapping()
    fun getAllLocks(): Mono<List<OperationLockResponse>> {
        return operationLockService.findAll()
            .map { lock -> OperationLockResponse(lock.operationName, lock.lockedAt.toString()) }
            .sort { lock1, lock2 -> lock1.operationName.compareTo(lock2.operationName) }
            .collectList()
    }

    @PostMapping("/unlock/{operationName}")
    fun unlockOperation(@PathVariable operationName: String): Mono<Void> {
        return operationLockService.unlock(operationName)
    }

    data class OperationLockResponse(val operationName: String, val lockedAt: String)
}