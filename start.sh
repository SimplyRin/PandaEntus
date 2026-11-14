#!/bin/sh
# PandaEntus の起動スクリプトの見本 (Linux / macOS)。Bot のフォルダに置いて使う。
# バックグラウンドで動かす場合は、screen や systemd などからこのスクリプトを呼び出す。
cd "$(dirname "$0")" || exit 1

JAR="PandaEntus-1.3-jar-with-dependencies.jar"

# !update でダウンロードした jar が残っていれば、置き換える
if [ -f "$JAR.update" ]; then
	mv -f "$JAR.update" "$JAR"
fi

# Java 25 以降が必要
exec java -Djava.awt.headless=true -Xms10M -Xmx512M -jar "$JAR"
