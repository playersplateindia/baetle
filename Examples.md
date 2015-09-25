# This page is being worked on..

TODO: How to format as code, but make urls "clickable"?

# Baetle examples #

## Netbeans Issuezilla ##

TODO: Add dates, milestone, URL (use seeAlso??), status whiteboard (??), more comments..

```
  @prefix btl: <http://xmlns.com/baetle/#> .
  @prefix wf: <http://www.w3.org/2005/01/wf/flow#> .
  @prefix sioc: <http://rdfs.org/sioc/ns#> .
  @prefix dc: <http://purl.org/dc/elements/1.1/> .
        
  <http://www.netbeans.org/issues/show_bug.cgi?id=130267> a btl:Bug ;
       btl:summary "AssertionError: Parent of D:\install\6.1_200803170003\userdir\update\deactivate\to_uninstall.txt exists and is directory.";
    
       btl:description """
2Build: NetBeans IDE Dev (Build 200803170003)
VM: Java HotSpot(TM) Client VM, 10.0-b19
OS: Windows Vista, 6.0, x86

User Comments: 
I had one module installed. Then I deactivated it, activated again and then I tried to uninstall.
    """;

    wf:state btl:Reopened ;

    btl:environment "All" ;

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

    #bblfishNote: isMajorFor is not defined in the ontology
    btl:isMajorFor
        [ is <http://netbeans.org> ;
          a doap:Project ;
          doap:release [ a doap:Version ;
                         doap:revision "6.1" 
                       ];
        ];

    #bblfishNote: this should probably be two btl:about
    #or it should be about the more precise one. In RDF the details get
    #will get lost in a merge
    btl:about [ a btl:SoftwarePackage ;
                btl:name "autoupdate";
                btl:contains [ a btl:SoftwarePackage; 
                               btl:name "code";
                             ] ;
              ];

  .
```

## Trac ##

Todo: Add dates, ticketkey, severity (??), more comments

```
  @prefix btl: <http://xmlns.com/baetle/#> .
  @prefix wf: <http://www.w3.org/2005/01/wf/flow#> .
  @prefix sioc: <http://rdfs.org/sioc/ns#> .
  @prefix dc: <http://purl.org/dc/elements/1.1/> .

 <http://trac.edgewall.org/ticket/6808> a baetle:Bug ;
    btl:summary "Exception: column name is not unique";

    btl:description """
In the admin section, add a version that already exists.

Python Traceback

Traceback (most recent call last):
  File "/usr/local/lib/python2.4/site-packages/trac/web/main.py", line 406, in dispatch_request
    dispatcher.dispatch(req)
  File "/usr/local/lib/python2.4/site-packages/trac/web/main.py", line 237, in dispatch
    resp = chosen_handler.process_request(req)
  File "/usr/local/lib/python2.4/site-packages/TracWebAdmin-0.1.2-py2.4.egg/webadmin/web_ui.py", line 109, in process_request
    path_info)
  File "/usr/local/lib/python2.4/site-packages/TracWebAdmin-0.1.2-py2.4.egg/webadmin/ticket.py", line 244, in process_admin_request
    ver.insert()
  File "/usr/local/lib/python2.4/site-packages/trac/ticket/model.py", line 723, in insert
    (self.name, self.time, self.description))
  File "/usr/local/lib/python2.4/site-packages/trac/db/util.py", line 50, in execute
    return self.cursor.execute(sql_escape_percent(sql), args)
  File "/usr/local/lib/python2.4/site-packages/trac/db/sqlite_backend.py", line 56, in execute
    args or [])
  File "/usr/local/lib/python2.4/site-packages/trac/db/sqlite_backend.py", line 48, in _rollback_on_error
    return function(self, *args, **kwargs)
IntegrityError: column name is not unique 
""";

    wf:state btl:New ;

    btl:reporter [ a sioc:User;
                   foaf:accountName "geert@…"
                 ] ;

    btl:assigned_to [ a sioc:User;
                      foaf:accountName "cmlenz"
                    ] ;

    btl:comment
        [ a sioc:Post;
          dc:title "#6855 and #6983 were closed as duplicates. ";
          
          sioc:has_creator [ a sioc:User; 
                             foaf:accountName "cboos"
                           ] ;
        ] ;

    btl:isCriticalFor
        [ is <http://trac.edgewall.org/> ;
          a doap:Project ;
          doap:release [ a doap:Version ;
                         doap:revision "devel" 
                       ];
        ] ;

    btl:about [ a baetle:SoftwarePackage ;
                   btl:name "admin/web "
              ] ;

    btl:target_milestone <http://trac.edgewall.org/milestone/0.12> ;

  .
```