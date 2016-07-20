import android.Keys._
//protifySettings

lazy val commonSettings = Seq(
  scalaVersion := "2.11.8",
  scalacOptions += "-Xexperimental",
  organization := "com.github.thangiee",
  resolvers ++= Dependencies.resolvers,
  libraryDependencies += "com.github.mpilquist" %% "simulacrum" % "0.7.0",
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
)

lazy val core = project
  .settings(commonSettings)
  .settings(
    exportJars := true,
    libraryDependencies ++= Dependencies.coreDeps
  )

lazy val app = project
  .settings(commonSettings)
  .settings(android.Plugin.androidBuild)
  .settings(
    javacOptions in Compile ++= "-source" :: "1.7" :: "-target" :: "1.7" :: Nil,
    minSdkVersion in Android :="21",
    platformTarget in Android := "android-24",
    dexMaxHeap := "8096m",
    libraryDependencies ++= Dependencies.androidDeps,
    proguardOptions in Android ++= ProguardValues.options,
    proguardCache in Android ++= ProguardValues.cache,
    //useProguard in Android := false
    //useProguardInDebug in Android := false
    //dexMulti in Android := true

    packagingOptions := PackagingOptions(
      excludes =
        "META-INF/notice.txt" ::
          "META-INF/license.txt" ::
          "META-INF/LICENSE" ::
          "META-INF/NOTICE" ::
          "META-INF/LICENSE.txt" ::
          "META-INF/NOTICE.txt" ::
          Nil
    ),
    dexInProcess := true,
    extraResDirectories ++= {
      val layout = baseDirectory.value / "src" / "main" / "res" / "layout"
      Seq(
        layout / "region"
      )
    },
    run <<= run in Android,
    compile <<= compile in Android
  )
  .dependsOn(core, riotapi)

lazy val riotapi = project
  .settings(commonSettings)
  .settings(
    exportJars := true,
    libraryDependencies ++= Dependencies.riotapiDeps
  )
  .dependsOn(core)
