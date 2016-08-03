package com.thangiee.lolhangouts3

import java.nio.ByteBuffer

import android.os.{Handler, Looper}
import android.support.design.widget.Snackbar
import android.util.Base64
import android.view.View
import boopickle.Default._
import com.pixplicity.easyprefs.library.Prefs

import scala.concurrent.duration.FiniteDuration
import scala.util.Try

trait AuxFunctions {

  def delay(duration: FiniteDuration)(fn: => Unit): Unit = {
    new Handler().postDelayed(() => fn, duration.toMillis)
  }

  def delayRunOnUi(duration: FiniteDuration)(fn: => Unit): Unit = {
    new Handler(Looper.getMainLooper).postDelayed(() => fn, duration.toMillis)
  }

  def runOnUi(f: => Unit): Unit = new Handler(Looper.getMainLooper).post(() => f)

  def snackbar(view: View, txt: String, millis: Int): Snackbar = Snackbar.make(view, txt, millis)

  def shortSnackbar(view: View, txt: String): Snackbar = snackbar(view, txt, Snackbar.LENGTH_SHORT)

  def longSnackbar(view: View, txt: String): Snackbar = snackbar(view, txt, Snackbar.LENGTH_LONG)

  def infSnackbar(view: View, txt: String): Snackbar = snackbar(view, txt, Snackbar.LENGTH_INDEFINITE)

  def prefsPut[A](key: String, value: A)(implicit pickler: Pickler[A]): Unit = {
    val string: String = Base64.encodeToString(Pickle.intoBytes(value).array(), Base64.DEFAULT)
    Prefs.putString(key, string)
  }

  def prefsGet[A](key: String)(implicit picker: Pickler[A]): Option[A] = {
    Try(Unpickle[A].fromBytes(ByteBuffer.wrap(Base64.decode(Prefs.getString(key, ""), Base64.DEFAULT)))).toOption
  }

  def prefsDelete(key: String): Unit = Prefs.remove(key)

  def prefsUpdate[A](key: String, f: A => A)(implicit pkl: Pickler[A]): Option[A] =
    for {
      a  <- prefsGet[A](key)
      fa = f(a)
      _  <- Some(prefsPut(key, fa))
    } yield fa
}

object AuxFunctions extends AuxFunctions