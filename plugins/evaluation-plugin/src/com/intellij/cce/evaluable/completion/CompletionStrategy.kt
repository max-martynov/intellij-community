// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.evaluable.completion

import com.intellij.cce.evaluable.EvaluationStrategy
import com.intellij.cce.filter.EvaluationFilter

data class CompletionStrategy(val completionType: CompletionType,
                              val prefix: CompletionPrefix,
                              val context: CompletionContext,
                              val filters: Map<String, EvaluationFilter>) : EvaluationStrategy()

sealed class CompletionPrefix(val emulateTyping: Boolean) {
  object NoPrefix : CompletionPrefix(false)
  class CapitalizePrefix(emulateTyping: Boolean) : CompletionPrefix(emulateTyping)
  class SimplePrefix(emulateTyping: Boolean, val n: Int) : CompletionPrefix(emulateTyping)
}

enum class CompletionType {
  BASIC,
  SMART,
  ML,
  FULL_LINE, // python only
  CLANGD, // c++ only
  RENAME
}

enum class CompletionContext {
  ALL,
  PREVIOUS
}
