# CapsuleToy

看板とチェストでカプセルトイ(ガチャ)を作る Paper 用プラグイン(Paper 1.21.11 以降・Java 21 必須)。コード付きの券を持って看板を右クリックすると、チェストから景品が 1 つランダムに出る。
リポジトリ: https://github.com/gorogoro-space/CapsuleToy (GPL-3.0)

## 作業の進め方(必ず守ること)

- **設計が確定するまで実装しない。** 機能追加や仕様変更は、まず設計案(何を・なぜ・どう変えるか、影響範囲)を提示し、承認を得てからコードを書く。
- 判断が必要な点は、選択肢を示して質問する。勝手に決めない。
- やり取りは日本語で行う。新しく書くコードのコメントは日本語でよい。既存の英語のコメントは、頼まれない限り書き換えない。
- 変更は必要最小限にする。頼まれていないリファクタリングや機能追加はしない。
- 作業後は、変更・追加・削除したファイルの一覧と変更内容を報告する。
- 仕様を変えたら README.md と CLAUDE.md も合わせて更新する。
- `git push` の前には必ず確認を取る。コミットは意味のある単位で分ける。
- リポジトリへの初回の `git push` の前には、「GitHub で Watch 設定(Custom → Issues と Pull requests)はしましたか?」と日本語で確認する。自動 Watch は廃止されており、設定しないと issue や PR の通知が届かない。
- コミットメッセージや PR に `Co-Authored-By: Claude` などの署名を付けない。`.claude/` は Git に入れない(`.git/info/exclude` で除外済み)。
- 実装中に設計の抜けや穴に気づいたら、黙って対処せず報告して相談する。
- プルリクエストをチェックするときは、次の観点で確認して報告する。
  - 変更概要(何を・なぜ変えているか)
  - 脆弱性(権限チェックの漏れ、入力値の検証、パスの扱い、SQL に埋め込む文字列の検証など)
  - 安全面(データの破損・消失、券の番号の二重使用、再読み込み時の後始末、他プラグインの妨げなど)
  - 性能(TPS など)の低下(高頻度イベントでの重い処理、メインスレッドでの同期 I/O、定期タスクの追加など。「最重要の設計方針」に沿っているか)

## 環境

- Paper 1.21.11(`io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT`、`compileOnly`)/ `plugin.yml` の `api-version: '1.21.11'`(1.21.11 未満のサーバーでは読み込まれない)
- **Java 21 必須。** `build.gradle.kts` の toolchain で JDK 21 を使い、Java 21 の class ファイルを出力する。勝手に変えない
- ビルド: Gradle(Kotlin DSL、wrapper 9.7.1)。`gradlew.bat clean build`(Windows)。**JDK 21 で実行する**(`JAVA_HOME` を `C:\Program Files\Java\jdk-21` にする。既定の `java` は JDK 27)。「ビルドして」と言われたら常にクリーンビルドする。成果物は `build/libs/CapsuleToy-<version>.jar`
  - IntelliJ の「アーティファクトのビルド」はクラスファイルが入らないことがある。必ず Gradle でビルドする
- バージョンは `gradle.properties` の `version` だけで管理する。`plugin.yml` の `version: '${version}'` は `processResources` でビルド時に置き換わる
- SQLite の JDBC ドライバ(`org.sqlite.JDBC`)はサーバー(Paper)に入っているものを使う。jar には同梱しない
- パッケージ: `space.gorogoro.capsuletoy`(**すべて小文字**。大文字が混ざると plugin.yml の main と一致せず起動しない)
- ビルド時に非推奨 API の警告が出る(`ChatColor`、`ItemMeta#setDisplayName`/`setLore`/`getLore`、`SignChangeEvent#getLine`/`setLine`、メタデータ、`finalize()` という名前のメソッドなど)。動作には影響しない。直すのは頼まれたとき
- 動作確認はサーバーを再起動して行う。PlugManX での読み込みは権限やコマンドの登録が不完全になることがある

## 最重要の設計方針

### TPS に影響させない
- 高頻度イベント(PlayerInteractEvent、PlayerMoveEvent、AsyncPlayerChatEvent など)を使うときは、安い判定を先に行い、対象外なら即座に抜ける。`onPlayerInteract` は右クリック以外と、看板・チェスト以外をすぐ返す
- BlockPhysicsEvent、VehicleMoveEvent など発生頻度が極端に高いイベントは使わない
- メインスレッドでファイルや DB の同期 I/O をしない(起動時・リロード時に一度だけ行う小さなファイルの読み込みは除く)
  - ただし既存の処理は、看板の右クリック・看板の設置・コマンドで SQLite をメインスレッドから同期で読み書きしている。看板かどうかの判定には登録済みの看板の位置のキャッシュを使うが、`isCapsuleToy(Location)` の期限の判定が逆になっている。そのため、読み直してから `cache-expire-seconds` の間は、どの看板を右クリックしても全件を読み直す。期限が過ぎると、登録・削除したときにしか読み直さない(2024 年からある既存の不具合。直すのは頼まれたとき)。新しく DB を使う処理を足すときは、頻度と非同期化の要否を設計案に書く
