package net.expensesapp.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*


class MyDBOpenHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "expenses_app", null, 1) {
    companion object {
        private var instance: MyDBOpenHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): MyDBOpenHelper {
            if (instance == null) {
                instance = MyDBOpenHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTable("expenses", true,
                "id" to SqlType.create("INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE"),
                "amount" to INTEGER,
                "currency" to TEXT,
                "datetime" to INTEGER
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

val Context.database: MyDBOpenHelper
    get() = MyDBOpenHelper.getInstance(applicationContext)