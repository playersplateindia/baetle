# Introduction #

Baetle stands for Bug And Enhancement Tracking LanguagE. It is an ontology that describes the information kept in BugDatabases such as Bugzilla, Jira and others do.

The aim of this is to enable a number of things:
  * SPARQL end points on a bug database that can be queried (see [SPARQLexamples](SPARQLexamples.md) )
  * Bugs in one open source project that refer to bugs in other open source projects, and that specify their dependency on these
  * A format for bug databases to exchange their bug data ( see [N3examples](N3examples.md) )
  * Relating bugs to software artifacts so that these issues can be tracked
  * others?

See DesignQuestions and UseCases

# Licence #

An ontology such as this is of no use if it is not done communally, fully openly and without any desire to collect royalties of any sort from anyone. This is a language to help diverse groups communicate. As such it should belong to everyone. Currently the license is new BSD. A CC license seems to be very common with Ontology developers. Probably it should be both. A CC license for the ontology and a BSD license for the testing code.

# Participating #

I have set up a [mailing list](http://groups.google.com/group/baetle) for discussion of the ontology. Writing a good ontology can, surprisingly enough, be a lot of work, at least more work than is feasible for one person not working on it full time. A lot of the work comes from having to understand the different needs of different people in the community, writing out test cases or mappers to different databases, and working well with OtherOntologies. So volunteers helpers are welcome.

I have given everyone on the mailing list with a gmail account access to the wiki.  Ask me if you would like a gmail account.

# History #

This idea has been developing for some time.  Henry Story (bblfish) first outlined the advantages such an ontology can have in [Google Video introduces the semantic web](http://blogs.sun.com/bblfish/entry/google_video_introduces_the_semantic). The first details of this ontology were written out in the blog entry [Baetle: Bug And Enhancement Tracking LanguagE](http://blogs.sun.com/bblfish/entry/baetle_bug_and_enhancement_tracking)



# Details #

Current thinking (version 0.001) is that the ontology should look something like this

![http://baetle.googlecode.com/svn/trunk/img/BaetleUML.jpg](http://baetle.googlecode.com/svn/trunk/img/BaetleUML.jpg)

The namespace used in the above diagram correspond to the followng ontologies:

  * wf: WorkFlowOntology
  * awol: [Atom Owl](http://bblfish.net/work/atom-owl/2006-06-06/) Ontology
  * foaf: [Friend Of a Friend](http://xmlns.com/foaf/0.1/)
  * skos: [Simple Knowledge Organisation Systems](http://www.w3.org/2004/02/skos/) Ontology
  * doap: [Description of a Project](http://usefulinc.com/doap/)
  * sioc: [Semantically Interlinked Online Communities](http://rdfs.org/sioc/ns#)
  * dct:  http://purl.org/dc/terms/ known as dcterms, or Dublin Core Terms

