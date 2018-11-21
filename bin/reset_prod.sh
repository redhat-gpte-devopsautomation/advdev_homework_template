#!/bin/bash
# Reset Production Project (initial active services: Blue)
# This sets all services to the Blue service so that any pipeline run will deploy Green
if [ "$#" -ne 1 ]; then
    echo "Usage:"
    echo "  $0 GUID"
    exit 1
fi

GUID=$1
echo "Resetting Tasks Production Environment in project ${GUID}-tasks-prod to Green Services"

# Parksmap
# Set route to Green Service
oc patch route tasks -n ${GUID}-tasks-prod -p '{"spec":{"to":{"name":"tasks-green"}}}'

# Add echo statement so that the script succeeds even if the patch didn't do anything
echo "Updated"