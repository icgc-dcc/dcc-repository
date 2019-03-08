# ICGC DCC - Repository - Client

Client module that is the execution entry point into the system.

## Build

To compile, test and package the module, execute the following from the root of the repository:

```shell
mvn -am -pl dcc-repository/dcc-repository-client
```

## Configuration

Configuration template may be found [here](src/main/conf).

## Command Line Interface

```
Usage: dcc-repository-client [options]
  Options:
  *     --config
       Path to the repository config file
        --sources
       Source to import. Comma seperated list of: 'aws', 'pcawg', 'tcga',
       'cghub'. By default all sources will be imported.
       Default: [CGHUB, TCGA, PCAWG, AWS]
```

Development:

to run this locally you need, using docker, the following containers :

1- Mongo instance (mounting a volume here is optional):
```bash
docker run -d -p 27017:27017 -v ~/data:/data/db mongo
```


2- start elastic search (can't use anything newer than version 5.6.15)
```bash
docker run -d --name elasticsearch \
    -p 9200:9200 -p 9300:9300 \
    -e "discovery.type=single-node" \
    -e "xpack.security.enabled=false" \
    -e "cluster.name=elasticsearch" \
    docker.elastic.co/elasticsearch/elasticsearch:5.6.15
```

3- (optional) kibana to view the indexing results
```bash
docker run --link elasticsearch:elasticsearch --name kibana \
    -p 5601:5601 -e "ELASTICSEARCH_URL=http://elasticsearch:9200" \
    docker.elastic.co/kibana/kibana:5.6.15
```

4- verify application.yml, then Run ClientMain (you do that can through intellj directly)