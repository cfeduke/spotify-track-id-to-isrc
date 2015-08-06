#!/bin/sh
# attempts to execute ~/.sbtrc then <project>/.sbtrc
java \
  -Xms512M \
  -Xmx8G \
  -Xss1M \
  -XX:+CMSClassUnloadingEnabled \
  -XX:PermSize=512M \
  -XX:MaxPermSize=2G \
  -jar `dirname $0`/sbt-launch.jar \
  "$@"
