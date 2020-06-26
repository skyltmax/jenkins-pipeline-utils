import hudson.tasks.junit.TestResultSummary
import hudson.Util
import net.sf.json.JSONArray
import net.sf.json.JSONObject
import hudson.model.*

def call(ArrayList<TestResultSummary> results = []) {
  def job_name = env.JOB_NAME.replaceAll("%2F", "/")
  def color
  def status = currentBuild.result ?: 'SUCCESS'
  def duration = "after ${Util.getTimeSpanString(System.currentTimeMillis() - currentBuild.startTimeInMillis)} ".toString()

  // it's a pull request
  if (env.BRANCH_NAME.startsWith('PR-')) {
    def name_parts = job_name.split("/PR-")
    job_name = name_parts[0] + "/" + env.CHANGE_BRANCH.replaceAll("%2F", "/") + " PR <${env.CHANGE_URL}|#${name_parts[1]}> merge"
  }

  if (status == 'SUCCESS') {
    color = '#37A254'
  } else if (status == 'UNSTABLE') {
    color = '#DCA047'
  } else {
    color = '#D22025'
  }

  def msg = "${job_name} - #${env.BUILD_NUMBER} ${status.toLowerCase().capitalize()} ${duration}(<${env.RUN_DISPLAY_URL}|Open>)".toString()

  JSONArray attachments = new JSONArray();

  if (results.size() > 0 && status != 'ABORTED') {
    def passCount = 0
    def failCount = 0
    def skipCount = 0

    results.each {
      passCount += it.getPassCount()
      failCount += it.getFailCount()
      skipCount += it.getSkipCount()
    }

    JSONObject resultAttachment = new JSONObject();

    resultAttachment.put('text', '');
    resultAttachment.put('fallback', "Test Status: Passed: ${passCount}, Failed: ${failCount}, Skipped: ${skipCount}".toString());
    resultAttachment.put('color', color);

    JSONArray fields = new JSONArray();

    JSONObject passedField = new JSONObject();
    passedField.put('title', 'Passed')
    passedField.put('value', passCount)
    passedField.put('short', true)
    fields.add(passedField);

    JSONObject failedField = new JSONObject();
    failedField.put('title', 'Failed')
    failedField.put('value', failCount)
    failedField.put('short', true)
    fields.add(failedField);

    JSONObject skippedField = new JSONObject();
    skippedField.put('title', 'Skipped')
    skippedField.put('value', skipCount)
    skippedField.put('short', true)
    fields.add(skippedField);

    JSONObject durationField = new JSONObject();
    durationField.put('title', 'Duration')
    durationField.put('value', Util.getTimeSpanString(System.currentTimeMillis() - currentBuild.startTimeInMillis))
    durationField.put('short', true)
    fields.add(durationField);

    resultAttachment.put('fields', fields);
    attachments.add(resultAttachment);
  }

  slackSend(color: color, message: msg, attachments: attachments.toString());
}
