import hudson.tasks.junit.TestResultSummary
import hudson.Util
import io.jenkins.plugins.analysis.core.steps.AnnotatedReport
import net.sf.json.JSONArray
import net.sf.json.JSONObject

def call(TestResultSummary summary = null, ArrayList<AnnotatedReport> warnings = new ArrayList<AnnotatedReport>([])) {
  def color
  def status = currentBuild.result ?: 'SUCCESS'
  def duration = "after ${Util.getTimeSpanString(System.currentTimeMillis() - currentBuild.startTimeInMillis)} "

  if (status == 'SUCCESS') {
    color = '#37A254'
  } else if (status == 'UNSTABLE') {
    color = '#DCA047'
  } else {
    color = '#D22025'
  }

  def msg = "${env.JOB_NAME} - #${env.BUILD_NUMBER} ${status.toLowerCase().capitalize()} ${duration}(<${env.RUN_DISPLAY_URL}|Open>)"

  JSONArray attachments = new JSONArray();

  if (summary != null) {
    JSONObject resultAttachment = new JSONObject();

    resultAttachment.put('text', '');
    resultAttachment.put('fallback', "Test Status: Passed: ${summary.getPassCount()}, Failed: ${summary.getFailCount()}, Skipped: ${summary.getSkipCount()}");
    resultAttachment.put('color', color);

    JSONArray fields = new JSONArray();

    JSONObject passedField = new JSONObject();
    passedField.put('title', 'Passed')
    passedField.put('value', summary.getPassCount())
    passedField.put('short', true)
    fields.add(passedField);

    JSONObject failedField = new JSONObject();
    failedField.put('title', 'Failed')
    failedField.put('value', summary.getFailCount())
    failedField.put('short', true)
    fields.add(failedField);

    JSONObject skippedField = new JSONObject();
    skippedField.put('title', 'Skipped')
    skippedField.put('value', summary.getSkipCount())
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

  warnings.each {
    def warningsColor

    if (it.size() > 0) {
      warningsColor = '#DCA047'
    } else {
      warningsColor = '#37A254'
    }

    JSONObject warningsAttachment = new JSONObject();

    warningsAttachment.put('text', '');
    warningsAttachment.put('fallback', "${it.getId()} warnings: ${it.size()}");
    warningsAttachment.put('color', warningsColor);

    JSONArray fields = new JSONArray();

    JSONObject warningsField = new JSONObject();
    warningsField.put('title', "${it.getId()} warnings")
    warningsField.put('value', it.size())
    warningsField.put('short', false)
    fields.add(warningsField);

    warningsAttachment.put('fields', fields);
    attachments.add(warningsAttachment);
  }

  slackSend(color: color, message: msg, attachments: attachments.toString())
}
