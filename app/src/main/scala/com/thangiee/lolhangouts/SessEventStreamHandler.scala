package com.thangiee.lolhangouts

import android.app.{Notification, PendingIntent}
import android.graphics.Color
import android.os.Bundle
import com.pixplicity.easyprefs.library.Prefs
import lolchat._
import lolchat.data.{AsyncResult, _}
import lolchat.model.{Friend, Msg}
import share.Message

import scala.concurrent.Future

trait RefreshFndList extends SessionAct {
  private lazy val fndListStream = session.friendListStream.map(_ => if (isActVisible) refreshFriendList())
  private lazy val msgStream = session.msgStream.map(_ => if (isActVisible) refreshFriendList())
  def refreshFriendList(): Unit

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState); fndListStream; msgStream
  }

  override def onDestroy(): Unit = { super.onDestroy(); fndListStream.kill(); msgStream.kill() }
}

trait NotifyReceivedMsg extends SessionAct {
  private lazy val eventStreamHandler = session.msgStream.map { msg =>
    // only notify when not in chat with the user that sent the msg
    val notFromThisChat = msg.fromId != activeFriendChat.map(_.id).getOrElse("-1")
    val prefNotifyMsgOn = Prefs.getBoolean(TR.string.pref_notify_msg.value, true)
    if (notFromThisChat && prefNotifyMsgOn) {
      playSound(R.raw.alert_pm_receive)
      mkMsgNotification(msg).map(showMsgNotifi)
    }
  }

  protected def activeFriendChat: Option[Friend]
  def msgNotificationPendingIntent(f: Friend): PendingIntent

  private def mkMsgNotification(msg: Msg): AsyncResult[Notification] = {
    LoLChat.run(friendById(msg.fromId)(session)).flatMap {
      case Some(f) => AsyncResult.right {
        val notify = new Notification.Builder(ctx)
          .setSmallIcon(TR.drawable.ic_launcher.resid)
          .setLargeIcon(toBitmap(TR.drawable.ic_launcher.resid))
          .setContentTitle("New Message")
          .setContentText(s"${f.name}: ${msg.txt}")
          .setContentIntent(msgNotificationPendingIntent(f))
          .setStyle(new Notification.BigTextStyle()
            .setSummaryText(s"Open chat with ${f.name}")
            .bigText(s"${f.name}: ${msg.txt}")
          )
          .setPriority(Notification.PRIORITY_HIGH)
          .setAutoCancel(true)
          .setLights(Color.BLUE, 300, 3000) // blue light, 300ms on, 3s off
          .build()

        notify.tickerText = s"${f.name}: ${msg.txt}"
        notify
      }
      case None => AsyncResult.left(Error(404, "msg not from someone in the friends list"))
    }
  }

  override def onCreate(savedInstanceState: Bundle): Unit = { super.onCreate(savedInstanceState); eventStreamHandler }
  override def onDestroy(): Unit = { super.onDestroy(); eventStreamHandler.kill() }
}

trait SaveReceivedMsg extends SessionAct {
  private lazy val eventStreamHandler = session.msgStream.map(msg => {
    val isRead = msg.fromId == activeFriendChat.map(_.id).getOrElse("-1")
    saveMsg(Message(_, msg.fromId.toInt, msg.txt, sender = false, isRead))
  })
  protected def activeFriendChat: Option[Friend]
  def saveMsg(partialMsg: (userSummId) => Message): Future[Unit]

  override def onCreate(savedInstanceState: Bundle): Unit = { super.onCreate(savedInstanceState); eventStreamHandler }
  override def onDestroy(): Unit = { super.onDestroy(); eventStreamHandler.kill() }
}

