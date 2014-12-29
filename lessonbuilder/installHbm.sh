#!/bin/sh

# Install the HBM files and rebuild

cd hbm
mvn install
cd ../
build