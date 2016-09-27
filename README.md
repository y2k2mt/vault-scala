Vault Scala Client
===============================================

[Hashicorp Vault](https://www.vaultproject.io/) client for scala.

Core components has *no dependencies*.

[![License](http://img.shields.io/:license-mit-blue.svg)](http://doge.mit-license.org)

## Quick Start

Add to sbt dependency.

```scala
libraryDependencies ++= Seq(
 "vaultscala" %% "vaultscala-core" % "0.1.0-SNAPSHOT",
 // You can switch alternative libraries.
 "vaultscala" %% "vaultscala-akka" % "0.1.0-SNAPSHOT",
 "vaultscala" %% "vaultscala-json4s" % "0.1.0-SNAPSHOT"
)
```

```scala
import vaultscala._
import vaultscala.akkahttp._
import vaultscala.json4s._

val vault = VaultLocation(java.net.URI.create("http://localhost:8200"))

// Future[Try[SingleSecretValue("bar")]]
Vault(vault,SingleSecret(ClientToken("token"),"foo"))

// Try[SingleSecretValue("bar")]
Vault.sync(vault,SingleSecret(ClientToken("token"),"foo"))
```
