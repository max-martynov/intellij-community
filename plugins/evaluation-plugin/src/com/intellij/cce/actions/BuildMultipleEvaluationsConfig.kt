package com.intellij.cce.actions

import com.intellij.cce.evaluable.EvaluationStrategy
import com.intellij.cce.workspace.Config
import com.intellij.cce.workspace.EvaluationWorkspace

fun List<EvaluationWorkspace>.buildMultipleEvaluationsConfig(strategyBuilder: (Map<String, Any>) -> EvaluationStrategy?): Config {
  val existingConfig = this.first().readConfig(strategyBuilder)
  return Config.build(existingConfig.projectPath, existingConfig.language) {
    for (workspace in this@buildMultipleEvaluationsConfig) {
      val config = workspace.readConfig(strategyBuilder)
      mergeFilters(config.reports.sessionsFilters)
      mergeComparisonFilters(config.reports.comparisonFilters)
    }
    evaluationTitle = "COMPARE_MULTIPLE"
  }
}