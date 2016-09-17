package riotapi

import cats.data._
import lolchat.data.{AsyncResult, Region}
import riotapi.models._
import riotapi.utils.Parsing._

class Parsing extends BaseSpec {

  def validate[A](xor: Xor[Throwable, A]) = xor.leftMap(err => fail(err))

  def validate[A](xor: Xor[Throwable, A], result: A) = xor match {
    case Xor.Right(b) => b shouldEqual result
    case Xor.Left(err) => fail(err)
  }

  def validate[A](xor: Xor[Throwable, A], p: A => Boolean) = xor match {
    case Xor.Right(b) => p(b) shouldEqual true
    case Xor.Left(err) => fail(err)
  }

  val summonerJson = """{"thangiee":{"id":25011185,"name":"Thangiee","profileIconId":984,"summonerLevel":30,"revisionDate":1451719420000}}"""

  "parseSummoner()" must "parse json string into Summoner" in {
    validate(parseSummoner("thangiee", summonerJson), Summoner(25011185, "Thangiee", 984, 30))
  }

  it must "be case insensitive" in {
    validate(parseSummoner("Thangiee", summonerJson))
  }

  val champStaticDataJson =
    """
      |{
      |   "id": 1,
      |   "title": "the Dark Child",
      |   "name": "Annie",
      |   "key": "Annie"
      |}
    """.stripMargin

  "parseChampion()" must "parse json string into Champion" in {
    validate(parseChampion(champStaticDataJson))
  }

  val leaguesJson = "{\"24194463\":[{\"name\":\"Sejuani's Brutes\",\"tier\":\"DIAMOND\",\"queue\":\"RANKED_SOLO_5x5\",\"entries\":[{\"playerOrTeamId\":\"24194463\",\"playerOrTeamName\":\"Alexei\",\"division\":\"V\",\"leaguePoints\":100,\"wins\":30,\"losses\":30,\"isHotStreak\":false,\"isVeteran\":false,\"isFreshBlood\":false,\"isInactive\":false,\"miniSeries\":{\"target\":2,\"wins\":1,\"losses\":1,\"progress\":\"WLN\"}}]}]}"

  "parseLeagueEntries()" must "parse json string into Vector of Leagues" in {
    validate(parseLeagueEntries(24194463, leaguesJson))
  }

  it must "also work for seq of Ids" in {
    parseLeagueEntries(Seq(24194463), leaguesJson).get(24194463).isDefined should be(true)
  }

  val spellJson: String =
    """
      |{"name":"Cleanse","description":"Removes all disables and summoner spell debuffs affecting your champion and lowers the duration of incoming disables by 65% for 3 seconds.","summonerLevel":6,"id":1,"key":"SummonerBoost"}""".stripMargin

  "parseSummonerSpell()" must "parse json string into SummonerSpell" in {
    validate(parseSummonerSpell(spellJson))
  }

