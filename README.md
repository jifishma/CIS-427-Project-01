## Getting Started
### Building the project
To build the project, navigate to the 'src' directory and run the following commands:

For the client:
>```javac -d .\bin\ .\src\Client\*.java``` 

For the server:
>```javac -d .\bin\ .\src\Server\*.java```

### Running the project
To run the project, run the following commands:

For the client:
>```java -cp Client.Client```

For the server:
>```java -cp Server.Server```

## Available commands
- Terminology:
    - \<\> = required argument
    - [ ] = optional argument
- LOGIN \<username\> \<password\>
    - Attempt to log in to a user account with the provided credentials
- SOLVE \<-c | -r\> \<radius | side length\> [side length 2]
    - If -c is specified, solve for a circle, if -r is specified, solve for a rectangle
    - For a circle:
        - Define a radius to solve Area and Cicumference for
    - For a rectangle:
        - Define one or two side lengths to solve Area and Perimeter for
- LIST [-all]
    - Print the current user's requested SOLVE operations and results
    - If logged in as "root", print all of the users' requested SOLVE operations and results
- LOGOUT
    - Log the current user out and exit
- SHUTDOWN
    - Shut down the Server, log the current user out, and exit

## Example of commands
### LOGIN

### SOLVE
### LIST
### LOGOUT
### SHUTDOWN