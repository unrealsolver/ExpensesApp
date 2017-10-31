package net.expensesapp

import org.joda.time.LocalDateTime

data class Payment(
    val datetime :LocalDateTime,
    val amount :Long,
    val currency :String,
    val organization :String // TODO mapping to category // TODO better name
)

