package com.thangiee.lolhangouts3

import lolchat._
import lolchat.data._
import lolchat.model.{Profile, Session}
import riotapi.free.RiotApiOps
import riotapi.models.Summoner
import AuxFunctions._
import boopickle.Default._

case class CurrentUserInfo(summoner: Summoner, profile: Profile)

object CurrentUserInfo {

  def load(sess: Session): AsyncResult[CurrentUserInfo] = {
    def setStatusMsg(profile: Profile): Profile = {
      if (profile.statusMsg.isEmpty) {
        val msg = prefsGet[String](s"${sess.user}-statusMsg").getOrElse("Using LoL Hangouts app")
        profile.copy(statusMsg = msg)
      } else {
        profile
      }
    }

    for {
      summoner <- riotApi.run(RiotApiOps.summonerByName(sess.user), sess.region)
      profile <- LoLChat.run(modifyProfile(setStatusMsg)(sess))
    } yield CurrentUserInfo(summoner, profile)
  }

  def saveStatusMsg(msg: String, sess: Session): Unit= prefsPut(s"${sess.user}-statusMsg", msg)
}