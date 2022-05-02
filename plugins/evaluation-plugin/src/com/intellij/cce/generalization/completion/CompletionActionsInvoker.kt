// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.generalization.completion

import com.intellij.cce.actions.CodeGolfEmulation
import com.intellij.cce.actions.CompletionType
import com.intellij.cce.actions.UserEmulator
import com.intellij.cce.core.*
import com.intellij.cce.evaluation.CodeCompletionHandlerFactory
import com.intellij.cce.interpreter.BasicActionsInvoker
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionProgressIndicator
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupEx
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.completion.ml.actions.MLCompletionFeaturesUtil
import com.intellij.completion.ml.util.prefix
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.refactoring.actions.RenameElementAction
import com.intellij.refactoring.rename.inplace.InplaceRefactoring

class CompletionActionsInvoker(
  private val project: Project,
  private val language: Language,
  completionType: CompletionType,
  userEmulationSettings: UserEmulator.Settings?,
  private val codeGolfSettings: CodeGolfEmulation.Settings?) : BasicActionsInvoker(project, language, completionType,
                                                                                   userEmulationSettings, codeGolfSettings) {
  override fun callFeature(expectedText: String, prefix: String?): Lookup {
    LOG.info("Call completion. Type: $completionType. ${positionToString(editor!!.caretModel.offset)}")
    //        assert(!dumbService.isDumb) { "Calling completion during indexing." }

    val start = System.currentTimeMillis()
    val isNew = LookupManager.getActiveLookup(editor) == null
    val activeLookup = invokeCompletion(expectedText, prefix)
    val latency = System.currentTimeMillis() - start
    if (activeLookup == null) {
      return com.intellij.cce.core.Lookup.fromExpectedText(expectedText, prefix ?: "", emptyList(), latency, isNew = isNew)
    }

    val lookup = activeLookup as LookupImpl
    val features = MLCompletionFeaturesUtil.getCommonFeatures(lookup)
    val resultFeatures = Features(
      CommonFeatures(features.context, features.user, features.session),
      lookup.items.map { MLCompletionFeaturesUtil.getElementFeatures(lookup, it).features }
    )
    val suggestions = lookup.items.map { it.asSuggestion() }

    return com.intellij.cce.core.Lookup.fromExpectedText(expectedText, lookup.prefix(), suggestions, latency, resultFeatures, isNew)
  }

  private fun invokeCompletion(expectedText: String, prefix: String?): LookupEx? {
    val handlerFactory = CodeCompletionHandlerFactory.findCompletionHandlerFactory(project, language)
    val handler = handlerFactory?.createHandler(completionType, expectedText, prefix) ?: object : CodeCompletionHandlerBase(completionType,
                                                                                                                            false, false,
                                                                                                                            true) {
      // Guarantees synchronous execution
      override fun isTestingCompletionQualityMode() = true
      override fun lookupItemSelected(indicator: CompletionProgressIndicator?,
                                      item: LookupElement,
                                      completionChar: Char,
                                      items: MutableList<LookupElement>?) {
        afterItemInsertion(indicator, null)
      }
    }
    try {
      handler.invokeCompletion(project, editor)
    }
    catch (e: AssertionError) {
      LOG.warn("Completion invocation ended with error", e)
    }
    return LookupManager.getActiveLookup(editor)
  }

  override fun finishSession(expectedText: String, prefix: String): Boolean {
        LOG.info("Finish completion. Expected text: $expectedText")
        val lookup = LookupManager.getActiveLookup(editor) as? LookupImpl ?: return false
        val expectedItemIndex = lookup.items.indexOfFirst { it.lookupString == expectedText }
        try {
          return if (expectedItemIndex != -1) lookup.finish(expectedItemIndex, expectedText.length - prefix.length) else false
        }
        finally {
          lookup.hide()
        }
  }

}