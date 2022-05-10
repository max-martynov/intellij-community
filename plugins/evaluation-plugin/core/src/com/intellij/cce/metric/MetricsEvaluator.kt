package com.intellij.cce.metric

import com.intellij.cce.core.Session

class MetricsEvaluator private constructor(private val evaluationType: String) {
  companion object {
    fun withDefaultMetrics(evaluationType: String): MetricsEvaluator {
      val evaluator = MetricsEvaluator(evaluationType)
      evaluator.registerDefaultMetrics()
      return evaluator
    }
  }

  private val metrics = mutableListOf<Metric>()

  fun registerDefaultMetrics() {
    //if (strategy.codeGolf) {
    //  registerMetric(CodeGolfMovesSumMetric())
    //  registerMetric(CodeGolfMovesCountNormalised())
    //  registerMetric(CodeGolfPerfectLine())
    //  registerMetric(MeanLatencyMetric())
    //  registerMetric(MaxLatencyMetric())
    //  registerMetric(SessionsCountMetric())
    //
    //  return
    //}

    registerMetric(FoundAtMetric(1))
    registerMetric(FoundAtMetric(5))
    registerMetric(RecallMetric())
    registerMetric(MeanLatencyMetric())
    registerMetric(MaxLatencyMetric())
    registerMetric(MeanRankMetric())
    registerMetric(SessionsCountMetric())
  }

  fun registerMetric(metric: Metric) {
    metrics.add(metric)
  }

  fun evaluate(sessions: List<Session>, comparator: SuggestionsComparator = SuggestionsComparator.DEFAULT): List<MetricInfo> {
    return metrics.map { MetricInfo(it.name, it.evaluate(sessions, comparator).toDouble(), evaluationType, it.valueType) }
  }

  fun result(): List<MetricInfo> {
    return metrics.map { MetricInfo(it.name, it.value, evaluationType, it.valueType) }
  }
}
