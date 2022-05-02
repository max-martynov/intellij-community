// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.generalization.rename

import com.intellij.cce.actions.CodeGolfEmulation
import com.intellij.cce.actions.CompletionType
import com.intellij.cce.actions.UserEmulator
import com.intellij.cce.core.Language
import com.intellij.cce.generalization.EvaluableFeature
import com.intellij.cce.interpreter.ActionsInvoker
import com.intellij.openapi.project.Project


class RenameFeature : EvaluableFeature {
  override val name = "rename"
  override val generateActionsProcessor = RenameGenerateActionsProcessor()

  override fun getActionsInvoker(project: Project,
                                 language: Language,
                                 completionType: CompletionType,
                                 userEmulationSettings: UserEmulator.Settings?,
                                 codeGolfSettings: CodeGolfEmulation.Settings?): ActionsInvoker {
    return RenameActionsInvoker(project, language, completionType, userEmulationSettings, codeGolfSettings)
  }

}