package com.thangiee.lolhangouts3
import java.nio.ByteBuffer

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.LinearLayout
import lolchat.model._
import boopickle.Default._

class ChatAct extends SessionAct {
  type RootView = LinearLayout
  lazy val views: TypedViewHolder.chat_act = TypedViewHolder.setContentView(this, TR.layout.chat_act)
  lazy val toolbar: Toolbar = views.toolbar.rootView

  lazy val friend = Unpickle[Friend].fromBytes(ByteBuffer.wrap(getIntent.getByteArrayExtra("arg1")))

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    getSupportActionBar.setTitle(friend.name)
    toolbar.setNavigationOnClickListener(_ => finish())
  }
}

object ChatAct {
  def apply(friend: Friend)(implicit ctx: Ctx): Intent = {
    val i = new Intent(ctx, classOf[ChatAct])
    i.putExtra("arg1", Pickle.intoBytes(friend).array())
  }
}