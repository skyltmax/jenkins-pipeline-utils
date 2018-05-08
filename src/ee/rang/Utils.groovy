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

  static def notifySlack(script, TestResultSummary summary = null) {
    def currentBuild = script.currentBuild
    def env = script.env
    def color
    def status = currentBuild.result || 'SUCCESS'
    def duration = "after ${Util.getTimeSpanString(System.currentTimeMillis() - currentBuild.startTimeInMillis)} "

    if (status == 'SUCCESS') {
      color = 'good'
    } else if (status == 'UNSTABLE') {
      color = 'warning'
    } else {
      color = 'danger'
    }

    def msg = "${env.JOB_NAME} - #${env.BUILD_NUMBER} ${status.toLowerCase().capitalize()} ${duration}(<${env.RUN_DISPLAY_URL}|Open>)"

    if (summary != null) {
      msg += "\nTest Status:\n    Passed: ${summary.getPassCount()}, Failed: ${summary.getFailCount()}, Skipped: ${summary.getSkipCount()}"
    }

    script.slackSend(color: color, message: msg)
  }
}
