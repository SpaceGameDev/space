#!/bin/bash

find -type d -printf 'exports %P;\n' | tr / . | sort
