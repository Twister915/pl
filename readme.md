# pl

pl, intentionally written in all lowercase, is the library for writing 
Bukkit plugins using a winning formula.

Also, I should note, this readme and the entire project are a **work in progress**. This notice
will be removed once the project is ready for production use.

The following sections of the readme need to be finished:
* Guice
* Rx Schedulers
* Annotation processing (commands, plugin)
* How to integrate with gradle
* Which nexus it'll be on
* Commands
* Dbi
* Sql stuff
* Parsing

And the following parts of the projects are ideas but are not implemented:
* Inventory GUI
* Json chat
* Configuration via dependency injection via annotations (`@ConfigKey("something")` fields)
* More examples of usage

## Usage

To use the project, add the following dependency

```groovy
//TODO
```

# Features

The goal of the library is to make the process of writing plugins faster. 
In that pursuit, the following features were designed:

## rx

Like [redemptive](https://github.com/Twister915/redemptive), events can be 
listened to using a library known as [RxJava](https://github.com/ReactiveX/RxJava/tree/1.x). 
Unlike redemptive, rxjava has been replaced with [RxJava2](https://github.com/ReactiveX/RxJava).

After I used rx for a while, mostly for listening to events, I started to realize where
rx really excelled. Encapsulating async operations. This realization, albeit an obvious one
if I had actually read what other pepole were using hte library for, was what led me to 
use it ubiquitously in my private projects, even those having nothing to do with Bukkit.

The original rx lacked a number of important features, and I was very excited to see the
team had developed rx2. I'm even more excited that I can include the library in a Bukkit
plugin library for the first time.

Let's have a quick look through the different things you can do using rx with this library:

### Listening to an event
```java
Disposable twisterJoinSub =
    plugin.observeEvent(PlayerJoinEvent.class)
          .filter(event -> event.getPlayer().getName().equals("Twister"))
          .map(event -> event.getPlayer())
          .subscribe(player -> player.sendMessage("Welcome to the server!"));
```

The `Disposable` represents the "subscription" to the event (PlayerJoinEvent), and can be 
used to stop listening to the event. For example, if I were to call: `twisterJoinSub.dispose();` 
then joined on the "Twister" account, I would not receive a message because the listener had 
been disposed.

### Buffering events

Another powerful use case of the rx event handlers is for buffering events. This may not
be a common use case, but it's certainly a powerful one.

```java
plugin.observeEvent(PlayerJoinEvent.class)
      .buffer(10, TimeUnit.SECONDS)
      .map(events -> events.stream()
                           .map(event -> event.getPlayer().getName())
                           .collect(Collectors.toList()))
      .subscribe(names -> {
          getLogger().info("There were " + names.size() + " joins in the last 10 seconds.")
          for (String name : names) {
            getLogger().info(name + " joined within the last 10 seconds");
          }
      });
```

Although useless, this example shows how simple it is to implement some otherwise
pretty complicated behavior using the rx operators.

### Listening to one event because of another

```java
plugin.observeEvent(PlayerJoinEvent.class)
      .flatMapCompletable(joinEvent -> plugin.observeEvent(PlayerMoveEvent.class)
                                  .filter(moveEvent -> moveEvent.getPlayer().equals(joinEvent.getPlayer()))
                                  .take(1)
                                  .toCompletable()
                                  .timeout(10, TimeUnit.SECONDS)
                                  .onErrorResumeNext(ex -> {
                                      if (ex instanceof TimeoutException) {
                                          joinEvent.getPlayer().kick("You didn't move in 10 seconds!");
                                          return Completable.complete();
                                      } else {
                                          return Completable.error(ex);
                                      }
                                  }))
      .subscribe();
```

Notice how simple it is to implement something like this. If it's not clear, the functionality
of the code written is:

* When a player joins the server, setup a PlayerMoveEvent listener for that player
* On the first PlayerMoveEvent, "complete"
* If there is no "completion" in 10 seconds, throw a "TimeoutException"
* If there is a "TimeoutException," kick the player in question

"Vanilla" implementations of this functionality would probably involve a full class dedicated to the
functionality, and a Map, some runnables, and two different methods for handling the events.