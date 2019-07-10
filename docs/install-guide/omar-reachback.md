# OMAR REACHBACK

## Purpose

OMAR Reachback is a service that allows for search and discovery of data in external repositories in order to identify gaps in search results.

## Installation in Openshift

**Assumption:** The omar-reachback docker image is pushed into the OpenShift server's internal docker registry and available to the project.

### Environment variables

|Variable|Value|
|------|------|
|SPRING_PROFILES_ACTIVE|Comma separated profile tags (*e.g. production, dev*)|
|SPRING_CLOUD_CONFIG_LABEL|The Git branch from which to pull config files (*e.g. master*)|
