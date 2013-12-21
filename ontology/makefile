TURTLE	= 	$(wildcard *.ttl)
RDF		=	$(TURTLE:.ttl=.rdf)
			
%.rdf	: %.ttl
	java jena.rdfcat -out RDF/XML $< > $@

rdf		: $(RDF)
	echo "Converted $(RDF)"
	