- 設定ファイルを追加する場合は、起動時・リロード時に一度だけ解析して保持する
- 定期タスクは最小限にし、追加するときは頻度と理由を設計案に書く

### データの保存
- `plugins/CapsuleToy/` の中だけを使う(`config.yml`、`config_jp.yml`、`sqlite.db`)。プラグインフォルダ以外には何も書き込まない
- `config.yml` はプラグインが書き換えない(無ければ jar 内のものを書き出すだけ)。`config_jp.yml` は日本語の見本で、起動のたびに jar 内のもので上書きする
- `sqlite.db` の表(`capsuletoy`、`ticket`)を変えるときは、既存のデータベースの移行を必ず考える。サーバー上に既存のデータがある前提(例: 1.2 で `ticket.capsuletoy_name` の列を起動時に自動で追加している)
- 券の番号は使ったら消す。複製した券では 2 回目以降は使えない、という前提を崩さない

### 他プラグインとの関係
- 既存の機能(例: GSit、看板の click_event など)を妨げないこと。イベントをキャンセルする範囲は必要最小限にする(今は登録済みの看板と、`modify` 中のチェストの右クリックだけキャンセルする)
- **ItemSlotMachine が `CapsuleToy#createCodedTicket(String)` をリフレクションで呼んでいる。** メソッド名・引数・戻り値(`ItemStack`、作れないときは null)、券の説明の並び(3 行目が `CAPSULETOY CODE:<番号>`、4 行目が登録名)を変えない。変える場合は ItemSlotMachine 側も合わせて直す

## 機能仕様

(現在の実装の動作。仕様を変えたらここを更新する)

- **起動時(`onEnable`)**: `config.yml` が無ければ jar 内のものを保存し、`config_jp.yml` を上書きで書き出す。`named-ticket-only` とメッセージを読み、データベースの表を作り(無い列は追加)、リスナーを登録する
- **看板の設置(`SignChangeEvent`)**: 1 行目が `[capsuletoy]`(大文字小文字は問わない)で、`capsuletoy.create` を持つときに登録する。2 行目が登録名(英数字と `_` だけ)、3 行目が表示名、4 行目が説明。登録後は `sign-line1-prefix` などの設定で看板の文字を書き換える。同じ場所に登録済みならキャンセルする
- **看板の右クリック**: 登録済みの看板なら操作をキャンセルし、手に持った紙の説明 3 行目から番号を読む。番号が無い・使用済みなら `not-found-ticket-code`。名前付きの券は同じ登録名の看板でだけ使える(違えば `wrong-capsule`、番号は減らない)。名前の無い券は `named-ticket-only` の看板では使えない(`needs-named-ticket`、番号は減らない)。使えたら番号を消して券を 1 枚減らし、チェストの枠を 1 つランダムに選ぶ。空の枠ならハズレ(`not-found-pick`)。景品は持ち物に入れ、入りきらなければ足元に落とす(`found-pick` / `dropped-pick`)
- **チェストの右クリック**: `/capsuletoy modify <名前>` のあとだけ、そのチェストを景品のチェストとして登録する
- **コマンド**: `list`・`modify`・`delete` はそれぞれ `capsuletoy.list`・`capsuletoy.modify`・`capsuletoy.delete` が必要(`plugin.yml` に未登録なので、既定では OP のみ)。`ticket <プレイヤー名|@a|@p|@s|@r>` は OP・コンソール・コマンドブロック。名前の無い券を空いている枠に入れる(空きが無ければ渡さない)。`reload`・`enable`・`disable` は OP のみ
- **メッセージ**: `found-pick`・`dropped-pick`・`wrong-capsule`・`needs-named-ticket` は `config.yml` に無ければ、ほかの文が日本語なら日本語、それ以外は英語の文を使う
- **`createCodedTicket()` / `createCodedTicket(String)`**: 他プラグイン向けの公開メソッド。番号をデータベースに作り、券(紙)を 1 枚返す。持ち物には入れない

## 過去にハマった点(他プロジェクトでの経験)

- `config.getString(path, "")` のように既定値を渡すと、jar 内 config.yml の既定値が参照されない。既定値なしで取得して null を判定すること
- plugin.yml で `default: true` にした権限でも、登録されないと Bukkit は「OP のみ」として扱う。全員向けの機能を権限で縛らない

## ファイル構成

- `src/main/java/space/gorogoro/capsuletoy/CapsuleToy.java` — メインクラス。起動・終了、コマンドの振り分け、`named-ticket-only` の読み込み、`createCodedTicket`
- `CapsuleToyCommand.java` — 各サブコマンドの処理
- `CapsuleToyListener.java` — 看板の設置、看板とチェストの右クリック
- `CapsuleToyDatabase.java` — SQLite の読み書き(カプセルトイ・券の番号)と看板のキャッシュ
- `CapsuleToyUtility.java` — 券の作成、番号の生成、セレクター(`@a` など)、景品の受け渡し、メッセージの選択
- `src/main/resources/plugin.yml` — プラグイン定義
- `src/main/resources/config.yml` / `config_jp.yml` — 英語の設定と日本語の見本
- `build.gradle.kts` / `settings.gradle.kts` / `gradle.properties` — Gradle の設定(バージョンは `gradle.properties`)
