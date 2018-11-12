#!/bin/bash

pushd $(dirname $0) &> /dev/null
basepath=$(pwd)
popd > /dev/null

DEFAULT_PORT=5000

usage() {
        echo -e "usage:"
        echo -e "\t$0\t-i/--server-ip <ip> "
        echo -e "\t\t\t\t-p/--port <port_no>"
        echo -e
        echo -e "\t-i/--server-ip :\tthis node ip address"
        echo -e "\t-p/--port :\t\tport number, defaults to ${DEFAULT_PORT}"
        echo -e
}

port=${DEFAULT_PORT}
serverIp=""

while [[ $# -gt 0 ]]; do
    key="$1"
    case ${key} in
        -p|--port)
        port="$2"
        shift # past argument
        shift # past value
        ;;

        -i|--server-ip)
        serverIp="$2"
        shift # past argument
        shift # past value
        ;;

        *)    # unknown option
        POSITIONAL+=("$1") # save it in an array for later
        shift # past argument
        ;;
    esac
done

if [ -z "$serverIp" ]
then
    usage
    exit
fi
set -- "${POSITIONAL[@]}" # restore positional parameters
echo "using ip ${serverIp}"
java -cp "$basepath"/nameservice.jar:"$basepath"/shared.jar \
  -Djava.rmi.server.codebase=file:"$basepath"/shared.jar \
  -Djava.security.policy="$basepath"/policy \
  -Djava.rmi.server.hostname="${serverIp}" \
  nameservice.NameService ${port} $*
