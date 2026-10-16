# lib folder

Put the SQLite JDBC driver jar here (it is not stored in the repository):

- `sqlite-jdbc-x.x.x.jar` - download from https://github.com/xerial/sqlite-jdbc/releases or Maven Central (`org.xerial:sqlite-jdbc`)
- `slf4j-api-x.x.x.jar` - only needed if you get `NoClassDefFoundError: org/slf4j/LoggerFactory` (newer sqlite-jdbc versions)
