#!/bin/bash

base_cmd=$1
project=$2
dataset=$3
num_node=$4
sim_id=$5
switch_size=$6
sequentiality=$7

input_file="input/projectorDS/${dataset}/${num_node}/${sim_id}_tor_${num_node}.txt"

log_path="${dataset}/${project}_${switch_size}/${num_node}/${sim_id}/"
filename="${dataset}_${num_node}"

zip_path="/home/caioc/facul/IC/test-outputs/${log_path}"
output_path="output/${log_path}"

zip_file=${zip_path}${filename}".zip"

sim_stream="output/sims/${sim_id}_${dataset}_${num_node}_${switch_size}.txt"
cmd="time ${base_cmd} ${project} -overwrite input=${input_file} switchSize=${switch_size} output=${output_path} isSequential=${sequentiality} AutoStart=true > ${sim_stream}"

echo $cmd
eval $cmd

mkdir -p $zip_path;
zip -r ${zip_file} "logs/${output_path}" -j

rm -r "logs/${output_path}"
