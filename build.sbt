name := "CSE-4303-Computer-Graphics-openGL"

version := "1.0"

scalaVersion := "2.11.6"

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

val scalaFx = "org.scalafx" %% "scalafx" % "8.0.31-R7"

val jogl = Seq(
  "org.jogamp.gluegen" % "gluegen-rt-main" % "2.2.4" withJavadoc(),
  "org.jogamp.jogl" % "jogl-all-main" % "2.2.4" withSources() withJavadoc()
)

libraryDependencies ++= jogl :+ scalaFx

val joglMergeStrategy = new sbtassembly.MergeStrategy {
  val name = "jogl_rename"
  def apply(tempDir: File, path: String, files: Seq[File]) =
    Right(files flatMap { file =>
      val (jar, _, _, isJar) = sbtassembly.AssemblyUtils.sourceOfFileForMerge(tempDir, file)
      if (isJar) Seq(file -> s"natives/${jar.getPath.split("-natives-")(1).split(".jar")(0)}/$path")
      else Seq(file -> path)
    })
}
assemblyMergeStrategy in assembly := {
  case x if x.endsWith(".so") || x.endsWith(".dll") || x.endsWith(".jnilib") => joglMergeStrategy
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}