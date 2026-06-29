#!/bin/sh
set -eu

mkdir -p /app/uploads/products /app/tmp

if [ -d /app/default-uploads/products ] && [ -z "$(find /app/uploads/products -mindepth 1 -print -quit)" ]; then
  cp -R /app/default-uploads/products/. /app/uploads/products/
fi

exec java $JAVA_OPTS -jar /app/ssm-shop.jar
