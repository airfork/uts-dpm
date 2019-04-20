#!/usr/bin/env bash
./node_modules/.bin/babel js-pre --out-dir assets/js --presets=@babel/env
go build -o dpm *.go
./dpm