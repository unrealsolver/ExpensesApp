package net.expensesapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.ListView
import android.widget.TextView
import net.expensesapp.db.MyDBOpenHelper
import net.expensesapp.db.database
import org.joda.time.LocalDateTime
import java.text.SimpleDateFormat
import java.util.*
import org.jetbrains.anko.db.*
import kotlin.collections.Grouping

fun groupCurrencies(payments: Iterable<Payment>): Map<String, List<Payment>> {
    return payments.groupingBy({ it.currency })
                     .fold(emptyList<Payment>(),
                        { acc: List<Payment>, el: Payment -> acc + el }
                    )
}

fun aggregateCurrencies(intermediate: Map<String, Iterable<Payment>>): Map<String, Long>{
    return intermediate.mapValues { ent -> ent.value.fold(0L, { acc, el -> acc + el.amount }) }
}

fun strToLong(value :String) :Long {
    return value.replace(".", "").toLong()
}

fun longToStr(value :Long) :String {
    return "${value / 100}.${value % 100}"
}

val fmt = SimpleDateFormat("dd-MM-yy hh:mm:ss")


class MainActivity : AppCompatActivity() {
    var granularity :GRANULARITY = GRANULARITY.MONTH
    // TODO Use memoization instad (to make context more distributed)
    var payments :List<Payment> = emptyList()

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.setChecked(true)

        // TODO that's insane
        granularity = when (item.itemId) {
            R.id.granularity_disabled -> GRANULARITY.NONE
            R.id.granularity_day -> GRANULARITY.DAY
            R.id.granularity_week -> GRANULARITY.WEEK
            R.id.granularity_month -> GRANULARITY.MONTH
            else -> granularity
        }

        update()
        return true
    }

    // FIXME It is period, not granularity
    fun createGranularityPredicate(level :GRANULARITY) :(Payment) -> Boolean {
        val now = Calendar.getInstance()
        val dateEl = findViewById(R.id.date) as TextView
        //dateEl.text = now.toString()

        val yearIsSame = {p: Payment -> p.datetime.year == now.get(Calendar.YEAR)}
        return when (level) {
            GRANULARITY.NONE -> {_ -> true }
            // TODO Chheck if Kotlin has neat composition like {p: Payment -> union(yearIsSame, monthIsSame) }
            GRANULARITY.DAY -> {p :Payment -> p.datetime.toDate() == now }
            GRANULARITY.WEEK -> {p :Payment -> (p.datetime.weekOfWeekyear == now.get(Calendar.WEEK_OF_YEAR) - 1) and yearIsSame(p) }
            GRANULARITY.MONTH -> {p :Payment -> (p.datetime.monthOfYear == now.get(Calendar.MONTH) + 1) and yearIsSame(p)}
        }
    }

    fun filterItems(items :List<Payment>) :List<Payment> {
        return items.filter(createGranularityPredicate(granularity))
    }

    fun displayItems(items :List<Payment>) {
        val mainListEl = findViewById(R.id.mainlist) as ListView
        val totalCountEl = findViewById(R.id.totalcount) as TextView
        val amountsEl = findViewById(R.id.amount) as TextView

        amountsEl.text = "Spendings: ${aggregateCurrencies(groupCurrencies(items))}"
        totalCountEl.text = "Total count: ${items.size}"
        mainListEl.adapter = PaymentListAdapter(this, items.toTypedArray<Payment>())
    }

    fun update() {
        displayItems(filterItems(payments))
    }

    // FIXME HACK
    var init = false
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // Init granularity
        // TODO remove init from layout
        if (!init) {
            (menu.findItem(R.id.granularity_month) as MenuItem).setChecked(true)
            init = true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val smses = SmsService.readInbox(this)

        val regex = Regex(
                // [     Date     ]  [     Time     ]
                """(\d\d-\d\d-\d\d)\s(\d\d:\d\d:\d\d)""" +
                //             [ Amount ]  [Cur]    [  Org ]
                """\.\sOplata\s(\d+\.\d+)\s(\w+)\.\s([\w+\s]+)"""
        )

        payments = smses.map(fun(d) :Payment? {
            val parts = regex.find(d.text)?.groupValues
            return if (parts != null) {
                Payment(
                        // TODO Use named groups
                        LocalDateTime(fmt.parse("${parts[1]} ${parts[2]}")),
                        strToLong(parts[3]),
                        parts[4],
                        parts[5]
                )
            } else null
        }).filterIsInstance<Payment>()

//        database.use {
//            for (d in payments) {
//                insert("expanses",
//                        "datetime" to d.datetime.millisOfSecond,
//                        "amount" to d.amount,
//                        "currency" to d.currency
//                )
//            }
//            select("expanses", "datetime", "amount", "currency")
//                .whereArgs(
//                        "datetime > {startTime} and datetime < {endTime}",
//                        "startTime" to 100,
//                        "endTime" to 200
//                ).exec { }
//
//        }

        payments.groupBy { it.datetime.toLocalDate() }

        update()
    }
}
