<img src="http://img.jcabi.com/logo-square.png" width="64px" height="64px" />

[![Made By Teamed.io](http://img.teamed.io/btn.svg)](http://www.teamed.io)
[![DevOps By Rultor.com](http://www.rultor.com/b/jcabi/jcabi-jdbc)](http://www.rultor.com/p/jcabi/jcabi-jdbc)
[![DevOps by Rultor.com](http://img.rultor.com/button-2.svg)](http://www.rultor.com/p/jcabi/jcabi-jdbc)

[![Build Status](https://travis-ci.org/jcabi/jcabi-jdbc.svg?branch=master)](https://travis-ci.org/jcabi/jcabi-jdbc)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-jdbc)

More details are here: [jdbc.jcabi.com](http://jdbc.jcabi.com/index.html)

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

## Questions?

If you have any questions about the framework, or something doesn't work as expected,
please [submit an issue here](https://github.com/yegor256/jcabi/issues/new).

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven build before submitting a pull request:

```
$ mvn clean install -Pqulice
```
