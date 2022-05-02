// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.generalization

import com.intellij.cce.actions.CodeGolfEmulation
import com.intellij.cce.actions.CompletionType
import com.intellij.cce.actions.UserEmulator
import com.intellij.cce.core.Language
import com.intellij.cce.evaluation.SetupSdkStep
import com.intellij.cce.interpreter.ActionsInvoker
import com.intellij.cce.processor.GenerateActionsProcessor
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

interface EvaluableFeature {
  companion object {
    private val EP_NAME = ExtensionPointName.create<EvaluableFeature>("com.intellij.cce.evaluableFeature")

    fun forFeature(project: Project, featureName: String): EvaluableFeature? {
      return EP_NAME.getExtensionList(project).singleOrNull { it.name == featureName }
    }

  }

  val name: String

  val generateActionsProcessor: GenerateActionsProcessor

  fun getActionsInvoker(project: Project,
                        language: Language,
                        completionType: CompletionType,
                        userEmulationSettings: UserEmulator.Settings?,
                        codeGolfSettings: CodeGolfEmulation.Settings?) : ActionsInvoker
}