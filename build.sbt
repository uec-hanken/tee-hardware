import Tests._

// This gives us a nicer handle  to the root project instead of using the
// implicit one
lazy val keystoneHardwareRoot = RootProject(file("."))
//lazy val chipyardRoot = RootProject(file("hardware/chipyard"))

lazy val commonSettings = Seq(
  organization := "ac.uec.vlsilab.ee",
  version := "0.1",
  scalaVersion := "2.12.4",
  traceLevel := 15,
  test in assembly := {},
  assemblyMergeStrategy in assembly := { _ match {
    case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
    case _ => MergeStrategy.first}},
  scalacOptions ++= Seq("-deprecation","-unchecked","-Xsource:2.11"),
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.6.1",
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  libraryDependencies += "com.github.scopt" %% "scopt" % "3.7.0",
  libraryDependencies += "org.scala-lang.modules" % "scala-jline" % "2.12.1",
  libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.10",
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  unmanagedBase := (keystoneHardwareRoot / unmanagedBase).value,
  allDependencies := allDependencies.value.filterNot{ case w =>
    w.organization == "ac.uec.vlsilab.ee" || w.organization == "edu.berkeley.cs"
  },
  exportJars := true,
  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases"),
    Resolver.mavenLocal))

val rocketChipDir = file("hardware/chipyard/generators/rocket-chip")

/*lazy val firesimAsLibrary = sys.env.get("FIRESIM_STANDALONE") == None
lazy val firesimDir = if (firesimAsLibrary) {
  file("hardware/chipyard/sims/firesim/sim/")
} else {
  file("../../")
}*/

/**
  * It has been a struggle for us to override settings in subprojects.
  * An example would be adding a dependency to rocketchip on midas's targetutils library,
  * or replacing dsptools's maven dependency on chisel with the local chisel project.
  *
  * This function works around this by specifying the project's root at src/ and overriding
  * scalaSource and resourceDirectory.
  */
def freshProject(name: String, dir: File): Project = {
  Project(id = name, base = dir / "src")
    .settings(
      scalaSource in Compile := baseDirectory.value / "main" / "scala",
      resourceDirectory in Compile := baseDirectory.value / "main" / "resources"
    )
}

// Fork each scala test for now, to work around persistent mutable state
// in Rocket-Chip based generators
def isolateAllTests(tests: Seq[TestDefinition]) = tests map { test =>
      val options = ForkOptions()
      new Group(test.name, Seq(test), SubProcess(options))
  } toSeq

// Subproject definitions begin
//
// FIRRTL is handled as an unmanaged dependency. Make will build the firrtl jar
// before launching sbt if any of the firrtl source files has been updated
// The jar is dropped in chipyard's lib/ directory, which is used as the unmanagedBase
// for all subprojects
lazy val chisel  = (project in file("hardware/chipyard/tools/chisel3"))

lazy val firrtl_interpreter = (project in file("hardware/chipyard/tools/firrtl-interpreter"))
  .settings(commonSettings)

lazy val treadle = (project in file("hardware/chipyard/tools/treadle"))
  .settings(commonSettings)

lazy val chisel_testers = (project in file("hardware/chipyard/tools/chisel-testers"))
  .dependsOn(chisel, firrtl_interpreter, treadle)
  .settings(
      commonSettings,
      libraryDependencies ++= Seq(
        "junit" % "junit" % "4.12",
        "org.scalatest" %% "scalatest" % "3.0.5",
        "org.scalacheck" %% "scalacheck" % "1.14.0",
        "com.github.scopt" %% "scopt" % "3.7.0"
      )
    )

// Contains annotations & firrtl passes you may wish to use in rocket-chip without
// introducing a circular dependency between RC and MIDAS
//lazy val midasTargetUtils = ProjectRef(firesimDir, "targetutils")

 // Rocket-chip dependencies (subsumes making RC a RootProject)
lazy val hardfloat  = (project in rocketChipDir / "hardfloat")
  .dependsOn(chisel)
  .settings(commonSettings)//.dependsOn(midasTargetUtils)

