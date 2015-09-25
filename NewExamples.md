#Examples using the new /ns/Baetle.ewl.n3

# Introduction #

Wanted to see how issues should be expressed with the new ontology..

## Example of Bug from Netbeans Issuezilla ##

```
  @prefix : <http://xmlns.com/baetle/#> .
  @prefix foaf: <http://xmlns.com/foaf/0.1/> .
  @prefix owl: <http://www.w3.org/2002/07/owl#> .
  @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
  @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
  @prefix lifecycle: <http://purl.org/vocab/lifecycle/schema#> .
  @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
  @prefix dc: <http://purl.org/dc/elements/1.1/> .
  @prefix dcterms: <http://purl.org/dc/terms/> .
  @prefix doap: <http://usefulinc.com/ns/doap#> .
  @prefix sioc: <http://rdfs.org/sioc/ns#> .
  @prefix skos: <http://www.w3.org/2004/02/skos/core#> .

  <http://www.netbeans.org/issues/show_bug.cgi?id=130267> a btl:Ticket ;

    #Is this correctly formatted according to dcterms?
    dcterms:created "Mon Mar 17 09:51:00 +0000 2008";

    btl:ticketState [ a btl:Resolved ];

    btl:reporter [ a sioc:User;
                   foaf:accountName "novakm";
                 ] ;

    btl:assigned_to [ a sioc:User;
                      foaf:accountName "jrechtacek";
                    ] ;

    btl:qa_contact [ a sioc:User; 
                      foaf:mbox <mailto:issues@autoupdate>;
                    ] ;
    btl:comment
        [ a sioc:Post;
          dc:title "Created an attachment (id=58470)";
          sioc:has_creator [ a sioc:User; 
                             foaf:accountName "novakm" ;
                             foaf:mbox <mailto:novakm@nospamplease>;
                           ] ;
          sioc:attachment <http://www.netbeans.org/nonav/issues/showattachment.cgi/58470/>
        ] ;

    foaf:primaryTopic 
        [ a btl:Bug ;

              btl:summary "AssertionError: Parent of D:\install\6.1_200803170003\userdir\update\deactivate\to_uninstall.txt exists and is directory.";
    
              btl:description """2Build: NetBeans IDE Dev (Build 200803170003) ...""";

              btl:issueState [ a btl:Fixed ] ;

              btl:environment "All" ;

              btl:isMajorFor
                  [ is <http://netbeans.org> ;
                    a doap:Project ;
                    doap:release [ a doap:Version ;
                                     doap:revision "6.1" 
                                  ];
                  ];

              btl:about [ a btl:SoftwarePackage ;
                          btl:name "autoupdate";
                          btl:contains [ a btl:SoftwarePackage; 
                                         btl:name "code";
                                       ] ;
                        ];
         ];

  .
```