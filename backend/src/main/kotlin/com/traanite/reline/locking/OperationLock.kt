package com.traanite.reline.locking

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
class OperationLock(
    val objectId: ObjectId? = null,
    @Indexed(unique = true)
    val operationName: String,
    val lockedAt: LocalDateTime
)