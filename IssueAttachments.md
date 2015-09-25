# Introduction #

Many issue trackers support attachments to issues, this document provides examples of how to express this with Baetle and SIOC


# Details #

# These examples are under discussion - please feel free to comment them, or see the mailing list

```
<...> a baetle:Issue;
     baetle:comment
        [ is <../projects/{project}/issues/{issue}/comments/{comment}> ;
          a sioc:Post;
          dc:title "An attachment";
          sioc:has_creator <../users/{user}>;
          sioc:attachment <../projects/{project}/issues/{issue}/attachments/{attachment}>
        ] .
```