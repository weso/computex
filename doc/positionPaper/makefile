pdf		:
	pdflatex -quiet document.tex
	bibtex -quiet document.aux
	pdflatex -quiet document.tex
	pdflatex -quiet document.tex
	

step: 
	pdflatex document.tex

verbose :
	pdflatex document.tex

view	:
	start document.pdf 
	
all		: pdf view
	
	
compress :
	tar -cvf paper.tar *.tex iosart2c.cls rdfGraph.bib makefile 
	gzip paper.tar

clean	:
	rm -f tmp/*
	rm *.bbl
	rm *.blg
	rm *.aux
	rm *.log
	rm *.out
	
