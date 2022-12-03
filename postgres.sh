helm install postgresql \
  --set global.postgresql.postgresqlDatabase=postgres \
  --set global.postgresql.postgresqlUsername=postgres \
  --set global.postgresql.postgresqlPassword=postgres \
  --set global.postgresql.servicePort=5432 \
  bitnami/postgresql;