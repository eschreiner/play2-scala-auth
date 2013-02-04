play2-auth
==========

This module is an authentication module for Play! 2.x allowing user/password authentication and integration with various OAuth1/2 providers.

Target
------

This module is targeted at [Scala](http://www.scala-lang.org) applications built on [Play! 2.x](http://www.playframework.org).

The tests are run against Play 2.0.4 only at the moment.

Motivation
----------

There are a number of security and authentication related modules available for Play 2.x, but none cover all aspects desirable for a one-stop solution:

- basic username / password authentication
- easy integration with social networks and authentication using OAuth1/2
- strong emphasis on advanced security features

This module is largely based on the Play! module [play-authenticate](https://github.com/joscha/play-authenticate) initiated by Joscha Feth which offers a good solution for the first two points, but is implemented in Java. 

Installation
------------

Add a dependency declaration into your `Build.scala` or `build.sbt` file:

* __for Play2.0.x__

        "com.sdc" %% "play2-auth" % "0.1"

For example your `Build.scala` might look like this:

```scala
  val appDependencies = Seq(
    "com.sdc" %% "play2-auth" % "0.1"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA)
```

You don't need to create a `play.plugins` file.

License
=======

Copyright (c) 2013 by Dr. Erich W. Schreiner, [Software Design & Consulting GmbH](http://www.sdc-software.com)

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


[securesocial2]: https://github.com/jaliss/securesocial
[deadbolt2]: https://github.com/schaloner/deadbolt-2
[Play20StartApp]: https://github.com/yesnault/Play20StartApp