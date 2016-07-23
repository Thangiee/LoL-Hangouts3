package com.thangiee.lolhangouts3

import lolchat._
import lolchat.data._
import lolchat.model.{Profile, Session}
import riotapi.free.RiotApiOps
import riotapi.models.Summoner

case class CurrentUserInfo(summoner: Summoner, profile: Profile)

object CurrentUserInfo {
  def load(session: Session): AsyncResult[CurrentUserInfo] =
    for {
      summoner <- riotApi.run(RiotApiOps.summonerByName(session.user), session.region)
      profile <- LoLChat.run(getProfile(session))
    } yield CurrentUserInfo(summoner, profile)
}