lazy val rocketMacros  = (project in rocketChipDir / "macros")
  .settings(commonSettings)

lazy val rocketchip = freshProject("rocketchip", rocketChipDir)
  .settings(commonSettings)
  .dependsOn(chisel, hardfloat, rocketMacros)

lazy val testchipip = (project in file("hardware/chipyard/generators/testchipip"))
  .dependsOn(rocketchip)
  .settings(commonSettings)

lazy val example = (project in file("hardware/chipyard/generators/example"))
  .dependsOn(boom, hwacha, sifive_blocks, sifive_cache, utilities, sha3, testchipip)
  .settings(commonSettings)

lazy val tracegen = (project in file("hardware/chipyard/generators/tracegen"))
  .dependsOn(rocketchip, sifive_cache, testchipip)
  .settings(commonSettings)

lazy val utilities = (project in file("hardware/chipyard/generators/utilities"))
  .dependsOn(rocketchip, boom, testchipip)
  .settings(commonSettings)

lazy val icenet = (project in file("hardware/chipyard/generators/icenet"))
  .dependsOn(rocketchip, testchipip)
  .settings(commonSettings)

lazy val hwacha = (project in file("hardware/chipyard/generators/hwacha"))
  .dependsOn(rocketchip)
  .settings(commonSettings)

lazy val boom = (project in file("hardware/chipyard/generators/boom"))
  .dependsOn(rocketchip)
  .settings(commonSettings)

lazy val sha3 = (project in file("hardware/chipyard/generators/sha3"))
  .dependsOn(rocketchip, chisel_testers)
  .settings(commonSettings)

lazy val tapeout = (project in file("./hardware/chipyard/tools/barstools/tapeout/"))
  .dependsOn(chisel_testers, example, testchipip)
  .settings(commonSettings)

lazy val mdf = (project in file("./hardware/chipyard/tools/barstools/mdf/scalalib/"))
  .settings(commonSettings)

lazy val barstoolsMacros = (project in file("./hardware/chipyard/tools/barstools/macros/"))
  .dependsOn(firrtl_interpreter, mdf, rocketchip)
  .enablePlugins(sbtassembly.AssemblyPlugin)
  .settings(commonSettings)

lazy val dsptools = (project in file("./hardware/chipyard/tools/dsptools"))
  .dependsOn(chisel, chisel_testers)
  .settings(
      commonSettings,
      libraryDependencies ++= Seq(
        "org.typelevel" %% "spire" % "0.14.1",
        "org.scalanlp" %% "breeze" % "0.13.2",
        "junit" % "junit" % "4.12" % "test",
        "org.scalatest" %% "scalatest" % "3.0.5" % "test",
        "org.scalacheck" %% "scalacheck" % "1.14.0" % "test"
  ))

lazy val `rocket-dsptools` = (project in file("./hardware/chipyard/tools/dsptools/rocket"))
  .dependsOn(rocketchip, dsptools)
  .settings(commonSettings)

lazy val sifive_blocks = (project in file("hardware/chipyard/generators/sifive-blocks"))
  .dependsOn(rocketchip)
  .settings(commonSettings)

lazy val sifive_cache = (project in file("hardware/chipyard/generators/sifive-cache")).settings(
    commonSettings,
    scalaSource in Compile := baseDirectory.value / "craft"
  ).dependsOn(rocketchip)

// Library components of FireSim
//lazy val midas      = ProjectRef(firesimDir, "midas")
//lazy val firesimLib = ProjectRef(firesimDir, "firesimLib")

/*lazy val firechip = (project in file("hardware/chipyard/generators/firechip"))
  .dependsOn(boom, icenet, testchipip, sifive_blocks, sifive_cache, sha3, utilities, tracegen, midasTargetUtils, midas, firesimLib % "test->test;compile->compile")
  .settings(
    commonSettings,
    testGrouping in Test := isolateAllTests( (definedTests in Test).value )
  )*/

