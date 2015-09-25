[N3](http://www.w3.org/2000/10/swap/doc/Overview.html) makes it much easier to write out RDF graphs. Here are a few examples of real bugs written out using the N3 notation

## Simple html bug tracking system: Issue rdfms-abouteachprefix ##

[Issue rdfms-about eachprefix](http://www.w3.org/2000/03/rdf-tracking/#rdfms-abouteachprefix) is writen out in simple html, in the you-can-get-no-simpler-than-this-bug-tracking-system (patent pending) of the w3c.

```
@prefix : <http://xmlns.com/baetle/0.1/ns#> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix awol: <http://bblfish.net/work/atom-owl/2006-06-06/#> .
@prefix wf:   <http://www.w3.org/2005/01/wf/flow#> .


<http://www.w3.org/2000/03/rdf-tracking/#rdfms-abouteachprefix>
    a :Issue ; 
    :title "Issue rdfms-abouteachprefix: Something should be done about aboutEachPrefix construct"; 
    :reporter <http://www.w3.org/People/Berners-Lee/card#i>;
    :raised "2000-02-29T00:00:00T"^^xsd:dateTime;
    :summary "Split of the editor module into multiple modules"@en;
    wf:state [ a :Solution;
               :comment [ a awol:Content;
                          awol:body """<div>On 1st June 2001, the WG decided that aboutEachPrefix would be removed from the RDF Model and Syntax Recommendation on the grounds that there is a lack of implementation experience, and it therefore should not be in the recommendation. A future version of RDF may consider support for this feature.</div>""";
                         awol:type "text/html";
                      ];
           ];
    :comment [ a awol:Content;
               awol:body """<div><p>See also:</p>
<ul>
  <li>search of RDF list archives for <a href="http://search.w3.org/Public/cgi-bin/query?mss=simple&amp;pg=q&amp;what=web&amp;filter=lists&amp;fmt=.&amp;q=%2Bwww-rdf+%2BaboutEachPrefix">"aboutEachPrefix"</a></li>
  <li><a href="http://www.w3.org/TR/1999/REC-rdf-syntax-19990222/#URIPrefix">Model+Syntax
    REC, 3.4. Containers Defined By A URI Pattern</a></li>
</ul>
</div>""";
            awol:type "text/html";
            ];
    .

```

Note: there is a problem here. A Comment can't be an awol:Content. There has to be something in between, that gives the author of the comment and the date at which it was made at least (though this would not be used here). Is this where we would need an awol:Entry? The annoying thing about an awol:Entry is that it forces us to take too much: a title and an id....

## NetBeans Bugs ##

Using a SPARQL query of the form

```
CONSTRUCT {
<http://www.netbeans.org/issues/show_bug.cgi?id=8997#it> ?r ?o;
        sioc:container_of ?post.
        ?post ?r2 ?o2 .
        ?o2 ?r3 ?r4 .
} WHERE {

<http://www.netbeans.org/issues/show_bug.cgi?id=8997#it>
    ?r ?o;
    sioc:container_of ?post .
    ?post ?r2 ?o2 .
    OPTIONAL {
      ?o2 a awol:Content;
          ?r3 ?o4 .
    }

}
```

an applying it to a D2RQ endpoint on the Netbeans bug database we got the following:

  * [Netbeans Bug 51486](http://baetle.googlecode.com/svn/trunk/examples/NetbeansBug51486.n3) in N3
  * [Netbeans Bug 8997](http://baetle.googlecode.com/svn/trunk/examples/NetbeansBug51486.n3) in N3
  * [Netbeans Bug 59890](http://baetle.googlecode.com/svn/trunk/examples/NetbeansBug51486.n3) in N3