TARGET =	CRAP

# Directories
ROOT =		$(PWD)
SRCDIR = 	$(ROOT)/src
LIBDIR =	$(ROOT)/libs
CLASSDIR = 	$(ROOT)/classes
MAIN =		$(SRCDIR)/$(TARGET)
PARSER =	$(SRCDIR)/parser
INTERP =	$(SRCDIR)/interp
JAVADOC =	$(ROOT)/javadoc
BIN =		$(ROOT)/bin

# Executable
EXEC = 		$(BIN)/$(TARGET)
JARFILE =	$(BIN)/$(TARGET).jar
MANIFEST=	$(BIN)/$(TARGET)_Manifest.txt

# Libraries and Classpath
LIB_ANTLR =	$(LIBDIR)/antlr-3.4-complete.jar
LIB_CLI =	$(LIBDIR)/commons-cli-1.2.jar

CLASSPATH=	$(LIB_ANTLR):$(LIB_CLI):$(LIBDIR)/lwjgl-glfw.jar:$(LIBDIR)/lwjgl-glfw-javadoc.jar:$(LIBDIR)/lwjgl-glfw-natives-linux.jar:$(LIBDIR)/lwjgl-glfw-sources.jar:$(LIBDIR)/lwjgl.jar:$(LIBDIR)/lwjgl-javadoc.jar:$(LIBDIR)/lwjgl-jemalloc.jar:$(LIBDIR)/lwjgl-jemalloc-javadoc.jar:$(LIBDIR)/lwjgl-jemalloc-natives-linux.jar:$(LIBDIR)/lwjgl-jemalloc-sources.jar:$(LIBDIR)/lwjgl-natives-linux.jar:$(LIBDIR)/lwjgl-opengl.jar:$(LIBDIR)/lwjgl-opengl-javadoc.jar:$(LIBDIR)/lwjgl-opengl-natives-linux.jar:$(LIBDIR)/lwjgl-opengl-sources.jar:$(LIBDIR)/lwjgl-sources.jar:$(LIBDIR)/lwjgl-stb.jar:$(LIBDIR)/lwjgl-stb-javadoc.jar:$(LIBDIR)/lwjgl-stb-natives-linux.jar:$(LIBDIR)/lwjgl-stb-sources.jar:$(LIBDIR)/lwjgl-tinyfd.jar:$(LIBDIR)/lwjgl-tinyfd-javadoc.jar:$(LIBDIR)/lwjgl-tinyfd-natives-linux.jar:$(LIBDIR)/lwjgl-tinyfd-sources.jar

JARPATH=	$(LIB_ANTLR) $(LIB_CLI)\
			 $(LIBDIR)/lwjgl-glfw.jar\n\
 $(LIBDIR)/lwjgl-glfw-javadoc.jar\n\
 $(LIBDIR)/lwjgl-glfw-natives-linux.jar\n\
 $(LIBDIR)/lwjgl-glfw-sources.jar\n\
 $(LIBDIR)/lwjgl.jar\n\
 $(LIBDIR)/lwjgl-javadoc.jar\n\
 $(LIBDIR)/lwjgl-jemalloc.jar\n\
 $(LIBDIR)/lwjgl-jemalloc-javadoc.jar\n\
 $(LIBDIR)/lwjgl-jemalloc-natives-linux.jar\n\
 $(LIBDIR)/lwjgl-jemalloc-sources.jar\n\
 $(LIBDIR)/lwjgl-natives-linux.jar\n\
 $(LIBDIR)/lwjgl-opengl.jar\n\
 $(LIBDIR)/lwjgl-opengl-javadoc.jar\n\
 $(LIBDIR)/lwjgl-opengl-natives-linux.jar\n\
 $(LIBDIR)/lwjgl-opengl-sources.jar\n\
 $(LIBDIR)/lwjgl-sources.jar\n\
 $(LIBDIR)/lwjgl-stb.jar\n\
 $(LIBDIR)/lwjgl-stb-javadoc.jar\n\
 $(LIBDIR)/lwjgl-stb-natives-linux.jar\n\
 $(LIBDIR)/lwjgl-stb-sources.jar\n\
 $(LIBDIR)/lwjgl-tinyfd.jar\n\
 $(LIBDIR)/lwjgl-tinyfd-javadoc.jar\
 $(LIBDIR)/lwjgl-tinyfd-natives-linux.jar\n\
 $(LIBDIR)/lwjgl-tinyfd-sources.jar

# Distribution (tar) file
DATE= 		$(shell date +"%d%b%y")
DISTRIB=	$(TARGET)_$(DATE).tgz

# Classpath


# Flags
JFLAGS =	-classpath $(CLASSPATH) -d $(CLASSDIR)
DOCFLAGS =	-classpath $(CLASSPATH) -d $(JAVADOC) -private

# Source files
GRAMMAR = 		$(PARSER)/$(TARGET).g

MAIN_SRC =		$(MAIN)/*.java

PARSER_SRC =	$(PARSER)/$(TARGET)Lexer.java \
				$(PARSER)/$(TARGET)Parser.java

INTERP_SRC =	$(INTERP)/Interp.java \
				$(INTERP)/Stack.java \
				$(INTERP)/Data.java \
				$(INTERP)/$(TARGET)Tree.java \
				$(INTERP)/CRAPTreeAdaptor.java

ALL_SRC =		$(MAIN_SRC) $(PARSER_SRC) $(INTERP_SRC)

all: compile exec

compile:
	java -jar $(LIB_ANTLR) -o $(PARSER) $(GRAMMAR)
	if [ ! -e $(CLASSDIR) ]; then\
	  mkdir $(CLASSDIR);\
	fi
	javac $(JFLAGS) $(ALL_SRC)

docs:
	javadoc $(DOCFLAGS) $(ALL_SRC)

exec:
	if [ ! -e $(BIN) ]; then\
	  mkdir $(BIN);\
	fi
	echo "Main-Class: CRAP.Main" > $(MANIFEST)
	printf "Class-Path: $(JARPATH)" >> $(MANIFEST)
	cd $(CLASSDIR); jar -cfm $(JARFILE) $(MANIFEST) *
	printf "#!/bin/sh\n\n" > $(EXEC)
#	printf 'exec java -enableassertions -jar $(JARFILE) "$$@"' >> $(EXEC)
	printf 'exec java -cp "$(BIN)/CRAP.jar:$(LIBDIR)/*" CRAP.Main "$$@"' >> $(EXEC) 
	chmod a+x $(EXEC)

clean:
	rm -rf $(PARSER)/*.java $(PARSER)/*.tokens 
	rm -rf $(CLASSDIR)
	rm -rf $(JAVADOC)
	rm -rf $(BIN)

tar: clean
	cd ..; tar cvzf $(DISTRIB) $(TARGET); mv $(DISTRIB) $(TARGET); cd $(TARGET) 
