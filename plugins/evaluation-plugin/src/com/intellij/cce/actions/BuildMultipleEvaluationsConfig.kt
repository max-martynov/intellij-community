package com.intellij.cce.actions

import com.intellij.cce.evaluable.EvaluationStrategy
import com.intellij.cce.evaluable.StrategyBuilder
import com.intellij.cce.evaluable.StrategySerializer
import com.intellij.cce.workspace.Config
import com.intellij.cce.workspace.EvaluationWorkspace

/**
 * TODO - it actually doesn't work
 */
fun<T : EvaluationStrategy> List<EvaluationWorkspace>.buildMultipleEvaluationsConfig(strategyBuilder: StrategyBuilder<T>, strategySerializer: StrategySerializer<T>): Config {
  val existingConfig = this.first().readConfig(strategyBuilder, strategySerializer)
  return Config.build(existingConfig.projectPath, existingConfig.language) {
    for (workspace in this@buildMultipleEvaluationsConfig) {
      val config = workspace.readConfig(strategyBuilder, strategySerializer)
      mergeFilters(config.reports.sessionsFilters)
      mergeComparisonFilters(config.reports.comparisonFilters)
    }
    evaluationTitle = "COMPARE_MULTIPLE"
  }
}