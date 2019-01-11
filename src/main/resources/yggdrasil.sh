#!/usr/bin/env bash
ygg=${project.build.finalName}.jar
java -jar ${ygg}
read -n 1 -p "Press any key to continue..." INP
if [[ ${INP} != '' ]] ; then
    echo -ne '\b \n'
fi
