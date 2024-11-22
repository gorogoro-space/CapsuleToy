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
