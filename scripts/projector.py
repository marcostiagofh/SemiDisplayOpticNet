#!/usr/bin/env python
import os
import numpy
import threading

from util import check_sim

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

projects = [ "semiDisplayOpticNet" ]

# parameters of simulation
num_nodes = [ 128 ]
datasets = [ "tor" ]
switch_sizes = [ 16 ]
mus = [ 4 ]
sequential = [ "true", "false" ]
num_simulations = 30

#number of threads to simulation
num_threads = 2

java = "java"
classpath = "binaries/bin:binaries/jdom.jar"
program = "sinalgo.Run"
args = " ".join(["-batch", "-project"])
base_cmd = f"{java} -cp {classpath} {program} {args}"

class Threader (threading.Thread):
    threadID: int
    commands: list[str]

    def __init__ (self, threadID: int, commands: list[str]) -> None:
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.commands = commands

    def run(self) -> None:
        for command in self.commands:
            self.execute(command)

    def execute (self, command) -> None:
        print(command)
        os.system(command)

        sim_file = command.split(" > ")[-1]
        if not check_sim(sim_file):
            file_lock.acquire()
            file.write(f"Error with {command}\n")
            file_lock.release()

for project in projects:
    commands = []

    # generate all possibles inputs for simulation
    for dataset in datasets:
        for num_node in num_nodes:
            for sim_id in range(1, num_simulations + 1):
                for switch_size in switch_sizes:
                    for sequentiality in sequential:
                        for mu in mus:
                            if switch_size == -1:
                                switch_size = 2 * num_node

                            elif switch_size == 256 and num_node == 128:
                                continue

                            elif switch_size <= 16 and num_node >= 256:
                                continue

                            elif switch_size <= 64 and num_node >= 512:
                                continue

                            if sequentiality == "true":
                                output_path = (
                                    f"output/{dataset}/SplayOpticNet_{num_node}/{switch_size}/{mu}/{sim_id}/"
                                )
                            else:
                                output_path = (
                                    f"output/{dataset}/{project}_{num_node}/{switch_size}/{mu}/{sim_id}/"
                                )

                            input_file = (
                                f"input/projectorDS/{dataset}/{num_node}/{sim_id}_tor_{num_node}.txt"
                            )

                            sim_stream = f"logs/{output_path}sim.txt"

                            if not os.path.exists(f"logs/{output_path}"):
                                os.makedirs(f"logs/{output_path}")

                            cmd = (
                                f"time --verbose {base_cmd} {project} -overwrite input={input_file} " \
                                f"switchSize={switch_size} mu={mu} output={output_path} " \
                                f"isSequential={sequentiality} AutoStart=true > {sim_stream}"
                            )

                            print(cmd)
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
        thread = Threader(threadID, chunks[idx])
        thread.start()
        threads.append(thread)
        threadID += 1

    # Wait for all threads to complete
    for t in threads:
        t.join()

print("Simulation Completed")
