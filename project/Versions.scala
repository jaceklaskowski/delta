import Mima.getMajorMinorPatch
import sbt.{Def, Task}

object Versions {
  val LATEST_RELEASED_SPARK_VERSION = "3.5.3"
  val SPARK_MASTER_VERSION = "4.0.1-SNAPSHOT"
  val SPARK_4_RC_VERSION = "4.0.0"

  val scala212 = "2.12.18"
  val scala213 = "2.13.13"
  val all_scala_versions = Seq(scala212, scala213)

  val defaultSparkVersion = LATEST_RELEASED_SPARK_VERSION
  val flinkVersion = "1.16.1"
  val hadoopVersion = "3.3.4"
  val scalaTestVersion = "3.2.15"
  val scalaTestVersionForConnectors = "3.0.8"
  val parquet4sVersion = "1.9.4"
  val junitVersion = "4.13.2"
  val parquetHadoopVersion = "1.12.3"

  // Versions for Hive 3
  val hadoopVersionForHive3 = "3.1.0"
  val hiveVersion = "3.1.2"
  val tezVersion = "0.9.2"

  // Versions for Hive 2
  val hadoopVersionForHive2 = "2.7.2"
  val hive2Version = "2.3.3"
  val tezVersionForHive2 = "0.8.4"

  val protoVersion = "3.25.1"
  val grpcVersion = "1.62.2"

  /**
   * Returns the current spark version, which is the same value as `sparkVersion.value`.
   *
   * A separate method because some call sites cannot access `sparkVersion.value`
   * e.g., callers that are not inside tasks or setting macros.
   */
  def getSparkVersion(): String = {
    val latestReleasedSparkVersionShort = getMajorMinorPatch(LATEST_RELEASED_SPARK_VERSION) match {
      case (maj, min, _) => s"$maj.$min"
    }
    val allValidSparkVersionInputs = Seq(
      "master",
      "latest",
      SPARK_4_RC_VERSION,
      SPARK_MASTER_VERSION,
      LATEST_RELEASED_SPARK_VERSION,
      latestReleasedSparkVersionShort
    )

    // e.g. build/sbt -DsparkVersion=master, build/sbt -DsparkVersion=4.0.0-SNAPSHOT
    val input = sys.props.getOrElse("sparkVersion", LATEST_RELEASED_SPARK_VERSION)
    input match {
      case LATEST_RELEASED_SPARK_VERSION | "latest" | `latestReleasedSparkVersionShort` =>
        LATEST_RELEASED_SPARK_VERSION
      case SPARK_MASTER_VERSION | "master" =>
        SPARK_MASTER_VERSION
      case SPARK_4_RC_VERSION =>
        SPARK_4_RC_VERSION
      case _ =>
        throw new IllegalArgumentException(s"Invalid sparkVersion: $input. Must be one of " +
          s"${allValidSparkVersionInputs.mkString("{", ",", "}")}")
    }
  }

  def runTaskOnlyOnSparkMaster[T](
    task: sbt.TaskKey[T],
    taskName: String,
    projectName: String,
    emptyValue: => T): Def.Initialize[Task[T]] = {
    if (getSparkVersion() == SPARK_MASTER_VERSION || getSparkVersion() == SPARK_4_RC_VERSION) {
      Def.task(task.value)
    } else {
      Def.task {
        // scalastyle:off println
        println(s"Project $projectName: Skipping `$taskName` as Spark version " +
          s"${getSparkVersion()} does not equal $SPARK_MASTER_VERSION.")
        // scalastyle:on println
        emptyValue
      }
    }
  }
}
