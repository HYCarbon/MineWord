package io.github.nwma_fywf.mineword.ui.util

import io.github.nwma_fywf.mineword.data.local.Meaning

object MeaningParser {
    fun groupByPos(meanings: List<Meaning>): Map<String?, List<Meaning>> {
        return meanings.groupBy { it.partOfSpeech }
    }
}
