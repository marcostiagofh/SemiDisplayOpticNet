#!/bin/bash

base_cmd=$1
project=$2
dataset=$3
num_node=$4
sim_id=$5
switch_size=$6

input_file="input/projectorDS/${dataset}/${num_node}/${sim_id}_tor_${num_node}.txt"

log_path="${project}/projectorDS/${dataset}/"
filename="${sim_id}_tor_${num_node}_${switch_size}"

zip_path="/media/external-hdd/caioc/${log_path}"
output_path="output/${log_path}${filename}"

zip_file=${zip_path}${filename}".zip"
output_file=${output_path}

sim_stream="output/sims/${sim_id}_${dataset}_${num_node}_${switch_size}.txt"
cmd="${base_cmd} ${project} -overwrite input=${input_file} switchSize=${switch_size} output=${output_path} AutoStart=true > ${sim_stream}"

eval $cmd

mkdir -p $zip_path;
zip -r ${zip_file} "logs/${output_path}"

rm -r "logs/${output_path}"
