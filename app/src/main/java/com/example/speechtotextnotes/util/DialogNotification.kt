package com.example.speechtotextnotes.util

import android.app.AlertDialog
import android.content.Context

class DialogNotification {
    companion object {
        fun createDialog(
            context: Context,
            title: String,
            message: String
        ) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setCancelable(true)
            builder.show()
        }
    }
}