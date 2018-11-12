#!/bin/bash

pushd $(dirname $0) &> /dev/null
basepath=$(pwd)
popd > /dev/null

DEFAULT_PORT=-1
DEFAULT_NAME_SERVICE_PORT=-1
DEFAULT_CAPACITY=10
DEFAULT_FAILURE_RATE=0

usage() {
        echo -e "usage:"
        echo -e "\t$0\t-i/--this-node-ip <ip> "
        echo -e "\t\t\t\t-t/--target-ip <ip> "
        echo -e "\t\t\t\t-p/--port <port> "
        echo -e "\t\t\t\t-tp/--nameservice-port <port>"
        echo -e
        echo -e "\t\t\t\t[-c/--capacity <value>]"
        echo -e "\t\t\t\t[-f/--failure-rate <rate>]"
        echo -e
        echo -e "\t-i/--this-node-ip :\t\tthis node ip address"
        echo -e "\t-p/--port :\t\t\tport number on this machine"
        echo -e "\t-t/--target-ip :\t\tip address of the target nameservice"
        echo -e "\t-tp/--nameservice-port :\tport ot the target nameservice"
        echo -e "\t-c/--capacity :\t\t\tcomputing capacity [1, 100], default to ${DEFAULT_CAPACITY}"
        echo -e "\t-f/--failure-rate :\t\tnode failure rate [0, 100], defaults to ${DEFAULT_FAILURE_RATE}"
        echo -e
}


port=${DEFAULT_PORT}
name_service_port=${DEFAULT_NAME_SERVICE_PORT}
failureRate=${DEFAULT_FAILURE_RATE}
capacity=${DEFAULT_CAPACITY}
thisIp=""
targetIp=""

while [[ $# -gt 0 ]]; do
    key="$1"
    case ${key} in
        -p|--port)
        port="$2"
        shift # past argument
        shift # past value
        ;;

        -i|--this-node-ip)
        thisIp="$2"
        shift # past argument
        shift # past value
        ;;

        -t|--target-ip)
        targetIp="$2"
        shift # past argument
        shift # past value
        ;;

        -c|--capacity)
        capacity="$2"
        shift # past argument
        shift # past value
        ;;

        -f|--failure-rate)
        failureRate="$2"
        shift # past argument
        shift # past value
        ;;

        -tp|--nameservice-port)
	name_service_port="$2"
        shift # past argument
        shift # past value
	;;

        *)    # unknown option
        POSITIONAL+=("$1") # save it in an array for later
        shift # past argument
        ;;
    esac
done

if [ -z "$thisIp" ] || [ -z "$targetIp" ]\
|| [ "${capacity}" -gt 100 ] || [ "${capacity}" -lt 1 ]\
|| [ "${failureRate}" -gt 100 ] || [ "${failureRate}" -lt 0 ] || [ "$port" -lt 0 ] || [ "$name_service_port" -lt 0 ]; then
    usage
    exit
fi
set -- "${POSITIONAL[@]}" # restore positional parameters

java -cp "$basepath"/computationnode.jar:"$basepath"/shared.jar \
    -Djava.rmi.server.codebase=file:"$basepath"/shared.jar \
    -Djava.security.policy="$basepath"/policy \
    -Djava.rmi.server.hostname=${thisIp} \
    computationnode.ComputationNode ${capacity} ${failureRate} ${targetIp} ${thisIp} ${port} ${name_service_port} $*

