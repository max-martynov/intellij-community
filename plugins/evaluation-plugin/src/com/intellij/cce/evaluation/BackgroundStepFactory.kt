package com.intellij.cce.evaluation

import com.intellij.cce.core.Language
import com.intellij.cce.evaluation.step.*
import com.intellij.cce.evaluable.EvaluableFeature
import com.intellij.cce.evaluable.EvaluationStrategy
import com.intellij.cce.interpreter.DelegationActionsInvoker
import com.intellij.cce.metric.SuggestionsComparator
import com.intellij.cce.workspace.Config
import com.intellij.cce.workspace.EvaluationWorkspace
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project

class BackgroundStepFactory(
  private val feature: EvaluableFeature<EvaluationStrategy>,
  private val config: Config,
  private val project: Project,
  private val isHeadless: Boolean,
  private val inputWorkspacePaths: List<String>?,
  private val evaluationRootInfo: EvaluationRootInfo
) : StepFactory {

  private val actionsInvoker = DelegationActionsInvoker(
    feature.getActionsInvoker(project, Language.resolve(config.language), config.strategy),
    project
  )

  override fun generateActionsStep(): EvaluationStep {
    return ActionsGenerationStep(config.actions, config.language, evaluationRootInfo,
                                 project, isHeadless,
                                 feature.getGenerateActionsProcessor(config.strategy), feature.name)
  }

  override fun interpretActionsStep(): EvaluationStep =
    ActionsInterpretationStep(config.interpret, config.language, actionsInvoker, project, isHeadless)

  override fun generateReportStep(): EvaluationStep =
    ReportGenerationStep(inputWorkspacePaths?.map { EvaluationWorkspace.open(it) },
                         config.reports.sessionsFilters, config.reports.comparisonFilters, project, isHeadless,
                         feature.getStrategyBuilder(), feature.getStrategySerializer())

  override fun interpretActionsOnNewWorkspaceStep(): EvaluationStep =
    ActionsInterpretationOnNewWorkspaceStep(config, actionsInvoker, project, isHeadless)

  override fun reorderElements(): EvaluationStep =
    ReorderElementsStep(config, project, isHeadless)

  override fun highlightTokensInIdeStep(): EvaluationStep = HighlightingTokensInIdeStep(
    SuggestionsComparator.create(Language.resolve(config.language)), project, isHeadless)

  override fun setupStatsCollectorStep(): EvaluationStep? =
    if ((config.interpret.saveLogs || config.interpret.saveFeatures || config.interpret.experimentGroup != null)
        && !ApplicationManager.getApplication().isUnitTestMode
        && SetupStatsCollectorStep.isStatsCollectorEnabled())
      SetupStatsCollectorStep(project, config.interpret.experimentGroup,
                              config.interpret.logLocationAndItemText, isHeadless)
    else null

  override fun setupCompletionStep(): EvaluationStep = SetupCompletionStep(config.language)

  override fun setupSdkStep(): EvaluationStep? = SetupSdkStep.forLanguage(project, Language.resolve(config.language))

  override fun checkSdkConfiguredStep(): EvaluationStep = CheckProjectSdkStep(project, config.language)

  override fun finishEvaluationStep(): EvaluationStep =
    if (isHeadless) HeadlessFinishEvaluationStep() else UIFinishEvaluationStep(project)
}
