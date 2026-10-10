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
