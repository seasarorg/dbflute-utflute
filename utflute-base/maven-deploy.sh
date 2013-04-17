#!/bin/bash

# Base
mvn -e clean deploy

# Core
cd ../utflute-core
. maven-deploy.sh

# Seasar
cd ../utflute-seasar
. maven-deploy.sh

# Spring
cd ../utflute-spring
. maven-deploy.sh
