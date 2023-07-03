#!/bin/bash

mvn clean -Pnative native:compile
#java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -jar target/comicreader-1.0.jar /var/www/HanMan/books.json /var/www/HanMan/images
#./comicreader /var/www/HanMan/books.json /var/www/HanMan/images
