// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.evaluable.completion

import com.intellij.cce.core.Language
import com.intellij.cce.evaluable.EvaluableFeature
import com.intellij.cce.evaluable.StrategyBuilder
import com.intellij.cce.evaluable.StrategySerializer
import com.intellij.cce.interpreter.ActionsInvoker
import com.intellij.cce.processor.GenerateActionsProcessor
import com.intellij.openapi.project.Project


class CompletionFeature : EvaluableFeature<CompletionStrategy> {
  override val name = "completion"

  override fun getGenerateActionsProcessor(strategy: CompletionStrategy): GenerateActionsProcessor {
    return CompletionGenerateActionsProcessor(strategy)
  }


  override fun getActionsInvoker(project: Project, language: Language, strategy: CompletionStrategy): ActionsInvoker {
    return CompletionActionsInvoker(project, language, strategy)
  }

  private inline fun <reified T> Map<String, *>.getAs(key: String): T {
    check(key in this.keys) { "$key not found. Existing keys: ${keys.toList()}" }
    val value = this.getValue(key)
    check(value is T) { "Unexpected type in config" }
    return value
  }

  override fun getStrategyBuilder(): StrategyBuilder<CompletionStrategy> {
    return CompletionStrategyBuilder()
  }

  override fun getStrategySerializer(): StrategySerializer<CompletionStrategy> {
    TODO("Not yet implemented")
  }

}