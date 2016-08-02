package com.thangiee.lolhangouts3

import android.os.Bundle
import com.thangiee.lolhangouts3.SessionAct.NoSessionException
import lolchat._
import lolchat.model.Session
import rx.Ctx.Owner

trait SessionAct extends BaseActivity {

  implicit val owner: Owner = rx.Ctx.Owner.safe()

  val session: Session =
    (for {
      config  <- LoginConfig.load
      session <- LoLChat.findSession(sess => sess.user == config.user)
    } yield session).getOrElse(throw NoSessionException())

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    val oldHandler = Thread.getDefaultUncaughtExceptionHandler

    Thread.setDefaultUncaughtExceptionHandler((thread: Thread, throwable: Throwable) => {
      throwable match {
        case e: RuntimeException =>
          e.getCause match {
            case NoSessionException() =>
              finish()
              startActivity(LoginAct("No session found. Please log in again."))
              android.os.Process.killProcess(android.os.Process.myPid())
              System.exit(0);
            case _ => if (oldHandler != null) oldHandler.uncaughtException(thread, throwable) else System.exit(2)
          }
        case _ => if (oldHandler != null) oldHandler.uncaughtException(thread, throwable) else System.exit(2)
      }
    })
  }

  private var actVisible = true
  def isActVisible: Boolean = actVisible

  override def onResume(): Unit = {
    super.onResume()
    actVisible = true
  }

  override def onPause(): Unit = {
    super.onPause()
    actVisible = false
  }

  override def onDestroy(): Unit = {
    super.onDestroy()
  }

}

object SessionAct {
  case class NoSessionException() extends Exception
}