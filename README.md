# play-logging-sample

Logging sample of Play framework.

Start server

```
sbt run
```

execute sample

```
curl -X POST -H "Content-Type: application/json" -d '{"key1":"val1", "key2":"val2"}' localhost:9000/filter-log
curl -X POST -H "Content-Type: application/json" -d '{"key1":"val1", "key2":"val2"}' localhost:9000/essential-filter-log
curl -X POST -H "Content-Type: application/json" -d '{"key1":"val1", "key2":"val2"}' localhost:9000/action-filter-log
curl -X POST -H "Content-Type: application/json" -d '{"key1":"val1", "key2":"val2"}' localhost:9000/action-filter-log2
```
