package org.helgi.investment

import cats.*
import cats.data.EitherT
import cats.effect.{Async, IO, Resource}
import cats.implicits.*
import com.comcast.ip4s.{ipv4, port}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.helgi.investment.config.{AppConfig, DbConfig}
import org.helgi.investment.repository.PortfolioRepo
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import pureconfig.*
import pureconfig.error.ConfigReaderException

object Server {
  def create[F[_] : Async]: Resource[F, Server] =
    for
      conf <- config[F]
      _ <- transactor[F](conf.db)
      s <- server[F]
    yield s

  private[this] def server[F[_] : Async]: Resource[F, Server] =
    EmberServerBuilder
      .default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .build

  private[this] def config[F[_]](implicit F: Async[F],
                                 reader: ConfigReader[AppConfig]): Resource[F, AppConfig] =
    Resource.eval(
      EitherT(F.blocking(ConfigSource.default.cursor()))
        .subflatMap(reader.from)
        .leftMap(ConfigReaderException[AppConfig])
        .rethrowT
    )

  private[this] def transactor[F[_] : Async](config: DbConfig): Resource[F, HikariTransactor[F]] =
    for
      ce <- ExecutionContexts.fixedThreadPool(config.pool)
      tx <- HikariTransactor.newHikariTransactor(
        config.driver,
        config.url,
        config.user,
        config.password,
        ce)
    yield tx
}
