package org.helgi.investment

import cats.effect.{ExitCode, IO, IOApp}


object App extends IOApp :
  def run(args: List[String]): IO[ExitCode] =
    Server.create[IO]
      .use(_ => IO.never)
      .as(ExitCode.Success)
