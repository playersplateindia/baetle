Tim Berners Lee wrote a [Workflow ontology](http://www.w3.org/2005/01/wf/), that could be useful for us. Bugs are tasks that are in certain states. Fixing bugs is transition their state to a terminal state. In order to get a quick overview of it I drew up a UML diagram:

![http://baetle.googlecode.com/svn/trunk/img/W3C_WorkFlowOntology.jpg](http://baetle.googlecode.com/svn/trunk/img/W3C_WorkFlowOntology.jpg)


Depends on a license related "doc" ontology http://www.w3.org/2000/10/swap/pim/doc also created by Tim Berners Lee: Workflow ontology uses _doc:Work_

The "doc" depends on a "contact" ontology http://www.w3.org/2000/10/swap/pim/contact still by TBL: "doc" ontology depends on contact:SocialEntity (which subsumes contact:Person).

Todo:
  * explain N3 Work Flow rules
  * draw diagrams of the other two ontologies?
  * how does this compare to the [Plans ontology](http://www.loa-cnr.it/ontologies/Plans.owl) (the rdf for that is broken btw)

## Relation to Plans.owl ##

  * [state](http://metacognition.info/ontologies/pomr-full.html#state)
  * [action](http://metacognition.info/ontologies/pomr-full.html#action)
  * [task](http://metacognition.info/ontologies/pomr-full.html#task)

Rendered using manchester [syntax](http://owl-workshop.man.ac.uk/acceptedLong/submission_9.pdf) for convenience