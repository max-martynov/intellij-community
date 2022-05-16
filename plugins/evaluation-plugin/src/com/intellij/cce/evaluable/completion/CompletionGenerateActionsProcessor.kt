// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.evaluable.completion

import com.intellij.cce.actions.*
import com.intellij.cce.core.CodeFragment
import com.intellij.cce.core.CodeToken
import com.intellij.cce.processor.GenerateActionsProcessor
import com.intellij.cce.processor.TextScopes


class CompletionGenerateActionsProcessor(private val strategy: CompletionStrategy) : GenerateActionsProcessor() {

  var previousTextStart = 0
  var text = ""

  override fun process(code: CodeFragment) {
    if (strategy.context == CompletionContext.ALL) {
      for (token in code.getChildren()) {
        processToken(token)
      }
      return
    }
    previousTextStart = code.offset
    text = code.text


    for (token in code.getChildren()) {
      processToken(token)
    }

    if (previousTextStart < code.offset + code.length) {

      val remainingText = this.text.substring(IntRange(previousTextStart, code.offset + code.length - 1))

      val scopes = TextScopes(remainingText)
      if (scopes.closedCount != 0) {
        addAction(DeleteRange(previousTextStart, previousTextStart + scopes.closedCount))
      }

      addAction(MoveCaret(previousTextStart))
      addAction(PrintText(remainingText))
    }
  }

  private val prefixCreator = when (strategy.prefix) {
    is CompletionPrefix.NoPrefix -> NoPrefixCreator()
    is CompletionPrefix.CapitalizePrefix -> CapitalizePrefixCreator(strategy.prefix.emulateTyping)
    is CompletionPrefix.SimplePrefix -> SimplePrefixCreator(strategy.prefix.emulateTyping, strategy.prefix.n)
  }

  private fun processToken(token: CodeToken) {
    if (!checkFilters(token)) return

    when (strategy.context) {
      CompletionContext.ALL -> prepareAllContext(token)
      CompletionContext.PREVIOUS -> preparePreviousContext(token)
    }

   // if (strategy.emulateUser) {
     // addAction(EmulateUserSession(token.text, token.properties))
    //} else {
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
  //  }
  }

  private fun prepareAllContext(token: CodeToken) {
    addAction(DeleteRange(token.offset, token.offset + token.length))
    addAction(MoveCaret(token.offset))
  }

  private fun preparePreviousContext(token: CodeToken) {
    if (previousTextStart <= token.offset) {
      val previousText = text.substring(IntRange(previousTextStart, token.offset - 1))

      val scopes = TextScopes(previousText)

      if (scopes.closedCount != 0) {
        addAction(DeleteRange(previousTextStart, previousTextStart + scopes.closedCount))
      }

      addAction(MoveCaret(previousTextStart))
      addAction(PrintText(previousText))

      if (scopes.reversedOpened.isNotEmpty()) {
        addAction(PrintText(scopes.reversedOpened))
      }

      previousTextStart = token.offset + token.length
      addAction(MoveCaret(token.offset))
    }
  }

  private fun checkFilters(token: CodeToken) = strategy.filters.all { it.value.shouldEvaluate(token.properties) }

}