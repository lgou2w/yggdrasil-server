#!/usr/bin/env bash
openssl genrsa -out private-key.pem 2048
openssl rsa -in private-key.pem -pubout -out yggdrasil-public-key.pem
openssl pkcs8 -topk8 -inform pem -in private-key.pem -outform pem -nocrypt > yggdrasil-private-key.pem
read -n 1 -p "Press any key to continue..." INP
if [[ ${INP} != '' ]] ; then
    echo -ne '\b \n'
fi
