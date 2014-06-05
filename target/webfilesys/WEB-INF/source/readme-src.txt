WebFileSys source files for customized user manager implementation
==================================================================

The WebFileSys distribution contains 3 source files,
that allow experienced Java programmers to write
their own user manager class (that could, for example, 
access an existing relational database containing
user records). 

The customized user manager class must extend the
abstract base class UserManagerBase.
This base class implements the UserManager interface.
That means that you have to implement all methods that are
declared in the UserManager interface.

To run WebFileSys with your own user manager, place the classes
in the WEB-INF/classes directory or your JAR archive in the
WEB-INF/lib directory.

Set the config parameter
  UserManagerClass
in webfilesys.conf to the name of your user manager class.

At startup WebFileSys prints the name of the active user manager
class in the system.log file.
    