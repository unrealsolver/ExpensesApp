package net.expensesapp

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import java.util.*


class SmsService {
    companion object {
        public fun readInbox(context: Context) :List<SMS> {
            val uri = Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, "inbox")
            val cursor = context.contentResolver.query(
                    uri,
                    arrayOf("_id", "address", "body", "type", "date"),
                    "address='Priorbank' AND body like '% Oplata %'",
                    null,
                    "date ASc"
            )

            val smses = LinkedList<SMS>()

            cursor.moveToLast()
            if (cursor.count > 0) {
                do {
                    smses.add(SMS(
                            cursor.getString(cursor.getColumnIndex("_id")),
                            cursor.getString(cursor.getColumnIndex("body")),
                            cursor.getString(cursor.getColumnIndex("address"))
                    ))
                } while (cursor.moveToPrevious())
            }

            cursor.close()

            return smses
        }
    }
}

