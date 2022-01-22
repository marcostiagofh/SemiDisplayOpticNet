#! /usr/bin/env python

import sys
import os
import threading
import numpy

# this file keep all completed experiments
log_path = "./scripts/logs/"
log_file = "skewedLog.txt"

if not os.path.exists(log_path):
    os.makedirs(log_path)

# read log file    
open(os.path.join(log_path, log_file), 'a').close()
    
log = set(line.rstrip() for line in open(os.path.join(log_path, log_file), 'r'))

# open log file for append and create a lock variable
file = open("scripts/logs/skewedLog.txt", "a+")
file_lock = threading.Lock()

projects = ["optnet", "cbnet", "splaynet", "displaynet", "semisplaynet", "seqsemisplaynet", "simplenet"]
# project = sys.argv[1]

# parameters of simulation
numNodes = [1024]
numSimulations = 10

x = [1] #skewed
y = [0.4] #skewed

#number of threads to simulation
numThreads = 10 

java = 'java'
classpath = 'binaries/bin:binaries/jdom.jar'
program = 'sinalgo.Run'
args = ['-batch', "-project"]
command = '{} -cp {} {} {}'.format(java, classpath, program, ' '.join(args))

#extends thread class
class myThread (threading.Thread):
    def __init__(self, threadID, commands):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.commands = commands
    def run(self):
        execute(self.commands)


def execute(commands):
    for command in commands:
        print(command)
        os.system(command)

        file_lock.acquire()
        file.write(command + "\n")
        file_lock.release()


#for each project executed
for project in projects:

    #list of commands to be executed
    commands = []

    # generate all possibles inputs for simulation
    for idx_1 in x:
        for idx_2 in y:
            for i in range(1, numSimulations + 1):
                for n in numNodes:
                    input = 'input/bursty/{}-{}/{}/{}_tor_{}.txt'.format(idx_1, idx_2, n, i, n)
                    output = 'output/skewed/{}-{}/{}/{}/{}'.format(idx_1, idx_2, project, n, i)
                    cmd = '{} {} -overwrite input={} output={} AutoStart=true > /dev/null'.format(command, project, input, output)

                    # not executed yet
                    if cmd not in log:
                        commands.append(cmd)

    numCommands = len(commands)

    # if number of threads is greater than pairsLenght
    # just makes number of threads equals to pairsLenght
    if numCommands == 0:
        print("No experiment to be executed for project {}".format(project))
        exit
    elif numThreads > numCommands:
        numThreads = numCommands

    # split task for threads
    size = numCommands // numThreads
    chunks =  numpy.array_split(commands, numThreads)

    threads = []
    threadID = 1

    # Create new threads
    for idx in range(0, numThreads):
        thread = myThread(threadID, chunks[idx])
        thread.start()
        threads.append(thread)
        threadID += 1


    # Wait for all threads to complete
    for t in threads:
        t.join()


print("Simulation Completed")
