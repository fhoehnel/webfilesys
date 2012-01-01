I recommend building WebFileSys with maven using the provided "pom.xml".
The directory "maven-repository" contains some non-public libraries
that are required to build WebFileSys. All other libraries that
WebFileSys depends on are automatically downloaded by maven from
public repositories.

Use the command
  maven install
to build the webfilesys.war web application archive which can be deployed
in any servlet container.

You can import the project into the Eclipse IDE using the provided
project files (".project", ".classpath", ".setting/*").

The ant script "build-hotdeploy.xml" can be used to copy changed
files to the deploy target of the exploaded webfilesys webapp
in the servlet container.

If you have problems getting the project to run contact the author at
frank_hoehnel@hotmail.com !