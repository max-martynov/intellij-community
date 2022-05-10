// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.evaluable

import com.intellij.cce.core.Language
import com.intellij.cce.interpreter.ActionsInvoker
import com.intellij.cce.processor.GenerateActionsProcessor
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

interface EvaluableFeature<T : EvaluationStrategy> {
  companion object {
    private val EP_NAME = ExtensionPointName.create<EvaluableFeature<EvaluationStrategy>>("com.intellij.cce.evaluableFeature")

    fun forFeature(featureName: String): EvaluableFeature<EvaluationStrategy>? {
      return EP_NAME.getExtensionList().singleOrNull { it.name == featureName }
    }

  }

  val name: String

  fun getGenerateActionsProcessor(strategy: T) : GenerateActionsProcessor

  fun getActionsInvoker(project: Project, language: Language, strategy: T): ActionsInvoker
}