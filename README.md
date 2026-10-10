# CapsuleToy
[![Paper 1.21.11](https://img.shields.io/badge/Paper-1.21.11-brightgreen.svg)](https://fill-ui.papermc.io/projects/paper/version/1.21.11)
[![GitHub release](https://img.shields.io/github/release/gorogoro-space/CapsuleToy.svg)](https://github.com/gorogoro-space/CapsuleToy/releases)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/gorogoro-space/CapsuleToy/issues)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://github.com/gorogoro-space/CapsuleToy/blob/main/LICENSE)

A Paper plugin (Minecraft) for capsule toy machines. Right-click the sign with a coded ticket to get a random prize from the chest.

看板とチェストでカプセルトイ(ガチャ)を作るプラグインです。コード付きの券を手に持って看板を右クリックすると、チェストの中から景品が 1 つランダムにもらえます。

# Requirements
- Paper 1.21.11 or later (1.21.11 以降。それより古いサーバーでは読み込まれません)
- Java 21 or later (21 以降)

# Installation method
Please place the .jar file in the Paper plugins folder.

.jar ファイルを Paper の plugins フォルダに置き、サーバーを再起動してください。
`plugins/CapsuleToy/config.yml` を編集したら、`/capsuletoy reload`(OP 権限が必要)で読み直せます。

# Useage
| コマンド | 説明 | 使える人 |
|---|---|---|
| `/capsuletoy list` | 登録したカプセルトイの一覧 | `capsuletoy.list` |
| `/capsuletoy modify <名前>` | 景品のチェストを設定する(このあとチェストを右クリック) | `capsuletoy.modify` |
| `/capsuletoy delete <名前>` | カプセルトイを削除する | `capsuletoy.delete` |
| `/capsuletoy ticket <プレイヤー名\|@a\|@p\|@s\|@r>` | 名前の無いコード付きの券を渡す | OP・コンソール・コマンドブロック |
| `/capsuletoy reload` | `config.yml` を読み直す | OP |

看板の 1 行目に `[capsuletoy]` と書いて登録するには `capsuletoy.create` が必要です。
権限は `plugin.yml` に登録していないため、権限プラグインで与えない限り OP だけが使えます。

# Config
- `plugins/CapsuleToy/config.yml` — 券の名前・説明、看板の文字、メッセージ、`named-ticket-only` など
- `plugins/CapsuleToy/config_jp.yml` — 日本語の見本です。起動のたびに jar 内のもので上書きされます。使うときは内容を `config.yml` に写してください
- `plugins/CapsuleToy/sqlite.db` — カプセルトイと券の番号を保存するデータベース

# コード付きの券（1.2）

スロットの景品は、コード付きの券です。当たった枚数だけ、1 枚ずつ別の番号で渡します。

スロットの券には、カプセルトイの登録名が付きます。使えるのは、その名前の看板だけです。雛形の名前は `infernal` です。別の看板で使っても、番号は消えません。

`/capsuletoy ticket` で出す券には名前がありません。券の名前や説明は変えていません。`config.yml` の `named-ticket-only` に書いた看板では使えず、番号も減りません。書いていないときは `infernal` だけです。大文字と小文字は区別します。一覧も文も、起動時と `/capsuletoy reload` のときに一度だけ読みます。すでに置いてある `config.yml` は、jar を入れても書き換わりません。

使った番号は消えます。同じ紙を複製しても、そのあとは使えません。`/capsuletoy stackticket` というコマンドはありません。

景品を渡せたときの文は `found-pick`、足元に落ちたときの文は `dropped-pick`、別の看板だったときの文は `wrong-capsule`、名前の無い券を名前付き専用の看板で使ったときの文は `needs-named-ticket` です。`config.yml` に書いてあれば、その文を使います。書いていないときは、同じファイルのほかの文が日本語なら日本語、それ以外は英語です。英語の設定ファイルにキーが無くても、日本語にはなりません。

1. 看板の 1 行目を `[capsuletoy]`、2 行目を `infernal` にして設置します。2 行目は英数字と `_` だけです。
2. `/capsuletoy modify infernal` のあと、景品を入れたチェストを右クリックします。
3. チェストの中身が景品です。同じ品を複数の枠に置くと、その品が出やすくなります。中身は `/im getloot <番号>` で出した実物を入れてください。
4. コード付きの券を手に持って看板を右クリックすると、1 枚減ります。チェストから枠を 1 つランダムにもらえます。持ち物に入りきらない景品は、足元に落ちます。名前付きの券は、説明の最後にその名前が出ます。別の看板では減りません。

# Migration from ver1.0 to ver1.1
~~~
# cd plugins/CapslueToy/
# cp -p sqlite.db sqlite.db.backup
# sqlite3 ./sqlite.db
sqlite> DROP INDEX world_name_sign_xyz_uindex;
sqlite> DROP INDEX world_name_chest_xyz_uindex;
sqlite> .output ./old_ticket_code.txt
sqlite> select 'INSERT INTO ticket (ticket_code) values (''' || ticket_code || ''');' from ticket;
sqlite> .quit
# cp ./old_ticket_code.txt ../CapsuleToy/
# cp -p ./sqlite.db ../CapsuleToy/
cd ./CapsuleToy
sqlite3 ./sqlite.db < ./old_ticket_code.txt
~~~

# Fixed a spelling mistake.
Works with capsuletoy instead of capsluetoy. The sign needs to be redone.

# Disclaimer
Do not assume any responsibility by use. Please use it at your own risk.

## ビルド

JDK 21 が必要です。Gradle はラッパー(`gradlew`)が自動でダウンロードするので、別途インストールする必要はありません。

```
gradlew.bat clean build
```

(macOS / Linux では `./gradlew clean build`)

## IntelliJ IDEA でのビルド手順

本プロジェクトはビルドツールに Gradle を使用しています。
IntelliJ IDEA 上で正しくプラグイン（JARファイル）を生成するには、以下の手順を実行してください。
「ビルド → アーティファクトのビルド」はクラスファイルが入らないことがあるので使わないでください。

### 🛠️ ビルド手順

1. IntelliJ IDEA の画面右端にある **「Gradle」タブ** をクリックして開きます。
2. プロジェクト名（CapsuleToy）を展開し、 **`Tasks`** ツリーを開きます。
3. リスト内にある **`clean`** をダブルクリックして実行します（古いビルドキャッシュを削除します）。
4. 続けてリスト内にある **`build`** をダブルクリックして実行します。

### 📦 生成されたファイルの場所
ビルドが成功すると、プロジェクトのルート直下に `build/libs` フォルダが作成（または更新）され、その中に中身の詰まった正しい JAR ファイルが生成されます。

* **生成先:** `build/libs/CapsuleToy-<version>.jar`(例: `CapsuleToy-1.2.jar`。バージョンは `gradle.properties` の `version`)

この JAR ファイルを Minecraft サーバーの `plugins` フォルダに配置してください。

## 開発(IntelliJ IDEA で Claude Code を使う)

このリポジトリには、AI コーディングツール [Claude Code](https://docs.claude.com/ja/docs/claude-code/overview) 向けの作業ルールを書いた `CLAUDE.md` があります。IntelliJ IDEA で使う手順は次のとおりです。

1. Claude の有料プラン(Pro / Max)か、Anthropic API のアカウントを用意します。
2. Windows では、先に [Git for Windows](https://git-scm.com/downloads/win) をインストールします。
3. Claude Code 本体をインストールします。Windows では PowerShell で次を実行します。
   ```
   irm https://claude.ai/install.ps1 | iex
   ```
4. IntelliJ IDEA の「設定 → プラグイン → Marketplace」で「Claude Code」を検索してインストールし、IDE を再起動します。検索結果には Anthropic 以外が作った似た名前のプラグインも表示されます。プラグイン名の下に書かれた提供元が「Anthropic」になっているものを選んでください。
5. プロジェクトを開いて、ターミナルで `claude` を実行します(`Ctrl+Esc` でも起動できます)。初回はブラウザでログインします。

起動すると `CLAUDE.md` が自動で読み込まれ、このプロジェクトの設計方針に沿って作業します。

## ライセンス

[LICENSE](LICENSE) を参照してください。

## kubotan へのメモ

リポジトリを新規作成したら、**必ず Watch を設定すること**(忘れない!)。
GitHub の自動 Watch 機能は 2025 年 5 月に廃止されたため、設定しないと他の人が立てた issue や PR の通知が届かない。

1. リポジトリのページ右上の **「Watch」** を押す
2. **「Custom」** を選び、**Issues** と **Pull requests** にチェックを入れる
