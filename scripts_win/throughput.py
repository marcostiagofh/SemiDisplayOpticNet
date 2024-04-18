#! /usr/bin/env python

import sys
import os
import numpy as np

#projects = ["cbnet", "cbnetAdapt", "seqcbnet", "displaynet", "splaynet"]
num_datasets = 7
projects = ["CBN", "CBNHL", "DSN", "DSNHL", "DSNHLAP"]
datasets = ["bursty-0.4-1", "facebookDS", "hpcDS-exact_boxlib_cns_nospec_large", "normalDS-0.2", "pfabDS-trace_0_5", "skewed-1-0.4", "tor"]
numberOfNodes = [128, 367, 1024, 1024, 144, 128, 128]
numberOfSimulations = 1

#x = [0.2, 0.4, 0.8, 1]
#y = [0.2, 0.4, 0.8, 1]
x = [0.4]
y = [1]

abbr: dict[str, str] = {
    "CBN": "CBOpticalNet-master",
    "CBNHL": "CBOpticalNet",
    "DSN": "SemiDisplayOpticNet-master",
    "DSNHL" : "SemiDisplayOpticNet",
    "DSNHLAP": "SemiDisplayOpticNet-AP"
}

fullProject: dict[str, str] = {
    "CBN": "cbOptNet",
    "CBNHL": "cbOptNet",
    "DSN": "semiDisplayOpticNet",
    "DSNHL" : "semiDisplayOpticNetHL",
    "DSNHLAP": "semiDisplayOpticNetHLAP"
}

#input_dir = "../../../Data/bursty"

for i in range(num_datasets):
    output_file = f"../csv_data/{datasets[i]}/throughput.csv"
    pr_file = open(output_file, "w")
    pr_file.write("project,size,value\n")
    for project in projects:
        n = numberOfNodes[i]
        switch_size = 2 * n
        if "CBN" in project:
            cluster_path = "../../{}/logs/output/{}/{}_{}/{}/mirrored/4/{}/throughput.csv".format(abbr[project], datasets[i], fullProject[project], n, switch_size, numberOfSimulations)
        else:
            cluster_path = "../../{}/logs/output/{}/{}_{}/{}/4/{}/throughput.csv".format(abbr[project], datasets[i], fullProject[project], n, switch_size, numberOfSimulations)
        
        with open(cluster_path) as f:
            content = f.readlines()
        
        num_rounds = int(content[-1].split(',')[1])
        print(num_rounds)
        requests_by_round = np.zeros(num_rounds+1)
        #content = [int(x.strip()) for x in content]
        
        for line in content[1:]:
            num_round = int(line.split(',')[1])
            for _ in range(int(line.split(',')[2])):
                pr_file.write("{},{},{}\n".format(abbr[project], n, num_round))
    
    pr_file.close()