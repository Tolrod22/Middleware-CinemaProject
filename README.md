# Project : Cinema

## Project install

To install the client-side and server-side projects, run, in each directory:

    mvn install
	
## Application execution

### Server
To launch the server application, from the server-side directory:

    java -jar target/server-side-1.jar

You can also use the `-h` option if you need some help

### Client
To launch a client application, from the client-side directory:
	
    java -jar client/target/client-1.jar

You can also use the `-h` option if you need some help

When the application is launched, first enter your bank account code, and then you can enter the following
commands:
* `QUIT`: to quit the application
* `GET`: to get a ticket for a movie
* `FILTER`: to sub to a special cinema
* `UNFILTER`: to unsub to a special cinema
* `PUBLISH`: to publish tickets for a movie
* `REQUEST`: to withdraw tickets for a movie
* `DISPLAY`: to show your actual ticket list


### Cinema
To launch a cinema application, from the client-side directory:
	
    java -jar cinema/target/cinema-1.jar

You can also use the `-h` option if you need some help

When the application is launched, first enter your bank account code, and then you can enter the following
commands:
* `QUIT`: to quit the application
* `ADD `: to share a new available movie
* `REMOVE`: to remove an unavailable movie
