#!/bin/bash
# Delete all Homework Projects
if [ "$#" -ne 1 ]; then
    echo "Usage:"
    echo "  $0 GUID"
    exit 1
fi

GUID=$1
echo "Removing all Homework Projects for GUID=$GUID"
oc delete project $GUID-jenkins
oc delete project $GUID-tasks-dev
oc delete project $GUID-tasks-prod