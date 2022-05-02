// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.generalization.rename

import com.intellij.cce.actions.*
import com.intellij.cce.core.CodeFragment
import com.intellij.cce.core.CodeToken
import com.intellij.cce.core.TypeProperty
import com.intellij.cce.processor.GenerateActionsProcessor

class RenameGenerateActionsProcessor : GenerateActionsProcessor() {

  override fun process(code: CodeFragment) {
    for (token in code.getChildren()) {
      processToken(token)
    }
  }

  private fun processToken(token: CodeToken) {
    if (token.properties.tokenType != TypeProperty.VARIABLE_DECLARATION)
      return
    addAction(DeleteRange(token.offset, token.offset + token.length))
    addAction(MoveCaret(token.offset))
    val defaultName = "variable"
    addAction(PrintText(defaultName, false))
    addAction(CallFeature("", token.text, token.properties))
    addAction(FinishSession())
    addAction(DeleteRange(token.offset, token.offset + defaultName.length, false))
    addAction(PrintText(token.text, false))
  }

}