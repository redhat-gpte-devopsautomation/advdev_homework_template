== Parks Map Multi-service Application

This is an example application consisting of three micro-services to be used in the Advanced OpenShift Development Homework Assignment. Read these instructions carefully to successfully complete the homework assignment.

You are expected to clone this repository into your own public repository (GitHub, Gogs, Gitlab). This repository needs to be accessible to the instructor.

In this repository are three services:

* Two backend services providing geospatial data about Nationalparks and Major League Baseball park. The backend services are exposed as services with label "type=parksmap-backend". The data can be stored in a MongoDB database.
* A frontend parksmap application that can display the data on a map on a web site. The parksmap application dynamically discovers services with label "type=parksmap-backend".

There is also a directory "Infrastructure" which needs to contain scripts to set up the entire homework environment from scratch as well as templates to create the various components. This directory contains place holders for all required scripts.

See the individual application directories for instructions / hints on how to set up this application on OpenShift.

Your Homework assignment will be graded by executing the pipeline at https://github.com/wkulhanek/advdev_homework_grading.

You can set up your own grading environment if you want to ensure that your submission will succeed.
