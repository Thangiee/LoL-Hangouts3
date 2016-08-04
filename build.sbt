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

lazy val share = project
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Dependencies.playJson,
    exportJars := true
  )

lazy val server = project
  .settings(commonSettings)
  .settings(
    version := "0.1.0",
    scalacOptions ++=  Seq("-Xexperimental", "-Ybackend:GenBCode", "-Ydelambdafy:method", "-target:jvm-1.8", "-Yopt:l:classpath"),
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.11" % "0.7.0",
    libraryDependencies ++= Dependencies.serverDeps
  )
  .enablePlugins(JavaServerAppPackaging)
  .dependsOn(share)

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
  .dependsOn(share, riotapi)

lazy val riotapi = project
  .settings(commonSettings)
  .settings(
    exportJars := true,
    libraryDependencies ++= Dependencies.riotapiDeps
  )
  .dependsOn(share)
