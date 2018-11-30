#!/bin/bash

# install sdk
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk version

# install java ecosystem
sdk install java
sdk install kotlin
sdk install gradle
sdk install micronaut

# install homebrew
/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"

# install git
brew install git

# cache git creds for 24 hours
git config credential.helper 'cache --timeout=86400'
