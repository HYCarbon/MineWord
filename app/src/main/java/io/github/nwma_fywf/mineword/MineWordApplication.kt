package io.github.nwma_fywf.mineword

import android.app.Application
import io.github.nwma_fywf.mineword.data.local.WordDatabase
import io.github.nwma_fywf.mineword.data.repository.WordRepository

class MineWordApplication : Application() {
    val database by lazy { WordDatabase.getDatabase(this) }
    val repository by lazy { WordRepository(database.wordDao(), database.meaningDao()) }
}
