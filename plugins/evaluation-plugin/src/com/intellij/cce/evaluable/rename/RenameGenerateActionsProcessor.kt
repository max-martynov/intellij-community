// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.evaluable.rename

import com.intellij.cce.actions.*
import com.intellij.cce.core.CodeFragment
import com.intellij.cce.core.CodeToken
import com.intellij.cce.core.TypeProperty
import com.intellij.cce.processor.GenerateActionsProcessor

class RenameGenerateActionsProcessor(
  private val strategy: RenameStrategy
) : GenerateActionsProcessor() {

  override fun process(code: CodeFragment) {
    for (token in code.getChildren()) {
      processToken(token)
    }
  }

  private fun processToken(token: CodeToken) {
    if (!checkFilters(token))
      return
    addAction(DeleteRange(token.offset, token.offset + token.length))
    addAction(MoveCaret(token.offset))
    addAction(PrintText(strategy.placeholderName, false))
    addAction(CallFeature("", token.text, token.offset, token.properties))
    addAction(FinishSession())
    addAction(DeleteRange(token.offset, token.offset + strategy.placeholderName.length, false))
    addAction(PrintText(token.text, false))
  }

  private fun checkFilters(token: CodeToken) = strategy.filters.all { it.value.shouldEvaluate(token.properties) }

}