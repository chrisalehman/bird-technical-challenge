#!/bin/bash

# upgrade gradlew to 5.0
./gradlew wrapper --gradle-version=5.0 --distribution-type=bin

# cache git creds for 24 hours
git config credential.helper 'cache --timeout=86400'
