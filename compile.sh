#!/bin/sh
mkdir -p bin
javac -cp libs/javaclient.jar -d bin src/*/*.java src/*.java
