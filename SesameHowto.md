# Introduction #

[Sesame](http://openrdf.org) is an open source framework and database for working with RDF graphs. It comes with libraries for parsing xml, linking to a number of databases, including a specialised RDF database, called the Native Sail. It also integrates a SPARQL endpoint. Here we describe how to load the NetBeans bug database into your Sesame instance.


# Details #

## Database Preparation ##

Here we load the data into a native database, which becomes queriable via the  `com.xmlns.baetle.sesame.Query` program in the subversion repository.

  1. Download [Sesame 2 beta 2](http://openrdf.org/download.jsp)
  1. Download the 70 MB [bziped package](http://bblfish.net/work/baetle/mappings/netbeans.org/) and unzip it. You should now have a 1 GB file in N-Triples format.
  1. Check out the baetle [subversion repository](http://code.google.com/p/baetle/source)
  1. Compile the `com.xmlns.baetle.sesame.LoadRDF` class (you need to point to the Sesame jars)
  1. Run `com.xmlns.baetle.sesame.LoadRDF -d datadir baetle.ntriples` command

This will take some time. You will see a bunch of files appearing in your data directory.

Now, you can use the `Query` class to send queries to the database. It's very fast on a 2 GHz machine. (Once the indexing has been properly done)

```
hjs@bblfish:0$ bin/openSesame com.xmlns.baetle.sesame.Query -d /Users/hjs/.aduna/openrdf/server/native -f json
using store in directory /Users/hjs/.aduna/openrdf/server/native
SELECT * 
WHERE { ?i a <http://xmlns.com/baetle/#Issue> . }
LIMIT 1

28139 [main] INFO org.openrdf.query.parser.QueryParserUtil - Unable to load query parser class: org.openrdf.query.parser.serqo.SeRQOParser
{
        "head": {
                "vars": [ "i" ]
        }, 
        "results": {
                "ordered": false, 
                "distinct": false, 
                "bindings": [
                        {
                                "i": { "type": "uri", "value": "http://www.netbeans.org/issues/show_bug.cgi?id=296#it" }
                        }
                ]
        }
}
```


## Server Install ##

Here we create an HTTP accessible SPARQL endpoint, so that web applications can query the database. See the Sesame Docs for more detailed info (please fill out extra things)

  1. download [Tomcat 6](http://tomcat.apache.org/download-60.cgi) (it's easier)
  1. unzip tomcat
  1. place the datadir in you ~/.aduna/openrdf/server/ directory. Call that dir "native" (on unix machines) for Windows see Sesame Docs.
  1. edit ~/.aduna/openrdf/server/repositories.xml
```
<?xml version="1.0" encoding="ISO-8859-1"?>
<openrdf-sesame>
   <admin password="admin"/>
   <repositorylist>
       <repository id="native">
         <title>Native Repository</title>
         <sailstack>
             <sail class="org.openrdf.sail.nativerdf.NativeStore">
                  <param name="triple-indexes" value="spoc,sopc,posc,psoc,opsc,ospc"/>
             </sail>
         </sailstack>
         <acl worldReadable="true" worldWriteable="true" />
       </repository>
    </repositorylist>
</openrdf-sesame>
```
  1. start tomcat
  1. in the manager application load the two wars from the Sesame distribution
  1. Send queries to the database:
    * go to the sesame client app and use the web interface to send the queries
    * use the com.xmlns.baetle.sesame.Query from the command line to send your queries

```
hjs@bblfish:0$ bin/openSesame -Xms125m -Xmx500m com.xmlns.baetle.sesame.Query -s http://localhost:8080/openrdf/
using repository at http://localhost:8080/openrdf/
PREFIX : <http://xmlns.com/baetle/#>
CONSTRUCT { ?issue :depends_on ?other . }
WHERE { ?issue :depends_on ?other . }
LIMIT 5

@prefix : <http://xmlns.com/baetle/#> .

@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .

<http://www.netbeans.org/issues/show_bug.cgi?id=2337#it> 
   :depends_on <http://www.netbeans.org/issues/show_bug.cgi?id=31362#it> ,
               <http://www.netbeans.org/issues/show_bug.cgi?id=32942#it> ,
               <http://www.netbeans.org/issues/show_bug.cgi?id=35023#it> , 
               <http://www.netbeans.org/issues/show_bug.cgi?id=46020#it> .

<http://www.netbeans.org/issues/show_bug.cgi?id=2419#it> 
   :depends_on <http://www.netbeans.org/issues/show_bug.cgi?id=7065#it> .
```


## Sending queries from JSON ##

Note: this is now documented in "[HTTP communication protocol for Sesame 2](http://www.openrdf.org/doc/sesame2/2.0-beta3/system/ch08.html)"

The requests between the client and the server are simple HTTP requests. I think these are following the standard SPARQL Application Protocol. Using a tool such as [tcpflow](http://www.circlemud.org/~jelson/software/tcpflow/) (which suddenly stopped working on my intel core 2 duo with OSX 10.4.9) or [tshark](http://www.wireshark.org/docs/man-pages/tshark.html) you can get a good idea of how to call this directly.

So I started tshark like this
```
sudo tshark -i lo0 -w - -R "tcp.port == 8080" > out
```

and ran the following Query

```
hjs@bblfish:0$ bin/openSesame com.xmlns.baetle.sesame.Query -s http://localhost:8080/openrdf/ 
using repository at http://localhost:8080/openrdf/
SELECT *
WHERE { ?s ?r ?b . }
LIMIT 1

```

The tshark output showed the above command to have sent the following request over the wire

```
GET /openrdf/repositories/native?queryLn=SPARQL&query=SELECT+*WHERE+%7B+%3Fs+%3Fr+%3Fb+.+%7DLIMIT+1&infer=true HTTP/1.1
Accept: application/sparql-results+xml
Accept: application/x-binary-rdf-results-table
Accept: application/sparql-results+json
User-Agent: Jakarta Commons-HttpClient/3.0.1
Host: localhost:8080

```

(notice how the SPARQL endpoint is really at `http://localhost:8080/openrdf/repositories/native`)

The above request was followed by the response

```
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Disposition: attachment; filename=queryresult.srx
Content-Type: application/sparql-results+xml
Transfer-Encoding: chunked
Date: Sun, 01 Apr 2007 11:16:18 GMT

<?xml version='1.0' encoding='UTF-8'?>
<sparql xmlns='http://www.w3.org/2005/sparql-results#'>
        <head>
                <variable name='s'/>
                <variable name='r'/>
                <variable name='b'/>
        </head>
        <results ordered='false' distinct='false'>
                <result>
                        <binding name='b'>
                                <uri>http://rdfs.org/sioc/ns#User</uri>
                        </binding>
                        <binding name='r'>
                                <uri>http://www.w3.org/1999/02/22-rdf-syntax-ns#type</uri>
                        </binding>
                        <binding name='s'>
                                <uri>http://netbeans.org/people/10</uri>
                        </binding>
                </result>
        </results>
</sparql>
```

So to get back a JSON response I ran the query directly from the command line but only asking for the json representation

```
hjs@bblfish:0$ telnet localhost 8080
Trying ::1...
Connected to localhost.
Escape character is '^]'.
GET /openrdf/repositories/native?queryLn=SPARQL&query=SELECT+*WHERE+%7B+%3Fs+%3Fr+%3Fb+.+%7DLIMIT+1&infer=true HTTP/1.1
Accept: application/sparql-results+json
User-Agent: Jakarta Commons-HttpClient/3.0.1
Host: localhost:8080

HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Disposition: attachment; filename=queryresult.srj
Content-Type: application/sparql-results+json
Transfer-Encoding: chunked
Date: Sun, 01 Apr 2007 11:20:20 GMT

164
{
        "head": {
                "vars": [ "s", "r", "b" ]
        }, 
        "results": {
                "ordered": false, 
                "distinct": false, 
                "bindings": [
                        {
                                "b": { "type": "uri", "value": "http://rdfs.org/sioc/ns#User" }, 
                                "r": { "type": "uri", "value": "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" }, 
                                "s": { "type": "uri", "value": "http://netbeans.org/people/10" }
                        }
9

                ]
        }
}
0
```

(I don't know why I get those extra numbers in there... answer: those are probably due to chunked transfer encoding, which a good http library should take care of. )

JavaScript experts should find it easy to build up such calls and get their preferred response.


