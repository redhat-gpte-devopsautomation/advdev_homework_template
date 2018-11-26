#!/bin/bash
# Setup Production Project (initial active services: Green)
if [ "$#" -ne 1 ]; then
    echo "Usage:"
    echo "  $0 GUID"
    exit 1
fi

GUID=$1
echo "Setting up Tasks Production Environment in project ${GUID}-tasks-prod"

# Set up Production Project
oc policy add-role-to-group system:image-puller system:serviceaccounts:${GUID}-tasks-prod -n ${GUID}-tasks-dev
oc policy add-role-to-user edit system:serviceaccount:${GUID}-jenkins:jenkins -n ${GUID}-tasks-prod

# Create Blue Application
oc new-app ${GUID}-tasks-dev/tasks:0.0 --name=tasks-blue --allow-missing-imagestream-tags=true -n ${GUID}-tasks-prod
oc set triggers dc/tasks-blue --remove-all -n ${GUID}-tasks-prod
oc expose dc tasks-blue --port 8080 -n ${GUID}-tasks-prod
oc create configmap tasks-blue-config --from-literal="application-users.properties=Placeholder" --from-literal="application-roles.properties=Placeholder" -n ${GUID}-tasks-prod
oc set volume dc/tasks-blue --add --name=jboss-config --mount-path=/opt/eap/standalone/configuration/application-users.properties --sub-path=application-users.properties --configmap-name=tasks-blue-config -n ${GUID}-tasks-prod
oc set volume dc/tasks-blue --add --name=jboss-config1 --mount-path=/opt/eap/standalone/configuration/application-roles.properties --sub-path=application-roles.properties --configmap-name=tasks-blue-config -n ${GUID}-tasks-prod
oc set probe dc/tasks-blue --readiness --get-url=http://:8080/ --initial-delay-seconds=30 --timeout-seconds=1 -n ${GUID}-tasks-prod
oc set probe dc/tasks-blue --liveness --get-url=http://:8080/ --initial-delay-seconds=30 --timeout-seconds=1 -n ${GUID}-tasks-prod
# Setting 'wrong' VERSION. This will need to be updated in the pipeline
oc set env dc/tasks-blue VERSION='0.0 (tsks-blue)' -n ${GUID}-tasks-prod


# Create Green Application
oc new-app ${GUID}-tasks-dev/tasks:0.0 --name=tasks-green --allow-missing-imagestream-tags=true -n ${GUID}-tasks-prod
oc set triggers dc/tasks-green --remove-all -n ${GUID}-tasks-prod
oc expose dc tasks-green --port 8080 -n ${GUID}-tasks-prod
oc create configmap tasks-green-config --from-literal="application-users.properties=Placeholder" --from-literal="application-roles.properties=Placeholder" -n ${GUID}-tasks-prod
oc set volume dc/tasks-green --add --name=jboss-config --mount-path=/opt/eap/standalone/configuration/application-users.properties --sub-path=application-users.properties --configmap-name=tasks-green-config -n ${GUID}-tasks-prod
oc set volume dc/tasks-green --add --name=jboss-config1 --mount-path=/opt/eap/standalone/configuration/application-roles.properties --sub-path=application-roles.properties --configmap-name=tasks-green-config -n ${GUID}-tasks-prod
oc set probe dc/tasks-green --readiness --get-url=http://:8080/ --initial-delay-seconds=30 --timeout-seconds=1 -n ${GUID}-tasks-prod
oc set probe dc/tasks-green --liveness --get-url=http://:8080/ --initial-delay-seconds=30 --timeout-seconds=1 -n ${GUID}-tasks-prod
# Setting 'wrong' VERSION. This will need to be updated in the pipeline
oc set env dc/tasks-green VERSION='0.0 (tsks-green)' -n ${GUID}-tasks-prod

# Expose Blue service as route to make green application active
oc expose svc/tasks-green --name tasks -n ${GUID}-tasks-prod