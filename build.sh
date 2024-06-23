#!/bin/bash

echo -e "Processing files from last build..."
rm -r build/
mkdir build/


echo -e "Building..."

javac -encoding utf-8 \
      -Xlint:deprecation -XDignore.symbol.file -Xdiags:verbose \
      -d build/ \
      -sourcepath src/java/ \
      src/java/tk/xhuoffice/psb4j/*.java

if [ $? -eq 0 ]; then
    echo -e "Packing..."
    cp ./README.md ./LICENSE build/
    cd build/
    jar -cvfm 'psb4j.jar' ../manifest -C ./ .
    cd ..
    echo -e "Done!"
    exit 0
else
    echo -e "Build failed!"
    exit 1
fi