  val currentGameInfoJson =
    """
      |{"gameId":2197123765,"mapId":11,"gameMode":"CLASSIC","gameType":"MATCHED_GAME","gameQueueConfigId":410,"participants":[{"teamId":100,"spell1Id":4,"spell2Id":7,"championId":236,"profileIconId":1117,"summonerName":"northkoreadabest","bot":false,"summonerId":28964314,"runes":[{"count":9,"runeId":5245},{"count":4,"runeId":5277},{"count":5,"runeId":5289},{"count":9,"runeId":5317},{"count":3,"runeId":5337}],"masteries":[{"rank":5,"masteryId":6111},{"rank":1,"masteryId":6122},{"rank":5,"masteryId":6131},{"rank":1,"masteryId":6141},{"rank":5,"masteryId":6151},{"rank":1,"masteryId":6162},{"rank":5,"masteryId":6312},{"rank":1,"masteryId":6322},{"rank":5,"masteryId":6331},{"rank":1,"masteryId":6343}]},{"teamId":100,"spell1Id":11,"spell2Id":4,"championId":31,"profileIconId":689,"summonerName":"Ks Artist","bot":false,"summonerId":36692193,"runes":[{"count":9,"runeId":5273},{"count":9,"runeId":5289},{"count":9,"runeId":5316},{"count":3,"runeId":5365}],"masteries":[{"rank":5,"masteryId":6114},{"rank":1,"masteryId":6121},{"rank":5,"masteryId":6134},{"rank":1,"masteryId":6142},{"rank":5,"masteryId":6312},{"rank":1,"masteryId":6323},{"rank":5,"masteryId":6331},{"rank":1,"masteryId":6343},{"rank":5,"masteryId":6352},{"rank":1,"masteryId":6362}]},{"teamId":100,"spell1Id":4,"spell2Id":12,"championId":92,"profileIconId":1117,"summonerName":"arizonaDEMON","bot":false,"summonerId":38270339,"runes":[{"count":9,"runeId":5245},{"count":9,"runeId":5295},{"count":9,"runeId":5317},{"count":2,"runeId":5335},{"count":1,"runeId":5355}],"masteries":[{"rank":5,"masteryId":6111},{"rank":1,"masteryId":6122},{"rank":5,"masteryId":6134},{"rank":1,"masteryId":6141},{"rank":5,"masteryId":6311},{"rank":1,"masteryId":6323},{"rank":5,"masteryId":6331},{"rank":1,"masteryId":6343},{"rank":5,"masteryId":6352},{"rank":1,"masteryId":6362}]},{"teamId":100,"spell1Id":3,"spell2Id":4,"championId":143,"profileIconId":982,"summonerName":"iZZardG","bot":false,"summonerId":38766160,"runes":[{"count":9,"runeId":5273},{"count":9,"runeId":5289},{"count":9,"runeId":5317},{"count":2,"runeId":5357},{"count":1,"runeId":5361}],"masteries":[{"rank":5,"masteryId":6114},{"rank":1,"masteryId":6122},{"rank":5,"masteryId":6131},{"rank":1,"masteryId":6141},{"rank":5,"masteryId":6312},{"rank":1,"masteryId":6322},{"rank":5,"masteryId":6331},{"rank":1,"masteryId":6343},{"rank":5,"masteryId":6351},{"rank":1,"masteryId":6362}]},{"teamId":100,"spell1Id":11,"spell2Id":14,"championId":28,"profileIconId":23,"summonerName":"cRusadeer","bot":false,"summonerId":20579216,"runes":[{"count":9,"runeId":5273},{"count":9,"runeId":5296},{"count":9,"runeId":5317},{"count":3,"runeId":5357}],"masteries":[{"rank":5,"masteryId":6114},{"rank":1,"masteryId":6121},{"rank":5,"masteryId":6134},{"rank":1,"masteryId":6142},{"rank":5,"masteryId":6312},{"rank":1,"masteryId":6321},{"rank":5,"masteryId":6331},{"rank":1,"masteryId":6343},{"rank":5,"masteryId":6351},{"rank":1,"masteryId":6362}]},{"teamId":200,"spell1Id":14,"spell2Id":4,"championId":23,"profileIconId":1109,"summonerName":"x THE FLASH x","bot":false,"summonerId":34573162,"runes":[{"count":9,"runeId":5245},{"count":9,"runeId":5289},{"count":9,"runeId":5317},{"count":3,"runeId":5337}],"masteries":[{"rank":5,"masteryId":6111},{"rank":1,"masteryId":6121},{"rank":5,"masteryId":6134},{"rank":1,"masteryId":6142},{"rank":5,"masteryId":6151},{"rank":1,"masteryId":6161},{"rank":5,"masteryId":6211},{"rank":1,"masteryId":6223},{"rank":5,"masteryId":6231},{"rank":1,"masteryId":6242}]},{"teamId":200,"spell1Id":3,"spell2Id":4,"championId":12,"profileIconId":770,"summonerName":"Jon Mclane","bot":false,"summonerId":51147667,"runes":[{"count":9,"runeId":5257},{"count":9,"runeId":5289},{"count":9,"runeId":5316},{"count":3,"runeId":5345}],"masteries":[{"rank":5,"masteryId":6211},{"rank":1,"masteryId":6221},{"rank":5,"masteryId":6231},{"rank":1,"masteryId":6241},{"rank":5,"masteryId":6252},{"rank":1,"masteryId":6262},{"rank":5,"masteryId":6311},{"rank":1,"masteryId":6322},{"rank":5,"masteryId":6331},{"rank":1,"masteryId":6343}]},{"teamId":200,"spell1Id":11,"spell2Id":4,"championId":121,"profileIconId":21,"summonerName":"MilkinCookies","bot":false,"summonerId":21726330,"runes":[{"count":1,"runeId":5245},{"count":8,"runeId":5253},{"count":6,"runeId":5295},{"count":3,"runeId":5296},{"count":9,"runeId":5317},{"count":3,"runeId":5335}],"masteries":[{"rank":5,"masteryId":6114},{"rank":1,"masteryId":6121},{"rank":5,"masteryId":6131},{"rank":1,"masteryId":6142},{"rank":5,"masteryId":6312},{"rank":1,"masteryId":6323},{"rank":5,"masteryId":6331},{"rank":1,"masteryId":6343},{"rank":5,"masteryId":6351},{"rank":1,"masteryId":6362}]},{"teamId":200,"spell1Id":14,"spell2Id":4,"championId":90,"profileIconId":744,"summonerName":"powerorgon","bot":false,"summonerId":26219524,"runes":[{"count":6,"runeId":5295},{"count":3,"runeId":5296},{"count":9,"runeId":5317},{"count":3,"runeId":5357},{"count":9,"runeId":5402}],"masteries":[{"rank":5,"masteryId":6114},{"rank":1,"masteryId":6122},{"rank":5,"masteryId":6131},{"rank":1,"masteryId":6142},{"rank":5,"masteryId":6154},{"rank":1,"masteryId":6164},{"rank":5,"masteryId":6312},{"rank":1,"masteryId":6322},{"rank":5,"masteryId":6331},{"rank":1,"masteryId":6343}]},{"teamId":200,"spell1Id":4,"spell2Id":7,"championId":51,"profileIconId":583,"summonerName":"Ploximus","bot":false,"summonerId":28699702,"runes":[{"count":8,"runeId":5245},{"count":1,"runeId":5251},{"count":9,"runeId":5289},{"count":9,"runeId":5317},{"count":3,"runeId":5337}],"masteries":[{"rank":5,"masteryId":6111},{"rank":1,"masteryId":6122},{"rank":5,"masteryId":6131},{"rank":1,"masteryId":6141},{"rank":5,"masteryId":6151},{"rank":1,"masteryId":6162},{"rank":5,"masteryId":6211},{"rank":1,"masteryId":6223},{"rank":5,"masteryId":6232},{"rank":1,"masteryId":6242}]}],"observers":{"encryptionKey":"ablCq8Bgut7eh1BPMIoBOJroj0ZmLY3N"},"platformId":"NA1","bannedChampions":[{"championId":63,"teamId":100,"pickTurn":1},{"championId":157,"teamId":200,"pickTurn":2},{"championId":238,"teamId":100,"pickTurn":3},{"championId":44,"teamId":200,"pickTurn":4},{"championId":16,"teamId":100,"pickTurn":5},{"championId":1,"teamId":200,"pickTurn":6}],"gameStartTime":1464113435648,"gameLength":113}""".stripMargin

