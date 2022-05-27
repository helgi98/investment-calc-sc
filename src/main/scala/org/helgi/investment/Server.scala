package org.helgi.investment

import cats.*
import cats.data.EitherT
import cats.effect.{Async, IO, Resource}
import cats.implicits.*
import com.comcast.ip4s.{Host, Port, ipv4, port}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import org.helgi.investment.config.{AppConfig, DbConfig, ServerConfig}
import org.helgi.investment.repository.PortfolioRepo
import org.helgi.investment.route.PortfolioRoutes
import org.helgi.investment.service.PortfolioService
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import pureconfig.*
import pureconfig.error.ConfigReaderException

object Server {
  def create[F[_] : Async]: Resource[F, Server] =
    for
      conf <- config[F]
      ta <- transactor[F](conf.db)
      rts = Router("api" -> routes(ta))
      s <- server[F](conf.server, rts)
    yield s

  private[this] def routes[F[_] : Async](ta: Transactor[F]): HttpRoutes[F] =
    val portfolioRepo = PortfolioRepo(ta)
    val portfolioService = PortfolioService(portfolioRepo)
    val portfolioRoutes = PortfolioRoutes(portfolioService)

    portfolioRoutes

  private[this] def server[F[_] : Async](config: ServerConfig,
                                         routes: HttpRoutes[F]): Resource[F, Server] =
    EmberServerBuilder
      .default[F]
      .withHostOption(Host.fromString(config.host))
      .withPort(Port.fromInt(config.port).getOrElse(port"8080"))
      .withHttpApp(routes.orNotFound)
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
