#!/bin/bash
# Create all Homework Projects
if [ "$#" -ne 1 ]; then
    echo "Usage:"
    echo "  $0 GUID"
    exit 1
fi

GUID=$1
echo "Creating all Homework Projects for GUID=$GUID"
oc new-project $GUID-nexus        --display-name="$GUID AdvDev Homework Nexus"
oc new-project $GUID-sonarqube    --display-name="$GUID AdvDev Homework Sonarqube"
oc new-project ${GUID}-jenkins    --display-name="${GUID} AdvDev Homework Jenkins"
oc new-project ${GUID}-parks-dev  --display-name="${GUID} AdvDev Homework Parks Development"
oc new-project ${GUID}-parks-prod --display-name="${GUID} AdvDev Homework Parks Production"
