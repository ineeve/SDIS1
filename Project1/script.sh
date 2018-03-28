#!/bin/bash
alias build='javac src/*.java -d bin/'
alias c='rm bin/*.class; rm stored/* ; rm data/* ; rm *.class'
alias run="java -classpath bin Peer $1 230.0.0.1 8080 230.0.0.2 8081 230.0.0.3 8082"