  "parseCurrentGameInfo()" must "parse json string into CurrentGameInfo" in {
    validate(parseCurrentGameInfo(currentGameInfoJson))
  }

  val rankedStatsJson =
    """
      |{"summonerId":25011185,"modifyDate":1406652627000,"champions":[{"id":99,"stats":{"totalSessionsPlayed":4,"totalSessionsLost":2,"totalSessionsWon":2,"totalChampionKills":20,"totalDamageDealt":512459,"totalDamageTaken":44328,"mostChampionKillsPerSession":11,"totalMinionKills":756,"totalDoubleKills":3,"totalTripleKills":0,"totalQuadraKills":0,"totalPentaKills":0,"totalUnrealKills":0,"totalDeathsPerSession":9,"totalGoldEarned":45375,"mostSpellsCast":0,"totalTurretsKilled":2,"totalPhysicalDamageDealt":68219,"totalMagicDamageDealt":443892,"totalFirstBlood":0,"totalAssists":27,"maxChampionsKilled":11,"maxNumDeaths":3}},{"id":157,"stats":{"totalSessionsPlayed":14,"totalSessionsLost":5,"totalSessionsWon":9,"totalChampionKills":110,"totalDamageDealt":1792905,"totalDamageTaken":268414,"mostChampionKillsPerSession":18,"totalMinionKills":2511,"totalDoubleKills":19,"totalTripleKills":4,"totalQuadraKills":0,"totalPentaKills":0,"totalUnrealKills":0,"totalDeathsPerSession":79,"totalGoldEarned":167227,"mostSpellsCast":0,"totalTurretsKilled":9,"totalPhysicalDamageDealt":1311486,"totalMagicDamageDealt":471702,"totalFirstBlood":0,"totalAssists":92,"maxChampionsKilled":18,"maxNumDeaths":13}},{"id":10,"stats":{"totalSessionsPlayed":1,"totalSessionsLost":1,"totalSessionsWon":0,"totalChampionKills":4,"totalDamageDealt":88307,"totalDamageTaken":19548,"mostChampionKillsPerSession":4,"totalMinionKills":137,"totalDoubleKills":1,"totalTripleKills":0,"totalQuadraKills":0,"totalPentaKills":0,"totalUnrealKills":0,"totalDeathsPerSession":7,"totalGoldEarned":8666,"mostSpellsCast":0,"totalTurretsKilled":0,"totalPhysicalDamageDealt":26003,"totalMagicDamageDealt":61920,"totalFirstBlood":0,"totalAssists":1,"maxChampionsKilled":4,"maxNumDeaths":7}},{"id":0,"stats":{"totalSessionsPlayed":19,"totalSessionsLost":8,"totalSessionsWon":11,"totalChampionKills":134,"killingSpree":65,"totalDamageDealt":2393671,"totalDamageTaken":332290,"mostChampionKillsPerSession":18,"totalMinionKills":3404,"totalDoubleKills":23,"totalTripleKills":4,"totalQuadraKills":0,"totalPentaKills":0,"totalUnrealKills":0,"totalDeathsPerSession":95,"totalGoldEarned":221268,"mostSpellsCast":0,"totalTurretsKilled":11,"totalPhysicalDamageDealt":1405708,"totalMagicDamageDealt":977514,"totalNeutralMinionsKilled":170,"totalFirstBlood":0,"totalAssists":120,"totalHeal":12877,"maxLargestKillingSpree":10,"maxLargestCriticalStrike":580,"maxChampionsKilled":18,"maxNumDeaths":13,"maxTimePlayed":2644,"maxTimeSpentLiving":1658,"normalGamesPlayed":0,"rankedSoloGamesPlayed":0,"rankedPremadeGamesPlayed":0,"botGamesPlayed":0}}]}
    """.stripMargin

