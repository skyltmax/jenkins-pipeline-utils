import hudson.tasks.junit.TestResultSummary
import hudson.Util

def call(TestResultSummary summary = null) {
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

  echo color
  echo msg

  slackSend(color: color, message: msg)
}
