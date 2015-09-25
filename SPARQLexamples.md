# Introduction #

Using the latest ontology here are some examples of SPARQL queries

# Examples #

### bugs that depend on a bug ###

Return all bugs that depend on the Netbeans [bug 59890](https://code.google.com/p/baetle/issues/detail?id=9890)

```
PREFIX beatle: <http://xmlns.com/baetle/0.1/ns#> 
SELECT ?bug
WHERE {
    ?bug baetle:depends_on <http://www.netbeans.org/issues/show_bug.cgi?id=59890#it> .

}
```

### bugs that are related to some source code ###

Return all bugs that are about some source code and that are open (here we may not wish to identify the source code by version, but rather all versions of it).

```
PREFIX baetle: <http://xmlns.com/baetle/0.1/ns#> 
SELECT ?bug
WHERE {
    ?bug beatle:about [ :id <http://www.netbeans.org/source/browse/beans/src/org/netbeans/modules/beans/BeanPatternGenerator.java>
                ];
         baetle:status :open .

}
```

### Bugs that block a bug of priority P1 ###

```
SELECT *
WHERE {
   ?bug baetle:blocks [
             baetle:priority <http://wiki.netbeans.org/baetle/priorities#P1>
             ] .
   OPTIONAL { ?bug baetle:assigned_to ?dev .
              ?dev sioc:email ?mail . }
}
LIMIT 20

}
```

In English: find me all bugs that are blocking bugs of priority P1, and give me the assigned developer if there is one.
Note that a system may not want to reveal the emails of developers. One way to do this would be to drop the developer information from being accessed via SPARQL, and put the information at the URL of the sioc:User, with that url being protected by access control.