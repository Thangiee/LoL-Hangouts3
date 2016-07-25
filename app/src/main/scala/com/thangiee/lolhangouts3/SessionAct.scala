package com.thangiee.lolhangouts3

import android.os.Bundle
import com.thangiee.lolhangouts3.SessionAct.NoSessionException
import lolchat._
import lolchat.model.Session

trait SessionAct extends BaseActivity {

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

  override def onResume(): Unit = {
    super.onResume()
  }

  override def onPause(): Unit = {
    super.onPause()
  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    stopAllSessionStreams()
  }

  def stopAllSessionStreams(): Unit = {
    session.msgStream.clear()
    session.connectionEventStream.clear()
    session.friendListStream.clear()
  }
}

object SessionAct {
  case class NoSessionException() extends Exception
}