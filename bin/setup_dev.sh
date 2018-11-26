#!/bin/bash
# Setup Development Project
if [ "$#" -ne 1 ]; then
    echo "Usage:"
    echo "  $0 GUID"
    exit 1
fi

GUID=$1
echo "Setting up Tasks Development Environment in project ${GUID}-tasks-dev"

# Set up Dev Project
oc policy add-role-to-user edit system:serviceaccount:${GUID}-jenkins:jenkins -n ${GUID}-tasks-dev

# Set up Dev Application
# oc new-build --binary=true --name="tasks" jboss-eap71-openshift:1.3 -n ${GUID}-tasks-dev
oc new-build --binary=true --name="tasks" --image-stream=openshift/jboss-eap71-openshift:1.1 -n ${GUID}-tasks-dev
oc new-app ${GUID}-tasks-dev/tasks:0.0-0 --name=tasks --allow-missing-imagestream-tags=true -n ${GUID}-tasks-dev
oc set triggers dc/tasks --remove-all -n ${GUID}-tasks-dev
oc expose dc tasks --port 8080 -n ${GUID}-tasks-dev
oc expose svc tasks -n ${GUID}-tasks-dev
oc create configmap tasks-config --from-literal="application-users.properties=Placeholder" --from-literal="application-roles.properties=Placeholder" -n ${GUID}-tasks-dev
oc set volume dc/tasks --add --name=jboss-config --mount-path=/opt/eap/standalone/configuration/application-users.properties --sub-path=application-users.properties --configmap-name=tasks-config -n ${GUID}-tasks-dev
oc set volume dc/tasks --add --name=jboss-config1 --mount-path=/opt/eap/standalone/configuration/application-roles.properties --sub-path=application-roles.properties --configmap-name=tasks-config -n ${GUID}-tasks-dev
oc set probe dc/tasks --readiness --get-url=http://:8080/ --initial-delay-seconds=30 --timeout-seconds=1 -n ${GUID}-tasks-dev
oc set probe dc/tasks --liveness --get-url=http://:8080/ --initial-delay-seconds=30 --timeout-seconds=1 -n ${GUID}-tasks-dev

# Setting 'wrong' VERSION. This will need to be updated in the pipeline
oc set env dc/tasks VERSION='0.0 (tsks-dev)' -n ${GUID}-tasks-dev