  "parseChampsStats()" must "parse json string into Vector of ChampionStats" in {
    validate(parseChampsStats(rankedStatsJson))
  }

  val statsSummaryJson =
    """
      |{"summonerId":20132258,"playerStatSummaries":[{"playerStatSummaryType":"AramUnranked5x5","wins":17,"modifyDate":1453284599000,"aggregatedStats":{"totalChampionKills":375,"totalTurretsKilled":22,"totalAssists":476}},{"playerStatSummaryType":"CoopVsAI","wins":3,"modifyDate":1453284599000,"aggregatedStats":{"totalChampionKills":73,"totalMinionKills":153,"totalTurretsKilled":5,"totalNeutralMinionsKilled":65,"totalAssists":29}},{"playerStatSummaryType":"OdinUnranked","wins":9,"modifyDate":1453284599000,"aggregatedStats":{"totalChampionKills":219,"totalAssists":132,"maxChampionsKilled":32,"averageNodeCapture":7,"averageNodeNeutralize":5,"averageTeamObjective":2,"averageTotalPlayerScore":1306,"averageCombatPlayerScore":588,"averageObjectivePlayerScore":719,"averageNodeCaptureAssist":2,"averageNodeNeutralizeAssist":2,"maxNodeCapture":11,"maxNodeNeutralize":10,"maxTeamObjective":2,"maxTotalPlayerScore":2394,"maxCombatPlayerScore":1387,"maxObjectivePlayerScore":1257,"maxNodeCaptureAssist":3,"maxNodeNeutralizeAssist":3,"totalNodeNeutralize":68,"totalNodeCapture":102,"averageChampionsKilled":14,"averageNumDeaths":7,"averageAssists":8,"maxAssists":19}},{"playerStatSummaryType":"RankedPremade3x3","wins":0,"losses":0,"modifyDate":1347526923000,"aggregatedStats":{}},{"playerStatSummaryType":"RankedPremade5x5","wins":0,"losses":0,"modifyDate":1369323990000,"aggregatedStats":{}},{"playerStatSummaryType":"RankedTeam3x3","wins":0,"losses":0,"modifyDate":1372728961000,"aggregatedStats":{}},{"playerStatSummaryType":"RankedTeam5x5","wins":0,"losses":0,"modifyDate":1334299476000,"aggregatedStats":{}},{"playerStatSummaryType":"Unranked","wins":766,"modifyDate":1453284599000,"aggregatedStats":{"totalChampionKills":12381,"totalMinionKills":181577,"totalTurretsKilled":1360,"totalNeutralMinionsKilled":33164,"totalAssists":9431}},{"playerStatSummaryType":"Unranked3x3","wins":0,"modifyDate":1453284599000,"aggregatedStats":{"totalChampionKills":15,"totalMinionKills":216,"totalTurretsKilled":0,"totalNeutralMinionsKilled":97,"totalAssists":10}},{"playerStatSummaryType":"RankedSolo5x5","wins":128,"losses":70,"modifyDate":1462140785000,"aggregatedStats":{"totalChampionKills":1450,"totalMinionKills":38440,"totalTurretsKilled":339,"totalNeutralMinionsKilled":2945,"totalAssists":1452}},{"playerStatSummaryType":"URF","wins":2,"modifyDate":1463295518000,"aggregatedStats":{"totalChampionKills":60,"totalMinionKills":320,"totalTurretsKilled":1,"totalNeutralMinionsKilled":74,"totalAssists":33}},{"playerStatSummaryType":"Hexakill","wins":0,"modifyDate":1464032320000,"aggregatedStats":{"totalChampionKills":9,"totalMinionKills":56,"totalTurretsKilled":0,"totalNeutralMinionsKilled":14,"totalAssists":8}}]}
    """.stripMargin

  "parsePlayerStatsSummary()" must "parse json string into Vector of PlayerStatsSummaries" in {
    validate(parsePlayerStatsSummary(statsSummaryJson))
  }

  it should "use case class default value if it's missing in the json string" in {
    validate[Summoner](parseSummoner("thangiee", """{"thangiee": {}}"""), result = Summoner())
  }

}
