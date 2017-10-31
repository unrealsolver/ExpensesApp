package net.expensesapp

enum class AGGREGATION(val value :String) {
    NONE("None"),
    DATE("Disabled"),
    CATEGORY("Category"),
    SELLER("Seller")
}

enum class GRANULARITY(val value :String) {
    NONE("None"),
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month")
}
