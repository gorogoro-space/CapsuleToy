# Migration from ver1.0 to ver1.1
~~~
# cd plugins/CapslueToy/
# cp -p sqlite.db sqlite.db.backup
# sqlite3 ./sqlite.db
sqlite> DROP INDEX world_name_sign_xyz_uindex;
sqlite> DROP INDEX world_name_chest_xyz_uindex;
sqlite> .quit
# cp -p ./sqlite.db ../plugins/CapsuleToy/
~~~
