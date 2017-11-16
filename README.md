<img src="http://img.jcabi.com/logo-square.png" width="64px" height="64px" />

[![Managed by Zerocracy](http://www.0crat.com/badge/C3RUBL5H9.svg)](http://www.0crat.com/p/C3RUBL5H9)
[![DevOps By Rultor.com](http://www.rultor.com/b/jcabi/jcabi-jdbc)](http://www.rultor.com/p/jcabi/jcabi-jdbc)

[![Build Status](https://travis-ci.org/jcabi/jcabi-jdbc.svg?branch=master)](https://travis-ci.org/jcabi/jcabi-jdbc)
[![PDD status](http://www.0pdd.com/svg?name=jcabi/jcabi-jdbc)](http://www.0pdd.com/p?name=jcabi/jcabi-jdbc)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-jdbc)
[![Javadoc](https://javadoc.io/badge/com.jcabi/jcabi-jdbc.svg)](http://www.javadoc.io/doc/com.jcabi/jcabi-jdbc)
[![Coverage Status](https://coveralls.io/repos/jcabi/jcabi-jdbc/badge.svg?branch=__rultor&service=github)](https://coveralls.io/github/jcabi/jcabi-jdbc?branch=__rultor)
[![Dependencies](https://www.versioneye.com/user/projects/561ac40ea193340f28001102/badge.svg?style=flat)](https://www.versioneye.com/user/projects/561ac40ea193340f28001102)

[![jpeek report](http://i.jpeek.org/com.jcabi/jcabi-jdbc/badge.svg)](http://i.jpeek.org/com.jcabi/jcabi-jdbc/)

More details are here: [jdbc.jcabi.com](http://jdbc.jcabi.com/index.html).
Also, read this blog post: [Fluent JDBC Decorator](http://www.yegor256.com/2014/08/18/fluent-jdbc-decorator.html).

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

Please make sure that you're doing so under user account without administrative rights, otherwise the build will fail (postgresql instance needed for tests can't be launched under admin/root account).
