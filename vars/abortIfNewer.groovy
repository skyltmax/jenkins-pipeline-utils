import hudson.tasks.junit.TestResultSummary
import hudson.Util
import io.jenkins.plugins.analysis.core.steps.AnnotatedReport
import net.sf.json.JSONArray
import net.sf.json.JSONObject
import hudson.model.*

def call() {
  def instance = Hudson.instance
  def pname = env.JOB_NAME.split('/')[0]

  instance.getItem(pname).getItem(env.JOB_BASE_NAME).getBuilds().each{ build ->
    def exec = build.getExecutor()

    if (build.number < currentBuild.number && exec != null) {
      exec.doStop()
      println("Aborted previous running build #${build.number}")
    }
  }
}
