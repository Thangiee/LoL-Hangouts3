package com.thangiee.lolhangouts3
import java.nio.ByteBuffer

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.RelativeLayout
import boopickle.Default._
import lolchat._
import lolchat.model._
import enrichments._
import com.hanhuy.android.extensions._
import ClientApi._
import autowire._
import share.Message

class ChatAct extends SessionAct {
  type RootView = RelativeLayout
  lazy val views: TypedViewHolder.chat_act = TypedViewHolder.setContentView(this, TR.layout.chat_act)
  lazy val toolbar: Toolbar = views.toolbar.rootView

  lazy val friend = Unpickle[Friend].fromBytes(ByteBuffer.wrap(getIntent.getByteArrayExtra("arg1")))

  lazy val sendBtn = views.sendBtn
  lazy val msgField = views.messageEdt

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    getSupportActionBar.setTitle(friend.name)
    toolbar.setNavigationOnClickListener(_ => finish())

    CurrentUserInfo.load(session).map(user =>
      sendBtn.onClick0 {
        val txtMsg = msgField.txt
        LoLChat.run(sendMsg(friend.id, txtMsg)(session))
          .map(_ => {
            clientApi.saveMsg(Message(user.summoner.id, friend.id.toInt, txtMsg)).call()
            runOnUi {
              msgField.setText("")
              // add msg to view
            }
          })
          .leftMap(err => {
            // show error message
          })
      }
    )
  }
}

object ChatAct {
  def apply(friend: Friend)(implicit ctx: Ctx): Intent = {
    val i = new Intent(ctx, classOf[ChatAct])
    i.putExtra("arg1", Pickle.intoBytes(friend).array())
  }
}