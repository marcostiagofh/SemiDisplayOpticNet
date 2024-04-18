#! /usr/bin/env python

import sys
import os
import numpy as np

#projects = ["cbnet", "cbnetAdapt", "seqcbnet", "displaynet", "splaynet"]
projects = ["CBN", "CBNHL", "DSN", "DSNHL", "DSNHLAP"]
numberOfNodes = [128]
swtichSize = []
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
output_file = "../csv_data/bursty/throughput.csv"

pr_file = open(output_file, "w")
pr_file.write("project,x,y,size,value\n")

for idx_1 in x:
    for idx_2 in y:
        for project in projects:
            for n in numberOfNodes:
                if "CBN" in project:
                    cluster_path = "../../{}/logs/output/bursty-{}-{}/{}_{}/{}/mirrored/4/{}/throughput.csv".format(abbr[project], idx_1, idx_2, fullProject[project], n, n*2, numberOfSimulations)
                else:
                    cluster_path = "../../{}/logs/output/bursty-{}-{}/{}_{}/{}/4/{}/throughput.csv".format(abbr[project], idx_1, idx_2, fullProject[project], n, n*2, numberOfSimulations)
                
                with open(cluster_path) as f:
                    content = f.readlines()
                
                num_rounds = int(content[-1].split(',')[1])
                print(num_rounds)
                requests_by_round = np.zeros(num_rounds+1)
                #content = [int(x.strip()) for x in content]
                
                for line in content[1:]:
                    num_round = int(line.split(',')[1])
                    requests_by_round[num_round] = int(line.split(',')[2])
                    #print(num_round)
                for i in range(1,num_rounds+1):
                    pr_file.write("{},{},{},{},{}\n".format(project, idx_1, idx_2, n, requests_by_round[i]))
        
pr_file.close()