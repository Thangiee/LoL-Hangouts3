import android.AutoBuild
import sbt._
import android.Keys._

//object Build extends AutoBuild

object Dependencies {

  lazy val resolvers = Seq(
    Resolver.jcenterRepo,
    "jitpack" at "https://jitpack.io",
    "repo" at "http://dl.bintray.com/pixplicity/maven"
  )

  lazy val common = Seq(
    "com.lihaoyi" %% "upickle" % "0.4.1",
    "com.github.cb372" %% "scalacache-guava" % "0.9.1",
    "org.scalaj" %% "scalaj-http"   % "2.1.0",
    "com.typesafe.play" %% "play-json" % "2.4.0-M2"
  ) ++ cats

  lazy val lolchat = Seq(
    "org.igniterealtime.smack" % "smack-android" % "4.1.7",
    "com.github.thangiee" %% "lol-chat-lib" % "0.3.2"
  ).map(_.exclude("xpp3", "xpp3"))

  lazy val lolchatCore = Seq("com.github.thangiee" %% "lol-chat-core" % "0.3.2")

  lazy val cats = {
    val ver = "0.6.0"
    Seq(
      "org.typelevel" %% "cats-macros" % ver,
      "org.typelevel" %% "cats-kernel" % ver,
      "org.typelevel" %% "cats-core" % ver,
      "org.typelevel" %% "cats-free" % ver
    )
  }

  lazy val macroid = {
    val ver = "2.0.0-M5"
    Seq(
      aar("org.macroid" %% "macroid" % ver),
      aar("org.macroid" %% "macroid-viewable" % ver),
      "com.android.support" % "support-v4" % "20.0.0"
    )
  }

  lazy val androidSupport = {
    val ver = "23.2.1"
    Seq(
      aar("com.android.support" % "design" % ver),
      aar("com.android.support" % "appcompat-v7" % ver),
      "com.android.support" % "cardview-v7" % ver
    )
  }

  lazy val androidLibs = {
    Seq(
      "com.github.bumptech.glide" % "glide" % "3.6.1",
      "com.makeramen" % "roundedimageview" % "2.2.1",
      "com.jude" % "easyrecyclerview" % "4.0.4",
      "com.hanhuy.android" %% "scala-conversions" % "23.1.1",
      "com.hanhuy.android" %% "scala-conversions-appcompat" % "23.1.1",
      "com.rengwuxian.materialedittext" % "library" % "2.1.4",
      "com.github.dmytrodanylyk" % "android-morphing-button" % "98a4986e56",
      "com.pixplicity.easyprefs" % "library" % "1.7",
      "com.github.florent37" % "viewanimator" % "1.0.4",
//      "com.noveogroup.android" % "android-logger" % "1.3.5",
      aar("com.mikepenz" % "materialdrawer" % "5.3.6")
    )
  }

  lazy val logging = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"
  )

  lazy val logback = logging ++ Seq(
    "ch.qos.logback" %  "logback-classic" % "1.1.7"
  )

  lazy val testing = Seq(
    "org.scalatest"          %% "scalatest"       % "2.2.5"     % "test",
    "org.scalacheck"         %% "scalacheck"      % "1.12.4"    % "test"
  )

  val coreDeps = common
  val riotapiDeps = common ++ lolchatCore ++ testing ++ logback.map(_ % "provided")
  val androidDeps = common ++ androidSupport ++ lolchat ++ androidLibs ++ logging
}

object ProguardValues {

  val cache = Seq(
    "org",
    "java",

    // lolchat
    "org.jivesoftware",
    "lolchat",
    "io.dylemma",

    // riotapi
    "riotapi",
    "play",
    "scalaj",
    "org.joda",
    "com.fasterxml",
    "cats",

    "macroid",
    "upickle",
    "com.hanhuy",
    "com.google",
    "org.joda",
    "scalacache",
    "android.support",
    "com.bumptech",
    "com.typesafe"
  )

  val options = Seq(
    "-dontobfuscate",
    "-dontoptimize",
    "-dontpreverify",
    "-keepattributes Signature",
    "-dontnote **",
    "-dontwarn scala.**",
    "-dontwarn org.slf4j.**",
    "-dontwarn java.**",
    "-dontwarn sun.misc.Unsafe",
    "-dontwarn javax.xml.bind.DatatypeConverter",

    // lolchat
    "-dontwarn org.jivesoftware.smack.**",
    "-dontwarn org.xmlpull.v1.**",
    "-dontwarn javax.xml.namespace.QName",
    "-keep class * implements org.jivesoftware.smack.provider.IQProvider",
    "-keep class * implements org.jivesoftware.smack.provider.PacketExtensionProvider",
    "-keep class org.jivesoftware.smack.** { *; }",
    "-keep class org.jivesoftware.smackx.** { *; }",
    "-keep class de.measite.smack.AndroidDebugger { *; }",

    // riotapi
    "-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry",
    "-dontwarn org.scalacheck.**",
    "-dontwarn org.specs2.**",
    "-dontwarn org.typelevel.**",

    "-keep class macroid.** { *; }",

    // glide
    "-keep public class * implements com.bumptech.glide.module.GlideModule",
    "-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {\n    **[] $VALUES;\n    public *;\n}",

    // roundedImgV
    "-dontwarn com.squareup.picasso.**",

    "-keep class com.noveogroup.android.log.**"
  )

}