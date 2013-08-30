Computex
========

Computex (Computing Statistical Indexes) can be seen as an extension of RDF Data Cube vocabulary to handle
 statistical indexes. Every Computex data will also be RDF Data Cube Data, although it can contain further
 declarations specific for statistical indexes.
 
The URI of the ontology is [this](http://purl.org/weso/ontology/computex)

Computex Validation Service
===========================

We have developed a [Computex Validation Service](http://computex.herokuapp.com/) which can validate 
both Computex and RDF Data Cube vocabularies.

The service can also expand the Data to compute Index data automatically.


Applications
============
The main application and motivation for this project is [The WebIndex](http://thewebindex.org) project, 
 although it could be to other statistical indexes in different domains.

Examples
========
We have developed a simplified example file with some index data inspired from the Web Index. 

We also developed a small Scala application which validates index data files and performs computations on them.
