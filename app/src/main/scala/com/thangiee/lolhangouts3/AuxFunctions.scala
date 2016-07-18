package com.thangiee.lolhangouts3

import android.os.{Handler, Looper}

import scala.concurrent.duration.FiniteDuration

trait AuxFunctions {

  def delay(duration: FiniteDuration)(fn: => Unit): Unit = {
    new Handler().postDelayed(() => fn, duration.toMillis)
  }

  def delayRunOnUi(duration: FiniteDuration)(fn: => Unit): Unit = {
    new Handler(Looper.getMainLooper).postDelayed(() => fn, duration.toMillis)
  }

  def runOnUi(f: => Unit): Unit = new Handler(Looper.getMainLooper).post(() => f)
}

object AuxFunctions extends AuxFunctions