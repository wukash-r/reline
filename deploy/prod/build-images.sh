#!/bin/sh
docker build -t wukashr/reline-backend:$1 -t wukashr/reline-backend:latest ../../backend
docker build -t wukashr/reline-ui:$1 -t wukashr/reline-ui:latest ../../ui

