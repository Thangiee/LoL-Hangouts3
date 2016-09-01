package com.thangiee

import android.content.Context
import riotapi._

import scala.language.implicitConversions

package object lolhangouts extends AnyRef with Conversions {
  type Ctx = Context
  type Error = lolchat.data.Error
  val Error = lolchat.data.Error
  type userSummId = Int

  implicit val exeCtx = scala.concurrent.ExecutionContext.Implicits.global

  val riotApi = RiotApi(Key.riotApiKeys)
}
