#!/usr/bin/env python
# coding: utf-8

# In[1]:
import os
import sys
import scienceplots
import matplotlib.pyplot as plt
import numpy as np

from importlib import reload

import DataReader as DataReader
import Plotter as Plotter


# In[2]:


reload(DataReader)
reload(Plotter)


# In[3]:

projects = [ #"semiDisplayOpticNet"
"semiDisplayOpticNetHL"
#,"SplayOpticNet"
]
switch_sizes = [ 16,32,64,128,256 ]
num_simulations = 30
datasets = [ "bursty-0.4-1" ]
num_nodes = [ 128 ]
mus = [ 4 ]


'''
num_nodes = [ 367 ]
datasets = [ "facebookDS" ]
switch_sizes = [ 16, 734 ]
num_simulations = 1

num_nodes = [ 1024 ] # Fixed number of nodes
datasets = [
    "exact_boxlib_cns_nospec_large"
]
switch_sizes = [ 256, 2048 ]
num_simulations = 1
'''

if not os.path.exists(f"output/{sys.argv[1]}"):
    os.makedirs(f"output/{sys.argv[1]}")


# In[4]:


tor_data = []


# In[5]:

for project in projects:
    for dataset in datasets:
        for num_node in num_nodes:
            for switch_size in switch_sizes:
                for mu in mus:
                    tor_data.append(
                        DataReader.DataReader(
                            dataset, project, num_node, switch_size, num_simulations, mu
                        )
                    )

tor_data = np.array(tor_data)

# In[7]:


slc = [ i for i in range(len(tor_data)) ]


# In[8]:


print(tor_data[slc])

# Collect unique values of data.switch_size
unique_switch_sizes = set(data.switch_size for data in tor_data[slc])

# Define a list of unique colors
colors = plt.cm.tab10.colors[:len(unique_switch_sizes)]  # Using a predefined colormap for colors

# Create a dictionary mapping each unique switch_size to a unique color
color_dict = dict(zip(unique_switch_sizes, colors))

# In[9]:

fig, ax = plt.subplots(figsize=(7, 4))
ax.set_ylabel("Work 10**4")
ax.set_xlabel("Project")

Plotter.Plotter.total_work_link_updates(tor_data[slc], normalize=1e4, ax=ax)

ax.plot()
fig.savefig(f"output/{sys.argv[1]}/total_work.png", dpi=300, transparent=False)
plt.close(fig)

print("finish")

# In[10]:


fig, ax = plt.subplots(figsize=(8, 4))
ax.set_title("CDF \% of active ports for switches")
ax.set_xlabel("Ports Percentage")
ax.set_ylabel("Switch Percentage")

for data in tor_data[slc]:
    color = color_dict[data.switch_size]  # Get color based on switch_size
    Plotter.Plotter.cdf_switches_active_ports(data.cdf_switch_active_ports(), ax, color=color)

ax.legend([
    f"{data.project}--{data.dataset}--{data.num_nodes}--{data.num_switches}--{data.switch_size}"
    for data in tor_data[slc]
], loc="best", frameon=False)

ax.plot()
fig.savefig(f"output/{sys.argv[1]}/switches_active_ports.png", dpi=300, transparent=False)
plt.close(fig)
print("finish")

# In[11]:


fig, ax = plt.subplots(figsize=(8, 4))
ax.set_title("CDF Active Ports per Round")
ax.set_xlabel("Rounds")
ax.set_ylabel("Ports Percentage")

for data in tor_data[slc]:
    color = color_dict[data.switch_size]  # Get color based on switch_size
    Plotter.Plotter.cdf_active_ports(data.cdf_active_ports(), ax, color=color)

ax.legend([
    f"{data.project}--{data.dataset}--{data.num_nodes}--{data.num_switches}--{data.switch_size}"
    for data in tor_data[slc]
], loc="best", frameon=False)

ax.plot()
fig.savefig(f"output/{sys.argv[1]}/active_ports.png", dpi=300, transparent=False)
plt.close(fig)
print("finish")

# In[ ]:


fig, ax = plt.subplots(figsize=(8, 4))
ax.set_title("CDF Routings per Node")
ax.set_xlabel("Routing")
ax.set_ylabel("Node Percentage")

for data in tor_data[slc]:
    color = color_dict[data.switch_size]  # Get color based on switch_size
    Plotter.Plotter.cdf_routings(data.cdf_node_routings(), ax, color=color)

ax.legend([
    f"{data.project}--{data.dataset}--{data.num_nodes}--{data.num_switches}--{data.switch_size}"
    for data in tor_data[slc]
], loc="best", frameon=False)

ax.plot()
fig.savefig(f"output/{sys.argv[1]}/node_routings.png", dpi=300, transparent=False)
plt.close(fig)
print("finish")

# In[ ]:


fig, ax = plt.subplots(figsize=(8, 4))
ax.set_title("CDF Alterations per Node")
ax.set_xlabel("Alterations")
ax.set_ylabel("Node Percentage")

for data in tor_data[slc]:
    color = color_dict[data.switch_size]  # Get color based on switch_size
    Plotter.Plotter.cdf_alterations(data.cdf_node_alterations(), ax, color=color)

ax.legend([
    f"{data.project}--{data.dataset}--{data.num_nodes}--{data.num_switches}--{data.switch_size}"
    for data in tor_data[slc]
], loc="best", frameon=False)

ax.plot()
fig.savefig(f"output/{sys.argv[1]}/node_alterations.png", dpi=300, transparent=False)
plt.close(fig)
print("finish")

# In[ ]:


fig, ax = plt.subplots(figsize=(8, 4))
ax.set_title("CDF Routings per Switch")
ax.set_xlabel("Routing")
ax.set_ylabel("Switches Percentage")

for data in tor_data[slc]:
    color = color_dict[data.switch_size]  # Get color based on switch_size
    Plotter.Plotter.cdf_routings(data.cdf_switch_routings(), ax, color=color)

ax.legend([
    f"{data.project}--{data.dataset}--{data.num_nodes}--{data.num_switches}--{data.switch_size}"
    for data in tor_data[slc]
], loc="best", frameon=False)

ax.plot()
fig.savefig(f"output/{sys.argv[1]}/switch_routings.png", dpi=300, transparent=False)
plt.close(fig)
print("finish")

# In[ ]:


fig, ax = plt.subplots(figsize=(8, 4))
ax.set_title("CDF Alterations per Switch")
ax.set_xlabel("Alterations")
ax.set_ylabel("Switches Percentage")

for data in tor_data[slc]:
    color = color_dict[data.switch_size]  # Get color based on switch_size
    Plotter.Plotter.cdf_alterations(data.cdf_switch_alterations(), ax, color=color)

ax.legend([
    f"{data.project}--{data.dataset}--{data.num_nodes}--{data.num_switches}--{data.switch_size}"
    for data in tor_data[slc]
], loc="best", frameon=False)

ax.plot()
fig.savefig(f"output/{sys.argv[1]}/switch_alterations.png", dpi=300, transparent=False)
plt.close(fig)
print("finish")
