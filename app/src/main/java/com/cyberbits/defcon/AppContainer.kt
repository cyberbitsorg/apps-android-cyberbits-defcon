package com.cyberbits.defcon

import android.content.Context
import com.cyberbits.defcon.data.DefconDatabase

class AppContainer(val applicationContext: Context) {
    val database: DefconDatabase by lazy { DefconDatabase.build(applicationContext) }
}
