# REST Server for D4M Schema

## URLs

### Management URLs
http://localhost:11001/dump
http://localhost:11001/env
http://localhost:11001/health
http://localhost:11001/metrics
http://localhost:11001/trace

### D4M URLs

http://localhost:11000/tables/list?user=root&password=secret
http://localhost:11000/tables/create?user=root&password=secret
http://localhost:11000/tables-create?baseTableName=foo
http://localhost:11000/tables-create?baseTableName=bar&addSplitsForSha1=true
http://localhost:11000/tables-delete?baseTableName=foo
http://localhost:11000/tables-delete?user=root&password=secretx
http://localhost:11000/record?row=004bec11d5c47d0c1fa06c54b958ba1416d867b8

## Start REST Server

mvn spring-boot:run

## Change Connection Properties

Edit /src/main/resource/application.properties.
