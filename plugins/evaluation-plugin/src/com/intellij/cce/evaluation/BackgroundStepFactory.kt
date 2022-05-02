package com.intellij.cce.evaluation

import com.intellij.cce.actions.CompletionType
import com.intellij.cce.core.Language
import com.intellij.cce.evaluation.step.*
import com.intellij.cce.generalization.EvaluableFeature
import com.intellij.cce.interpreter.ActionsInvoker
import com.intellij.cce.interpreter.BasicActionsInvoker
import com.intellij.cce.interpreter.DelegationActionsInvoker
import com.intellij.cce.metric.SuggestionsComparator
import com.intellij.cce.workspace.Config
import com.intellij.cce.workspace.EvaluationWorkspace
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project

class BackgroundStepFactory(
  private val config: Config,
  private val project: Project,
  private val isHeadless: Boolean,
  private val inputWorkspacePaths: List<String>?,
  private val evaluationRootInfo: EvaluationRootInfo
) : StepFactory {

  private val featureName =
    if (config.interpret.completionType == CompletionType.RENAME)
      "rename"
    else
      "completion"
  private var evaluableFeature = EvaluableFeature.forFeature(project, featureName)!!

  private var myActionsInvoker: ActionsInvoker = DelegationActionsInvoker(
    evaluableFeature.getActionsInvoker(
      project,
      Language.resolve(config.language),
      config.interpret.completionType,
      config.interpret.emulationSettings,
      config.interpret.codeGolfSettings
    ),
    project
  )

  override fun generateActionsStep(): EvaluationStep =
    ActionsGenerationStep(config.actions, config.language, evaluationRootInfo, project, isHeadless, evaluableFeature.generateActionsProcessor)

  override fun interpretActionsStep(): EvaluationStep =
    ActionsInterpretationStep(config.interpret, config.language, myActionsInvoker, project, isHeadless)

  override fun generateReportStep(): EvaluationStep =
    ReportGenerationStep(inputWorkspacePaths?.map { EvaluationWorkspace.open(it) },
                         config.reports.sessionsFilters, config.reports.comparisonFilters, project, isHeadless)

  override fun interpretActionsOnNewWorkspaceStep(): EvaluationStep =
    ActionsInterpretationOnNewWorkspaceStep(config, myActionsInvoker, project, isHeadless)

  override fun reorderElements(): EvaluationStep =
    ReorderElementsStep(config, project, isHeadless)

  override fun highlightTokensInIdeStep(): EvaluationStep = HighlightingTokensInIdeStep(
    SuggestionsComparator.create(Language.resolve(config.language), config.interpret.completionType), project, isHeadless)

  override fun setupStatsCollectorStep(): EvaluationStep? =
    if ((config.interpret.saveLogs || config.interpret.saveFeatures || config.interpret.experimentGroup != null)
        && !ApplicationManager.getApplication().isUnitTestMode
        && SetupStatsCollectorStep.isStatsCollectorEnabled())
      SetupStatsCollectorStep(project, config.interpret.experimentGroup,
                              config.interpret.logLocationAndItemText, isHeadless)
    else null

  override fun setupCompletionStep(): EvaluationStep = SetupCompletionStep(config.language, config.interpret.completionType)

  override fun setupSdkStep(): EvaluationStep? = SetupSdkStep.forLanguage(project, Language.resolve(config.language))

  override fun checkSdkConfiguredStep(): EvaluationStep = CheckProjectSdkStep(project, config.language)

  override fun finishEvaluationStep(): EvaluationStep =
    if (isHeadless) HeadlessFinishEvaluationStep() else UIFinishEvaluationStep(project)
}
