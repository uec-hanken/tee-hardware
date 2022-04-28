import Tests._

// This gives us a nicer handle to the root project instead of using the
// implicit one
lazy val teeHardwareRoot = Project("teeHardwareRoot", file("."))

lazy val commonSettings = Seq(
  organization := "vlsilab.ee.uec.ac",
  version := "0.4",
  scalaVersion := "2.12.10",
  test in assembly := {},
  assemblyMergeStrategy in assembly := { _ match {
    case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
    case _ => MergeStrategy.first}},
  scalacOptions ++= Seq("-deprecation","-unchecked","-Xsource:2.11"),
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
  unmanagedBase := (teeHardwareRoot / unmanagedBase).value,
  allDependencies := {
    // drop specific maven dependencies in subprojects in favor of Chipyard's version
    val dropDeps = Seq(("edu.berkeley.cs", "rocketchip"))
    allDependencies.value.filterNot { dep =>
      dropDeps.contains((dep.organization, dep.name))
    }
  },
  exportJars := true,
  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases"),
    Resolver.mavenLocal))

val rocketChipDir = file("hardware/chipyard/generators/rocket-chip")

lazy val firesimDir = 
  file("hardware/chipyard/sims/firesim/sim/")

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

// -- Rocket Chip --

// This needs to stay in sync with the chisel3 and firrtl git submodules
val chiselVersion = "3.5.1"

lazy val chiselSettings = Seq(
  libraryDependencies ++= Seq("edu.berkeley.cs" %% "chisel3" % chiselVersion),
  addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full))

val firrtlVersion = "1.5.1"

lazy val firrtlSettings = Seq(libraryDependencies ++= Seq("edu.berkeley.cs" %% "firrtl" % firrtlVersion))

val chiselTestVersion = "2.5.1"

lazy val chiselTestSettings = Seq(libraryDependencies ++= Seq("edu.berkeley.cs" %% "chisel-iotesters" % chiselTestVersion))

 // Rocket-chip dependencies (subsumes making RC a RootProject)

// -- Rocket Chip --

lazy val hardfloat  = (project in rocketChipDir / "hardfloat")
  .settings(chiselSettings)
  .dependsOn(midasTargetUtils)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.json4s" %% "json4s-jackson" % "3.6.1",
      "org.scalatest" %% "scalatest" % "3.2.0" % "test"
    )
  )

lazy val rocketMacros  = (project in rocketChipDir / "macros")
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.json4s" %% "json4s-jackson" % "3.6.1",
      "org.scalatest" %% "scalatest" % "3.2.0" % "test"
    )
  )

lazy val rocketConfig = (project in rocketChipDir / "api-config-chipsalliance/build-rules/sbt")
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.json4s" %% "json4s-jackson" % "3.6.1",
      "org.scalatest" %% "scalatest" % "3.2.0" % "test"
    )
  )

lazy val rocketchip = freshProject("rocketchip", rocketChipDir)
  .dependsOn(hardfloat, rocketMacros, rocketConfig)
  .settings(commonSettings)
  .settings(chiselSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.json4s" %% "json4s-jackson" % "3.6.1",
      "org.scalatest" %% "scalatest" % "3.2.0" % "test"
    )
  )
  .settings( // Settings for scalafix
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalacOptions += "-Ywarn-unused-import"
  )
lazy val rocketLibDeps = (rocketchip / Keys.libraryDependencies)

// -- Chipyard-managed External Projects --

// Contains annotations & firrtl passes you may wish to use in rocket-chip without
// introducing a circular dependency between RC and MIDAS
// TODO: Check
lazy val midasTargetUtils = (project in firesimDir / "midas" / "targetutils")
  .settings(chiselSettings)
  .settings(firrtlSettings)

lazy val testchipip = (project in file("hardware/chipyard/generators/testchipip"))
  .dependsOn(rocketchip, sifive_blocks)
  .settings(libraryDependencies ++= rocketLibDeps.value)
  .settings(commonSettings)

lazy val chipyard = (project in file("hardware/chipyard/generators/chipyard"))
  .dependsOn(testchipip, rocketchip, boom, hwacha, sifive_blocks, sifive_cache, iocell,
    sha3, // On separate line to allow for cleaner tutorial-setup patches
    dsptools, `rocket-dsp-utils`,
    gemmini, icenet, tracegen, cva6, nvdla, sodor, ibex, fft_generator)
  .settings(libraryDependencies ++= rocketLibDeps.value)
  .settings(commonSettings)

lazy val fft_generator = (project in file("hardware/chipyard/generators/fft-generator"))
  .dependsOn(rocketchip, `rocket-dsp-utils`)
  .settings(libraryDependencies ++= rocketLibDeps.value)
  .settings(commonSettings)

lazy val tracegen = (project in file("hardware/chipyard/generators/tracegen"))
  .dependsOn(testchipip, rocketchip, sifive_cache, boom)
  .settings(libraryDependencies ++= rocketLibDeps.value)
  .settings(commonSettings)

lazy val icenet = (project in file("hardware/chipyard/generators/icenet"))
  .dependsOn(testchipip, rocketchip)
  .settings(libraryDependencies ++= rocketLibDeps.value)
  .settings(commonSettings)

lazy val hwacha = (project in file("hardware/chipyard/generators/hwacha"))
  .dependsOn(rocketchip)
  .settings(libraryDependencies ++= rocketLibDeps.value)
  .settings(commonSettings)

