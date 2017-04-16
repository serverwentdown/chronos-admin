#!/bin/bash

cp lib/mysql-connector-java-5.1.41.jar out/artifacts/chronos-admin/
cd out/artifacts/chronos-admin/
zip chronos-admin.zip *.jar

echo "Exported zip file is at out/artifacts/chronos-admin/chronos-admin.zip"
