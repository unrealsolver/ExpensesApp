package net.expensesapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class PaymentListAdapter(context :Context, items :Array<Payment>) :
        ArrayAdapter<Payment>(context, R.layout.payment_item, items) {

    // Should be defined somewhere else (probably)
    val pfmt = android.text.format.DateFormat.getDateFormat(context)
    val ptfmt = android.text.format.DateFormat.getTimeFormat(context)

    override fun getView(position: Int, convertView :View?, parent :ViewGroup?) :View {
        val payment = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.payment_item, null)

        (view.findViewById(R.id.payment_amount) as TextView).setText(
                "${longToStr(payment.amount)} ${payment.currency}"
        )
        (view.findViewById(R.id.payment_datetime) as TextView).setText(
                "${pfmt.format(payment.datetime.toDate())} " +
                        "${ptfmt.format(payment.datetime.toDate())}"
        )
        (view.findViewById(R.id.payment_organization) as TextView).setText(
                payment.organization
        )

        return view
    }
}
