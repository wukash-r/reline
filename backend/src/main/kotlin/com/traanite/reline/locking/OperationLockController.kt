package com.traanite.reline.locking

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/locks")
class OperationLockController(private val operationLockService: OperationLockService) {

    @PostMapping("/unlock/{operationName}")
    fun unlockOperation(@PathVariable operationName: String): Mono<Void> {
        return operationLockService.unlock(operationName)
    }


}