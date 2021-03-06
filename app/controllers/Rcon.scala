package controllers

import models.{Ranks, ChatMessage, MongoLogger, UtAdminUser}
import play.api.Logger
import securesocial.core.RuntimeEnvironment

/**
  * REST services for using rcon commands
  */
class Rcon(override implicit val env: RuntimeEnvironment[UtAdminUser])
  extends securesocial.core.SecureSocial[UtAdminUser] {

  val log = Logger(this getClass() getName())
  val server = UtServer

  def slap(player: Int, name: String) = SecuredAction {
    request =>
      MongoLogger.logAction(request.user, "Slapping player " + player)
      sendMsg(s"!slap $name", request.user)
      server.rcon.rcon("slap " + player)
      Redirect("/")
  }

  def kill(player: Int, name: String) = SecuredAction {
    request =>
      if (request.user.rank != Ranks.Mod) {
        MongoLogger.logAction(request.user, "Killing player " + player)
        sendMsg(s"!kill $name", request.user)
        server.rcon.rcon("smite " + player)
        Redirect("/")
      } else {
        Unauthorized("You are not allowed to do this")
      }
  }

  def nuke(player: Int, name: String) = SecuredAction {
    request =>
      if (request.user.rank != Ranks.Mod) {
        MongoLogger.logAction(request.user, "Nukeing player " + player)
        sendMsg(s"!nuke $name", request.user)
        server.rcon.rcon("nuke " + player)
        Redirect("/")
      } else {
        Unauthorized("You are not allowed to do this")
      }

  }

  def forceRed(player: Int, name: String) = force(player, "red", name)

  def forceBlue(player: Int, name: String) = force(player, "blue", name)

  def forceSpec(player: Int, name: String) = force(player, "spectator", name)

  private def force(player: Int, team: String, name: String) = SecuredAction {
    request =>
      MongoLogger.logAction(request.user, "Force player " + player)
      sendMsg(s"!force $name $team", request.user)
      server.rcon.rcon("forceteam " + player + " " + team)
      Redirect("/")
  }

  def privateMessage(receiverSlot: Int, text: String) = SecuredAction { request =>
    MongoLogger.logAction(request.user, s"PM TO : $receiverSlot text: $text")
    val toSend = "^2" + request.user.main.userId + "^7: " + text
    server.rcon.rcon(s"tell $receiverSlot $toSend")
    ChatMessage.insertChatMessage(
      connection = server.b3.connection,
      adminName = request.user.main.userId,
      message = text,
      msgType = "PM",
      request.user.b3Id
    )
    Ok("Sent!.")
  }

  def sendMsg(text: String, user: UtAdminUser): Unit = {
    val toSend = "^2" + user.main.userId + "^7: " + text
    server.rcon.rcon("say " + toSend)
    ChatMessage.insertChatMessage(
      connection  = server.b3.connection,
      adminName = user.main.userId,
      message = text,
      adminId = user.b3Id)
  }

  def say(text: String) = SecuredAction { request =>
    MongoLogger.logAction(request.user, "Saying: " + text)
    sendMsg(text, request.user)
    Ok("Sent!.")
  }

  def kick(id: Int, name: String) = SecuredAction { request =>
    if (request.user.rank != Ranks.Mod) {
      MongoLogger.logAction(request.user, s"Kicking Player $name Id= $id")
      sendMsg(s"!kick $name", request.user)
      server.rcon.rcon("kick " + id)
      Redirect(routes.Application.index())
    } else {
      Unauthorized("You are not allowed to do that")
    }
  }
}
