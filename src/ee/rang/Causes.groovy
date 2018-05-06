package ee.rang
class Causes {
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
}
