# Parser

The parser assignment depends on your `LexicalAnalyzer` assignment and 
Maven needs to be able to find the jar file from assignment one. You
have two options:

1. Use the model solution which is deployed to Maven. By default the project
is configured to use the model solution for assignment one so nothing
needs to be done if you want to use this option.
2. Use your own Lexical analyzer (recommended for maximum points).

## Using Your Own Lexical Analyzer

To use your own lexical analyzer you must first:  

1. Install your lexical analzyer into your local repository (~/.m2/repository)
  by running the command `mvn install` in your lexical anaylzer project
  directory
1. Modify the pom.xml file to use your lexical analyzer instead of the
  model solution.  The `groupId` for the model solution is `lexical-analyzer-model`
  and you simply need to remove the `-model` bit.  The dependency for 
  the lexical analyzer should now look like:
  
```xml
<dependency>
    <groupId>edu.vassar.cs.cmpu331</groupId>    
    <artifactId>lexical-analyzer</artifactId>
    <version>1.0.0</version>
</dependency>
```

**NOTE** If (when) you find and fix bugs in your lexical analyzer you will
need to run `mvn install` again to install the fixed version into your
local repository.

**Tip** It is recommended to use the model solution for the lexical analyzer
until your parser is working.  This will allow you to focus on the parser and
isolate parser bugs from lexer bugs.

## Bonus: Continuous Integrationg with Travis-CI

The project already comes with all the configuration files needed for 
[Travis-CI](https://education.travis-ci.com) to build your project whenever
code is pushed to GitHub.  The only wrinkle is that Travis will not have access
to your lexical analyzer. This is not a problem if you are using the model
solution as it is available from a public Maven repository.  If you want
to use your own lexical analyzer and Travis at the same time you will need
to deploy to lexical analyzer to a public Maven repository as well.

Fortunately, GitHub makes it easy to host an ad-hoc public repository that
Travis can locate.

### Creating an Ad-Hoc Maven Repository.

1. Login to GitHub and create a public repository under your own user account.
  Name the repository `mvn-repo`.
1. Clone this repository to your computer.
  
  ```
  $> git clone https://github.com/username/mvn-repo.git
  ```
1. Install your lexical analyzer jar into the mvn-repo directory you just cloned.
  
  ```
  $> cd [lexical-analyzer]    
  $> mvn install:install-file -DpomFile=pom.xml -Dfile=target/lexical-analyzer-1.0.0.jar -DlocalRepository=[repo]  
  ```

  Where:

  1. `[lexical-analyzer]` is the directory containing your lexical analyzer project.
  1. `[repo]` is the directory containing the `mvn-repo` you cloned from GitHub.
1. Edit the `pom.xml` file and:
  1. Change the `username` property to your GitHub username. E.G.
  ```xml
  <properties>
      <username>ksuderman</username>
  </properties>
  ```
  1. Push your local `mvn-repo` to GitHub.
  1. Uncomment the `<repository>` at the end of the file.  That is delete
  the line that starts with &lt;!-- and the line that starts with --&gt;
  
**Note**

If you make changes to your lexical analzyer don't forget to push the
update version to your `mvn-repo` on GitHub or Travis will continue to
use the old version.