# [![Jcabi Logo](https://www.jcabi.com/logo-square.svg)](https://www.jcabi.com/logo-square.svg)

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](https://www.rultor.com/b/jcabi/jcabi-jdbc)](https://www.rultor.com/p/jcabi/jcabi-jdbc)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![mvn](https://github.com/jcabi/jcabi-jdbc/actions/workflows/mvn.yml/badge.svg)](https://github.com/jcabi/jcabi-jdbc/actions/workflows/mvn.yml)
[![PDD status](https://www.0pdd.com/svg?name=jcabi/jcabi-jdbc)](https://www.0pdd.com/p?name=jcabi/jcabi-jdbc)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-jdbc)
[![Javadoc](https://javadoc.io/badge/com.jcabi/jcabi-jdbc.svg)](https://www.javadoc.io/doc/com.jcabi/jcabi-jdbc)
[![Coverage Status](https://coveralls.io/repos/jcabi/jcabi-jdbc/badge.svg?branch=__rultor&service=github)](https://coveralls.io/github/jcabi/jcabi-jdbc?branch=__rultor)
[![jpeek report](https://i.jpeek.org/com.jcabi/jcabi-jdbc/badge.svg)](https://i.jpeek.org/com.jcabi/jcabi-jdbc/)
[![Hits-of-Code](https://hitsofcode.com/github/jcabi/jcabi-jdbc)](https://hitsofcode.com/view/github/jcabi-jdbc)

More details are here: [jdbc.jcabi.com](http://jdbc.jcabi.com/index.html).

Also, read this blog post:
[_Fluent JDBC Decorator_](http://www.yegor256.com/2014/08/18/fluent-jdbc-decorator.html).

`JdbcSession` is a convenient fluent wrapper around JDBC:

```java
import com.jcabi.jdbc.JdbcSession;
public class Main {
  public static void main(String[] args) {
    String name = new JdbcSession(/* JDBC data source */)
      .sql("SELECT name FROM foo WHERE id = ?")
      .set(123)
      .select(new SingleOutcome<String>(String.class));
  }
}
```

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven build before submitting a pull request:

```shell

mvn clean install -Pqulice
```

Please make sure that you're doing so under user account without administrative
rights, otherwise the build will fail (postgresql instance needed for tests
can't be launched under admin/root account).
