// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.generalization

import com.intellij.cce.actions.CodeGolfEmulation
import com.intellij.cce.actions.CompletionType
import com.intellij.cce.actions.UserEmulator
import com.intellij.cce.core.Language
import com.intellij.cce.core.Lookup
import com.intellij.cce.interpreter.BasicActionsInvoker
import com.intellij.openapi.project.Project

class FeatureInvoker(
  private val project: Project,
  private val language: Language,
  completionType: CompletionType,
  userEmulationSettings: UserEmulator.Settings?,
  private val codeGolfSettings: CodeGolfEmulation.Settings?) : BasicActionsInvoker(project, language, completionType,
                                                                                   userEmulationSettings, codeGolfSettings) {
  override fun callFeature(expectedText: String, prefix: String?): Lookup {
    TODO("Not yet implemented")
  }

  override fun finishSession(expectedText: String, prefix: String): Boolean {
    TODO("Not yet implemented")
  }

}