name := "CSE-4303-Computer-Graphics-openGL"

version := "1.0"

scalaVersion := "2.11.6"

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

val scalaFx = "org.scalafx" %% "scalafx" % "8.0.31-R7"

val jogl = Seq(
  "org.jogamp.gluegen" % "gluegen-rt-main" % "2.2.4",
  "org.jogamp.jogl" % "jogl-all-main" % "2.2.4"
)

libraryDependencies ++= jogl :+ scalaFx