import Server.Initiate
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Akka extends App {
  val server: ActorSystem[Server.Initiate] = ActorSystem(Server(), "AkkaTest")
  server ! Initiate("Sujit")
}

object Server {
  final case class Initiate(name: String)

  def apply(): Behavior[Initiate] = {
    Behaviors.setup { context =>
      // To create another actor
      val greeter = context.spawn()
      Behaviors.receive { (ctx, message) =>
        ctx.log.info("Hello {}!", message.name)
        Behaviors.same
      }

    }
  }
}
