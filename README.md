# subethasmtp
<a href="https://travis-ci.org/davidmoten/subethasmtp"><img src="https://travis-ci.org/davidmoten/subethasmtp.svg"/></a><br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/subethasmtp/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/subethasmtp)<br/>
[![codecov](https://codecov.io/gh/davidmoten/subethasmtp/branch/master/graph/badge.svg)](https://codecov.io/gh/davidmoten/subethasmtp)<br/>

SubEtha SMTP is a Java library which allows your application to receive SMTP mail with a simple, easy-to-understand API.

This component can be used in almost any kind of email  processing application.  Hypothetical (and not-so hypothetical) uses include:

  * A mailing list manager (see SubEthaMail)
  * A mail server that delivers mail to user inboxes
  * A mail archiver like [The Mail Archive](http://www.mail-archive.com/)
  * An email test harness (see [Wiser](Wiser.md))
  * An email2fax system
  * SMTPseudo [A filtering forwarding server](http://code.google.com/p/smtpseudo/)
  * [Baton](http://code.google.com/p/baton/) SMTP proxy for one or more backends (rules based on sender/envelope)
  * [Mireka](http://code.google.com/p/mireka/) - Mail server and SMTP proxy with detailed logging, statistics and built-in, fail-fast filters


SubEthaSMTP's simple, low-level API is suitable for writing almost any kind of mail-receiving application.  Read more in [UsingSubEthaSMTP](UsingSubEthaSMTP.md) or join our MailingList.

## Getting started
Use this maven dependency:

```xml
<dependency>
    <groupId>com.github.davidmoten</groupId>
    <artifactId>subethasmtp</artifactId>
    <version>4.0-RC2</version>
</dependency>
```

## A Little History ##
SubEthaSMTP was split out of the SubEthaMail mailing list manager because it is a useful standalone component.  When we wrote SubEtha, the last thing we wanted to do was write our own SMTP server.  In our search for a modular Java SMTP component, we examined:

  * [Apache JAMES](http://james.apache.org/)
  * [JBoss Mail Server](http://labs.jboss.com/portal/jbossmail/index.html), now also defunct [Meldware Mail](http://www.buni.org/mediawiki/index.php/Meldware_Mail)
  * [Dumbster](http://quintanasoft.com/dumbster/)
  * [Jsmtpd](http://www.jsmtpd.org/site/)
  * [JES](http://www.ericdaugherty.com/java/mailserver/)
  * [Green Mail](http://www.icegreen.com/greenmail/)

Since you're reading this page you probably already know what we found:  Seven different SMTP implementations without the slightest thought given to reusability. Even Jstmpd, which purports to be a "A Modular Java SMTP Daemon", isn't.  Even though JBoss Mail/Meldware Mail is in active development, the team was unintersted in componentization of the SMTP processing portion of their server.  GreenMail, which is based on the JAMES code base is best summarized with this [blog posting](http://eokyere.blogspot.com/2006/10/get-wiser-with-subethasmtp.html).

During the development of SubEtha's testing harness, we tried out the [Dumbster](http://quintanasoft.com/dumbster/) software  and found that not only was the API difficult to use, it did it not work properly, the developer has not done any development on it in about a year and it does not work reliably on Mac OS X. With two simple classes we re-implemented it as an included project called [Wiser](Wiser.md).

We hate reinventing wheels.  This should be the LAST FREAKING JAVA SMTP IMPLEMENTATION.

## A New Fork ##
This new fork by Engine821.com intends to tidy up some of the structure, while keeping the original (and excellent!) design and code. We are using this as a base for a production product, so intend on keeping the code fresh and well maintained.

We too did a survey of existing Java SMTP implementations and were unsatisfied... until we found SubEthaSMTP! The code is clean and very well thought out. So far the changes we've made are minor, including...

* Eliminating the embedded `/lib` directory. Maven correctly handles pulling in all the dependencies and best practices discourage keeping binary artifacts inside version control.
* Updating to the latest versions of some of the libraries used. 
* Removing some of the IDE metadata files. Your IDE can rercreate whichever ones you need based on your preferences and the Maven POM.
* Making the message handing exceptions be `checked`. This is possibly controversial, but we thought about it a lot and prefer to have these exceptions show up in the `throws` clause rather than have them potentially pop-up unexpectedly at run-time. 

### Fork of a Fork!
Dave Moten came across this and 
* fixed tests
* migrated mocking to use Mockito (with apologies but was the most time-efficient way for me to restore the broken tests!)
* set up pom.xml for release under the `com.github.davidmoten:subethasmtp` artifact 
* released to Maven Central
* submitted the changes back to the Engine821.com fork (apart from the groupId change and release changes)
* added multi-JDK continuous integration using Travis
* added code coverage using coverage.io
* added round trip unit test of STARTTLS
* removed MigBase64 because is complex code without unit tests (even in original source) and Java 8 Base64 is faster
* cleaned up code (made fields private and final where appropriate, remove public keyword from interface methods)
* minor coverage improvements
* required Java 8 (just because of Base64 class at the moment, Java 7 required now because of use in unit test of `X509TrustManager`)
* converted `SMTPServer` to be largely immutable and is created with a builder pattern
* adjusted `Wiser` API to cope with an immutable `SMTPServer`
* disallowed inheritance of `SMTPServer` (now final)
* `Wiser` now created with builder pattern (disallowed inheritance and added `accepter` builder method)
* used composition instead of inheritance in `SmartClient`
* used static factory method for `SmartClient` so that references don't escape the constructor (`connect` was called from the constructor)
* used explicit character set with `InputStreamReader` (US_ASCII) in `SMTPClient` and `SmartClient`
* used `java.util.Optional` and `Preconditions` in `SmartClient`, `SMTPClient` and `SMTPServer`
* added `@Override` annotations
* added `EmailUtils` tests
* moved classes that are not part of the public API to internal packages


## Project Authors ##
Ian McFarland contributed the first codebase to SubEtha Mail. Then, Jon Stevens and Jeff Schnitzer re-wrote most of Ian's code into what we have today. Edouard De Oliveira and Scott Hernandez have also made significant contributions.

## Support ##
If you have any bug reports, questions or comments about this SubEtha SMTP fork, it's best that you use the GitHub issue tracker to get in touch. Please do not email the authors directly.

## Spec Compliance ##
For now, we have just focused on implementing just the minimal  required aspects of http://rfc.net/rfc2821.html#s4.5.1. We also return SMTP status responses that mimic what Postfix returns.

Thanks to a contribution from [Mike Wildpaner](mailto:mikeREMOVETHISPART@wildpaner.com), we support the [StartTLS specification](http://rfc.net/rfc2487.html).

Thanks to a contribution from [Marco Trevisan](mailto:mrctrevisanREMOVETHISPART@yahoo.it), we support the [SMTP AUTH specification](http://rfc.net/rfc2554.html).
