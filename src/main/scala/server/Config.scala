package server

import java.net.InetAddress

/**
  * Created by Sascha on 08.06.2016.
  */
object Config {
  val PROD_IP = "192.168.166.1"
  val ips = InetAddress.getAllByName(InetAddress.getLocalHost.getHostAddress)

  val production = if(ips.exists(p => { p.getHostAddress == PROD_IP })) true else false
  val host = if(production) PROD_IP else "localhost"

  println(s"[LOG]\tRunning on $host")

  object templateservice {
    val host = Config.host
    val port = 8080

    object db {
      val host = "localhost"
      val port = "27017"
      val name = "checklist_core_data"
    }
  }

  object checklistservice {
    val host = Config.host
    val port = 8081

    object db {
      val host = "localhost"
      val port = "27017"
      val name = "checklist_transact_data"
    }
  }

  object aaservice {
    val host = Config.host
    val port = 8082

    object db {
      val host = "localhost"
      val port = "27017"
      val name = "auth_data"
    }
  }

  object schedulerservice {
    val host = Config.host
    val port = 8083

    object db {
      val host = "localhost"
      val port = "27017"
      val name = "scheduler_data"
    }
  }
}
