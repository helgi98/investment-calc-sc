package org.helgi.investment

import cats.data.EitherT
import cats.effect.{Async, Resource}
import cats.implicits.*
import com.comcast.ip4s.{Host, Port}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import org.helgi.investment.config.{AppConfig, DbConfig, ServerConfig}
import org.helgi.investment.integration.FmpApiClient
import org.helgi.investment.repository.{PortfolioRepo, StockPricesRepo}
import org.helgi.investment.route.PortfolioRoutes
import org.helgi.investment.service.PortfolioService
import org.http4s.HttpRoutes
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import pureconfig.*
import pureconfig.error.ConfigReaderException

object Server:
  def create[F[_] : Async]: Resource[F, Server] =
    for
      cf <- config[F]
      ta <- transactor[F](cf.db)
      _ <- migrate(ta)
      hc <- httpClient[F]
      rts = Router("api" -> routes(cf, ta, hc))
      s <- server[F](cf.server, rts)
    yield s

  private[this] def routes[F[_] : Async](config: AppConfig, ta: Transactor[F], hc: Client[F]): HttpRoutes[F] =
    val fmpApiClient = FmpApiClient(hc, config.integration.fmp)

    val portfolioRepo = PortfolioRepo(ta)
    val stockPricesRepo = StockPricesRepo.fmp(fmpApiClient)

    val portfolioService = PortfolioService(portfolioRepo, stockPricesRepo)

    val portfolioRoutes = PortfolioRoutes(portfolioService)

    portfolioRoutes

  private[this] def server[F[_] : Async](config: ServerConfig,
                                         routes: HttpRoutes[F]): Resource[F, Server] =
    EmberServerBuilder
      .default[F]
      .withHostOption(Host.fromString(config.host))
      .withPort(Port.fromInt(config.port).get)
      .withHttpApp(routes.orNotFound)
      .build

  private[this] def config[F[_]](implicit F: Async[F],
                                 reader: ConfigReader[AppConfig]): Resource[F, AppConfig] =
    Resource.eval(
      EitherT(F.blocking(ConfigSource.default.load[AppConfig]))
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

  private[this] def migrate[F[_]](ta: HikariTransactor[F])(implicit F: Async[F]): Resource[F, Unit] =
    Resource.eval(ta.configure { ds =>
      F.blocking {
        Flyway.configure().dataSource(ds).load().migrate()
        ()
      }
    })

  private[this] def httpClient[F[_] : Async]: Resource[F, Client[F]] =
    EmberClientBuilder.default[F].build
