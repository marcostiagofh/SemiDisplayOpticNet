#!/usr/bin/env python

import sys
import os
import threading
import numpy

# this file keep all completed experiments
log_path = "./scripts/logs/"
log_file = "projectorLog.txt"

if not os.path.exists(log_path):
    os.makedirs(log_path)

# read log file
open(os.path.join(log_path, log_file), 'a').close()
log = set(line.rstrip() for line in open(os.path.join(log_path, log_file), 'r'))

# open log file for append and create a lock variable
file = open("scripts/logs/projectorLog.txt", "a+")
file_lock = threading.Lock()

projects = [ "cbOptNet" ]

# parameters of simulation
num_nodes = [ 1024 ]
datasets = [ "newTor" ]
switch_sizes = [ -1 ]
sequential = [ "false" ]
num_simulations = 30

#number of threads to simulation
num_threads = 2

java = "java"
classpath = "binaries/bin:binaries/jdom.jar"
program = "sinalgo.Run"
args = " ".join(["-batch", "-project"])
base_cmd = f"{java} -cp {classpath} {program} {args}"

#extends thread class
class myThread (threading.Thread):
    def __init__ (self, threadID: int, commands: list[str]) -> None:
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.commands = commands

    def run (self) -> None:
        execute(self.commands)

def execute (commands) -> None:
    for command in commands:
        print(command)
        os.system(command)

        file_lock.acquire()
        file.write(command + "\n")
        file_lock.release()


for project in projects:
    commands = []

    # generate all possibles inputs for simulation
    for dataset in datasets:
        for num_node in num_nodes:
            for sequentiality in sequential:
                for sim_id in range(1, num_simulations + 1):
                    for switch_size in switch_sizes:
                        if switch_size == -1:
                            switch_size = 2 * num_node

                        elif switch_size <= 16 and num_node >= 512:
                            continue

                        elif switch_size <= 8 and num_node >= 256:
                            continue

                        cmd = (
                            f"./scripts/compress-results.sh \"{base_cmd}\" {project} {dataset} {num_node} " \
                            f"{sim_id} {switch_size} {sequentiality}"
                        )

                        print(cmd)
                        if cmd not in log:
                            commands.append(cmd)

    num_commands = len(commands)

    # if number of threads is greater than pairsLenght
    # just makes number of threads equals to pairsLenght
    if num_commands == 0:
        print(f"No experiment to be executed for project {project}")
        exit

    elif num_threads > num_commands:
        num_threads = num_commands

    # split task for threads
    size = num_commands // num_threads
    chunks =  numpy.array_split(commands, num_threads)

    threads = []
    threadID = 1

    # Create new threads
    for idx in range(0, num_threads):
        thread = myThread(threadID, chunks[idx])
        thread.start()
        threads.append(thread)
        threadID += 1

    # Wait for all threads to complete
    for t in threads:
        t.join()

print("Simulation Completed")
