#!/bin/bash
cd "$(dirname "$0")"

make
if [ $? -ne 0 ] ; then echo "ERROR with Make" ; exit 1 ; fi

FILE=$1
./bin/CRAP ${FILE} -dot -ast ${FILE}.ast.dot 
if [ -z $2 ]
then
	rm ${FILE}.ast.dot
	echo ""
else
	dot -Tpdf ${FILE}.ast.dot -o ${FILE}.ast.pdf 
	xdg-open ${FILE}.ast.pdf
fi
