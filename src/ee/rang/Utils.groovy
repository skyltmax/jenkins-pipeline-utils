package ee.rang

import hudson.tasks.junit.TestResultSummary
import hudson.Util

class Utils {
  static def manualTrigger(currentBuild) {
    def isManual = false
    def causes = currentBuild.rawBuild.getCauses()

    for (cause in causes) {
      if (cause.properties.shortDescription =~ 'Started by user') {
        isManual = true
        break
      }
    }

    isManual
  }

  static def notifySlack(env, String status = 'STARTED', TestResultSummary summary = null) {
    def color
    def duration = ''
    status = status ?: 'SUCCESS'

    if (status == 'STARTED') {
      color = '#244F7D'
    } else if (status == 'SUCCESS') {
      color = 'good'
    } else if (status == 'UNSTABLE') {
      color = 'warning'
    } else {
      color = 'danger'
    }

    if (status != 'STARTED') {
      duration = "after ${Util.getTimeSpanString(System.currentTimeMillis() - currentBuild.startTimeInMillis)} "
    }

    def msg = "${env.JOB_NAME} - #${env.BUILD_NUMBER} ${status.toLowerCase().capitalize()} ${duration}(<${env.RUN_DISPLAY_URL}|Open>)"

    if (status == 'SUCCESS' || status == 'UNSTABLE' && summary != null) {
      msg += "\nTest Status:\n    Passed: ${summary.getPassCount()}, Failed: ${summary.getFailCount()}, Skipped: ${summary.getSkipCount()}"
    }

    slackSend(color: color, message: msg)
  }
}
