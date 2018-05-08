package ee.rang

import hudson.tasks.junit.TestResultSummary
import hudson.Util

class Utils {
  static def manualTrigger(script) {
    def isManual = false
    def causes = script.currentBuild.rawBuild.getCauses()

    for (cause in causes) {
      if (cause.properties.shortDescription =~ 'Started by user') {
        isManual = true
        break
      }
    }

    isManual
  }
}
