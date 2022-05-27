package org.helgi.investment

import cats.effect.*
import cats.syntax.all.*
import com.comcast.ip4s.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.Router

import scala.concurrent.duration.*

val helloWorldService = HttpRoutes.of[IO] {
  case GET -> Root / "hello" / name =>
    Ok(s"Hello, $name.")
}

case class Tweet(id: Int, message: String)

def getTweet(tweetId: Int): IO[Tweet] = ???

def getPopularTweets(): IO[Seq[Tweet]] = ???

val tweetService = HttpRoutes.of[IO] {
  case GET -> Root / "tweets" / "popular" =>
    getPopularTweets().flatMap(Ok(_))
  case GET -> Root / "tweets" / IntVar(tweetId) =>
    getTweet(tweetId).flatMap(Ok(_))
}

val services = tweetService <+> helloWorldService
val httpApp = Router("/" -> helloWorldService, "/api" -> services).orNotFound

object App extends IOApp :
  def run(args: List[String]): IO[ExitCode] =
    Server.create[IO]
      .use(_ => IO.never)
      .as(ExitCode.Success)
