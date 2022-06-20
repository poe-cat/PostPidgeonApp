# Post Pidgeon App

In this application authenticated users can compose, read and scroll inside-app messages.

It uses Java, Spring Boot and Apache Cassandra for database management. For frontend there are Thymeleaf engine and Bootstrap library for CSS.

App is authenticated using OAuth2. The OAuth 2.0 authorization framework is a protocol that allows a user to grant a third-party web site or application access to the user's protected resources, without necessarily revealing their long-term credentials or even their identity.

To run this app make sure you set your credentials in application.yml. You need client id and client secret from GitHub (for OAuth2 authentication). 
Additionaly, you need your Cassandra username and password, DataStax Astra secure connect bundle and your database details (id, region, keyspace and application token). 
All these details can be collected from your DataStax Astra account. If you have problems with setting things up, I'll describe the process below. 
