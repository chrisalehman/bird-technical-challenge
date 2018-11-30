#!/bin/bash

# scans jars for input text - extremely helpful for finding which jars a class belongs to
jarscan() {
  pattern=$(echo $1 | tr . /)
  find . -name '*.jar' | while read jarfile; do if jar tf "$jarfile" | grep "$pattern"; then echo "$jarfile"; fi; done
}

jarscan $1
