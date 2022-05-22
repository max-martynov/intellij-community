// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.evaluable.completion

import com.intellij.cce.evaluable.StrategyBuilder
import com.intellij.cce.evaluable.StrategyBuilder.Companion.getOrThrow

class CompletionStrategyBuilder : StrategyBuilder<CompletionStrategy> {
  override fun build(map: Map<String, Any>): CompletionStrategy {
    val completionType = CompletionType.valueOf(map.getOrThrow<String>("completionType"))
    val context = CompletionContext.valueOf(map.getOrThrow<String>("context"))
    return CompletionStrategy(completionType, CompletionPrefix.NoPrefix, context, mapOf())
  }
}