lazy val boom = (project in file("hardware/chipyard/generators/boom"))
  .dependsOn(testchipip, rocketchip)
  .settings(libraryDependencies ++= rocketLibDeps.value)
  .settings(commonSettings)

lazy val cva6 = (project in file("hardware/chipyard/generators/cva6"))
  .dependsOn(rocketchip)
  .settings(libraryDependencies ++= rocketLibDeps.value)
  .settings(commonSettings)

lazy val ibex = (project in file("hardware/chipyard/generators/ibex"))
  .dependsOn(rocketchip)
  .settings(libraryDependencies ++= rocketLibDeps.value)
  .settings(commonSettings)

lazy val sodor = (project in file("hardware/chipyard/generators/riscv-sodor"))
  .dependsOn(rocketchip)
  .settings(libraryDependencies ++= rocketLibDeps.value)
  .settings(commonSettings)

lazy val sha3 = (project in file("hardware/chipyard/generators/sha3"))
  .dependsOn(rocketchip, midasTargetUtils)
  .settings(libraryDependencies ++= rocketLibDeps.value)
  .settings(chiselTestSettings)
  .settings(commonSettings)

lazy val gemmini = (project in file("hardware/chipyard/generators/gemmini"))
  .dependsOn(testchipip, rocketchip, midasTargetUtils)
  .settings(libraryDependencies ++= rocketLibDeps.value)
  .settings(chiselTestSettings)
  .settings(commonSettings)

lazy val nvdla = (project in file("hardware/chipyard/generators/nvdla"))
  .dependsOn(rocketchip)
  .settings(libraryDependencies ++= rocketLibDeps.value)
  .settings(commonSettings)

lazy val iocell = Project(id = "iocell", base = file("./hardware/chipyard/tools/barstools/") / "src")
  .settings(
    Compile / scalaSource := baseDirectory.value / "main" / "scala" / "barstools" / "iocell",
    Compile / resourceDirectory := baseDirectory.value / "main" / "resources"
  )
  .settings(chiselSettings)
  .settings(commonSettings)

lazy val tapeout = (project in file("./hardware/chipyard/tools/barstools/"))
  .settings(chiselSettings)
  .settings(chiselTestSettings)
  .enablePlugins(sbtassembly.AssemblyPlugin)
  .settings(commonSettings)

lazy val dsptools = freshProject("dsptools", file("./hardware/chipyard/tools/dsptools"))
  .settings(
    chiselSettings,
    chiselTestSettings,
    commonSettings,
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.+" % "test",
      "org.typelevel" %% "spire" % "0.16.2",
      "org.scalanlp" %% "breeze" % "1.1",
      "junit" % "junit" % "4.13" % "test",
      "org.scalacheck" %% "scalacheck" % "1.14.3" % "test",
  ))

lazy val `api-config-chipsalliance` = freshProject("api-config-chipsalliance", file("./hardware/chipyard/tools/api-config-chipsalliance"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.+" % "test",
      "org.scalacheck" %% "scalacheck" % "1.14.3" % "test",
    ))

lazy val `rocket-dsp-utils` = freshProject("rocket-dsptools", file("./hardware/chipyard/tools/rocket-dsp-utils"))
  .dependsOn(rocketchip, `api-config-chipsalliance`, dsptools)
  .settings(libraryDependencies ++= rocketLibDeps.value)
  .settings(commonSettings)

lazy val sifive_blocks = (project in file("hardware/chipyard/generators/sifive-blocks"))
  .dependsOn(rocketchip)
  .settings(libraryDependencies ++= rocketLibDeps.value)
  .settings(commonSettings)

lazy val sifive_cache = (project in file("hardware/chipyard/generators/sifive-cache"))
  .settings(
    commonSettings,
    Compile / scalaSource := baseDirectory.value / "design/craft")
  .dependsOn(rocketchip)
  .settings(libraryDependencies ++= rocketLibDeps.value)

// Library components of FireSim
//lazy val midas      = ProjectRef(firesimDir, "midas")
//lazy val firesimLib = ProjectRef(firesimDir, "firesimLib")

/*lazy val firechip = (project in file("hardware/chipyard/generators/firechip"))
  .dependsOn(chipyard, midasTargetUtils, midas, firesimLib % "test->test;compile->compile")
  .settings(
    chiselSettings,
    commonSettings,
    Test / testGrouping := isolateAllTests( (Test / definedTests).value ),
    Test / testOptions += Tests.Argument("-oF")
  )*/
lazy val fpga_shells = (project in file("./hardware/fpga-shells"))
  .dependsOn(rocketchip, sifive_blocks)
  .settings(libraryDependencies ++= rocketLibDeps.value)
  .settings(
      commonSettings,
      unmanagedSources / excludeFilter := HiddenFileFilter || "*microsemi*" // Avoid microsemi, because does not compile
  )

lazy val fpga_platforms = (project in file("./hardware/chipyard/fpga"))
  .dependsOn(chipyard, fpga_shells)
  .settings(commonSettings)

// -- Our tee-hardware project --
lazy val teehardware = (project in file("hardware/teehw")).
  dependsOn(rocketchip, sifive_blocks, fpga_shells, tapeout, chipyard).
  settings(libraryDependencies ++= rocketLibDeps.value).
  settings(commonSettings)
