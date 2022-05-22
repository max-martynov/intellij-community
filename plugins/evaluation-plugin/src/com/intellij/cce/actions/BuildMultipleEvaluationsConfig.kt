package com.intellij.cce.actions

import com.intellij.cce.evaluable.EvaluationStrategy
import com.intellij.cce.evaluable.StrategyBuilder
import com.intellij.cce.workspace.Config
import com.intellij.cce.workspace.EvaluationWorkspace

fun<T : EvaluationStrategy> List<EvaluationWorkspace>.buildMultipleEvaluationsConfig(strategyBuilder: StrategyBuilder<T>): Config {
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