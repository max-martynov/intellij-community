// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.evaluable.rename


import com.intellij.cce.core.Language
import com.intellij.cce.evaluable.EvaluableFeature
import com.intellij.cce.interpreter.ActionsInvoker
import com.intellij.cce.processor.GenerateActionsProcessor
import com.intellij.openapi.project.Project


class RenameFeature : EvaluableFeature<RenameStrategy> {
  override val name = "rename"

  override fun getGenerateActionsProcessor(strategy: RenameStrategy): GenerateActionsProcessor {
    return RenameGenerateActionsProcessor()
  }

  override fun getActionsInvoker(project: Project, language: Language, strategy: RenameStrategy): ActionsInvoker {
    return RenameActionsInvoker(project, language, strategy)
  }

  override fun buildStrategy(map: Map<String, Any>): RenameStrategy? = RenameStrategy()

}