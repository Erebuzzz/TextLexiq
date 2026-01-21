package com.textlexiq.llm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LlamaCppClient {

    suspend fun summarize(text: String): String = withContext(Dispatchers.IO) {
        // TODO: Bridge llama.cpp JNI bindings and execute inference.
        "Summary placeholder"
    }
}
