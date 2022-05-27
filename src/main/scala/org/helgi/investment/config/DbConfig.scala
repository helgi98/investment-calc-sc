package org.helgi.investment.config

case class DbConfig(driver: String,
                    url: String,
                    user: String,
                    password: String,
                    pool: Int)