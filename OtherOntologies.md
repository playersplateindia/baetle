Competing ontologies

  * [EvoOnt](http://www.ifi.unizh.ch/ddis/evoont.html) developed at the University of Zürich, in Switzerland, contains a very complete description of relations for what baetle is trying to do. This ontology is now on baetle server, available to be edited and improoved, with the approval of the University of Zurich, who have joined the mailing list, as owners of course. See EvoOntBomMappings

Listed here are some ontologies that we should look at and work with

  * [FOAF](http://xmlns.com/foaf/0.1/): The Friend of a Friend Ontology is very important to identify people and relations between them
  * [DOAP](http://usefulinc.com/ns/doap): To describe bugs the Description of a Project ontology is clearly very handy. Local UML diagram at: [DoapOntology](DoapOntology.md)
  * [SKOS](http://www.w3.org/2004/02/skos/): there are many ways of organising projects and classifying bugs. The SKOS ontology gives a very lightweigh way of creating metadata for wiki powered ontologies that are project specific, without requiring a full blown ontology such as this one to be designed.
  * WorkFlowOntology: Tim Berners Lee wrote out a WorkFlowOntology. Bugs affect workflows. They are in certain states and can have state transitions.
  * [SIOC](http://rdfs.org/sioc/spec/) Semantically Interlinked Online Communities. We have a bug tracking community to interlink here. Things get Posted to Issues too.
  * [dcterms](http://dublincore.org/documents/dcmi-terms/): Dublin Core Terms. Very basic but useful vocabulary. Namespace: http://purl.org/dc/terms/

Suggested one we can work with or should be compatible with
  * [Project Vocabulary](http://dannyayers.com:88/xmlns/project/) Danny Ayers started a Project Vocabulary. Software projects are projects, so this looks useful.
  * [AtomOwl](http://bblfish.net/work/atom/2006-06-06) When looking at bug reports such as [Netbeans bug id 21365](http://www.netbeans.org/issues/show_bug.cgi?id=21365) the bug looks very much like a feed, with the comments looking very much like entries. It also seems obvious that one could use the [Atom Publication Protocol](http://bitworking.org/projects/atom/) to publish replies to a bug or to create new issues.
  * [Tag Ontology](http://www.holygoat.co.uk/projects/tags/) every bug system allows one to assign keywords to bugs. Keywords are similar to tags which are similar to Atom Categories as argued in  [Folksonomies, Ontologies and Atom](http://blogs.sun.com/bblfish/entry/folksonomies_ontologies_atom_and_the)
  * [MetaLink ](http://www.metalinker.org/) has an xml format for helping mirroring of downloads and helping clients selecting the best mirror. [twanj is working on an Ontology](http://chatlogs.planetrdf.com/swig/2007-03-06.html#T17-05-23) for it.
  * [Web Archictecture](http://sw.nokia.com/WebArch-1/) very helpful vocabulary. It would be nice to have it backed by a standards body.
  * [Test Metadata](http://www.w3.org/TR/test-metadata/) a w3c approved rdf vocabulary for describing tests
  * [EARL](http://www.w3.org/TR/EARL10/) Evaluation and Report Language, a w3c approved ontology to test resources (could tie test classes to source code?)
  * [Plans Ontology](http://www.loa-cnr.it/ontologies/Plans.owl) (the rdf/xml is broken) How does this relate to the WorkFlowOntology?