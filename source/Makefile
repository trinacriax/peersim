VER=1.0.2

.PHONY: all clean doc release

all:
	javac -g -classpath .:jep-2.3.0.jar:djep-1.0.0.jar `find -name "*.java"`
clean:
	rm -f `find -name "*.class"`
doc:
	rm -rf doc/*
	javadoc -overview overview.html -verbose -classpath .:jep-2.3.0.jar:djep-1.0.0.jar -d doc \
                -group "Peersim" "peersim*" \
                -group "Examples" "example.*" \
		peersim \
		peersim.cdsim \
		peersim.config \
		peersim.core \
		peersim.dynamics \
		peersim.edsim \
		peersim.graph \
		peersim.rangesim \
		peersim.reports \
		peersim.transport \
		peersim.util \
		peersim.vector \
		example.aggregation \
		example.loadbalance \
		example.edaggregation \
		example.hot \
		example.newscast 

docnew:
	rm -rf doc/*
	javadoc -overview overview.html -docletpath peersim-doclet.jar -doclet doclets.standard.Standard -classpath .:jep-2.3.0.jar:djep-1.0.0.jar -d doc \
                -group "Peersim" "peersim*" \
                -group "Examples" "example.*" \
		peersim \
		peersim.cdsim \
		peersim.config \
		peersim.core \
		peersim.dynamics \
		peersim.edsim \
		peersim.graph \
		peersim.rangesim \
		peersim.reports \
		peersim.transport \
		peersim.util \
		peersim.vector \
		example.aggregation \
		example.loadbalance \
		example.hot \
		example.edaggregation \
		example.newscast 


release: clean all
#	rm -fr peersim-$(VER)
#	mkdir peersim-$(VER)
#	mv doc peersim-$(VER)
#	cp Makefile README CHANGELOG RELEASE-NOTES build.xml peersim-$(VER)
#	mkdir peersim-$(VER)/example
#	cp example/*.txt peersim-$(VER)/example
#	mkdir peersim-$(VER)/src
#	cp --parents `find peersim example -name "*.java"` peersim-$(VER)/src
	rm -f peersim-$(VER).jar
	jar cf peersim-$(VER).jar `find peersim example -name "*.class"`
#	mv peersim-$(VER).jar peersim-$(VER)
#	cp jep-2.3.0.jar peersim-$(VER)
#	cp djep-1.0.0.jar peersim-$(VER)
