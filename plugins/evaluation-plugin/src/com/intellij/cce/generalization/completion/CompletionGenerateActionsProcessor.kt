// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.generalization.completion

import com.intellij.cce.actions.*
import com.intellij.cce.core.CodeFragment
import com.intellij.cce.core.CodeToken
import com.intellij.cce.core.TypeProperty
import com.intellij.cce.processor.GenerateActionsProcessor

class CompletionGenerateActionsProcessor : GenerateActionsProcessor() {

  override fun process(code: CodeFragment) {
    for (token in code.getChildren()) {
      processToken(token)
    }
  }

  private fun processToken(token: CodeToken) {
    if (token.properties.tokenType == TypeProperty.VARIABLE_DECLARATION)
      return
    addAction(DeleteRange(token.offset, token.offset + token.length))
    addAction(MoveCaret(token.offset))
    val prefixCreator = NoPrefixCreator()
    val prefix = prefixCreator.getPrefix(token.text)
    var currentPrefix = ""
    if (prefixCreator.completePrevious) {
      for (symbol in prefix) {
        addAction(CallFeature(currentPrefix, token.text, token.properties))
        addAction(PrintText(symbol.toString(), false))
        currentPrefix += symbol
      }
    }
    else if (prefix.isNotEmpty()) addAction(PrintText(prefix, false))
    addAction(CallFeature(prefix, token.text, token.properties))
    addAction(FinishSession())

    if (prefix.isNotEmpty())
      addAction(DeleteRange(token.offset, token.offset + prefix.length, true))
    addAction(PrintText(token.text, true))
  }

}